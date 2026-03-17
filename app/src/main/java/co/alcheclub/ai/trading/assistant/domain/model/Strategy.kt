package co.alcheclub.ai.trading.assistant.domain.model

import java.util.Date
import java.util.UUID

/**
 * Strategy domain model fetched from Supabase `strategies` table.
 */
data class Strategy(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String = "",
    val style: TradingStyle = TradingStyle.SWING_TRADING,
    val timeframe: String = "4h",
    val direction: TradingDirection = TradingDirection.BOTH,
    val riskPerTradePercent: Double = 2.0,
    val maxOpenPositions: Int = 3,
    val isPreset: Boolean = false,
    val isActive: Boolean = true,
    val isDefault: Boolean = false,
    val enabledEntryRuleCount: Int = 0,
    val stopLossDescription: String = "",
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    val riskPerTradeFormatted: String
        get() {
            val intVal = riskPerTradePercent.toInt()
            return if (intVal.toDouble() == riskPerTradePercent) "$intVal%" else "%.1f%%".format(riskPerTradePercent)
        }
}

enum class TradingStyle(val value: String, val displayName: String) {
    SCALPING("scalping", "Scalping"),
    DAY_TRADING("day_trading", "Day Trading"),
    SWING_TRADING("swing_trading", "Swing Trading"),
    POSITION_TRADING("position_trading", "Position Trading"),
    INVESTING("investing", "Investing");

    companion object {
        fun from(value: String): TradingStyle = entries.find {
            it.value.equals(value, ignoreCase = true)
        } ?: SWING_TRADING
    }
}

enum class TradingDirection(val value: String, val displayName: String) {
    LONG_ONLY("long_only", "Long Only"),
    SHORT_ONLY("short_only", "Short Only"),
    BOTH("both", "Both");

    companion object {
        fun from(value: String): TradingDirection = entries.find {
            it.value.equals(value, ignoreCase = true)
        } ?: BOTH
    }
}
