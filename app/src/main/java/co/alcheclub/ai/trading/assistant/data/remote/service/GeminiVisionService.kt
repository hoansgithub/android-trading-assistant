package co.alcheclub.ai.trading.assistant.data.remote.service

import android.util.Base64
import android.util.Log
import co.alcheclub.ai.trading.assistant.core.util.ImageOptimizer
import co.alcheclub.ai.trading.assistant.data.remote.ApiKeyManager
import co.alcheclub.ai.trading.assistant.domain.model.ChartRecognitionResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Gemini 2.5 Flash vision service for chart recognition.
 * Sends base64 chart images → returns ChartRecognitionResult.
 */
class GeminiVisionService(
    private val client: HttpClient,
    private val keyManager: ApiKeyManager,
    private val json: Json
) {
    companion object {
        private const val TAG = "GeminiVision"
        private const val MODEL = "gemini-2.5-flash"
        private const val MAX_OUTPUT_TOKENS = 500
        private const val THINKING_BUDGET = 0
        private const val BASE_URL = "https://generativelanguage.googleapis.com"

        private val PROMPT = """
            You are a trading chart analyzer. Look at this chart image and extract:
            1. The asset/ticker symbol
            2. The chart timeframe/interval
            3. The asset type: "crypto", "stock", or "unknown"

            Respond ONLY with valid JSON: {"asset": "<TICKER>", "interval": "<TIMEFRAME>", "assetType": "<TYPE>"}

            Symbol rules:
            - Return the exchange ticker WITHOUT separators (BTCUSDT not BTC/USDT)
            - Strip exchange prefixes (BINANCE:BTCUSDT → BTCUSDT, NASDAQ:AAPL → AAPL)
            - Strip perpetual suffixes (BTCUSDT.P → BTCUSDT, BTC-PERP → BTC)
            - Crypto exchanges (Binance/Bybit/OKX): use exact trading pair (XAUUSDT, BTCUSDT)
            - Stock/broker charts: ticker only (AAPL, TSLA, GLD)
            - Market indices: return index ticker WITHOUT ^ prefix (SPX not ^GSPC, DJI not ^DJI, NDX not ^IXIC)
            - Forex: return pair without separator (EURUSD, GBPJPY, USDJPY)
            - International stocks: return the local ticker or company name (SAMSUNG, TOYOTA, ALIBABA, TSMC)

            Interval rules:
            - Standard format: 1m, 5m, 15m, 30m, 1h, 2h, 4h, 6h, 8h, 12h, 1d, 3d, 1w, 1M
            - Convert localized labels (1 ngày/1日=1d, 1 giờ/1时=1h, 1 jour=1d, 1 tuần/1周=1w)
            - All lowercase except 1M (monthly)

            Asset type rules:
            - "crypto": Cryptocurrencies AND commodities on crypto exchanges. Examples: BTC, ETH, SOL, XAUUSDT (gold on Binance)
            - "stock": Stocks, ETFs, indices, forex, international stocks, commodities on traditional platforms. Examples: AAPL, SPX, EURUSD, SAMSUNG, GLD
            - "unknown": When platform cannot be identified

            Important:
            - Image may be a phone photo of a screen — look carefully for ticker text
            - Use "UNKNOWN" for asset/interval if cannot determine
        """.trimIndent()
    }

    suspend fun recognizeChart(imageData: ByteArray): Result<ChartRecognitionResult> {
        if (imageData.isEmpty()) return Result.failure(Exception("Empty image data"))

        // Optimize image before sending (resize + JPEG compress for bandwidth)
        val optimizedData = ImageOptimizer.optimize(imageData, ImageOptimizer.Config.CHART_RECOGNITION)
        if (optimizedData == null) {
            Log.e(TAG, "Image optimization failed (${imageData.size} bytes)")
            return Result.failure(Exception("Image optimization failed"))
        }

        if (keyManager.availableKeyCount <= 0) {
            return Result.failure(Exception("No API keys configured"))
        }

        keyManager.resetFailures()

        while (true) {
            val apiKey = keyManager.nextKey()
                ?: return Result.failure(Exception("All API keys exhausted"))

            try {
                val base64Image = Base64.encodeToString(optimizedData, Base64.NO_WRAP)
                val mimeType = detectMimeType(optimizedData)

                val requestBody = GeminiVisionRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(text = PROMPT),
                                GeminiPart(
                                    inlineData = GeminiInlineData(
                                        mimeType = mimeType,
                                        data = base64Image
                                    )
                                )
                            )
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json",
                        maxOutputTokens = MAX_OUTPUT_TOKENS,
                        thinkingConfig = GeminiThinkingConfig(thinkingBudget = THINKING_BUDGET)
                    )
                )

                val url = "$BASE_URL/v1beta/models/$MODEL:generateContent?key=$apiKey"

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

                return parseChartRecognition(text)

            } catch (e: Exception) {
                if (e.message?.contains("429") == true || e.message?.contains("rate") == true) {
                    keyManager.markKeyFailed(apiKey)
                    continue
                }
                return Result.failure(e)
            }
        }
    }

    private fun extractText(response: GeminiResponse): String? {
        val candidate = response.candidates?.firstOrNull() ?: return null

        if (candidate.finishReason != null && candidate.finishReason != "STOP") {
            Log.w(TAG, "Response truncated: ${candidate.finishReason}")
            return null
        }

        val parts = candidate.content?.parts ?: return null
        return parts.lastOrNull { it.thought != true && it.text != null }?.text
    }

    private fun parseChartRecognition(text: String): Result<ChartRecognitionResult> {
        return try {
            val result = json.decodeFromString<ChartRecognitionResult>(text)
            if (result.asset.isEmpty() || result.asset.uppercase() == "UNKNOWN") {
                Result.failure(Exception("Could not determine asset from chart"))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse: $text", e)
            Result.failure(Exception("Failed to parse chart recognition response"))
        }
    }

    private fun logCost(response: GeminiResponse) {
        val usage = response.usageMetadata ?: return
        val inputTokens = usage.promptTokenCount ?: 0
        val outputTokens = usage.candidatesTokenCount ?: 0
        val totalTokens = usage.totalTokenCount ?: 0
        // Flash pricing: $0.30/1M input, $2.50/1M output
        val inputCost = inputTokens / 1_000_000.0 * 0.30
        val outputCost = outputTokens / 1_000_000.0 * 2.50
        val totalCost = inputCost + outputCost
        Log.d(TAG, "[$MODEL] Vision Cost: ${"%.6f".format(totalCost)} " +
            "(input: $inputTokens → ${"%.6f".format(inputCost)}, " +
            "output: $outputTokens → ${"%.6f".format(outputCost)}, " +
            "total tokens: $totalTokens)")
    }

    private fun detectMimeType(data: ByteArray): String {
        if (data.size < 12) return "image/jpeg"
        // PNG: 89 50 4E 47
        if (data[0] == 0x89.toByte() && data[1] == 0x50.toByte() &&
            data[2] == 0x4E.toByte() && data[3] == 0x47.toByte()
        ) return "image/png"
        // WebP: RIFF....WEBP
        if (data[0] == 0x52.toByte() && data[1] == 0x49.toByte() &&
            data[8] == 0x57.toByte() && data[9] == 0x45.toByte()
        ) return "image/webp"
        return "image/jpeg"
    }
}

// region Request/Response DTOs

@Serializable
internal data class GeminiVisionRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

@Serializable
internal data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
internal data class GeminiPart(
    val text: String? = null,
    @kotlinx.serialization.SerialName("inline_data")
    val inlineData: GeminiInlineData? = null
)

@Serializable
internal data class GeminiInlineData(
    @kotlinx.serialization.SerialName("mime_type")
    val mimeType: String,
    val data: String
)

@Serializable
internal data class GeminiGenerationConfig(
    val responseMimeType: String,
    val maxOutputTokens: Int,
    val thinkingConfig: GeminiThinkingConfig? = null
)

@Serializable
internal data class GeminiThinkingConfig(
    val thinkingBudget: Int
)

@Serializable
internal data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val usageMetadata: GeminiUsageMetadata? = null
)

@Serializable
internal data class GeminiCandidate(
    val content: GeminiResponseContent? = null,
    val finishReason: String? = null
)

@Serializable
internal data class GeminiResponseContent(
    val parts: List<GeminiResponsePart>? = null
)

@Serializable
internal data class GeminiResponsePart(
    val text: String? = null,
    val thought: Boolean? = null
)

@Serializable
internal data class GeminiUsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int? = null,
    val thoughtsTokenCount: Int? = null
)

// endregion
