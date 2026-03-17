package co.alcheclub.ai.trading.assistant.domain.model

import java.math.BigDecimal
import java.util.Date

data class MarketData(
    val symbol: String,
    val interval: String,
    val timestamp: Date,
    val klines: List<Kline>,
    val ticker24h: Ticker24h,
    val bookTicker: BookTicker
)

data class Kline(
    val openTime: Date,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: BigDecimal,
    val closeTime: Date,
    val quoteVolume: BigDecimal,
    val trades: Int,
    val takerBuyBaseVolume: BigDecimal,
    val takerBuyQuoteVolume: BigDecimal
)

data class Ticker24h(
    val symbol: String,
    val lastPrice: BigDecimal,
    val priceChange: BigDecimal,
    val priceChangePercent: BigDecimal,
    val highPrice: BigDecimal,
    val lowPrice: BigDecimal,
    val volume: BigDecimal,
    val quoteVolume: BigDecimal,
    val tradeCount: Int
)

data class BookTicker(
    val symbol: String,
    val bidPrice: BigDecimal,
    val bidQty: BigDecimal,
    val askPrice: BigDecimal,
    val askQty: BigDecimal
)
