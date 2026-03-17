package co.alcheclub.ai.trading.assistant.core.util

/**
 * Normalizes chart-recognized symbols to exchange-specific formats.
 */
object SymbolNormalizer {

    // region Binance

    private val cryptoCommonNames = mapOf(
        "BITCOIN" to "BTC", "ETHEREUM" to "ETH", "RIPPLE" to "XRP",
        "LITECOIN" to "LTC", "CARDANO" to "ADA", "POLKADOT" to "DOT",
        "DOGECOIN" to "DOGE", "SOLANA" to "SOL", "AVALANCHE" to "AVAX",
        "CHAINLINK" to "LINK", "POLYGON" to "MATIC", "UNISWAP" to "UNI",
        "STELLAR" to "XLM", "COSMOS" to "ATOM", "NEAR" to "NEAR",
        "TETHER" to "USDT", "BINANCECOIN" to "BNB", "TRON" to "TRX",
        "SHIBA" to "SHIB", "PEPE" to "PEPE", "SUI" to "SUI",
        "APTOS" to "APT", "ARBITRUM" to "ARB", "OPTIMISM" to "OP",
        "FILECOIN" to "FIL", "HEDERA" to "HBAR", "MONERO" to "XMR",
        "BITCOINCASH" to "BCH", "BITCOINSV" to "BSV",
        "SHIBAINU" to "SHIB", "INTERNETCOMPUTER" to "ICP",
        "ETHEREUMCLASSIC" to "ETC",
        "XAUT" to "PAXG", "TETHERGOLD" to "PAXG",
        "WBTC" to "BTC", "WRAPPEDBITCOIN" to "BTC",
        "WETH" to "ETH", "WRAPPEDETHER" to "ETH",
        "STETH" to "ETH", "CBETH" to "ETH",
        "GOLD" to "XAU", "SILVER" to "XAG",
    )

    private val quoteCurrencies = listOf("USDT", "BUSD", "USDC", "FDUSD", "BTC", "ETH", "BNB")

    fun normalizeBinanceSymbol(symbol: String): String {
        var cleaned = symbol.uppercase().replace(" ", "")

        // Full-string match first (e.g. "BITCOIN" → "BTCUSDT")
        cryptoCommonNames[cleaned]?.let { return it + "USDT" }

        // Strip quote currency to get base symbol
        var quote = "USDT"
        for (q in quoteCurrencies) {
            if (cleaned.endsWith(q) && cleaned.length > q.length) {
                quote = q
                cleaned = cleaned.dropLast(q.length)
                break
            }
        }

        // Handle trailing "USD"
        if (cleaned.endsWith("USD") && cleaned.length > 3) {
            cleaned = cleaned.dropLast(3)
            quote = "USDT"
        }

        // Map base symbol
        cryptoCommonNames[cleaned]?.let { cleaned = it }

        return cleaned + quote
    }

    // endregion

    // region Alpaca

    private val stockCommonNames = mapOf(
        "APPLE" to "AAPL", "GOOGLE" to "GOOGL", "ALPHABET" to "GOOGL",
        "AMAZON" to "AMZN", "MICROSOFT" to "MSFT", "TESLA" to "TSLA",
        "META" to "META", "FACEBOOK" to "META", "NVIDIA" to "NVDA",
        "NETFLIX" to "NFLX", "DISNEY" to "DIS", "INTEL" to "INTC",
        "AMD" to "AMD", "PALANTIR" to "PLTR", "SNAPCHAT" to "SNAP",
        "UBER" to "UBER", "AIRBNB" to "ABNB", "SPOTIFY" to "SPOT",
        "PAYPAL" to "PYPL", "COINBASE" to "COIN", "ROBINHOOD" to "HOOD",
    )

