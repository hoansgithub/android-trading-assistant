package co.alcheclub.ai.trading.assistant.modules.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.model.TradingDirection
import co.alcheclub.ai.trading.assistant.domain.model.TradingStyle
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.BgPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.Border
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextMuted
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

/**
 * Strategy create/edit screen matching iOS StrategyBuilderView.
 * When existingStrategy is provided, pre-fills form for editing.
 */
@Composable
fun StrategyBuilderScreen(
    existingStrategy: Strategy? = null,
    onDismiss: () -> Unit,
    onSave: (Strategy) -> Unit
) {
    val isEditing = existingStrategy != null

    var name by remember { mutableStateOf(existingStrategy?.name ?: "") }
    var description by remember { mutableStateOf(existingStrategy?.description ?: "") }
    var selectedStyle by remember { mutableStateOf(existingStrategy?.style ?: TradingStyle.SWING_TRADING) }
    var selectedTimeframe by remember { mutableStateOf(existingStrategy?.timeframe ?: "4h") }
    var selectedDirection by remember { mutableStateOf(existingStrategy?.direction ?: TradingDirection.BOTH) }
    var riskPercent by remember { mutableDoubleStateOf(existingStrategy?.riskPerTradePercent ?: 2.0) }
    var maxPositions by remember { mutableIntStateOf(existingStrategy?.maxOpenPositions ?: 3) }

    val isValid = name.isNotBlank()
    val focusManager = LocalFocusManager.current

    val timeframes = listOf("1m", "5m", "15m", "30m", "1h", "4h", "1d", "1w")

    Column(
        Modifier.fillMaxSize().background(BgPrimary).statusBarsPadding()
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
    ) {
        // Top bar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "Close", tint = TextSecondary)
            }
            Text(
                if (isEditing) stringResource(R.string.strategy_edit) else stringResource(R.string.strategy_new),
                fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp, color = TextPrimary, modifier = Modifier.weight(1f)
            )
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            SectionLabel(stringResource(R.string.strategy_name_label))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(R.string.strategy_name_placeholder), fontFamily = PoppinsFontFamily, color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Emerald
                )
            )

            // Description
            SectionLabel(stringResource(R.string.strategy_description_label))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text(stringResource(R.string.strategy_description_placeholder), fontFamily = PoppinsFontFamily, color = TextMuted) },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Emerald
                )
            )

            // Style picker
            SectionLabel(stringResource(R.string.strategy_style_label))
            ChipGroup(
                options = TradingStyle.entries.map { it.displayName },
                selected = selectedStyle.displayName,
                onSelect = { name -> selectedStyle = TradingStyle.entries.first { it.displayName == name } }
            )

            // Timeframe picker
            SectionLabel(stringResource(R.string.strategy_timeframe_label))
            ChipGroup(
                options = timeframes.map { it.uppercase() },
                selected = selectedTimeframe.uppercase(),
                onSelect = { selectedTimeframe = it.lowercase() }
            )

            // Direction picker
            SectionLabel(stringResource(R.string.strategy_direction_label))
            ChipGroup(
                options = TradingDirection.entries.map { it.displayName },
                selected = selectedDirection.displayName,
                onSelect = { name -> selectedDirection = TradingDirection.entries.first { it.displayName == name } }
            )

            // Risk per trade slider
            SectionLabel(stringResource(R.string.strategy_risk_label, "%.1f".format(riskPercent)))
            Slider(
                value = riskPercent.toFloat(),
                onValueChange = { riskPercent = (it * 10).toInt() / 10.0 },
                valueRange = 0.5f..10f,
                steps = 18,
                colors = SliderDefaults.colors(
                    thumbColor = Emerald,
                    activeTrackColor = Emerald,
                    inactiveTrackColor = Border
                )
            )

            // Max positions
            SectionLabel(stringResource(R.string.strategy_max_positions_label, maxPositions))
            Slider(
                value = maxPositions.toFloat(),
                onValueChange = { maxPositions = it.toInt() },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = Emerald,
                    activeTrackColor = Emerald,
                    inactiveTrackColor = Border
                )
            )

            Spacer(Modifier.height(8.dp))
        }

        // Create button
        Button(
            onClick = {
                val strategy = (existingStrategy ?: Strategy(name = "")).copy(
                    name = name.trim(),
                    description = description.trim(),
                    style = selectedStyle,
                    timeframe = selectedTimeframe,
                    direction = selectedDirection,
                    riskPerTradePercent = riskPercent,
                    maxOpenPositions = maxPositions,
                    isPreset = if (isEditing) existingStrategy!!.isPreset else false,
                    isActive = true,
                    isDefault = if (isEditing) existingStrategy!!.isDefault else false
                )
                onSave(strategy)
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isValid) Emerald else Border,
                contentColor = Color.White,
                disabledContainerColor = Border,
                disabledContentColor = TextMuted
            )
        ) {
            Text(
                if (isEditing) stringResource(R.string.strategy_save_button) else stringResource(R.string.strategy_create_button),
                fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextSecondary)
}

@Composable
private fun ChipGroup(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Use a flow layout approach — wrap chips
    }
    // Simple scrollable row of chips
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options.size) { index ->
            val option = options[index]
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Emerald else BgCard)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = option,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else TextSecondary
                )
            }
        }
    }
}
