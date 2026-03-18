package co.alcheclub.ai.trading.assistant.modules.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.di.AppModule
import co.alcheclub.ai.trading.assistant.domain.model.ExperienceLevel
import co.alcheclub.ai.trading.assistant.domain.model.LearningStyle
import co.alcheclub.ai.trading.assistant.domain.model.PrimaryGoal
import co.alcheclub.ai.trading.assistant.domain.model.RiskComfort
import co.alcheclub.ai.trading.assistant.domain.model.TimeAvailability
import co.alcheclub.ai.trading.assistant.modules.onboarding.components.OnboardingProgressBar
import co.alcheclub.ai.trading.assistant.modules.onboarding.components.SurveyStepView
import co.alcheclub.ai.trading.assistant.modules.onboarding.steps.AllSetView
import co.alcheclub.ai.trading.assistant.modules.onboarding.steps.AnalyzingChartView
import co.alcheclub.ai.trading.assistant.modules.onboarding.steps.DisclaimerView
import co.alcheclub.ai.trading.assistant.modules.onboarding.steps.ProcessingView
import co.alcheclub.ai.trading.assistant.modules.onboarding.steps.RateUsView
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.BgPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary
import co.alcheclub.ai.trading.assistant.ui.theme.Warning

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    activity: android.app.Activity,
    onComplete: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val isCompleted by viewModel.isCompleted.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analyzingProgress by viewModel.analyzingProgress.collectAsStateWithLifecycle()
    val analysisError by viewModel.analysisError.collectAsStateWithLifecycle()
    val dimens = AppDimens.current

    // Navigate when completed (LaunchedEffect prevents multiple fires on recomposition)
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            onComplete()
        }
    }
    if (isCompleted) return

    // Show analyzing overlay when image is captured
    if (isAnalyzing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPrimary)
        ) {
            AnalyzingChartView(progress = analyzingProgress)
        }
        return
    }

    // Show error screen if analysis failed (matching iOS analysisError state)
    if (analysisError != null) {
        val errorText = analysisError ?: ""
        val parts = errorText.split("\n", limit = 2)
        val title = parts.getOrElse(0) { stringResource(R.string.error_analysis_failed) }
        val message = parts.getOrElse(1) { "" }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPrimary),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(dimens.spaceXxl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Warning
                )

                Spacer(modifier = Modifier.height(dimens.spaceXxl))

                Text(
                    text = title,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(dimens.spaceMd))

                Text(
                    text = message,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(dimens.space2Xl))

                Button(
                    onClick = { viewModel.dismissAnalysisError() },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = stringResource(R.string.continue_text),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Header with back button + progress bar (steps 1-6 only)
        AnimatedVisibility(visible = currentStep.showsHeader) {
            Column {
                Spacer(modifier = Modifier.height(dimens.space3Xl))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.spaceLg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep.stepNumber > 1) {
                        // Back button with proper 44dp tap target (matching iOS)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false, radius = 22.dp)
                                ) { viewModel.goToPreviousStep() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(24.dp),
                                tint = TextPrimary
                            )
                        }
                    } else {
                        // Placeholder for alignment
                        Spacer(modifier = Modifier.width(44.dp))
                    }

                    OnboardingProgressBar(
                        currentStep = currentStep.stepNumber,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = dimens.spaceSm)
                    )

                    // Trailing spacer for symmetry
                    Spacer(modifier = Modifier.width(44.dp))
                }

                Spacer(modifier = Modifier.height(dimens.spaceMd))
            }
        }

        // Step content with slide animation
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val direction = if (targetState.stepNumber > initialState.stepNumber) 1 else -1
                    (slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> direction * fullWidth / 3 }
                    ) + fadeIn(tween(300)))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(300),
                                targetOffsetX = { fullWidth -> -direction * fullWidth / 3 }
                            ) + fadeOut(tween(200))
                        )
                },
                label = "onboardingStep"
            ) { step ->
                when (step) {
                    OnboardingStep.EXPERIENCE_LEVEL -> {
                        val selected by viewModel.selectedExperience.collectAsStateWithLifecycle()
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
                        val selected by viewModel.selectedTime.collectAsStateWithLifecycle()
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
                        val selected by viewModel.selectedRisk.collectAsStateWithLifecycle()
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
                        val selected by viewModel.selectedGoal.collectAsStateWithLifecycle()
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
                        val selected by viewModel.selectedLearning.collectAsStateWithLifecycle()
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
                            onRateUs = { viewModel.requestReviewAndAdvance(activity) }
                        )
                    }

                    OnboardingStep.PROCESSING -> {
                        val progress by viewModel.processingProgress.collectAsStateWithLifecycle()
                        ProcessingView(progress = progress)
                    }

                    OnboardingStep.DISCLAIMER -> {
                        DisclaimerView(
                            onContinue = { viewModel.goToNextStep() }
                        )
                    }

                    OnboardingStep.ALL_SET -> {
                        val strategy by viewModel.generatedStrategy.collectAsStateWithLifecycle()
                        AllSetView(
                            strategy = strategy,
                            onImageCaptured = { imageData ->
                                val userId = AppModule.authRepository.getCurrentUserId()
                                viewModel.onImageCaptured(imageData, userId)
                            }
                        )
                    }
                }
            }
        }
    }
}
