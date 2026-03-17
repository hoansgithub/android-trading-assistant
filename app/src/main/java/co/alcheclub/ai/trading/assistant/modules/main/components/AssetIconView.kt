package co.alcheclub.ai.trading.assistant.modules.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import android.graphics.BitmapFactory
import co.alcheclub.ai.trading.assistant.ui.theme.BgElevated
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Circular asset icon with two-source loading (crypto → stock fallback).
 *
 * URL sources matching iOS CryptoIconService:
 * - Crypto: https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/{symbol}.png
 * - Stock:  https://financialmodelingprep.com/image-stock/{SYMBOL}.png
 *
 * Falls back to text initials if both fail.
 */
@Composable
fun AssetIconView(
    symbol: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val baseSymbol = extractBaseSymbol(symbol)
    val initials = remember(baseSymbol) { baseSymbol.take(2).uppercase() }

    var bitmap by remember(baseSymbol) { mutableStateOf<ImageBitmap?>(null) }

    // Load icon in background coroutine — try crypto then stock, with cache
    LaunchedEffect(baseSymbol) {
        val cached = iconCache[baseSymbol]
        if (cached != null) {
            bitmap = cached
        } else if (!iconCache.containsKey(baseSymbol)) {
            val result = loadAssetIcon(baseSymbol)
            iconCache[baseSymbol] = result
            bitmap = result
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(BgElevated),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = symbol,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            // Initials placeholder (while loading or on failure)
            Text(
                text = initials,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.32f).sp,
                color = TextSecondary
            )
        }
    }
}

/**
 * Try loading icon from crypto source, then stock source.
 * Returns null if both fail.
 */
private suspend fun loadAssetIcon(baseSymbol: String): ImageBitmap? = withContext(Dispatchers.IO) {
    val urls = listOf(
        "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${baseSymbol.lowercase()}.png",
        "https://financialmodelingprep.com/image-stock/${baseSymbol.uppercase()}.png"
    )

    for (url in urls) {
        try {
            val connection = URL(url).openConnection().apply {
                connectTimeout = 5000
                readTimeout = 5000
            }
            val bytes = connection.getInputStream().use { it.readBytes() }
            if (bytes.isNotEmpty()) {
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) return@withContext bmp.asImageBitmap()
            }
        } catch (e: Exception) {
            // Try next URL
        }
    }
    null
}

// In-memory cache to avoid re-fetching on recomposition.
// ConcurrentHashMap is thread-safe for concurrent coroutine access.
private val iconCache = java.util.concurrent.ConcurrentHashMap<String, ImageBitmap?>()

/**
 * Extract base symbol from trading pair.
 * "BTCUSDT" → "BTC", "BTC/USDT" → "BTC", "BNBTUSD" → "BNB", "AAPL" → "AAPL"
 */
private fun extractBaseSymbol(symbol: String): String {
    for (sep in charArrayOf('/', '-', ':')) {
        val idx = symbol.indexOf(sep)
        if (idx > 0) return symbol.substring(0, idx).uppercase()
    }

    val upper = symbol.uppercase()
    val quoteCurrencies = listOf(
        "FDUSD", "USDT", "BUSD", "USDC", "TUSD",
        "BIDR", "BVND",
        "DAI", "EUR", "GBP", "TRY", "BRL", "ARS",
        "BTC", "ETH", "BNB", "XRP", "DOGE",
        "USD"
    )
    for (quote in quoteCurrencies) {
        if (upper.endsWith(quote) && upper.length > quote.length) {
            return upper.dropLast(quote.length)
        }
    }

    return upper
}
