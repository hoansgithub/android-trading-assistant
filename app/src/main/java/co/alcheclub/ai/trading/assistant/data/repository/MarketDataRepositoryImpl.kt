package co.alcheclub.ai.trading.assistant.data.repository

import android.util.Log
import co.alcheclub.ai.trading.assistant.core.util.SymbolNormalizer
import co.alcheclub.ai.trading.assistant.data.remote.service.AlpacaBarDTO
import co.alcheclub.ai.trading.assistant.data.remote.service.AlpacaService
import co.alcheclub.ai.trading.assistant.data.remote.service.AlpacaSnapshotDTO
import co.alcheclub.ai.trading.assistant.data.remote.service.BinanceService
import co.alcheclub.ai.trading.assistant.data.remote.service.SymbolNotFoundException
import co.alcheclub.ai.trading.assistant.domain.model.AssetType
import co.alcheclub.ai.trading.assistant.domain.model.BookTicker
import co.alcheclub.ai.trading.assistant.domain.model.Kline
import co.alcheclub.ai.trading.assistant.domain.model.MarketData
import co.alcheclub.ai.trading.assistant.domain.model.Ticker24h
import co.alcheclub.ai.trading.assistant.domain.repository.MarketDataRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Routes market data requests to Binance (crypto) or Alpaca (stocks).
 * For unknown asset types, tries crypto first then stocks.
 */
