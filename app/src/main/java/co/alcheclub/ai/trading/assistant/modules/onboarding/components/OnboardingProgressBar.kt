package co.alcheclub.ai.trading.assistant.modules.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import co.alcheclub.ai.trading.assistant.modules.onboarding.OnboardingStep
import co.alcheclub.ai.trading.assistant.ui.theme.Border
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald

@Composable
fun OnboardingProgressBar(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val totalSteps = OnboardingStep.SURVEY_STEP_COUNT
    val shape = RoundedCornerShape(2.dp)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..totalSteps) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(shape)
                    .then(
                        if (i <= currentStep) {
                            Modifier.background(Emerald, shape)
                        } else {
                            Modifier.border(1.dp, Border, shape)
                        }
                    )
            )
        }
    }
}
