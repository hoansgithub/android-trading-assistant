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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import co.alcheclub.ai.trading.assistant.domain.model.Analysis
import co.alcheclub.ai.trading.assistant.domain.model.RiskLevel
import co.alcheclub.ai.trading.assistant.domain.model.TradingSignal
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.Bearish
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.Caution
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary
import co.alcheclub.ai.trading.assistant.ui.theme.Warning
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val dimens = AppDimens.current

    LaunchedEffect(Unit) { viewModel.onViewAppear() }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald, modifier = Modifier.size(40.dp))
                }
            }

            is HomeUiState.Empty -> EmptyAnalysesView()

            is HomeUiState.Loaded -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = dimens.spaceLg, vertical = dimens.spaceMd),
                    verticalArrangement = Arrangement.spacedBy(dimens.spaceMd)
                ) {
                    items(state.analyses, key = { it.id }) { analysis ->
                        AnalysisCard(analysis = analysis)
                    }
                    item { Spacer(Modifier.height(80.dp)) } // FAB clearance
                }
            }

            is HomeUiState.Error -> {
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
private fun EmptyAnalysesView() {
    val dimens = AppDimens.current
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = dimens.spaceXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TextSecondary.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(dimens.spaceXxl))
        Text(
            text = "No Analyses Yet",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(dimens.spaceSm))
        Text(
            text = "Capture a trading chart to get\nAI-powered analysis",
            fontFamily = PoppinsFontFamily,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun AnalysisCard(analysis: Analysis) {
    val dimens = AppDimens.current
    val signalColor = when (analysis.signal) {
        TradingSignal.BULLISH -> Emerald
        TradingSignal.BEARISH -> Bearish
        else -> TextSecondary
    }
    val riskColor = when (analysis.riskAssessment.level) {
        RiskLevel.LOW -> Emerald
        RiskLevel.MODERATE -> Caution
        RiskLevel.HIGH -> Warning
        RiskLevel.VERY_HIGH -> Bearish
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(dimens.spaceLg)
    ) {
        // Header: Symbol + Signal badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = analysis.assetSymbol,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (analysis.strategyName != null) {
                    Text(
                        text = analysis.strategyName,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Signal badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(signalColor.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = analysis.signal.value,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = signalColor
                )
            }
        }

        Spacer(Modifier.height(dimens.spaceMd))

        // Metrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricItem(label = "Confidence", value = "${analysis.confidenceScore}%")
            MetricItem(label = "Price", value = formatPrice(analysis.currentPrice))
            MetricItem(label = "Timeframe", value = analysis.timeframe.uppercase())
            MetricItem(label = "Risk", value = analysis.riskAssessment.level.name.replace("_", " "), valueColor = riskColor)
        }

        Spacer(Modifier.height(dimens.spaceSm))

        // Footer: Relative time
        Text(
            text = relativeTime(analysis.analyzedAt),
            fontFamily = PoppinsFontFamily,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
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
            color = valueColor,
            maxLines = 1
        )
    }
}

private fun formatPrice(price: java.math.BigDecimal): String {
    val d = price.toDouble()
    return when {
        d >= 1000 -> "%,.0f".format(d)
        d >= 1 -> "%,.2f".format(d)
        d >= 0.01 -> "%.4f".format(d)
        else -> "%.6f".format(d)
    }
}

private fun relativeTime(date: java.util.Date): String {
    val diffMs = System.currentTimeMillis() - date.time
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val days = TimeUnit.MILLISECONDS.toDays(diffMs)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "${days / 7}w ago"
    }
}
