package co.alcheclub.ai.trading.assistant.data.repository

import android.util.Log
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.model.TradingDirection
import co.alcheclub.ai.trading.assistant.domain.model.TradingStyle
import co.alcheclub.ai.trading.assistant.domain.repository.StrategyRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
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

    override suspend fun saveStrategy(strategy: Strategy, userId: UUID): Result<Strategy> {
        return try {
            // Ensure session token is fresh before insert (RLS requires valid auth.uid())
            try { supabaseClient.auth.refreshCurrentSession() } catch (_: Exception) {}

            val dto = buildJsonObject {
                put("id", JsonPrimitive(strategy.id.toString()))
                put("user_id", JsonPrimitive(userId.toString()))
                put("name", JsonPrimitive(strategy.name))
                put("description", JsonPrimitive(strategy.description))
                put("style", JsonPrimitive(strategy.style.value))
                put("timeframe", JsonPrimitive(strategy.timeframe))
                put("direction", JsonPrimitive(strategy.direction.value))
                put("risk_tolerance", JsonPrimitive("moderate"))
                put("risk_per_trade_percent", JsonPrimitive(strategy.riskPerTradePercent))
                put("config", buildJsonObject {
                    put("maxOpenPositions", JsonPrimitive(strategy.maxOpenPositions))
                    put("entry", buildJsonObject {
                        put("logic", JsonPrimitive("all"))
                        put("minConditions", JsonPrimitive(1))
                        put("rules", kotlinx.serialization.json.JsonArray(emptyList()))
                    })
                    put("targets", buildJsonObject {
                        put("takeProfit", buildJsonObject {
                            put("method", JsonPrimitive("risk_multiple"))
                            put("levels", kotlinx.serialization.json.JsonArray(listOf(
                                buildJsonObject {
                                    put("level", JsonPrimitive(1))
                                    put("riskMultiple", JsonPrimitive(2.0))
                                },
                                buildJsonObject {
                                    put("level", JsonPrimitive(2))
                                    put("riskMultiple", JsonPrimitive(3.0))
                                }
                            )))
                        })
                        put("stopLoss", buildJsonObject {
                            put("method", JsonPrimitive("atr_multiple"))
                            put("value", JsonPrimitive(1.5))
                        })
                    })
                    put("filters", buildJsonObject {})
                })
                put("is_preset", JsonPrimitive(strategy.isPreset))
                put("is_active", JsonPrimitive(strategy.isActive))
                put("is_default", JsonPrimitive(strategy.isDefault))
            }

            val response = supabaseClient.postgrest[TABLE]
                .insert(dto) { select() }

            val rows = response.decodeList<JsonObject>()
            val savedRow = rows.firstOrNull()
            val savedStrategy = savedRow?.let { mapRowToDomain(it) } ?: strategy

            Log.d(TAG, "Strategy saved: ${savedStrategy.name} (${savedStrategy.id})")
            Result.success(savedStrategy)
        } catch (e: Exception) {
            Log.e(TAG, "Save strategy failed", e)
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
