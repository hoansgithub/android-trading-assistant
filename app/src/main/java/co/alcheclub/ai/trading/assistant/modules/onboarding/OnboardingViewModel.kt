package co.alcheclub.ai.trading.assistant.modules.onboarding

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.review.ReviewManagerFactory
import co.alcheclub.ai.trading.assistant.domain.model.Analysis
import co.alcheclub.ai.trading.assistant.domain.model.ExperienceLevel
import co.alcheclub.ai.trading.assistant.domain.model.LearningStyle
import co.alcheclub.ai.trading.assistant.domain.model.MockStrategy
import co.alcheclub.ai.trading.assistant.domain.model.OnboardingSurvey
import co.alcheclub.ai.trading.assistant.domain.model.PrimaryGoal
import co.alcheclub.ai.trading.assistant.domain.model.RiskComfort
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.model.TimeAvailability
import co.alcheclub.ai.trading.assistant.domain.model.TradingDirection
import co.alcheclub.ai.trading.assistant.domain.model.TradingStyle
import co.alcheclub.ai.trading.assistant.domain.repository.AuthRepository
import co.alcheclub.ai.trading.assistant.domain.repository.OnboardingRepository
import co.alcheclub.ai.trading.assistant.domain.repository.StrategyRepository
import co.alcheclub.ai.trading.assistant.domain.usecase.AnalyzeChartUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val authRepository: AuthRepository,
    private val strategyRepository: StrategyRepository,
    private val analyzeChartUseCase: AnalyzeChartUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "OnboardingVM"
    }

    private val _currentStep = MutableStateFlow(OnboardingStep.EXPERIENCE_LEVEL)
    val currentStep: StateFlow<OnboardingStep> = _currentStep.asStateFlow()

    private val _selectedExperience = MutableStateFlow<ExperienceLevel?>(null)
    val selectedExperience: StateFlow<ExperienceLevel?> = _selectedExperience.asStateFlow()

    private val _selectedTime = MutableStateFlow<TimeAvailability?>(null)
    val selectedTime: StateFlow<TimeAvailability?> = _selectedTime.asStateFlow()

    private val _selectedRisk = MutableStateFlow<RiskComfort?>(null)
    val selectedRisk: StateFlow<RiskComfort?> = _selectedRisk.asStateFlow()

    private val _selectedGoal = MutableStateFlow<PrimaryGoal?>(null)
    val selectedGoal: StateFlow<PrimaryGoal?> = _selectedGoal.asStateFlow()

    private val _selectedLearning = MutableStateFlow<LearningStyle?>(null)
    val selectedLearning: StateFlow<LearningStyle?> = _selectedLearning.asStateFlow()

    private val _processingProgress = MutableStateFlow(0f)
    val processingProgress: StateFlow<Float> = _processingProgress.asStateFlow()

    private val _generatedStrategy = MutableStateFlow<MockStrategy?>(null)
    val generatedStrategy: StateFlow<MockStrategy?> = _generatedStrategy.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private val _analyzingProgress = MutableStateFlow(0f)
    val analyzingProgress: StateFlow<Float> = _analyzingProgress.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisResult = MutableStateFlow<Analysis?>(null)
    val analysisResult: StateFlow<Analysis?> = _analysisResult.asStateFlow()

    private val _analysisError = MutableStateFlow<String?>(null)
    val analysisError: StateFlow<String?> = _analysisError.asStateFlow()

    /** The saved Strategy from Supabase (with DB-assigned ID for FK reference) */
    private var savedStrategy: Strategy? = null
    private var strategySavedToDB = false

    fun selectExperience(level: ExperienceLevel) {
        _selectedExperience.value = level
    }

    fun selectTime(time: TimeAvailability) {
        _selectedTime.value = time
    }

    fun selectRisk(risk: RiskComfort) {
        _selectedRisk.value = risk
    }

    fun selectGoal(goal: PrimaryGoal) {
        _selectedGoal.value = goal
    }

    fun selectLearning(style: LearningStyle) {
        _selectedLearning.value = style
    }

    private val canAdvance: Boolean
        get() = when (_currentStep.value) {
            OnboardingStep.EXPERIENCE_LEVEL -> _selectedExperience.value != null
            OnboardingStep.TIME_AVAILABILITY -> _selectedTime.value != null
            OnboardingStep.RISK_COMFORT -> _selectedRisk.value != null
            OnboardingStep.PRIMARY_GOAL -> _selectedGoal.value != null
            OnboardingStep.LEARNING_STYLE -> _selectedLearning.value != null
            OnboardingStep.RATE_US, OnboardingStep.DISCLAIMER, OnboardingStep.ALL_SET -> true
            OnboardingStep.PROCESSING -> false
        }

    fun goToNextStep() {
        if (!canAdvance) return
        val steps = OnboardingStep.entries
        val currentIndex = steps.indexOf(_currentStep.value)
        if (currentIndex < steps.size - 1) {
            val nextStep = steps[currentIndex + 1]
            _currentStep.value = nextStep
            if (nextStep == OnboardingStep.PROCESSING) {
                startProcessing()
            }
        }
    }

    fun goToPreviousStep() {
        val steps = OnboardingStep.entries
        val currentIndex = steps.indexOf(_currentStep.value)
        if (currentIndex > 0) {
            _currentStep.value = steps[currentIndex - 1]
        }
    }

    private fun startProcessing() {
        viewModelScope.launch {
            _processingProgress.value = 0f

            // Generate strategy from survey
            val survey = OnboardingSurvey(
                experienceLevel = _selectedExperience.value ?: ExperienceLevel.BEGINNER,
                timeAvailability = _selectedTime.value ?: TimeAvailability.DAILY,
                riskComfort = _selectedRisk.value ?: RiskComfort.MODERATE,
                primaryGoal = _selectedGoal.value ?: PrimaryGoal.GROW,
                learningStyle = _selectedLearning.value ?: LearningStyle.SOME_TIPS
            )
            val mockStrategy = MockStrategy.generateFromSurvey(survey)
            _generatedStrategy.value = mockStrategy

            // Convert to real Strategy for DB save
            val strategy = mockStrategyToDomain(mockStrategy)

            // Run animation + DB save concurrently (matching iOS pattern)
            val saveJob = async {
                try {
                    val userId = authRepository.getCurrentUserId()
                    val result = strategyRepository.saveStrategy(strategy, userId)
                    result.onSuccess { saved ->
                        savedStrategy = saved
                        strategySavedToDB = true
                        Log.d(TAG, "Strategy saved to Supabase: ${saved.name} (${saved.id})")
                    }.onFailure { e ->
                        Log.e(TAG, "Strategy save failed — will retry before analysis", e)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Strategy save failed — will retry before analysis", e)
                }
            }

            // Processing animation (3 seconds)
            val totalDuration = 3000L
            val steps = 100
            val stepDelay = totalDuration / steps
            for (i in 1..steps) {
                delay(stepDelay)
                _processingProgress.value = i / steps.toFloat()
            }

            // Wait for save to finish
            saveJob.await()

            delay(500)
            _currentStep.value = OnboardingStep.DISCLAIMER
        }
    }

    /**
     * Launch Google Play in-app review, then advance to next step.
     */
    fun requestReviewAndAdvance(activity: Activity) {
        viewModelScope.launch {
            try {
                val reviewManager = ReviewManagerFactory.create(activity)
                val reviewInfo = reviewManager.requestReviewFlow().await()
                reviewManager.launchReviewFlow(activity, reviewInfo).await()
            } catch (e: Exception) {
                Log.d(TAG, "In-app review not available: ${e.message}")
            }
            goToNextStep()
        }
    }

    /**
     * Called when user captures a chart image during onboarding.
     * Runs the full analysis pipeline with the saved strategy.
     */
    fun onImageCaptured(imageData: ByteArray, userId: UUID) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analyzingProgress.value = 0f
            _analysisError.value = null

            // Retry strategy save if it failed during processing
            if (!strategySavedToDB && savedStrategy == null) {
                val mockStrategy = _generatedStrategy.value
                if (mockStrategy != null) {
                    val strategy = mockStrategyToDomain(mockStrategy)
                    try {
                        strategyRepository.saveStrategy(strategy, userId).onSuccess { saved ->
                            savedStrategy = saved
                            strategySavedToDB = true
                            Log.d(TAG, "Strategy retry save succeeded: ${saved.id}")
                        }.onFailure {
                            Log.e(TAG, "Strategy retry save failed — proceeding without FK")
                            savedStrategy = null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Strategy retry save failed — proceeding without FK", e)
                        savedStrategy = null
                    }
                }
            }

            // Start progress animation in parallel with real analysis
            val progressJob = launch {
                val steps = 90
                val stepDelay = 110L
                for (i in 1..steps) {
                    delay(stepDelay)
                    _analyzingProgress.value = i / 100f
                }
                while (_isAnalyzing.value) {
                    delay(100)
                }
            }

            // Run real analysis with strategy reference
            val result = analyzeChartUseCase.execute(
                imageData = imageData,
                userId = userId,
                strategy = savedStrategy
            )

            progressJob.cancel()
            _analyzingProgress.value = 1f

            result.onSuccess { analysis ->
                Log.d(TAG, "Analysis complete: ${analysis.signal} ${analysis.confidenceScore}%")
                _analysisResult.value = analysis
                delay(300)
                completeOnboarding()
            }.onFailure { error ->
                Log.e(TAG, "Analysis failed: ${error.message}", error)
                _analysisError.value = mapAnalysisError(error)
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * User taps "Continue" on error screen → complete onboarding anyway.
     * Matches iOS dismissAnalysisError() pattern.
     */
    fun dismissAnalysisError() {
        _analysisError.value = null
        completeOnboarding()
    }

    fun skipAnalysis() {
        completeOnboarding()
    }

    /**
     * Map error to user-friendly message matching iOS error strings.
     */
    private fun mapAnalysisError(error: Throwable): String {
        val msg = error.message?.lowercase() ?: ""
        return when {
            msg.contains("chart recognition") || msg.contains("could not determine") || msg.contains("image optimization") ->
                "Chart Recognition Failed\nWe couldn't read your chart. Don't worry — you can analyze charts anytime from the home screen."
            msg.contains("market data") || msg.contains("symbol not found") || msg.contains("klines") ->
                "Market Data Unavailable\nCouldn't fetch price data for this asset. You can try again from the home screen."
            else ->
                "AI Analysis Failed\nOur AI couldn't complete the analysis. You can try again from the home screen."
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                onboardingRepository.completeOnboarding(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync onboarding to Supabase (non-fatal)", e)
            }
            _isCompleted.value = true
        }
    }

    /**
     * Convert display-only MockStrategy → domain Strategy for Supabase.
     */
    private fun mockStrategyToDomain(mock: MockStrategy): Strategy {
        return Strategy(
            name = mock.name,
            description = "Generated from onboarding survey",
            style = parseTradingStyle(mock.style),
            timeframe = parseTimeframe(mock.timeframe),
            direction = parseTradingDirection(mock.direction),
            riskPerTradePercent = parseRiskPercent(mock.riskPerTrade),
            maxOpenPositions = 3,
            isPreset = true,
            isActive = true,
            isDefault = true
        )
    }

    private fun parseTradingStyle(style: String): TradingStyle = when {
        style.contains("Scalp", ignoreCase = true) -> TradingStyle.SCALPING
        style.contains("Day", ignoreCase = true) -> TradingStyle.DAY_TRADING
        style.contains("Swing", ignoreCase = true) -> TradingStyle.SWING_TRADING
        style.contains("Position", ignoreCase = true) -> TradingStyle.POSITION_TRADING
        style.contains("Invest", ignoreCase = true) -> TradingStyle.INVESTING
        else -> TradingStyle.SWING_TRADING
    }

    private fun parseTimeframe(timeframe: String): String = when {
        timeframe.contains("1M", ignoreCase = false) -> "1M"
        timeframe.contains("1W", ignoreCase = true) || timeframe.contains("Weekly", ignoreCase = true) -> "1w"
        timeframe.contains("Daily", ignoreCase = true) || timeframe.contains("1D", ignoreCase = true) -> "1d"
        timeframe.contains("4H", ignoreCase = true) -> "4h"
        timeframe.contains("1H", ignoreCase = true) -> "1h"
        timeframe.contains("15", ignoreCase = true) -> "15m"
        timeframe.contains("5", ignoreCase = true) -> "5m"
        timeframe.contains("1m", ignoreCase = true) -> "1m"
        else -> "4h"
    }

    private fun parseTradingDirection(direction: String): TradingDirection = when {
        direction.contains("Long", ignoreCase = true) && !direction.contains("Both", ignoreCase = true) -> TradingDirection.LONG_ONLY
        direction.contains("Short", ignoreCase = true) && !direction.contains("Both", ignoreCase = true) -> TradingDirection.SHORT_ONLY
        else -> TradingDirection.BOTH
    }

    private fun parseRiskPercent(risk: String): Double {
        // "1-2%" → 1.5, "2%" → 2.0, "3%" → 3.0
        val numbers = Regex("""\d+\.?\d*""").findAll(risk).map { it.value.toDouble() }.toList()
        return when {
            numbers.size >= 2 -> (numbers[0] + numbers[1]) / 2
            numbers.size == 1 -> numbers[0]
            else -> 2.0
        }
    }
}
