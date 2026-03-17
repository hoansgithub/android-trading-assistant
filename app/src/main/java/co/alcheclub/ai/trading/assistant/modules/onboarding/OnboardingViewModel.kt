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
import co.alcheclub.ai.trading.assistant.domain.model.TimeAvailability
import co.alcheclub.ai.trading.assistant.domain.repository.AuthRepository
import co.alcheclub.ai.trading.assistant.domain.repository.OnboardingRepository
import co.alcheclub.ai.trading.assistant.domain.usecase.AnalyzeChartUseCase
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
            val totalDuration = 3000L
            val steps = 100
            val stepDelay = totalDuration / steps

            for (i in 1..steps) {
                delay(stepDelay)
                _processingProgress.value = i / steps.toFloat()
            }

            // Generate strategy
            val survey = OnboardingSurvey(
                experienceLevel = _selectedExperience.value ?: ExperienceLevel.BEGINNER,
                timeAvailability = _selectedTime.value ?: TimeAvailability.DAILY,
                riskComfort = _selectedRisk.value ?: RiskComfort.MODERATE,
                primaryGoal = _selectedGoal.value ?: PrimaryGoal.GROW,
                learningStyle = _selectedLearning.value ?: LearningStyle.SOME_TIPS
            )
            _generatedStrategy.value = MockStrategy.generateFromSurvey(survey)

            delay(500)
            // Advance to disclaimer
            _currentStep.value = OnboardingStep.DISCLAIMER
        }
    }

    /**
     * Launch Google Play in-app review, then advance to next step.
     * Runs in viewModelScope so it survives recomposition/AnimatedContent transitions.
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
     * Runs the full analysis pipeline: recognize → market data → AI analysis → save.
     */
    fun onImageCaptured(imageData: ByteArray, userId: UUID) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analyzingProgress.value = 0f
            _analysisError.value = null

            // Start progress animation in parallel with real analysis
            val progressJob = launch {
                // Animate to 90% over ~10s, then wait for real completion
                val steps = 90
                val stepDelay = 110L // ~10s total
                for (i in 1..steps) {
                    delay(stepDelay)
                    _analyzingProgress.value = i / 100f
                }
                // Hold at 90% until analysis completes
                while (_isAnalyzing.value) {
                    delay(100)
                }
            }

            // Run real analysis
            val result = analyzeChartUseCase.execute(imageData, userId)

            // Complete progress
            progressJob.cancel()
            _analyzingProgress.value = 1f

            result.onSuccess { analysis ->
                Log.d(TAG, "Analysis complete: ${analysis.signal} ${analysis.confidenceScore}%")
                _analysisResult.value = analysis
                delay(300)
                completeOnboarding()
            }.onFailure { error ->
                Log.e(TAG, "Analysis failed: ${error.message}", error)
                _analysisError.value = error.message ?: "Analysis failed"
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Fallback: skip analysis and complete onboarding.
     */
    fun skipAnalysis() {
        completeOnboarding()
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            onboardingRepository.completeOnboarding(userId)
            _isCompleted.value = true
        }
    }
}