    val etfFallbackMap = mapOf(
        // US Indices (standard + Yahoo Finance format with ^ stripped)
        "SPX" to "SPY", "SP500" to "SPY", "S&P500" to "SPY", "S&P" to "SPY", "GSPC" to "SPY",
        "NDX" to "QQQ", "NASDAQ" to "QQQ", "NASDAQ100" to "QQQ", "NQ" to "QQQ", "IXIC" to "QQQ",
        "DJI" to "DIA", "DJIA" to "DIA", "DOWJONES" to "DIA", "DOW" to "DIA", "YM" to "DIA",
        "RUT" to "IWM", "RUSSELL" to "IWM", "RUSSELL2000" to "IWM",
        "VIX" to "VIXY", "VOLATILITY" to "VIXY",
        // International Indices
        "FTSE" to "EWU", "FTSE100" to "EWU",
        "DAX" to "EWG", "DAX40" to "EWG",
        "CAC" to "EWQ", "CAC40" to "EWQ",
        "NIKKEI" to "EWJ", "N225" to "EWJ", "NIKKEI225" to "EWJ",
        "HSI" to "EWH", "HANGSENG" to "EWH",
        "KOSPI" to "EWY", "KOSPI200" to "EWY",
        "ASX" to "EWA", "ASX200" to "EWA",
        "SENSEX" to "INDA", "NIFTY" to "INDA", "NIFTY50" to "INDA",
        // Sector Indices
        "SOX" to "SOXX", "PHLX" to "SOXX",
        // Precious Metals
        "XAU" to "GLD", "GOLD" to "GLD", "GC" to "GLD",
        "XAUT" to "GLD", "TETHERGOLD" to "GLD", "PAXG" to "GLD",
        "XAG" to "SLV", "SILVER" to "SLV", "SI" to "SLV",
        "XPT" to "PPLT", "PLATINUM" to "PPLT",
        // Energy
        "OIL" to "USO", "CRUDE" to "USO", "CRUDEOIL" to "USO",
        "WTI" to "USO", "CL" to "USO",
        "BRENT" to "BNO", "BZ" to "BNO",
        "NATURALGAS" to "UNG", "NATGAS" to "UNG", "NG" to "UNG",
        // Other Commodities
        "COPPER" to "CPER", "HG" to "CPER",
        "WHEAT" to "WEAT", "CORN" to "CORN", "SOYBEAN" to "SOYB",
        "COTTON" to "BAL", "SUGAR" to "CANE", "COFFEE" to "JO",
        // Forex → Currency ETFs
        "EURUSD" to "FXE", "EUR" to "FXE",
        "GBPUSD" to "FXB", "GBP" to "FXB",
        "JPYUSD" to "FXY", "JPY" to "FXY", "USDJPY" to "FXY",
        "AUDUSD" to "FXA", "AUD" to "FXA",
        "CADUSD" to "FXC", "CAD" to "FXC", "USDCAD" to "FXC",
        "CHFUSD" to "FXF", "CHF" to "FXF", "USDCHF" to "FXF",
        "CNHUSD" to "FXCH", "CNY" to "FXCH", "USDCNH" to "FXCH",
        "GBPJPY" to "FXB", "EURJPY" to "FXE", "EURGBP" to "FXE",
        // International Stocks → US-listed ADRs
        "SAMSUNG" to "SSNLF", "005930" to "SSNLF",
        "TOYOTA" to "TM", "7203" to "TM",
        "SONY" to "SONY", "6758" to "SONY",
        "ALIBABA" to "BABA", "9988" to "BABA",
        "TENCENT" to "TCEHY", "0700" to "TCEHY",
        "BIDU" to "BIDU", "BAIDU" to "BIDU",
        "JD" to "JD", "JDCOM" to "JD",
        "NIO" to "NIO", "XPENG" to "XPEV", "LI" to "LI",
        "TSMC" to "TSM", "2330" to "TSM",
        "ASML" to "ASML", "SAP" to "SAP",
        "NOVARTIS" to "NVS", "ROCHE" to "RHHBY",
        "NESTLE" to "NSRGY", "SHELL" to "SHEL", "ROYALDUTCHSHELL" to "SHEL",
        "UNILEVER" to "UL", "SIEMENS" to "SIEGY",
        "BMW" to "BMWYY", "VOLKSWAGEN" to "VWAGY", "VW" to "VWAGY",
    )

    fun normalizeAlpacaSymbol(symbol: String): String {
        var cleaned = symbol.uppercase().replace(" ", "")

        // Strip trailing quote currencies
        for (suffix in listOf("USDT", "BUSD", "USDC", "FDUSD", "USD")) {
            if (cleaned.endsWith(suffix) && cleaned.length > suffix.length) {
                cleaned = cleaned.dropLast(suffix.length)
                break
            }
        }

        // Map common names
        stockCommonNames[cleaned]?.let { return it }

        return cleaned
    }

    // endregion

    // region Interval

    private val supportedIntervals = setOf(
        "1m", "3m", "5m", "15m", "30m",
        "1h", "2h", "4h", "6h", "8h", "12h",
        "1d", "3d", "1w", "1M"
    )

    fun normalizeInterval(raw: String): String {
        if (raw == "1M") return "1M"
        val lowered = raw.lowercase()
        if (lowered in supportedIntervals) return lowered
        if (lowered == "unknown" || lowered.isEmpty()) return "4h"
        return "4h"
    }

    fun isValidBinanceInterval(interval: String): Boolean =
        interval in supportedIntervals || interval == "1s"

    /**
     * Map internal interval to Alpaca timeframe format.
     */
    fun mapToAlpacaTimeframe(interval: String): String = when (interval) {
        "1m" -> "1Min"
        "5m" -> "5Min"
        "15m" -> "15Min"
        "30m" -> "30Min"
        "1h" -> "1Hour"
        "2h" -> "2Hour"
        "4h" -> "4Hour"
        "1d" -> "1Day"
        "1w" -> "1Week"
        "1M" -> "1Month"
        else -> "4Hour"
    }

    // endregion
}
