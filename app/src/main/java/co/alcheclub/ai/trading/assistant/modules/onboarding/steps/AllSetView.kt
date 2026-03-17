package co.alcheclub.ai.trading.assistant.modules.onboarding.steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.domain.model.MockStrategy
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

@Composable
fun AllSetView(
    strategy: MockStrategy?,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AppDimens.current
    val displayStrategy = strategy ?: MockStrategy(
        name = "Steady Grower",
        style = "Swing Trading",
        timeframe = "4H - Daily",
        riskPerTrade = "1-2%",
        direction = "Long Bias"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimens.spaceXxl)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(dimens.space2Xl))

        // All set image
        Image(
            painter = painterResource(id = R.drawable.ic_all_set),
            contentDescription = "All set",
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(dimens.spaceXxl))

        Text(
            text = "You're all set!",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimens.spaceSm))

        Text(
            text = "We've created the \"${displayStrategy.name}\" strategy for you.",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimens.spaceXxl))

        // Strategy summary card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BgCard)
                .padding(dimens.spaceLg),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceMd)
        ) {
            StrategyRow(label = "Strategy", value = displayStrategy.name)
            StrategyRow(label = "Style", value = displayStrategy.style)
            StrategyRow(label = "Timeframe", value = displayStrategy.timeframe)
            StrategyRow(label = "Risk/Trade", value = displayStrategy.riskPerTrade)
            StrategyRow(label = "Direction", value = displayStrategy.direction)
        }

        Spacer(modifier = Modifier.height(dimens.spaceLg))

        // Footer text (matching iOS)
        Row {
            Text(
                text = "You can change this anytime in the ",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = "Strategy tab.",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Emerald,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "Start Your First Analysis",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(dimens.space2Xl))
    }
}

@Composable
private fun StrategyRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Emerald
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}
