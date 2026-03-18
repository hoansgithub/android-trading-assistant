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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.domain.model.Analysis
import co.alcheclub.ai.trading.assistant.domain.model.RiskLevel
import co.alcheclub.ai.trading.assistant.domain.model.TradingSignal
import co.alcheclub.ai.trading.assistant.modules.main.components.AssetIconView
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.Bearish
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.BgPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.Border
import co.alcheclub.ai.trading.assistant.ui.theme.Caution
import co.alcheclub.ai.trading.assistant.ui.theme.Danger
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextMuted
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary
import co.alcheclub.ai.trading.assistant.ui.theme.TextTertiary
import co.alcheclub.ai.trading.assistant.ui.theme.Warning
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisDetailScreen(
    analysis: Analysis,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    val dimens = AppDimens.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDisclaimer by remember { mutableStateOf(false) }

    val signalColor = when (analysis.signal) {
        TradingSignal.BULLISH -> Emerald
        TradingSignal.BEARISH -> Bearish
        else -> Caution
    }

    // Delete confirmation
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.analysis_delete_title), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold) },
            text = { Text(stringResource(R.string.analysis_delete_message), fontFamily = PoppinsFontFamily) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text(stringResource(R.string.delete), color = Danger, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel), fontFamily = PoppinsFontFamily)
                }
            }
        )
    }

    // Disclaimer bottom sheet
    if (showDisclaimer) {
        ModalBottomSheet(
            onDismissRequest = { showDisclaimer = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = BgPrimary
        ) {
            DisclaimerContent(onDone = { showDisclaimer = false })
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BgPrimary).statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = TextPrimary)
            }
            Text(
                text = analysis.assetName ?: analysis.assetSymbol,
                fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp, color = TextPrimary, modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, stringResource(R.string.more), tint = TextSecondary)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.disclaimer), fontFamily = PoppinsFontFamily) },
                        onClick = { showMenu = false; showDisclaimer = true },
                        leadingIcon = { Icon(Icons.Default.Warning, null, modifier = Modifier.size(18.dp)) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_analysis), fontFamily = PoppinsFontFamily, color = Danger) },
                        onClick = { showMenu = false; showDeleteDialog = true },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Danger, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = dimens.spaceLg),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Signal Hero
            SignalHeroSection(analysis, signalColor)

            // Action Plan
            ActionPlanCard(analysis, signalColor)

            // AI Explanation
            if (analysis.aiExplanation.isNotEmpty()) {
                SectionCard(icon = Icons.Default.Info, title = stringResource(R.string.analysis_ai_analysis), color = Emerald) {
                    MarkdownText(
                        text = analysis.aiExplanation,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            // Risk Assessment
            RiskAssessmentCard(analysis)

            // Market Context
            if (!analysis.marketContext.isNullOrEmpty()) {
                SectionCard(icon = Icons.AutoMirrored.Filled.TrendingUp, title = stringResource(R.string.analysis_market_context), color = Emerald) {
                    Text(
                        text = analysis.marketContext,
                        fontFamily = PoppinsFontFamily, fontSize = 14.sp,
                        color = TextPrimary, lineHeight = 22.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SignalHeroSection(analysis: Analysis, signalColor: Color) {
    val signalIcon = when (analysis.signal) {
        TradingSignal.BULLISH -> Icons.AutoMirrored.Filled.TrendingUp
        TradingSignal.BEARISH -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingFlat
    }

    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(signalColor.copy(alpha = 0.25f), signalColor.copy(alpha = 0.08f))))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Asset info
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AssetIconView(symbol = analysis.assetSymbol, size = 36.dp)
            Text(analysis.assetSymbol, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
        }

        Spacer(Modifier.height(12.dp))

        // Signal badge
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(signalIcon, null, Modifier.size(24.dp), tint = signalColor)
            }
            Column {
                Text(analysis.signal.value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary)
                Text(stringResource(R.string.analysis_signal_detected), fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = TextSecondary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Confidence bar
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.analysis_confidence), fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = TextSecondary)
            Text("${analysis.confidenceScore}%", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(signalColor.copy(alpha = 0.2f))) {
            Box(Modifier.fillMaxWidth(analysis.confidenceScore / 100f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(signalColor))
        }

        Spacer(Modifier.height(12.dp))

        // Metadata chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetadataChip(analysis.timeframe.uppercase(), signalColor)
            MetadataChip(SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(analysis.analyzedAt), signalColor)
            MetadataChip(analysis.analysisType.value, signalColor)
        }
    }
}

@Composable
private fun MetadataChip(text: String, color: Color) {
    Text(
        text = text, fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextSecondary,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun ActionPlanCard(analysis: Analysis, signalColor: Color) {
    SectionCard(icon = Icons.AutoMirrored.Filled.TrendingUp, title = stringResource(R.string.analysis_action_plan), color = Emerald) {
        ActionRow(stringResource(R.string.analysis_current_price), "$${formatPrice(analysis.currentPrice)}", TextSecondary)

        if (analysis.actionPlan.entryMin != null && analysis.actionPlan.entryMax != null) {
            ThinDivider()
            ActionRow(stringResource(R.string.analysis_entry_zone), "$${formatPrice(analysis.actionPlan.entryMin)} - $${formatPrice(analysis.actionPlan.entryMax)}", Emerald)
        }

        ThinDivider()
        ActionRow(stringResource(R.string.analysis_stop_loss), "$${formatPrice(analysis.actionPlan.stopLoss)}", Bearish)

        analysis.actionPlan.takeProfits.forEach { tp ->
            ThinDivider()
            val subtitle = tp.riskRewardRatio?.let { "R:R ${it.setScale(1, java.math.RoundingMode.HALF_UP)}:1" }
            ActionRow(stringResource(R.string.analysis_take_profit, tp.level), "$${formatPrice(tp.price)}", Emerald, subtitle)
        }
    }
}

@Composable
private fun ActionRow(label: String, value: String, valueColor: Color, subtitle: String? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = valueColor)
            if (subtitle != null) {
                Text(subtitle, fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun ThinDivider() {
    Box(Modifier.fillMaxWidth().padding(start = 32.dp).height(0.5.dp).background(Border))
}

@Composable
private fun RiskAssessmentCard(analysis: Analysis) {
    val riskColor = when (analysis.riskAssessment.level) {
        RiskLevel.LOW -> Emerald
        RiskLevel.MODERATE -> Caution
        RiskLevel.HIGH -> Warning
        RiskLevel.VERY_HIGH -> Danger
    }

    SectionCard(icon = Icons.Default.Warning, title = stringResource(R.string.analysis_risk_assessment), color = riskColor) {
        // Risk badge
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = analysis.riskAssessment.level.name.replace("_", " "),
                fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = riskColor,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(riskColor.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        if (analysis.riskAssessment.factors.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.analysis_key_factors), fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            analysis.riskAssessment.factors.forEach { factor ->
                Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.padding(top = 7.dp).size(6.dp).clip(CircleShape).background(Emerald))
                    Spacer(Modifier.width(8.dp))
                    Text(factor, fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextPrimary)
                }
            }
        }

        if (analysis.riskAssessment.warnings.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Warning.copy(alpha = 0.08f)).padding(12.dp)
            ) {
                Text(stringResource(R.string.analysis_warnings), fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = Warning)
                Spacer(Modifier.height(4.dp))
                analysis.riskAssessment.warnings.forEach { warning ->
                    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Warning, null, Modifier.size(14.dp).padding(top = 2.dp), tint = Warning)
                        Spacer(Modifier.width(8.dp))
                        Text(warning, fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(icon: ImageVector, title: String, color: Color, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BgCard).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, Modifier.size(20.dp), tint = color)
            Text(title, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun DisclaimerContent(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Warning, null, Modifier.size(48.dp), tint = Caution)
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.disclaimer), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.disclaimer_text),
            fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary,
            textAlign = TextAlign.Center, lineHeight = 22.sp
        )
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onDone) {
            Text(stringResource(R.string.done), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Emerald)
        }
    }
}

/**
 * Renders inline markdown to styled AnnotatedString.
 * Supports: **bold**, *italic*, • bullets, \n line breaks.
 * Matching iOS AttributedString(markdown:) behavior.
 */
@Composable
private fun MarkdownText(text: String, fontSize: TextUnit, lineHeight: TextUnit) {
    // Normalize line breaks
    val normalized = text
        .replace("\\n", "\n")
        .replace("\r\n", "\n")

    val annotated = buildAnnotatedString {
        var i = 0
        val src = normalized
        while (i < src.length) {
            when {
                // **bold**
                i + 1 < src.length && src[i] == '*' && src[i + 1] == '*' -> {
                    val end = src.indexOf("**", i + 2)
                    if (end > 0) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                            append(src.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(src[i])
                        i++
                    }
                }
                // *italic* (but not **)
                src[i] == '*' && (i + 1 >= src.length || src[i + 1] != '*') -> {
                    val end = src.indexOf('*', i + 1)
                    if (end > 0) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = TextPrimary)) {
                            append(src.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(src[i])
                        i++
                    }
                }
                else -> {
                    append(src[i])
                    i++
                }
            }
        }
    }

    Text(
        text = annotated,
        fontFamily = PoppinsFontFamily,
        fontSize = fontSize,
        color = TextPrimary,
        lineHeight = lineHeight
    )
}

private fun formatPrice(price: BigDecimal): String {
    val d = price.toDouble()
    return when {
        d >= 1000 -> "%,.2f".format(d)
        d >= 1 -> "%,.2f".format(d)
        d >= 0.01 -> "%.4f".format(d)
        else -> "%.6f".format(d)
    }
}
