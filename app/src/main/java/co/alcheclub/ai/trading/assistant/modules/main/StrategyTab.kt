package co.alcheclub.ai.trading.assistant.modules.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.model.TradingStyle
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.Caution
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyTab(
    viewModel: StrategyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val dimens = AppDimens.current

    LaunchedEffect(Unit) { viewModel.onViewAppear() }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is StrategyUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald, modifier = Modifier.size(40.dp))
                }
            }

            is StrategyUiState.Empty -> EmptyStrategiesView()

            is StrategyUiState.Loaded -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = dimens.spaceLg, vertical = dimens.spaceMd),
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceMd)
                ) {
                    items(state.strategies, key = { it.id }) { strategy ->
                        StrategyCard(strategy = strategy)
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }

            is StrategyUiState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(dimens.spaceXxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(dimens.spaceLg))
                    Button(
                        onClick = { viewModel.refresh() },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.Black)
                    ) {
                        Text("Retry", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStrategiesView() {
    val dimens = AppDimens.current
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = dimens.spaceXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TextSecondary.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(dimens.spaceXxl))
        Text(
            text = "No Strategies Yet",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(dimens.spaceSm))
        Text(
            text = "Create a trading strategy to\npersonalize your analysis",
            fontFamily = PoppinsFontFamily,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun StrategyCard(strategy: Strategy) {
    val dimens = AppDimens.current
    val styleIcon = when (strategy.style) {
        TradingStyle.SCALPING -> "\u26A1"
        TradingStyle.DAY_TRADING -> "\u2600\uFE0F"
        TradingStyle.SWING_TRADING -> "\uD83D\uDCC8"
        TradingStyle.POSITION_TRADING -> "\uD83D\uDCC5"
        TradingStyle.INVESTING -> "\uD83C\uDFE6"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(dimens.spaceLg)
    ) {
        // Header: Icon + Name + Style + Preset badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Emerald.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = styleIcon, fontSize = 20.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = strategy.name,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (strategy.isPreset) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Caution.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRESET",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Caution
                            )
                        }
                    }
                }
                Text(
                    text = strategy.style.displayName,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(dimens.spaceMd))

        // Metrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StrategyMetric(label = "Timeframe", value = strategy.timeframe.uppercase())
            StrategyMetric(label = "Direction", value = strategy.direction.displayName)
            StrategyMetric(label = "Entry Rules", value = "${strategy.enabledEntryRuleCount}")
        }

        Spacer(Modifier.height(dimens.spaceSm))

        // Footer: Risk + Stop Loss
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Risk: ${strategy.riskPerTradeFormatted}/trade",
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = TextSecondary
            )
            if (strategy.stopLossDescription.isNotEmpty()) {
                Text(
                    text = "SL: ${strategy.stopLossDescription}",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StrategyMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontSize = 10.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = TextPrimary,
            maxLines = 1
        )
    }
}
