package co.alcheclub.ai.trading.assistant.domain.model

import java.math.BigDecimal
import java.util.Date
import java.util.UUID

data class Analysis(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val strategyId: UUID? = null,
    val assetSymbol: String,
    val assetName: String? = null,
    val signal: TradingSignal,
    val confidenceScore: Int,
    val actionPlan: ActionPlan,
    val riskAssessment: RiskAssessment,
    val aiExplanation: String,
    val marketContext: String? = null,
    val strategyName: String? = null,
    val currentPrice: BigDecimal,
    val analysisType: AnalysisType = AnalysisType.IMAGE,
    val imageUrl: String? = null,
    val timeframe: String = "4h",
    val isSaved: Boolean = false,
    val notifyEnabled: Boolean = false,
    val analyzedAt: Date = Date(),
    val createdAt: Date = Date()
)

enum class TradingSignal(val value: String) {
    BULLISH("BULLISH"),
    BEARISH("BEARISH"),
    NEUTRAL("NEUTRAL"),
    NO_SIGNAL("NO_SIGNAL");

    companion object {
        fun from(value: String): TradingSignal = entries.find {
            it.value.equals(value, ignoreCase = true)
        } ?: NEUTRAL
    }
}

data class ActionPlan(
    val entryMin: BigDecimal? = null,
    val entryMax: BigDecimal? = null,
    val stopLoss: BigDecimal,
    val takeProfits: List<TakeProfit>
)

data class TakeProfit(
    val id: UUID = UUID.randomUUID(),
    val level: Int,
    val price: BigDecimal,
    val riskRewardRatio: BigDecimal? = null
)

data class RiskAssessment(
    val level: RiskLevel,
    val factors: List<String>,
    val warnings: List<String> = emptyList()
)

enum class RiskLevel(val value: String) {
    LOW("low"),
    MODERATE("medium"),
    HIGH("high"),
    VERY_HIGH("extreme");

    companion object {
        fun from(value: String): RiskLevel = when (value.uppercase()) {
            "LOW" -> LOW
            "MODERATE", "MEDIUM" -> MODERATE
            "HIGH" -> HIGH
            "VERY_HIGH", "EXTREME" -> VERY_HIGH
            else -> MODERATE
        }
    }
}

enum class AnalysisType(val value: String) {
    API("api"),
    IMAGE("image");

    companion object {
        fun from(value: String): AnalysisType = entries.find {
            it.value.equals(value, ignoreCase = true)
        } ?: API
    }
}

/**
 * Pure AI output — excludes user-specific fields.
 * The use case maps this to a full Analysis by adding user context.
 */
data class AIAnalysisResult(
    val signal: TradingSignal,
    val confidenceScore: Int,
    val actionPlan: ActionPlan,
    val riskAssessment: RiskAssessment,
    val aiExplanation: String,
    val marketContext: String?
)
