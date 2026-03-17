package co.alcheclub.ai.trading.assistant.domain.usecase

import co.alcheclub.ai.trading.assistant.core.util.SymbolNormalizer
import co.alcheclub.ai.trading.assistant.data.remote.service.GeminiVisionService
import co.alcheclub.ai.trading.assistant.domain.model.ChartRecognitionResult

/**
 * Thin orchestrator: image bytes → ChartRecognitionResult with normalized interval.
 */
class RecognizeChartUseCase(
    private val visionService: GeminiVisionService
) {
    suspend fun execute(imageData: ByteArray): Result<ChartRecognitionResult> {
        val result = visionService.recognizeChart(imageData)
        return result.map { recognition ->
            recognition.copy(
                interval = SymbolNormalizer.normalizeInterval(recognition.interval)
            )
        }
    }
}