class MarketDataRepositoryImpl(
    private val binanceSpotService: BinanceService,
    private val binanceFuturesService: BinanceService,
    private val alpacaService: AlpacaService
) : MarketDataRepository {

    companion object {
        private const val TAG = "MarketDataRepo"
    }

    override suspend fun fetchMarketData(
        symbol: String,
        interval: String,
        limit: Int
    ): Result<MarketData> {
        val normalizedInterval = SymbolNormalizer.normalizeInterval(interval)
        return fetchCryptoMarketData(symbol, normalizedInterval, limit)
    }

    override suspend fun fetchMarketData(
        symbol: String,
        assetType: AssetType,
        interval: String,
        limit: Int
    ): Result<MarketData> {
        val normalizedInterval = SymbolNormalizer.normalizeInterval(interval)

        return when (assetType) {
            AssetType.CRYPTO -> fetchCryptoMarketData(symbol, normalizedInterval, limit)
            AssetType.STOCK -> fetchStockMarketData(symbol, normalizedInterval, limit)
            AssetType.UNKNOWN -> {
                // Try crypto first, then stocks
                val cryptoResult = fetchCryptoMarketData(symbol, normalizedInterval, limit)
                if (cryptoResult.isSuccess) return cryptoResult
                fetchStockMarketData(symbol, normalizedInterval, limit)
            }
        }
    }

    // region Crypto (Binance)

    private suspend fun fetchCryptoMarketData(
        symbol: String,
        interval: String,
        limit: Int
    ): Result<MarketData> {
        val normalizedSymbol = SymbolNormalizer.normalizeBinanceSymbol(symbol)
        Log.d(TAG, "Binance: '$symbol' → '$normalizedSymbol' interval=$interval")

        if (!SymbolNormalizer.isValidBinanceInterval(interval)) {
            return Result.failure(Exception("Invalid interval: $interval"))
        }

        // Try spot first
        val spotResult = fetchFromBinance(binanceSpotService, normalizedSymbol, interval, limit)
        if (spotResult.isSuccess) return spotResult

        // Try futures on symbol not found
        val spotError = spotResult.exceptionOrNull()
        if (spotError is SymbolNotFoundException) {
            Log.d(TAG, "Symbol '$normalizedSymbol' not on spot, trying futures")
            val futuresResult = fetchFromBinance(binanceFuturesService, normalizedSymbol, interval, limit)
            if (futuresResult.isSuccess) return futuresResult

            // BUSD migration fallback
            if (normalizedSymbol.endsWith("BUSD")) {
                val usdtSymbol = normalizedSymbol.dropLast(4) + "USDT"
                Log.d(TAG, "BUSD pair not found, trying '$usdtSymbol'")
                return fetchFromBinance(binanceSpotService, usdtSymbol, interval, limit)
            }

            return futuresResult
        }

        return spotResult
    }

    private suspend fun fetchFromBinance(
        service: BinanceService,
        symbol: String,
        interval: String,
        limit: Int
    ): Result<MarketData> = coroutineScope {
        try {
            val klinesDeferred = async { service.fetchKlines(symbol, interval, limit) }
            val tickerDeferred = async { service.fetchTicker24h(symbol) }
            val bookDeferred = async { service.fetchBookTicker(symbol) }

            val klines = klinesDeferred.await().getOrThrow()
            val ticker = tickerDeferred.await().getOrThrow()
            val book = bookDeferred.await().getOrThrow()

            Result.success(
                MarketData(
                    symbol = symbol,
                    interval = interval,
                    timestamp = Date(),
                    klines = klines.mapNotNull { dto ->
                        try {
                            Kline(
                                openTime = Date(dto.openTime),
                                open = BigDecimal(dto.open),
                                high = BigDecimal(dto.high),
                                low = BigDecimal(dto.low),
                                close = BigDecimal(dto.close),
                                volume = BigDecimal(dto.volume),
                                closeTime = Date(dto.closeTime),
                                quoteVolume = BigDecimal(dto.quoteVolume),
                                trades = dto.trades,
                                takerBuyBaseVolume = BigDecimal(dto.takerBuyBase),
                                takerBuyQuoteVolume = BigDecimal(dto.takerBuyQuote)
                            )
                        } catch (e: Exception) {
                            null
                        }
                    },
                    ticker24h = Ticker24h(
                        symbol = ticker.symbol,
                        lastPrice = ticker.lastPrice.toBigDecimalSafe(),
                        priceChange = ticker.priceChange.toBigDecimalSafe(),
                        priceChangePercent = ticker.priceChangePercent.toBigDecimalSafe(),
                        highPrice = ticker.highPrice.toBigDecimalSafe(),
                        lowPrice = ticker.lowPrice.toBigDecimalSafe(),
                        volume = ticker.volume.toBigDecimalSafe(),
                        quoteVolume = ticker.quoteVolume.toBigDecimalSafe(),
                        tradeCount = ticker.count
                    ),
                    bookTicker = BookTicker(
                        symbol = book.symbol,
                        bidPrice = book.bidPrice.toBigDecimalSafe(),
                        bidQty = book.bidQty.toBigDecimalSafe(),
                        askPrice = book.askPrice.toBigDecimalSafe(),
                        askQty = book.askQty.toBigDecimalSafe()
                    )
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // endregion

    // region Stocks (Alpaca)

    private suspend fun fetchStockMarketData(
        symbol: String,
        interval: String,
        limit: Int
    ): Result<MarketData> {
        val normalizedSymbol = SymbolNormalizer.normalizeAlpacaSymbol(symbol)
        Log.d(TAG, "Alpaca: '$symbol' → '$normalizedSymbol' interval=$interval")

        // Try direct
        val result = fetchFromAlpaca(normalizedSymbol, interval, limit)
        if (result.isSuccess) return result

        // Try ETF fallback
        val etfSymbol = SymbolNormalizer.etfFallbackMap[normalizedSymbol]
        if (result.exceptionOrNull() is SymbolNotFoundException && etfSymbol != null) {
            Log.d(TAG, "Mapped '$normalizedSymbol' → ETF '$etfSymbol'")
            return fetchFromAlpaca(etfSymbol, interval, limit)
        }

        return result
    }

    private suspend fun fetchFromAlpaca(
        symbol: String,
        interval: String,
        limit: Int
    ): Result<MarketData> = coroutineScope {
        try {
            val alpacaTimeframe = SymbolNormalizer.mapToAlpacaTimeframe(interval)

            val barsDeferred = async { alpacaService.fetchBars(symbol, alpacaTimeframe, limit) }
            val snapshotDeferred = async { alpacaService.fetchSnapshot(symbol) }

            val bars = barsDeferred.await().getOrThrow()
            val snapshot = snapshotDeferred.await().getOrThrow()

            Result.success(
                MarketData(
                    symbol = symbol,
                    interval = interval,
                    timestamp = Date(),
                    klines = bars.mapNotNull { mapAlpacaBar(it) },
                    ticker24h = mapAlpacaTicker(symbol, snapshot),
                    bookTicker = mapAlpacaBookTicker(symbol, snapshot)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapAlpacaBar(dto: AlpacaBarDTO): Kline? {
        return try {
            val timestamp = parseISO8601(dto.t)
            val open = BigDecimal.valueOf(dto.o)
            val volume = BigDecimal.valueOf(dto.v)
            val vwap = BigDecimal.valueOf(dto.vw)

            Kline(
                openTime = timestamp,
                open = open,
                high = BigDecimal.valueOf(dto.h),
                low = BigDecimal.valueOf(dto.l),
                close = BigDecimal.valueOf(dto.c),
                volume = volume,
                closeTime = timestamp,
                quoteVolume = vwap.multiply(volume),
                trades = dto.n,
                takerBuyBaseVolume = BigDecimal.ZERO,
                takerBuyQuoteVolume = BigDecimal.ZERO
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun mapAlpacaTicker(symbol: String, snapshot: AlpacaSnapshotDTO): Ticker24h {
        val daily = snapshot.dailyBar
        val prev = snapshot.prevDailyBar

        val lastPrice = BigDecimal.valueOf(daily?.c ?: 0.0)
        val prevClose = BigDecimal.valueOf(prev?.c ?: 0.0)
        val priceChange = if (prevClose > BigDecimal.ZERO) lastPrice.subtract(prevClose) else BigDecimal.ZERO
        val priceChangePercent = if (prevClose > BigDecimal.ZERO) {
            priceChange.multiply(BigDecimal(100)).divide(prevClose, 2, java.math.RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        return Ticker24h(
            symbol = symbol,
            lastPrice = lastPrice,
            priceChange = priceChange,
            priceChangePercent = priceChangePercent,
            highPrice = BigDecimal.valueOf(daily?.h ?: 0.0),
            lowPrice = BigDecimal.valueOf(daily?.l ?: 0.0),
            volume = BigDecimal.valueOf(daily?.v ?: 0),
            quoteVolume = BigDecimal.valueOf(daily?.vw ?: 0.0)
                .multiply(BigDecimal.valueOf(daily?.v ?: 0)),
            tradeCount = daily?.n ?: 0
        )
    }

    private fun mapAlpacaBookTicker(symbol: String, snapshot: AlpacaSnapshotDTO): BookTicker {
        val quote = snapshot.latestQuote
        return BookTicker(
            symbol = symbol,
            bidPrice = BigDecimal.valueOf(quote?.bp ?: 0.0),
            bidQty = BigDecimal.valueOf(quote?.bs?.toLong() ?: 0),
            askPrice = BigDecimal.valueOf(quote?.ap ?: 0.0),
            askQty = BigDecimal.valueOf(quote?.askSize?.toLong() ?: 0)
        )
    }

    // endregion

    // region Helpers

    private fun parseISO8601(dateString: String): Date {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(dateString) ?: continue
            } catch (_: Exception) {
                continue
            }
        }
        return Date()
    }

    private fun String.toBigDecimalSafe(): BigDecimal =
        toBigDecimalOrNull() ?: BigDecimal.ZERO

    // endregion
}
