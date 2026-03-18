package co.alcheclub.ai.trading.assistant.modules.main

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.model.TradingStyle
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.BgPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.Caution
import co.alcheclub.ai.trading.assistant.ui.theme.Danger
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextMuted
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

@Composable
fun StrategyDetailScreen(
    strategy: Strategy,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Strategy?", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold) },
            text = { Text("This strategy will be permanently deleted. Existing analyses will keep their strategy snapshot.", fontFamily = PoppinsFontFamily) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = Danger, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", fontFamily = PoppinsFontFamily)
                }
            }
        )
    }

    val styleEmoji = when (strategy.style) {
        TradingStyle.SCALPING -> "\u26A1"
        TradingStyle.DAY_TRADING -> "\u2600\uFE0F"
        TradingStyle.SWING_TRADING -> "\uD83D\uDCC8"
        TradingStyle.POSITION_TRADING -> "\uD83D\uDCC5"
        TradingStyle.INVESTING -> "\uD83C\uDFE6"
    }

    Column(Modifier.fillMaxSize().background(BgPrimary).statusBarsPadding()) {
        // Top bar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(strategy.name, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp, color = TextPrimary, modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More", tint = TextSecondary)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit", fontFamily = PoppinsFontFamily) },
                        onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, null, Modifier.size(18.dp)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate", fontFamily = PoppinsFontFamily) },
                        onClick = { showMenu = false; onDuplicate() },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp)) }
                    )
                    if (canDelete) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete", fontFamily = PoppinsFontFamily, color = Danger) },
                            onClick = { showMenu = false; showDeleteDialog = true },
                            leadingIcon = { Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = Danger) }
                        )
                    }
                }
            }
        }

        // Content
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BgCard).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape).background(Emerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(styleEmoji, fontSize = 28.sp) }

                Spacer(Modifier.height(12.dp))
                Text(strategy.name, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)
                Text(strategy.style.displayName, fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)

                if (strategy.description.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(strategy.description, fontFamily = PoppinsFontFamily, fontSize = 13.sp, color = TextMuted,
                        lineHeight = 20.sp, modifier = Modifier.fillMaxWidth())
                }

                if (strategy.isPreset) {
                    Spacer(Modifier.height(8.dp))
                    Text("PRESET", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp,
                        color = Caution, modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(Caution.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 3.dp))
                }

                Spacer(Modifier.height(12.dp))

                // Badges row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoBadge(strategy.timeframe.uppercase())
                    InfoBadge(strategy.direction.displayName)
                    InfoBadge(strategy.riskPerTradeFormatted)
                }
            }

            // Entry Rules
            DetailSection(icon = Icons.Default.Speed, title = "Entry Rules", color = Emerald) {
                if (strategy.enabledEntryRuleCount > 0) {
                    Text("${strategy.enabledEntryRuleCount} active rule${if (strategy.enabledEntryRuleCount > 1) "s" else ""}",
                        fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextPrimary)
                } else {
                    Text("No entry rules configured", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextMuted)
                }
            }

            // Targets
            DetailSection(icon = Icons.Default.Shield, title = "Targets", color = Emerald) {
                if (strategy.stopLossDescription.isNotEmpty()) {
                    DetailRow("Stop Loss", strategy.stopLossDescription)
                }
            }

            // Risk Management
            DetailSection(icon = Icons.Default.Warning, title = "Risk Management", color = Emerald) {
                DetailRow("Risk per Trade", strategy.riskPerTradeFormatted)
                DetailRow("Max Positions", strategy.maxOpenPositions.toString())
                DetailRow("Direction", strategy.direction.displayName)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoBadge(text: String) {
    Text(text, fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Emerald.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 4.dp))
}

@Composable
private fun DetailSection(icon: ImageVector, title: String, color: androidx.compose.ui.graphics.Color, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BgCard).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, Modifier.size(20.dp), tint = color)
            Text(title, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
    }
}
