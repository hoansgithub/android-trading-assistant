package co.alcheclub.ai.trading.assistant.modules.onboarding.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.domain.model.ExperienceLevel
import co.alcheclub.ai.trading.assistant.ui.theme.AlphaProfitTheme
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.BgPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun <T> SurveyStepView(
    question: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    onContinue: () -> Unit,
    optionId: (T) -> String,
    optionEmoji: (T) -> String,
    optionTitle: (T) -> String,
    optionSubtitle: (T) -> String,
    modifier: Modifier = Modifier
) {
    val dimens = AppDimens.current

    // Staggered appearance animation state
    var visibleCount by remember(question) { mutableStateOf(0) }
    LaunchedEffect(question) {
        visibleCount = 0
        for (i in options.indices) {
            delay(80L)
            visibleCount = i + 1
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimens.spaceXxl)
    ) {
        Spacer(modifier = Modifier.height(dimens.spaceXl))

        Text(
            text = question,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = TextPrimary,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(dimens.spaceXxl))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceMd),
            contentPadding = PaddingValues(bottom = dimens.spaceLg)
        ) {
            itemsIndexed(
                items = options,
                key = { _, item -> optionId(item) }
            ) { index, option ->
                AnimatedVisibility(
                    visible = index < visibleCount,
                    enter = fadeIn(tween(250)) + slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { it / 3 }
                    )
                ) {
                    OnboardingOptionCard(
                        emoji = optionEmoji(option),
                        title = optionTitle(option),
                        subtitle = optionSubtitle(option),
                        isSelected = option == selectedOption,
                        onClick = { onOptionSelected(option) }
                    )
                }
            }
        }

        Button(
            onClick = onContinue,
            enabled = selectedOption != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Emerald,
                contentColor = Color.Black,
                disabledContainerColor = Emerald.copy(alpha = 0.3f),
                disabledContentColor = Color.Black.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = stringResource(R.string.continue_text),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(dimens.space2Xl))
    }
}

@Preview(showBackground = true)
@Composable
private fun SurveyStepViewPreview() {
    AlphaProfitTheme {
        var selected by remember { mutableStateOf<ExperienceLevel?>(ExperienceLevel.SOME) }

        SurveyStepView(
            question = "How familiar are you with trading?",
            options = ExperienceLevel.entries,
            selectedOption = selected,
            onOptionSelected = { selected = it },
            onContinue = {},
            optionId = { it.name },
            optionEmoji = { it.emoji },
            optionTitle = { it.displayName },
            optionSubtitle = { it.subtitle },
            modifier = Modifier.background(BgPrimary)
        )
    }
}
