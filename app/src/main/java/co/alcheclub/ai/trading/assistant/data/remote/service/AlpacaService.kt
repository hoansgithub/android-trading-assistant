package co.alcheclub.ai.trading.assistant.data.remote.service

import android.util.Log
import co.alcheclub.ai.trading.assistant.data.remote.KeyPairManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Alpaca Markets REST API client for stock bars and snapshots.
 */
class AlpacaService(
    private val client: HttpClient,
    private val keyPairManager: KeyPairManager
) {
    companion object {
        private const val TAG = "AlpacaService"
        private const val BASE_URL = "https://data.alpacamarkets.com"
    }

    suspend fun fetchBars(symbol: String, timeframe: String, limit: Int): Result<List<AlpacaBarDTO>> {
        val startDate = calculateStartDate(timeframe, limit)

        return executeWithKeyRotation { keyPair ->
            val response = client.get("$BASE_URL/v2/stocks/bars") {
                parameter("symbols", symbol)
                parameter("timeframe", timeframe)
                parameter("limit", limit)
                parameter("start", startDate)
                header("APCA-API-KEY-ID", keyPair.keyId)
                header("APCA-API-SECRET-KEY", keyPair.secretKey)
            }

            val statusCode = response.status.value
            if (statusCode == 404 || statusCode == 422) throw SymbolNotFoundException(symbol)
            if (statusCode == 401 || statusCode == 403) throw HttpStatusException(statusCode)
            if (statusCode !in 200..299) throw HttpStatusException(statusCode)

            val barsResponse = response.body<AlpacaBarsResponseDTO>()
            barsResponse.bars[symbol] ?: emptyList()
        }
    }

    suspend fun fetchSnapshot(symbol: String): Result<AlpacaSnapshotDTO> {
        return executeWithKeyRotation { keyPair ->
            val response = client.get("$BASE_URL/v2/stocks/snapshots") {
                parameter("symbols", symbol)
                header("APCA-API-KEY-ID", keyPair.keyId)
                header("APCA-API-SECRET-KEY", keyPair.secretKey)
            }

            val statusCode = response.status.value
            if (statusCode == 404 || statusCode == 422) throw SymbolNotFoundException(symbol)
            if (statusCode !in 200..299) throw HttpStatusException(statusCode)

            val snapshots = response.body<Map<String, AlpacaSnapshotDTO>>()
            snapshots[symbol] ?: throw SymbolNotFoundException(symbol)
        }
    }

    private suspend fun <T> executeWithKeyRotation(
        operation: suspend (KeyPairManager.KeyPair) -> T
    ): Result<T> {
        keyPairManager.resetFailures()

        while (true) {
            val keyPair = keyPairManager.nextKeyPair()
                ?: return Result.failure(Exception("All Alpaca API keys exhausted"))

            try {
                return Result.success(operation(keyPair))
            } catch (e: HttpStatusException) {
                if (e.statusCode == 429 || e.statusCode == 401 || e.statusCode == 403) {
                    Log.w(TAG, "Key failed with HTTP ${e.statusCode}, rotating")
                    keyPairManager.markKeyPairFailed(keyPair.keyId)
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

    private fun calculateStartDate(timeframe: String, limit: Int): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val bufferedLimit = (limit * 1.5).toInt()

        when (timeframe) {
            "1Min" -> calendar.add(Calendar.MINUTE, -bufferedLimit)
            "5Min" -> calendar.add(Calendar.MINUTE, -bufferedLimit * 5)
            "15Min" -> calendar.add(Calendar.MINUTE, -bufferedLimit * 15)
            "30Min" -> calendar.add(Calendar.MINUTE, -bufferedLimit * 30)
            "1Hour" -> calendar.add(Calendar.HOUR_OF_DAY, -bufferedLimit)
            "2Hour" -> calendar.add(Calendar.HOUR_OF_DAY, -bufferedLimit * 2)
            "4Hour" -> calendar.add(Calendar.HOUR_OF_DAY, -bufferedLimit * 4)
            "1Day" -> calendar.add(Calendar.DAY_OF_YEAR, -bufferedLimit)
            "1Week" -> calendar.add(Calendar.WEEK_OF_YEAR, -bufferedLimit)
            "1Month" -> calendar.add(Calendar.MONTH, -bufferedLimit)
            else -> calendar.add(Calendar.DAY_OF_YEAR, -bufferedLimit)
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(calendar.time)
    }
}

// region DTOs

@Serializable
data class AlpacaBarsResponseDTO(
    val bars: Map<String, List<AlpacaBarDTO>> = emptyMap(),
    @SerialName("next_page_token")
    val nextPageToken: String? = null
)

@Serializable
data class AlpacaBarDTO(
    val t: String,   // Timestamp
    val o: Double,   // Open
    val h: Double,   // High
    val l: Double,   // Low
    val c: Double,   // Close
    val v: Long,     // Volume
    val n: Int,      // Trade count
    val vw: Double   // VWAP
)

@Serializable
data class AlpacaSnapshotDTO(
    val dailyBar: AlpacaBarDTO? = null,
    val prevDailyBar: AlpacaBarDTO? = null,
    val latestQuote: AlpacaQuoteDTO? = null
)

@Serializable
data class AlpacaQuoteDTO(
    val bp: Double = 0.0,   // Bid price
    val bs: Int = 0,        // Bid size
    val ap: Double = 0.0,   // Ask price
    @SerialName("as")
    val askSize: Int = 0,   // Ask size
    val t: String = ""      // Timestamp
)

// endregion
