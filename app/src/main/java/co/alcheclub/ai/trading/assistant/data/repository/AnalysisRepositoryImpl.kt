package co.alcheclub.ai.trading.assistant.data.repository

import android.util.Log
import co.alcheclub.ai.trading.assistant.domain.model.ActionPlan
import co.alcheclub.ai.trading.assistant.domain.model.Analysis
import co.alcheclub.ai.trading.assistant.domain.model.AnalysisType
import co.alcheclub.ai.trading.assistant.domain.model.MarketData
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.model.RiskAssessment
import co.alcheclub.ai.trading.assistant.domain.model.RiskLevel
import co.alcheclub.ai.trading.assistant.domain.model.TakeProfit
import co.alcheclub.ai.trading.assistant.domain.model.TradingSignal
import co.alcheclub.ai.trading.assistant.domain.repository.AnalysisRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class AnalysisRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : AnalysisRepository {

    companion object {
        private const val TAG = "AnalysisRepo"
        private const val TABLE = "analyses"
    }

    override suspend fun fetchAnalyses(userId: UUID): Result<List<Analysis>> {
        return try {
            val response = supabaseClient.postgrest[TABLE]
                .select {
                    filter { eq("user_id", userId.toString()) }
                    order("analyzed_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(50)
                }

            val rows = response.decodeList<JsonObject>()
            val analyses = rows.mapNotNull { mapRowToDomain(it) }
            Result.success(analyses)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch failed", e)
            Result.failure(e)
        }
    }

    override suspend fun saveAnalysis(analysis: Analysis, marketData: MarketData, strategy: Strategy?): Result<Analysis> {
        return try {
            // Refresh session and use auth.uid() directly for RLS compliance
            try { supabaseClient.auth.refreshCurrentSession() } catch (_: Exception) {}
            val session = supabaseClient.auth.currentSessionOrNull()
                ?: return Result.failure(Exception("No active session — cannot save analysis"))
            val authUid = UUID.fromString(session.user?.id ?: return Result.failure(Exception("No user in session")))
            Log.d(TAG, "Saving analysis: auth.uid=$authUid, passed userId=${analysis.userId}")

            val dto = buildInsertDto(analysis.copy(userId = authUid), marketData, strategy)

            val response = supabaseClient.postgrest[TABLE]
                .insert(dto) { select() }

            val rows = response.decodeList<JsonObject>()
            val row = rows.firstOrNull()
                ?: return Result.failure(Exception("No row returned after insert"))

            val savedId = row["id"]?.jsonPrimitive?.content?.let {
                try { UUID.fromString(it) } catch (_: Exception) { null }
            } ?: analysis.id

            Result.success(analysis.copy(id = savedId, userId = authUid))
        } catch (e: Exception) {
            Log.e(TAG, "Save failed", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteAnalysis(analysisId: UUID): Result<Unit> {
        return try {
            supabaseClient.postgrest[TABLE]
                .delete { filter { eq("id", analysisId.toString()) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Delete failed", e)
            Result.failure(e)
        }
    }

    override suspend fun updateImageUrl(analysisId: UUID, imageUrl: String): Result<Unit> {
        return try {
            supabaseClient.postgrest[TABLE]
                .update(buildJsonObject {
                    put("image_url", JsonPrimitive(imageUrl))
                }) {
                    filter { eq("id", analysisId.toString()) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Update image URL failed", e)
            Result.failure(e)
        }
    }

    // region DTO Building

    private fun buildInsertDto(analysis: Analysis, marketData: MarketData, strategy: Strategy?): JsonObject {
        val currentPrice = analysis.currentPrice
        val stopLoss = analysis.actionPlan.stopLoss
        val takeProfits = analysis.actionPlan.takeProfits

        val tp1 = takeProfits.firstOrNull { it.level == 1 }
        val tp2 = takeProfits.firstOrNull { it.level == 2 }
        val tp3 = takeProfits.firstOrNull { it.level == 3 }

        val slPercent = calculatePercent(currentPrice, stopLoss)
        val rrRatio = calculateRiskRewardRatio(currentPrice, stopLoss, tp1?.price)

        return buildJsonObject {
            put("user_id", JsonPrimitive(analysis.userId.toString()))
            put("asset", JsonPrimitive(analysis.assetSymbol))
            put("asset_name", JsonPrimitive(analysis.assetName))
            put("strategy_id", analysis.strategyId?.let { JsonPrimitive(it.toString()) } ?: JsonPrimitive(null as String?))
            put("strategy_snapshot", if (strategy != null) {
                buildJsonObject {
                    put("name", JsonPrimitive(strategy.name))
                    put("style", JsonPrimitive(strategy.style.value))
                    put("timeframe", JsonPrimitive(strategy.timeframe))
                    put("direction", JsonPrimitive(strategy.direction.value))
                    put("riskPerTrade", JsonPrimitive(strategy.riskPerTradeFormatted))
                }
            } else {
                JsonPrimitive(null as String?)
            })
            put("input_source", JsonPrimitive(analysis.analysisType.value))
            put("image_url", analysis.imageUrl?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
            put("timeframe", JsonPrimitive(analysis.timeframe))

            // Signal
            put("signal", JsonPrimitive(analysis.signal.value))
            put("confidence", JsonPrimitive(analysis.confidenceScore))
            put("confidence_level", JsonPrimitive(deriveConfidenceLevel(analysis.confidenceScore)))
            put("signal_strength", JsonPrimitive(deriveSignalStrength(analysis.confidenceScore)))

            // Action plan
            put("entry_type", if (analysis.actionPlan.entryMin != null) JsonPrimitive("zone") else JsonPrimitive(null as String?))
            put("entry_zone_low", analysis.actionPlan.entryMin?.let { JsonPrimitive(it.toDouble()) } ?: JsonPrimitive(null as String?))
            put("entry_zone_high", analysis.actionPlan.entryMax?.let { JsonPrimitive(it.toDouble()) } ?: JsonPrimitive(null as String?))
            put("stop_loss_price", JsonPrimitive(stopLoss.toDouble()))
            put("stop_loss_percent", slPercent?.let { JsonPrimitive(it.toDouble()) } ?: JsonPrimitive(null as String?))
            put("take_profit_1_price", tp1?.price?.let { JsonPrimitive(it.toDouble()) } ?: JsonPrimitive(null as String?))
            put("take_profit_1_percent", tp1?.price?.let { calculatePercent(currentPrice, it) }?.let { JsonPrimitive(it.toDouble()) } ?: JsonPrimitive(null as String?))
            put("take_profit_2_price", tp2?.price?.let { JsonPrimitive(it.toDouble()) } ?: JsonPrimitive(null as String?))
            put("take_profit_2_percent", tp2?.price?.let { calculatePercent(currentPrice, it) }?.let { JsonPrimitive(it.toDouble()) } ?: JsonPrimitive(null as String?))
            put("take_profit_3_price", tp3?.price?.let { JsonPrimitive(it.toDouble()) } ?: JsonPrimitive(null as String?))
            put("take_profit_3_percent", tp3?.price?.let { calculatePercent(currentPrice, it) }?.let { JsonPrimitive(it.toDouble()) } ?: JsonPrimitive(null as String?))
            put("risk_reward_ratio", rrRatio?.let { JsonPrimitive(it.toDouble()) } ?: JsonPrimitive(null as String?))
            put("risk_reward_quality", rrRatio?.let { JsonPrimitive(deriveRiskRewardQuality(it)) } ?: JsonPrimitive(null as String?))

            // Risk
            put("risk_level", JsonPrimitive(analysis.riskAssessment.level.value))
            put("risk_score", JsonPrimitive(deriveRiskScore(analysis.riskAssessment.level)))

            // Market snapshot
            put("current_price", JsonPrimitive(currentPrice.toDouble()))
            put("price_change_24h_percent", JsonPrimitive(marketData.ticker24h.priceChangePercent.toDouble()))
            put("volume_24h", JsonPrimitive(marketData.ticker24h.volume.toDouble()))

            // JSONB
            put("result_snapshot", buildJsonObject {
                put("aiExplanation", JsonPrimitive(analysis.aiExplanation))
                put("marketContext", JsonPrimitive(analysis.marketContext))
                put("riskFactors", buildJsonArray {
                    analysis.riskAssessment.factors.forEach { add(JsonPrimitive(it)) }
                })
                put("riskWarnings", buildJsonArray {
                    analysis.riskAssessment.warnings.forEach { add(JsonPrimitive(it)) }
                })
            })

            put("is_saved", JsonPrimitive(analysis.isSaved))
            put("notify_enabled", JsonPrimitive(analysis.notifyEnabled))
        }
    }

    // endregion

    // region Row Mapping

    private fun mapRowToDomain(row: JsonObject): Analysis? {
        return try {
            val signal = TradingSignal.from(row["signal"]?.jsonPrimitive?.content ?: return null)
            val riskLevel = RiskLevel.from(row["risk_level"]?.jsonPrimitive?.content ?: "medium")

            val resultSnapshot = row["result_snapshot"]?.takeIf { it !is kotlinx.serialization.json.JsonNull }?.jsonObject
            val strategySnapshot = row["strategy_snapshot"]?.takeIf { it !is kotlinx.serialization.json.JsonNull }?.jsonObject

            val takeProfits = mutableListOf<TakeProfit>()
            row["take_profit_1_price"]?.jsonPrimitive?.contentOrNull?.toBigDecimalOrNull()?.let {
                takeProfits.add(TakeProfit(level = 1, price = it,
                    riskRewardRatio = row["risk_reward_ratio"]?.jsonPrimitive?.contentOrNull?.toBigDecimalOrNull()))
            }
            row["take_profit_2_price"]?.jsonPrimitive?.contentOrNull?.toBigDecimalOrNull()?.let {
                takeProfits.add(TakeProfit(level = 2, price = it))
            }
            row["take_profit_3_price"]?.jsonPrimitive?.contentOrNull?.toBigDecimalOrNull()?.let {
                takeProfits.add(TakeProfit(level = 3, price = it))
            }

            val riskFactors = resultSnapshot?.get("riskFactors")?.jsonArray?.map {
                it.jsonPrimitive.content
            } ?: emptyList()
            val riskWarnings = resultSnapshot?.get("riskWarnings")?.jsonArray?.map {
                it.jsonPrimitive.content
            } ?: emptyList()

            Analysis(
                id = UUID.fromString(row["id"]?.jsonPrimitive?.content ?: return null),
                userId = UUID.fromString(row["user_id"]?.jsonPrimitive?.content ?: return null),
                strategyId = row["strategy_id"]?.jsonPrimitive?.contentOrNull?.let {
                    try { UUID.fromString(it) } catch (_: Exception) { null }
                },
                assetSymbol = row["asset"]?.jsonPrimitive?.content ?: "",
                assetName = row["asset_name"]?.jsonPrimitive?.contentOrNull,
                signal = signal,
                confidenceScore = row["confidence"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                actionPlan = ActionPlan(
                    entryMin = row["entry_zone_low"]?.jsonPrimitive?.contentOrNull?.toBigDecimalOrNull(),
                    entryMax = row["entry_zone_high"]?.jsonPrimitive?.contentOrNull?.toBigDecimalOrNull(),
                    stopLoss = row["stop_loss_price"]?.jsonPrimitive?.content?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                    takeProfits = takeProfits
                ),
                riskAssessment = RiskAssessment(
                    level = riskLevel,
                    factors = riskFactors,
                    warnings = riskWarnings
                ),
                aiExplanation = resultSnapshot?.get("aiExplanation")?.jsonPrimitive?.contentOrNull ?: "",
                marketContext = resultSnapshot?.get("marketContext")?.jsonPrimitive?.contentOrNull,
                strategyName = strategySnapshot?.get("name")?.jsonPrimitive?.contentOrNull,
                currentPrice = row["current_price"]?.jsonPrimitive?.content?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                analysisType = AnalysisType.from(row["input_source"]?.jsonPrimitive?.content ?: "api"),
                imageUrl = row["image_url"]?.jsonPrimitive?.contentOrNull,
                timeframe = row["timeframe"]?.jsonPrimitive?.content ?: "4h",
                isSaved = row["is_saved"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false,
                notifyEnabled = row["notify_enabled"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false,
                analyzedAt = row["analyzed_at"]?.jsonPrimitive?.contentOrNull?.let { parseISO8601(it) } ?: Date(),
                createdAt = row["created_at"]?.jsonPrimitive?.contentOrNull?.let { parseISO8601(it) } ?: Date()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to map row", e)
            null
        }
    }

    // endregion

    // region Helpers

    private fun deriveConfidenceLevel(score: Int): String = when (score) {
        in 0..40 -> "low"
        in 41..60 -> "medium"
        in 61..80 -> "high"
        else -> "very_high"
    }

    private fun deriveSignalStrength(score: Int): String = when (score) {
        in 0..40 -> "weak"
        in 41..70 -> "moderate"
        else -> "strong"
    }

    private fun deriveRiskScore(level: RiskLevel): Int = when (level) {
        RiskLevel.LOW -> 25
        RiskLevel.MODERATE -> 50
        RiskLevel.HIGH -> 75
        RiskLevel.VERY_HIGH -> 90
    }

    private fun calculatePercent(from: BigDecimal, to: BigDecimal): BigDecimal? {
        if (from.compareTo(BigDecimal.ZERO) == 0) return null
        return to.subtract(from).abs().multiply(BigDecimal(100)).divide(from, 2, java.math.RoundingMode.HALF_UP)
    }

    private fun calculateRiskRewardRatio(
        currentPrice: BigDecimal,
        stopLoss: BigDecimal,
        takeProfit: BigDecimal?
    ): BigDecimal? {
        val tp = takeProfit ?: return null
        val slDistance = currentPrice.subtract(stopLoss).abs()
        if (slDistance.compareTo(BigDecimal.ZERO) == 0) return null
        val tpDistance = tp.subtract(currentPrice).abs()
        return tpDistance.divide(slDistance, 2, java.math.RoundingMode.HALF_UP)
    }

    private fun deriveRiskRewardQuality(ratio: BigDecimal): String = when {
        ratio < BigDecimal.ONE -> "poor"
        ratio < BigDecimal(2) -> "acceptable"
        ratio < BigDecimal(3) -> "good"
        else -> "excellent"
    }

    private fun parseISO8601(dateString: String): Date {
        // Trim microseconds to milliseconds for compatibility
        val trimmed = dateString.replace(Regex("""(\.\d{3})\d+"""), "$1")
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(trimmed) ?: continue
            } catch (_: Exception) {
                continue
            }
        }
        return Date()
    }

    // endregion
}
