package co.alcheclub.ai.trading.assistant.data.repository

import android.util.Log
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.model.TradingDirection
import co.alcheclub.ai.trading.assistant.domain.model.TradingStyle
import co.alcheclub.ai.trading.assistant.domain.repository.StrategyRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class StrategyRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : StrategyRepository {

    companion object {
        private const val TAG = "StrategyRepo"
        private const val TABLE = "strategies"
    }

    override suspend fun fetchStrategies(userId: UUID): Result<List<Strategy>> {
        return try {
            val response = supabaseClient.postgrest[TABLE]
                .select {
                    filter { eq("user_id", userId.toString()) }
                    order("created_at", Order.DESCENDING)
                }

            val rows = response.decodeList<JsonObject>()
            val strategies = rows.mapNotNull { mapRowToDomain(it) }
            Result.success(strategies)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch strategies failed", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteStrategy(strategyId: UUID): Result<Unit> {
        return try {
            supabaseClient.postgrest[TABLE]
                .delete { filter { eq("id", strategyId.toString()) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Delete strategy failed", e)
            Result.failure(e)
        }
    }

    private fun mapRowToDomain(row: JsonObject): Strategy? {
        return try {
            val config = row["config"]?.jsonObject
            val entryRules = config?.get("entry")?.jsonObject?.get("rules")
            val enabledCount = try {
                entryRules?.let { rules ->
                    val arr = kotlinx.serialization.json.Json.parseToJsonElement(rules.toString())
                    if (arr is kotlinx.serialization.json.JsonArray) {
                        arr.count { elem ->
                            elem.jsonObject["enabled"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: true
                        }
                    } else 0
                } ?: 0
            } catch (_: Exception) { 0 }

            val stopLoss = config?.get("targets")?.jsonObject?.get("stopLoss")?.jsonObject
            val slMethod = stopLoss?.get("method")?.jsonPrimitive?.contentOrNull ?: ""
            val slValue = stopLoss?.get("value")?.jsonPrimitive?.doubleOrNull ?: 0.0
            val slDesc = when (slMethod) {
                "atr_multiple" -> "ATR Multiple (${slValue}x)"
                "fixed_percent" -> "Fixed (${slValue}%)"
                "swing_low_high" -> "Swing Low/High"
                "support_resistance" -> "Support/Resistance"
                else -> slMethod
            }

            Strategy(
                id = UUID.fromString(row["id"]?.jsonPrimitive?.content ?: return null),
                name = row["name"]?.jsonPrimitive?.content ?: "",
                description = row["description"]?.jsonPrimitive?.contentOrNull ?: "",
                style = TradingStyle.from(row["style"]?.jsonPrimitive?.content ?: "swing_trading"),
                timeframe = row["timeframe"]?.jsonPrimitive?.content ?: "4h",
                direction = TradingDirection.from(row["direction"]?.jsonPrimitive?.content ?: "both"),
                riskPerTradePercent = row["risk_per_trade_percent"]?.jsonPrimitive?.doubleOrNull ?: 2.0,
                maxOpenPositions = config?.get("maxOpenPositions")?.jsonPrimitive?.intOrNull ?: 3,
                isPreset = row["is_preset"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false,
                isActive = row["is_active"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true,
                isDefault = row["is_default"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: false,
                enabledEntryRuleCount = enabledCount,
                stopLossDescription = slDesc,
                createdAt = row["created_at"]?.jsonPrimitive?.contentOrNull?.let { parseISO8601(it) },
                updatedAt = row["updated_at"]?.jsonPrimitive?.contentOrNull?.let { parseISO8601(it) }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to map strategy row", e)
            null
        }
    }

    private fun parseISO8601(dateString: String): Date {
        val trimmed = dateString.replace(Regex("""(\.\d{3})\d+"""), "$1")
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(trimmed) ?: continue
            } catch (_: Exception) { continue }
        }
        return Date()
    }
}
