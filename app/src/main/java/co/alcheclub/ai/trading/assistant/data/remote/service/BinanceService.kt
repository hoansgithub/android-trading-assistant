package co.alcheclub.ai.trading.assistant.data.remote.service

import android.util.Log
import co.alcheclub.ai.trading.assistant.data.remote.ApiKeyManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/**
 * Binance REST API client for klines, 24h ticker, and book ticker.
 * Supports both spot (/api/v3) and futures (/fapi/v1).
 */
class BinanceService(
    private val client: HttpClient,
    private val keyManager: ApiKeyManager,
    private val baseUrl: String = "https://api.binance.com",
    private val pathPrefix: String = "/api/v3"
) {
    companion object {
        private const val TAG = "BinanceService"
    }

    suspend fun fetchKlines(symbol: String, interval: String, limit: Int): Result<List<BinanceKlineDTO>> {
        return executeWithKeyRotation { apiKey ->
            val response = client.get("$baseUrl$pathPrefix/klines") {
                parameter("symbol", symbol)
                parameter("interval", interval)
                parameter("limit", limit)
                header("X-MBX-APIKEY", apiKey)
            }

            val statusCode = response.status.value
            if (statusCode == 400) {
                throw parseBinanceBadRequest(response.bodyAsText(), symbol)
            }
            if (statusCode !in 200..299) throw HttpStatusException(statusCode)

            val jsonArray = Json.parseToJsonElement(response.bodyAsText()).jsonArray
            jsonArray.map { element ->
                val arr = element.jsonArray
                BinanceKlineDTO(
                    openTime = arr[0].jsonPrimitive.long,
                    open = arr[1].jsonPrimitive.content,
                    high = arr[2].jsonPrimitive.content,
                    low = arr[3].jsonPrimitive.content,
                    close = arr[4].jsonPrimitive.content,
                    volume = arr[5].jsonPrimitive.content,
                    closeTime = arr[6].jsonPrimitive.long,
                    quoteVolume = arr[7].jsonPrimitive.content,
                    trades = arr[8].jsonPrimitive.int,
                    takerBuyBase = arr[9].jsonPrimitive.content,
                    takerBuyQuote = arr[10].jsonPrimitive.content
                )
            }
        }
    }

    suspend fun fetchTicker24h(symbol: String): Result<BinanceTicker24hDTO> {
        return executeWithKeyRotation { apiKey ->
            val response = client.get("$baseUrl$pathPrefix/ticker/24hr") {
                parameter("symbol", symbol)
                header("X-MBX-APIKEY", apiKey)
            }

            val statusCode = response.status.value
            if (statusCode == 400) throw parseBinanceBadRequest(response.bodyAsText(), symbol)
            if (statusCode !in 200..299) throw HttpStatusException(statusCode)

            response.body<BinanceTicker24hDTO>()
        }
    }

    suspend fun fetchBookTicker(symbol: String): Result<BinanceBookTickerDTO> {
        return executeWithKeyRotation { apiKey ->
            val response = client.get("$baseUrl$pathPrefix/ticker/bookTicker") {
                parameter("symbol", symbol)
                header("X-MBX-APIKEY", apiKey)
            }

            val statusCode = response.status.value
            if (statusCode == 400) throw parseBinanceBadRequest(response.bodyAsText(), symbol)
            if (statusCode !in 200..299) throw HttpStatusException(statusCode)

            response.body<BinanceBookTickerDTO>()
        }
    }

    /**
     * Parse Binance 400 error response with structured JSON parsing.
     * Binance error format: {"code": -1121, "msg": "Invalid symbol."}
     */
    private fun parseBinanceBadRequest(body: String, symbol: String): Exception {
        return try {
            val errorDto = Json.decodeFromString<BinanceErrorDTO>(body)
            if (errorDto.code == -1121) {
                SymbolNotFoundException(symbol)
            } else {
                Exception("Binance error ${errorDto.code}: ${errorDto.msg}")
            }
        } catch (e: Exception) {
            SymbolNotFoundException(symbol)
        }
    }

    private suspend fun <T> executeWithKeyRotation(operation: suspend (String) -> T): Result<T> {
        keyManager.resetFailures()

        while (true) {
            val apiKey = keyManager.nextKey()
                ?: return Result.failure(Exception("All Binance API keys exhausted"))

            try {
                return Result.success(operation(apiKey))
            } catch (e: HttpStatusException) {
                if (e.statusCode == 429) {
                    Log.w(TAG, "Rate limited, rotating key")
                    keyManager.markKeyFailed(apiKey)
                    continue
                }
                return Result.failure(e)
            } catch (e: SymbolNotFoundException) {
                return Result.failure(e)
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }
    }
}

// region DTOs

data class BinanceKlineDTO(
    val openTime: Long,
    val open: String,
    val high: String,
    val low: String,
    val close: String,
    val volume: String,
    val closeTime: Long,
    val quoteVolume: String,
    val trades: Int,
    val takerBuyBase: String,
    val takerBuyQuote: String
)

@Serializable
data class BinanceTicker24hDTO(
    val symbol: String,
    val lastPrice: String,
    val priceChange: String,
    val priceChangePercent: String,
    val highPrice: String,
    val lowPrice: String,
    val volume: String,
    val quoteVolume: String,
    val count: Int
)

@Serializable
data class BinanceBookTickerDTO(
    val symbol: String,
    val bidPrice: String,
    val bidQty: String,
    val askPrice: String,
    val askQty: String
)

@Serializable
private data class BinanceErrorDTO(
    val code: Int,
    val msg: String
)

class SymbolNotFoundException(val symbol: String) : Exception("Symbol not found: $symbol")
class HttpStatusException(val statusCode: Int) : Exception("HTTP $statusCode")

// endregion
