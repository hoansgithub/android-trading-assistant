package co.alcheclub.ai.trading.assistant.modules.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.core.analytics.AnalyticsEvent
import co.alcheclub.ai.trading.assistant.core.compose.TrackScreenView
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.model.TradingStyle
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.BgPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.Caution
import co.alcheclub.ai.trading.assistant.ui.theme.Danger
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.EmeraldDark
import co.alcheclub.ai.trading.assistant.ui.theme.TextMuted
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyTab(
    viewModel: StrategyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMore.collectAsStateWithLifecycle()
    val dimens = AppDimens.current
    var strategyToDelete by remember { mutableStateOf<Strategy?>(null) }
    var selectedStrategy by remember { mutableStateOf<Strategy?>(null) }
    var showBuilder by remember { mutableStateOf(false) }
    var editingStrategy by remember { mutableStateOf<Strategy?>(null) }

    TrackScreenView(AnalyticsEvent.Screen.STRATEGY, "StrategyTab")

    LaunchedEffect(Unit) { viewModel.onViewAppear() }

    // Delete confirmation dialog
    if (strategyToDelete != null) {
        AlertDialog(
            onDismissRequest = { strategyToDelete = null },
            title = { Text(stringResource(R.string.strategy_delete_title), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold) },
            text = { Text(stringResource(R.string.strategy_delete_message), fontFamily = PoppinsFontFamily) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteStrategy(strategyToDelete!!.id)
                    strategyToDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = Danger, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { strategyToDelete = null }) {
                    Text(stringResource(R.string.cancel), fontFamily = PoppinsFontFamily)
                }
            }
        )
    }

    // Error message dialog (e.g. can't delete last strategy)
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text(stringResource(R.string.notice), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold) },
            text = { Text(errorMessage ?: "", fontFamily = PoppinsFontFamily) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text(stringResource(R.string.ok), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Strategy detail screen
    if (selectedStrategy != null) {
        val strategyCount = (uiState as? StrategyUiState.Loaded)?.strategies?.size ?: 0
        StrategyDetailScreen(
            strategy = selectedStrategy!!,
            onBack = { selectedStrategy = null },
            onEdit = {
                editingStrategy = selectedStrategy
                selectedStrategy = null
            },
            onDuplicate = {
                viewModel.duplicateStrategy(selectedStrategy!!)
                selectedStrategy = null
            },
            onDelete = {
                viewModel.deleteStrategy(selectedStrategy!!.id)
                selectedStrategy = null
            },
            canDelete = strategyCount > 1
        )
        return
    }

    // Strategy builder screen (create or edit)
    if (showBuilder || editingStrategy != null) {
        StrategyBuilderScreen(
            existingStrategy = editingStrategy,
            onDismiss = { showBuilder = false; editingStrategy = null },
            onSave = { strategy ->
                if (editingStrategy != null) {
                    viewModel.updateStrategy(strategy)
                } else {
                    viewModel.createStrategy(strategy)
                }
                showBuilder = false
                editingStrategy = null
            }
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
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
                        StrategyCard(
                            strategy = strategy,
                            onClick = { selectedStrategy = strategy },
                            onEdit = { editingStrategy = strategy },
                            onDuplicate = { viewModel.duplicateStrategy(strategy) },
                            onDelete = { strategyToDelete = strategy }
                        )
                    }
                    if (canLoadMore) {
                        item {
                            LaunchedEffect(Unit) { viewModel.loadMore() }
                            if (isLoadingMore) {
                                co.alcheclub.ai.trading.assistant.modules.main.components.ShimmerLoadingItem()
                            }
                        }
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
                        Text(stringResource(R.string.retry), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // FAB
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(20.dp)
            .size(56.dp)
            .clip(CircleShape)
            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Emerald, EmeraldDark)))
            .clickable { showBuilder = true },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Add, stringResource(R.string.strategy_new), Modifier.size(28.dp), tint = Color.White)
    }
    } // Close outer Box
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
            text = stringResource(R.string.strategy_empty_title),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(dimens.spaceSm))
        Text(
            text = stringResource(R.string.strategy_empty_subtitle),
            fontFamily = PoppinsFontFamily,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun StrategyCard(
    strategy: Strategy,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDuplicate: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
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
            .clickable(onClick = onClick)
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
                                text = stringResource(R.string.preset),
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

            // Three-dot menu
            Box {
                var showCardMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showCardMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, stringResource(R.string.menu), Modifier.size(18.dp), tint = TextMuted)
                }
                DropdownMenu(expanded = showCardMenu, onDismissRequest = { showCardMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit), fontFamily = PoppinsFontFamily) },
                        onClick = { showCardMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.duplicate), fontFamily = PoppinsFontFamily) },
                        onClick = { showCardMenu = false; onDuplicate() },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp)) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete), fontFamily = PoppinsFontFamily, color = Danger) },
                        onClick = { showCardMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Danger, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }

        Spacer(Modifier.height(dimens.spaceMd))

        // Metrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StrategyMetric(label = stringResource(R.string.label_timeframe), value = strategy.timeframe.uppercase())
            StrategyMetric(label = stringResource(R.string.label_direction), value = strategy.direction.displayName)
            StrategyMetric(label = stringResource(R.string.label_entry_rules), value = "${strategy.enabledEntryRuleCount}")
        }

        Spacer(Modifier.height(dimens.spaceSm))

        // Footer: Risk + Stop Loss
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.strategy_risk_per_trade, strategy.riskPerTradeFormatted),
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = TextSecondary
            )
            if (strategy.stopLossDescription.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.strategy_sl_prefix, strategy.stopLossDescription),
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
