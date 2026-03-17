package co.alcheclub.ai.trading.assistant.data.remote.service

import android.util.Log
import co.alcheclub.ai.trading.assistant.core.util.JsonRepair
import co.alcheclub.ai.trading.assistant.data.remote.ApiKeyManager
import co.alcheclub.ai.trading.assistant.domain.model.AIAnalysisResult
import co.alcheclub.ai.trading.assistant.domain.model.ActionPlan
import co.alcheclub.ai.trading.assistant.domain.model.MarketData
import co.alcheclub.ai.trading.assistant.domain.model.RiskAssessment
import co.alcheclub.ai.trading.assistant.domain.model.RiskLevel
import co.alcheclub.ai.trading.assistant.domain.model.TakeProfit
import co.alcheclub.ai.trading.assistant.domain.model.TradingSignal
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal
import java.util.Locale

/**
 * Gemini text service for AI market analysis.
 * Builds structured prompts from market data → returns AIAnalysisResult.
 */
class GeminiTextService(
    private val client: HttpClient,
    private val keyManager: ApiKeyManager,
    private val json: Json,
    private val model: String = "gemini-2.5-flash",
    private val maxOutputTokens: Int = 16384,
    private val thinkingBudget: Int? = 1024
) {
    companion object {
        private const val TAG = "GeminiText"
        private const val BASE_URL = "https://generativelanguage.googleapis.com"
    }

    /**
     * Analyze market data with optional strategy context.
     * @param strategy Strategy context for personalized analysis (name, style, direction, rules etc.)
     */
    suspend fun analyzeMarketData(
        marketData: MarketData,
        assetName: String?,
        strategy: StrategyContext? = null
    ): Result<AIAnalysisResult> {
        if (marketData.klines.isEmpty()) {
            return Result.failure(Exception("No market data"))
        }

        if (keyManager.availableKeyCount <= 0) {
            return Result.failure(Exception("No API keys configured"))
        }

        val prompt = buildPrompt(marketData, assetName, strategy)
        keyManager.resetFailures()

        while (true) {
            val apiKey = keyManager.nextKey()
                ?: return Result.failure(Exception("All API keys exhausted"))

            try {
                val thinkingConfig = thinkingBudget?.let {
                    GeminiThinkingConfig(thinkingBudget = it)
                }

                val requestBody = GeminiTextRequest(
                    contents = listOf(
                        GeminiTextContent(parts = listOf(GeminiTextPart(text = prompt)))
                    ),
                    generationConfig = GeminiTextGenerationConfig(
                        responseMimeType = "application/json",
                        maxOutputTokens = maxOutputTokens,
                        thinkingConfig = thinkingConfig
                    )
                )

                val url = "$BASE_URL/v1beta/models/$model:generateContent?key=$apiKey"

                val response = client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

                val statusCode = response.status.value
                if (statusCode == 429 || statusCode == 401 || statusCode == 403) {
                    Log.w(TAG, "Key failed with HTTP $statusCode, rotating")
                    keyManager.markKeyFailed(apiKey)
                    continue
                }

                if (statusCode !in 200..299) {
                    return Result.failure(Exception("Server error: HTTP $statusCode"))
                }

                val geminiResponse = response.body<GeminiResponse>()
                logCost(geminiResponse)
                val text = extractText(geminiResponse)
                    ?: return Result.failure(Exception("Empty response from Gemini"))

                return parseAnalysisResponse(text)

            } catch (e: Exception) {
                if (e.message?.contains("429") == true) {
                    keyManager.markKeyFailed(apiKey)
                    continue
                }
                return Result.failure(e)
            }
        }
    }

    private fun extractText(response: GeminiResponse): String? {
        val candidate = response.candidates?.firstOrNull() ?: return null

        // Allow MAX_TOKENS — JSON repair can handle truncation
        if (candidate.finishReason != null &&
            candidate.finishReason != "STOP" &&
            candidate.finishReason != "MAX_TOKENS"
        ) {
            Log.e(TAG, "Response rejected: ${candidate.finishReason}")
            return null
        }

        if (candidate.finishReason == "MAX_TOKENS") {
            Log.w(TAG, "Response truncated (MAX_TOKENS) — will attempt JSON repair")
        }

        val parts = candidate.content?.parts ?: return null
        return parts.lastOrNull { it.thought != true && it.text != null }?.text
    }

    private fun logCost(response: GeminiResponse) {
        val usage = response.usageMetadata ?: return
        val inputTokens = usage.promptTokenCount ?: 0
        val outputTokens = usage.candidatesTokenCount ?: 0
        val thinkingTokens = usage.thoughtsTokenCount ?: 0
        val totalTokens = usage.totalTokenCount ?: 0

        val isProModel = model.contains("pro")
        val inputRate = if (isProModel) 1.25 else 0.30
        val outputRate = if (isProModel) 10.00 else 2.50

        val inputCost = inputTokens / 1_000_000.0 * inputRate
        val outputCost = (outputTokens + thinkingTokens) / 1_000_000.0 * outputRate
        val totalCost = inputCost + outputCost

        Log.d(TAG, "[$model] Cost: ${"%.6f".format(totalCost)} " +
            "(input: $inputTokens → ${"%.6f".format(inputCost)}, " +
            "output: $outputTokens → ${"%.6f".format(outputTokens / 1_000_000.0 * outputRate)}, " +
            "thinking: $thinkingTokens → ${"%.6f".format(thinkingTokens / 1_000_000.0 * outputRate)}, " +
            "total tokens: $totalTokens)")
    }

    private fun parseAnalysisResponse(rawText: String): Result<AIAnalysisResult> {
        // Step 1: Try raw decode
        tryParse(rawText)?.let { return Result.success(it) }

        // Step 2: Extract + sanitize
        var cleaned = JsonRepair.extractJsonObject(rawText)
        cleaned = JsonRepair.sanitizeJson(cleaned)
        tryParse(cleaned)?.let { return Result.success(it) }

        // Step 3: Truncation repair
        val repaired = JsonRepair.repairTruncatedJson(cleaned)
        tryParse(repaired)?.let {
            Log.w(TAG, "Recovered via JSON repair")
            return Result.success(it)
        }

        // Step 4: Strip text fields and retry
        val stripped = JsonRepair.stripTextFields(cleaned)
        tryParse(stripped)?.let {
            Log.w(TAG, "Recovered by stripping text fields")
            return Result.success(it)
        }

        // Step 5: Strip text fields from repaired version too
        val strippedRepaired = JsonRepair.stripTextFields(repaired)
        tryParse(strippedRepaired)?.let {
            Log.w(TAG, "Recovered by stripping text fields from repaired JSON")
            return Result.success(it)
        }

        val preview = if (rawText.length > 1000) {
            "${rawText.take(500)}...${rawText.takeLast(500)}"
        } else rawText
        Log.e(TAG, "Failed to parse response: $preview")
        return Result.failure(Exception("Failed to parse AI analysis response"))
    }

    private fun tryParse(text: String): AIAnalysisResult? {
        return try {
            val jsonElement = json.parseToJsonElement(text)
            val obj = jsonElement.jsonObject

            val signal = TradingSignal.from(obj["signal"]?.jsonPrimitive?.content ?: "NEUTRAL")
            val confidenceScore = obj["confidenceScore"]?.flexInt() ?: 50
            val entryMin = obj["entryMin"]?.flexBigDecimal()
            val entryMax = obj["entryMax"]?.flexBigDecimal()
            val stopLoss = obj["stopLoss"]?.flexBigDecimal() ?: BigDecimal.ZERO
            val riskLevel = RiskLevel.from(obj["riskLevel"]?.jsonPrimitive?.content ?: "MODERATE")

            val takeProfits = obj["takeProfits"]?.jsonArray?.map { tp ->
                val tpObj = tp.jsonObject
                TakeProfit(
                    level = tpObj["level"]?.flexInt() ?: 1,
                    price = tpObj["price"]?.flexBigDecimal() ?: BigDecimal.ZERO,
                    riskRewardRatio = tpObj["riskRewardRatio"]?.flexBigDecimal()
                )
            } ?: emptyList()

            val riskFactors = obj["riskFactors"]?.jsonArray?.mapNotNull {
                it.jsonPrimitive.contentOrNull
            } ?: emptyList()

            val riskWarnings = obj["riskWarnings"]?.jsonArray?.mapNotNull {
                it.jsonPrimitive.contentOrNull
            } ?: emptyList()

            AIAnalysisResult(
                signal = signal,
                confidenceScore = confidenceScore.coerceIn(0, 100),
                actionPlan = ActionPlan(
                    entryMin = entryMin,
                    entryMax = entryMax,
                    stopLoss = stopLoss,
                    takeProfits = takeProfits
                ),
                riskAssessment = RiskAssessment(
                    level = riskLevel,
                    factors = riskFactors,
                    warnings = riskWarnings
                ),
                aiExplanation = obj["aiExplanation"]?.jsonPrimitive?.contentOrNull ?: "",
                marketContext = obj["marketContext"]?.jsonPrimitive?.contentOrNull
            )
        } catch (e: Exception) {
            null
        }
    }

    // region Prompt Building

    /**
     * Returns the device language name in English (e.g. "Vietnamese" not "Tiếng Việt").
     * This ensures Gemini understands the target language reliably.
     */
    private val deviceLanguage: String
        get() {
            val langCode = Locale.getDefault().language
            // Use English locale to get a stable English name for the language
            return Locale.forLanguageTag(langCode).getDisplayLanguage(Locale.ENGLISH).ifEmpty { "English" }
        }

    private fun buildPrompt(
        marketData: MarketData,
        assetName: String?,
        strategy: StrategyContext?
    ): String {
        val sections = mutableListOf<String>()

        // System instruction
        sections.add(buildSystemInstruction(strategy != null))

        // Asset info
        val displayName = assetName ?: marketData.symbol
        sections.add(
            """
            ## Asset Information
            - Symbol: ${marketData.symbol}
            - Name: $displayName
            - Timeframe: ${marketData.interval}
            """.trimIndent()
        )

        // Current snapshot
        val ticker = marketData.ticker24h
        val book = marketData.bookTicker
        val pricePrecision = decimalPlaces(ticker.lastPrice)
        sections.add(
            """
            ## Current Snapshot
            - Last Price: ${ticker.lastPrice} (use $pricePrecision decimal places for ALL output prices)
            - 24h Change: ${ticker.priceChange} (${ticker.priceChangePercent}%)
            - 24h High: ${ticker.highPrice}
            - 24h Low: ${ticker.lowPrice}
            - 24h Volume: ${ticker.volume}
            - 24h Quote Volume: ${ticker.quoteVolume}
            - Trade Count: ${ticker.tradeCount}
            - Best Bid: ${book.bidPrice} (qty: ${book.bidQty})
            - Best Ask: ${book.askPrice} (qty: ${book.askQty})
            - Spread: ${book.askPrice.subtract(book.bidPrice)}
            """.trimIndent()
        )

        // Klines
        val recentKlines = marketData.klines.takeLast(50)
        val klinesSection = buildString {
            appendLine("## Recent OHLCV Data (last ${recentKlines.size} candles)")
            appendLine("Open | High | Low | Close | Volume")
            appendLine("--- | --- | --- | --- | ---")
            for (kline in recentKlines) {
                appendLine("${kline.open} | ${kline.high} | ${kline.low} | ${kline.close} | ${kline.volume}")
            }
        }
        sections.add(klinesSection)

        // Strategy context (if provided)
        if (strategy != null) {
            sections.add(buildStrategySection(strategy))
        }

        // Output schema
        sections.add(buildOutputSchema(strategy))

        return sections.joinToString("\n\n")
    }

    private fun buildSystemInstruction(hasStrategy: Boolean): String {
        var instruction = """
            You are an expert trading analyst AI. Analyze the following market data and provide a detailed, actionable trading analysis.
            You MUST respond ONLY with valid JSON matching the exact schema provided below.
            IMPORTANT: All human-readable text fields (aiExplanation, marketContext, riskFactors, riskWarnings) MUST be written in $deviceLanguage.
            CRITICAL: Keep your response concise. The entire JSON response must be under 2000 tokens. Be brief in text fields — prioritize actionable insight over length.
        """.trimIndent()

        if (hasStrategy) {
            instruction += "\n" + """
            IMPORTANT: The user has a personalized trading strategy. You MUST:
            1. Evaluate EACH entry rule against the current market data and state whether it is met or not.
            2. Respect the strategy's direction constraint — if "Long Only", never suggest a BEARISH signal; if "Short Only", never suggest BULLISH. Use NEUTRAL instead.
            3. Calculate stop loss using the strategy's stop loss method (e.g., ATR-based).
            4. Set take profit levels aligned with the strategy's target risk/reward ratios.
            5. Check all active filters — if a filter condition fails (e.g., volume too low), reduce confidence or signal NEUTRAL.
            6. Base confidence score on how many entry rules are currently met: all met = high confidence, some met = moderate, few met = low/neutral.
            7. In aiExplanation, explicitly describe which entry rules passed/failed and why, so the user understands the reasoning.
            """.trimIndent()
        }

        return instruction
    }

    private fun buildStrategySection(strategy: StrategyContext): String {
        return buildString {
            appendLine("## User's Trading Strategy (MUST follow)")
            appendLine("- Strategy: ${strategy.name}")
            appendLine("- Style: ${strategy.style}")
            appendLine("- Preferred Timeframe: ${strategy.timeframe}")
            appendLine("- Direction Constraint: ${strategy.direction}")
            appendLine("- Risk Per Trade: ${strategy.riskPerTrade}")
            if (strategy.stopLossMethod != null) {
                appendLine("- Stop Loss Method: ${strategy.stopLossMethod}")
            }
            if (strategy.entryRules.isNotEmpty()) {
                appendLine()
                appendLine("Entry Rules (evaluate EACH against current data):")
                strategy.entryRules.forEach { appendLine(it) }
            }
            if (strategy.takeProfitTargets.isNotEmpty()) {
                appendLine()
                appendLine("Take Profit Targets:")
                strategy.takeProfitTargets.forEach { appendLine(it) }
            }
            if (strategy.activeFilters.isNotEmpty()) {
                appendLine()
                appendLine("Active Filters (signal should be NEUTRAL if any filter fails):")
                strategy.activeFilters.forEach { appendLine(it) }
            }
        }
    }

    private fun buildOutputSchema(strategy: StrategyContext?): String {
        val strategyRules = if (strategy != null) {
            """
            Strategy-specific rules:
            - signal MUST respect direction constraint: ${strategy.direction}
            - stopLoss MUST use the strategy's method: ${strategy.stopLossMethod ?: "default"}
            - If active filters fail, set signal to NEUTRAL and explain which filter failed
            """.trimIndent()
        } else ""

        val strategyExplanation = if (strategy != null) {
            "\n- Add a fourth section: **Strategy Rules**\\n• Rule Name — ✅ Met / ❌ Not Met: brief evidence"
        } else ""

        return """
            ## Required JSON Output Schema
            Respond with ONLY this JSON structure, no markdown, no code fences:
            {
                "signal": "BULLISH" | "BEARISH" | "NEUTRAL",
                "confidenceScore": <integer 0-100>,
                "entryMin": <number or null if neutral>,
                "entryMax": <number or null if neutral>,
                "stopLoss": <number>,
                "takeProfits": [
                    {"level": 1, "price": <number>, "riskRewardRatio": <number>},
                    {"level": 2, "price": <number>, "riskRewardRatio": <number>}
                ],
                "riskLevel": "LOW" | "MODERATE" | "HIGH" | "VERY_HIGH",
                "riskFactors": ["<factor1>", "<factor2>"],
                "riskWarnings": ["<warning1>"],
                "aiExplanation": "<concise markdown analysis>",
                "marketContext": "<brief market context summary>"
            }

            Rules:
            - entryMin/entryMax: specific price levels for entry zone, null if signal is NEUTRAL
            - stopLoss: always provide a stop loss price
            - takeProfits: 1-3 levels with risk/reward ratios
            - marketContext: brief 1-2 sentence market context
            - CRITICAL PRICE RULES:
              1. All prices MUST use the SAME decimal precision as the Last Price in Current Snapshot
              2. Entry zone should be within 1-5% of the current price
              3. Stop loss and take profits MUST differ from entry — use actual support/resistance from OHLCV data
              4. For low-priced assets, use enough decimals to show meaningful differences
              5. Never return prices that are orders of magnitude away from the current price
            $strategyRules

            aiExplanation FORMAT (concise markdown, 150-250 words max):
            The value must be a single JSON string using inline markdown (**bold**, *italic*) and \n for line breaks.
            Structure: **Summary** (2-3 sentences), **Key Signals** (3-5 bullets with •), **Trade Setup** (2-3 bullets)
            Rules: Use **bold** for key prices/values. NO # headers, NO code fences. Keep each bullet to ONE sentence.$strategyExplanation
        """.trimIndent()
    }

    private fun decimalPlaces(value: BigDecimal): Int {
        val str = value.stripTrailingZeros().toPlainString()
        val dotIndex = str.indexOf('.')
        return if (dotIndex < 0) 0 else str.length - dotIndex - 1
    }

    // endregion
}

