package co.alcheclub.ai.trading.assistant.modules.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.alcheclub.ai.trading.assistant.domain.model.ExperienceLevel
import co.alcheclub.ai.trading.assistant.domain.model.LearningStyle
import co.alcheclub.ai.trading.assistant.domain.model.MockStrategy
import co.alcheclub.ai.trading.assistant.domain.model.OnboardingSurvey
import co.alcheclub.ai.trading.assistant.domain.model.PrimaryGoal
import co.alcheclub.ai.trading.assistant.domain.model.RiskComfort
import co.alcheclub.ai.trading.assistant.domain.model.TimeAvailability
import co.alcheclub.ai.trading.assistant.domain.repository.OnboardingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

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

    fun completeOnboarding() {
        onboardingRepository.completeOnboarding()
        _isCompleted.value = true
    }
}
