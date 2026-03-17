package co.alcheclub.ai.trading.assistant.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChartRecognitionResult(
    val asset: String,
    val interval: String,
    val assetType: String // "crypto", "stock", "unknown"
)

enum class AssetType(val value: String) {
    CRYPTO("crypto"),
    STOCK("stock"),
    UNKNOWN("unknown");

    companion object {
        fun from(value: String): AssetType = entries.find {
            it.value.equals(value, ignoreCase = true)
        } ?: UNKNOWN
    }
}