/**
 * Strategy context for personalized AI analysis.
 * Passed from the strategy builder to customize prompts.
 */
data class StrategyContext(
    val name: String,
    val style: String,
    val timeframe: String,
    val direction: String,
    val riskPerTrade: String,
    val stopLossMethod: String? = null,
    val entryRules: List<String> = emptyList(),
    val takeProfitTargets: List<String> = emptyList(),
    val activeFilters: List<String> = emptyList()
)

// region Request DTOs

@Serializable
internal data class GeminiTextRequest(
    val contents: List<GeminiTextContent>,
    val generationConfig: GeminiTextGenerationConfig
)

@Serializable
internal data class GeminiTextContent(
    val parts: List<GeminiTextPart>
)

@Serializable
internal data class GeminiTextPart(
    val text: String
)

@Serializable
internal data class GeminiTextGenerationConfig(
    val responseMimeType: String,
    val maxOutputTokens: Int,
    val thinkingConfig: GeminiThinkingConfig? = null
)

// endregion

// region Helpers

/** Decode Int from JSON number or string */
private fun JsonElement.flexInt(): Int? {
    val prim = jsonPrimitive
    return prim.intOrNull ?: prim.contentOrNull?.toIntOrNull()
}

/** Decode BigDecimal from JSON number or string */
private fun JsonElement.flexBigDecimal(): BigDecimal? {
    val prim = jsonPrimitive
    return prim.doubleOrNull?.let { BigDecimal.valueOf(it) }
        ?: prim.contentOrNull?.toBigDecimalOrNull()
}

// endregion
