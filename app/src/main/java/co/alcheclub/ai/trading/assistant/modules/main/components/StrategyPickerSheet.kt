package co.alcheclub.ai.trading.assistant.modules.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.model.TradingStyle
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.BgElevated
import co.alcheclub.ai.trading.assistant.ui.theme.Border
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextMuted
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

/**
 * Strategy selection list with radio buttons matching iOS StrategySelectionView.
 * Used inside a BottomSheet before starting analysis.
 */
@Composable
fun StrategyPickerContent(
    strategies: List<Strategy>,
    selectedStrategy: Strategy?,
    onSelect: (Strategy) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Select a Strategy",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = TextPrimary
        )

        Spacer(Modifier.height(16.dp))

        if (strategies.isEmpty()) {
            Text(
                text = "No strategies found. Create one in the Strategy tab.",
                fontFamily = PoppinsFontFamily,
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(strategies, key = { it.id }) { strategy ->
                    val isSelected = selectedStrategy?.id == strategy.id
                    StrategyOptionCard(
                        strategy = strategy,
                        isSelected = isSelected,
                        onClick = { onSelect(strategy) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Start Analysis button (pinned at bottom)
        Button(
            onClick = onStart,
            enabled = selectedStrategy != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedStrategy != null) Emerald else Border,
                contentColor = Color.White,
                disabledContainerColor = Border,
                disabledContentColor = TextMuted
            )
        ) {
            Text(
                text = "Start Analysis",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
private fun StrategyOptionCard(
    strategy: Strategy,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Emerald else Border
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Style icon
        val iconBg = if (isSelected) Emerald.copy(alpha = 0.15f) else BgElevated
        val iconColor = if (isSelected) Emerald else TextMuted
        val styleEmoji = when (strategy.style) {
            TradingStyle.SCALPING -> "\u26A1"
            TradingStyle.DAY_TRADING -> "\u2600\uFE0F"
            TradingStyle.SWING_TRADING -> "\uD83D\uDCC8"
            TradingStyle.POSITION_TRADING -> "\uD83D\uDCC5"
            TradingStyle.INVESTING -> "\uD83C\uDFE6"
        }

        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = styleEmoji, fontSize = 20.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = strategy.name,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${strategy.style.displayName} · ${strategy.timeframe.uppercase()}",
                fontFamily = PoppinsFontFamily,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }

        Spacer(Modifier.width(12.dp))

        // Radio indicator
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(2.dp, if (isSelected) Emerald else Border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(Modifier.size(14.dp).clip(CircleShape).background(Emerald))
            }
        }
    }
}
