package co.alcheclub.ai.trading.assistant.modules.main.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.core.analytics.Analytics
import co.alcheclub.ai.trading.assistant.core.analytics.AnalyticsEvent
import co.alcheclub.ai.trading.assistant.ui.theme.Bearish
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.BgElevated
import co.alcheclub.ai.trading.assistant.ui.theme.Border
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

private const val PREF_KEY = "hasDisabledChartImageGuide"

/**
 * Check if chart image guide has been permanently dismissed.
 */
fun isChartGuideDisabled(context: Context): Boolean {
    return context.getSharedPreferences("chart_guide", Context.MODE_PRIVATE)
        .getBoolean(PREF_KEY, false)
}

private fun disableChartGuide(context: Context) {
    context.getSharedPreferences("chart_guide", Context.MODE_PRIVATE)
        .edit().putBoolean(PREF_KEY, true).apply()
}

/**
 * Full-screen overlay showing chart image quality tips.
 * Matches iOS ChartImageGuideOverlay with good/bad comparison and tips.
 */
@Composable
fun ChartImageGuideOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onDontShowAgain: () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        val context = LocalContext.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Block clicks behind overlay */ },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .border(1.dp, Border, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = stringResource(R.string.guide_title),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )

                Spacer(Modifier.height(16.dp))

                // Good / Bad comparison
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExampleCard(
                        imageRes = R.drawable.tutorial_chart_good,
                        label = stringResource(R.string.guide_good),
                        isGood = true,
                        modifier = Modifier.weight(1f)
                    )
                    ExampleCard(
                        imageRes = R.drawable.tutorial_chart_bad,
                        label = stringResource(R.string.guide_bad),
                        isGood = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Tips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TipRow(icon = Icons.Default.Image, text = stringResource(R.string.guide_tip_quality))
                    TipRow(icon = Icons.Default.TextFields, text = stringResource(R.string.guide_tip_asset))
                    TipRow(icon = Icons.Default.Schedule, text = stringResource(R.string.guide_tip_timeframe))
                }

                Spacer(Modifier.height(16.dp))

                // OK button
                Button(
                    onClick = {
                        Analytics.track(AnalyticsEvent.CHART_GUIDE, mapOf(AnalyticsEvent.Param.VALUE to "ok"))
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.ok),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Don't show again button
                Button(
                    onClick = {
                        Analytics.track(AnalyticsEvent.CHART_GUIDE, mapOf(AnalyticsEvent.Param.VALUE to "dont_show_again"))
                        disableChartGuide(context)
                        onDontShowAgain()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BgElevated,
                        contentColor = TextSecondary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.guide_dont_show),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ExampleCard(
    imageRes: Int,
    label: String,
    isGood: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isGood) Emerald else Bearish

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = label,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isGood) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = borderColor
            )
            Text(
                text = label,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = borderColor
            )
        }
    }
}

@Composable
private fun TipRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Emerald
        )
        Text(
            text = text,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}
