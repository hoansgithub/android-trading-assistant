package co.alcheclub.ai.trading.assistant.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Optimizes images before sending to LLM vision APIs.
 *
 * Resizes to max dimension and compresses as JPEG to reduce bandwidth.
 * Gemini charges ~258 tokens per image regardless of resolution, so the
 * benefit is purely bandwidth + encoding speed.
 *
 * Returns null on failure instead of falling back to original data —
 * sending unoptimized 5-15MB camera photos would cause network timeouts.
 */
object ImageOptimizer {

    private const val TAG = "ImageOptimizer"

    data class Config(
        val maxDimension: Int,
        val jpegQuality: Int, // 0-100
        val maxOutputBytes: Int
    ) {
        companion object {
            /** Preset for chart recognition: 1024px max, 70% quality (~100-300KB output) */
            val CHART_RECOGNITION = Config(maxDimension = 1024, jpegQuality = 70, maxOutputBytes = 512_000)

            /** Preset for early compression: 2048px max, 80% quality (applied at capture time) */
            val CAPTURE = Config(maxDimension = 2048, jpegQuality = 80, maxOutputBytes = 1_500_000)
        }
    }

    /**
     * Optimize image data by resizing and compressing.
     * @param imageData Original image data (JPEG/PNG/WebP)
     * @param config Optimization configuration
     * @return Optimized JPEG data, or null if image cannot be processed
     */
    fun optimize(imageData: ByteArray, config: Config): ByteArray? {
        // Decode with inJustDecodeBounds first to get dimensions without allocating
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)

        val originalWidth = options.outWidth
        val originalHeight = options.outHeight
        if (originalWidth <= 0 || originalHeight <= 0) {
            Log.e(TAG, "Failed to decode image dimensions (${imageData.size} bytes)")
            return null
        }

        // Calculate inSampleSize for memory-efficient downsampling
        val longestEdge = max(originalWidth, originalHeight)
        val sampleSize = calculateSampleSize(longestEdge, config.maxDimension)

        // Decode with downsampling
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size, decodeOptions)
        if (bitmap == null) {
            Log.e(TAG, "Failed to decode image (${imageData.size} bytes)")
            return null
        }

        // Fine-scale resize if still larger than maxDimension after subsampling
        val resized = resizeIfNeeded(bitmap, config.maxDimension)

        // Compress to JPEG with quality stepping
        val result = compressToLimit(resized, config.jpegQuality, config.maxOutputBytes)

        // Capture dimensions before recycling
        val finalWidth = resized.width
        val finalHeight = resized.height

        // Recycle bitmaps
        if (resized != bitmap) resized.recycle()
        bitmap.recycle()

        if (result == null) {
            Log.e(TAG, "Failed to compress image to JPEG")
            return null
        }

        Log.d(TAG, "${imageData.size} -> ${result.size} bytes (${finalWidth}x${finalHeight})")
        return result
    }

    /**
     * Calculate BitmapFactory.Options.inSampleSize for efficient downsampling.
     * Returns power-of-2 sample size (1, 2, 4, 8...).
     */
    private fun calculateSampleSize(longestEdge: Int, maxDimension: Int): Int {
        var sampleSize = 1
        while (longestEdge / (sampleSize * 2) >= maxDimension) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun resizeIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val longestEdge = max(bitmap.width, bitmap.height)
        if (longestEdge <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / longestEdge
        val targetWidth = (bitmap.width * scale).roundToInt()
        val targetHeight = (bitmap.height * scale).roundToInt()

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /**
     * Compress to JPEG, stepping down quality if result exceeds maxBytes.
     * Minimum quality: 30%.
     */
    private fun compressToLimit(bitmap: Bitmap, quality: Int, maxBytes: Int): ByteArray? {
        // Try at requested quality first
        val stream = ByteArrayOutputStream()
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)) return null
        var data = stream.toByteArray()

        if (data.size <= maxBytes) return data

        // Step down quality in increments of 10 until under limit
        var currentQuality = quality - 10
        while (currentQuality >= 30) {
            stream.reset()
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, stream)) break
            data = stream.toByteArray()
            if (data.size <= maxBytes) return data
            currentQuality -= 10
        }

        // Return whatever we have — still much smaller than original
        return data
    }
}
