package co.alcheclub.ai.trading.assistant.modules.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.alcheclub.ai.trading.assistant.domain.model.ExperienceLevel
import co.alcheclub.ai.trading.assistant.domain.model.LearningStyle
import co.alcheclub.ai.trading.assistant.domain.model.PrimaryGoal
import co.alcheclub.ai.trading.assistant.domain.model.RiskComfort
import co.alcheclub.ai.trading.assistant.domain.model.TimeAvailability
import co.alcheclub.ai.trading.assistant.modules.onboarding.components.OnboardingProgressBar
import co.alcheclub.ai.trading.assistant.modules.onboarding.components.SurveyStepView
import co.alcheclub.ai.trading.assistant.modules.onboarding.steps.AllSetView
import co.alcheclub.ai.trading.assistant.modules.onboarding.steps.DisclaimerView
import co.alcheclub.ai.trading.assistant.modules.onboarding.steps.ProcessingView
import co.alcheclub.ai.trading.assistant.modules.onboarding.steps.RateUsView
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.BgPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val dimens = AppDimens.current

    // Navigate when completed (LaunchedEffect prevents multiple fires on recomposition)
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            onComplete()
        }
    }
    if (isCompleted) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Header with back button + progress bar (steps 1-6 only)
        if (currentStep.showsHeader) {
            Spacer(modifier = Modifier.height(dimens.space3Xl))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.spaceLg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep.stepNumber > 1) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { viewModel.goToPreviousStep() },
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(dimens.spaceMd))
                } else {
                    // Placeholder for alignment
                    Spacer(modifier = Modifier.width(36.dp))
                }

                OnboardingProgressBar(
                    currentStep = currentStep.stepNumber,
                    modifier = Modifier.weight(1f)
                )

                // Trailing spacer for symmetry
                Spacer(modifier = Modifier.width(36.dp))
            }
        }

        // Step content
        Box(modifier = Modifier.weight(1f)) {
            when (currentStep) {
                OnboardingStep.EXPERIENCE_LEVEL -> {
                    val selected by viewModel.selectedExperience.collectAsState()
                    SurveyStepView(
                        question = "How familiar are you with trading?",
                        options = ExperienceLevel.entries.toList(),
                        selectedOption = selected,
                        onOptionSelected = { viewModel.selectExperience(it) },
                        onContinue = { viewModel.goToNextStep() },
                        optionId = { it.name },
                        optionEmoji = { it.emoji },
                        optionTitle = { it.displayName },
                        optionSubtitle = { it.subtitle }
                    )
                }

                OnboardingStep.TIME_AVAILABILITY -> {
                    val selected by viewModel.selectedTime.collectAsState()
                    SurveyStepView(
                        question = "How often can you check the markets?",
                        options = TimeAvailability.entries.toList(),
                        selectedOption = selected,
                        onOptionSelected = { viewModel.selectTime(it) },
                        onContinue = { viewModel.goToNextStep() },
                        optionId = { it.name },
                        optionEmoji = { it.emoji },
                        optionTitle = { it.displayName },
                        optionSubtitle = { it.subtitle }
                    )
                }

                OnboardingStep.RISK_COMFORT -> {
                    val selected by viewModel.selectedRisk.collectAsState()
                    SurveyStepView(
                        question = "How do you feel about risk?",
                        options = RiskComfort.entries.toList(),
                        selectedOption = selected,
                        onOptionSelected = { viewModel.selectRisk(it) },
                        onContinue = { viewModel.goToNextStep() },
                        optionId = { it.name },
                        optionEmoji = { it.emoji },
                        optionTitle = { it.displayName },
                        optionSubtitle = { it.subtitle }
                    )
                }

                OnboardingStep.PRIMARY_GOAL -> {
                    val selected by viewModel.selectedGoal.collectAsState()
                    SurveyStepView(
                        question = "What's your main goal?",
                        options = PrimaryGoal.entries.toList(),
                        selectedOption = selected,
                        onOptionSelected = { viewModel.selectGoal(it) },
                        onContinue = { viewModel.goToNextStep() },
                        optionId = { it.name },
                        optionEmoji = { it.emoji },
                        optionTitle = { it.displayName },
                        optionSubtitle = { it.subtitle }
                    )
                }

                OnboardingStep.LEARNING_STYLE -> {
                    val selected by viewModel.selectedLearning.collectAsState()
                    SurveyStepView(
                        question = "Do you want to learn while analyzing?",
                        options = LearningStyle.entries.toList(),
                        selectedOption = selected,
                        onOptionSelected = { viewModel.selectLearning(it) },
                        onContinue = { viewModel.goToNextStep() },
                        optionId = { it.name },
                        optionEmoji = { it.emoji },
                        optionTitle = { it.displayName },
                        optionSubtitle = { it.subtitle }
                    )
                }

                OnboardingStep.RATE_US -> {
                    RateUsView(
                        onRateUs = { viewModel.goToNextStep() }
                    )
                }

                OnboardingStep.PROCESSING -> {
                    val progress by viewModel.processingProgress.collectAsState()
                    ProcessingView(progress = progress)
                }

                OnboardingStep.DISCLAIMER -> {
                    DisclaimerView(
                        onContinue = { viewModel.goToNextStep() }
                    )
                }

                OnboardingStep.ALL_SET -> {
                    val strategy by viewModel.generatedStrategy.collectAsState()
                    AllSetView(
                        strategy = strategy,
                        onStart = { viewModel.completeOnboarding() }
                    )
                }
            }
        }
    }
}
