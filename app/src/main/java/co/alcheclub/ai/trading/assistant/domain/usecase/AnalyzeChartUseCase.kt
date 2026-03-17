package co.alcheclub.ai.trading.assistant.domain.usecase

import android.util.Log
import co.alcheclub.ai.trading.assistant.data.remote.service.GeminiTextService
import co.alcheclub.ai.trading.assistant.data.remote.service.ImageUploadService
import co.alcheclub.ai.trading.assistant.domain.model.Analysis
import co.alcheclub.ai.trading.assistant.domain.model.AnalysisType
import co.alcheclub.ai.trading.assistant.domain.model.AssetType
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.repository.AnalysisRepository
import co.alcheclub.ai.trading.assistant.domain.repository.MarketDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Main orchestrator: image → recognize → market data → AI analysis → save → upload (fire-and-forget).
 */
class AnalyzeChartUseCase(
    private val recognizeChartUseCase: RecognizeChartUseCase,
    private val marketDataRepository: MarketDataRepository,
    private val geminiTextService: GeminiTextService,
    private val analysisRepository: AnalysisRepository,
    private val imageUploadService: ImageUploadService
) {
    companion object {
        private const val TAG = "AnalyzeChart"
    }

    suspend fun execute(
        imageData: ByteArray,
        userId: UUID,
        strategy: Strategy? = null
    ): Result<Analysis> {
        // Step 1: Recognize chart
        Log.d(TAG, "Step 1: Recognizing chart...")
        val recognition = recognizeChartUseCase.execute(imageData).getOrElse { e ->
            Log.e(TAG, "Chart recognition failed", e)
            return Result.failure(e)
        }
        Log.d(TAG, "Recognized: ${recognition.asset} / ${recognition.interval} / ${recognition.assetType}")

        // Step 2: Fetch market data
        Log.d(TAG, "Step 2: Fetching market data...")
        val assetType = AssetType.from(recognition.assetType)
        val marketData = marketDataRepository.fetchMarketData(
            symbol = recognition.asset,
            assetType = assetType,
            interval = recognition.interval
        ).getOrElse { e ->
            Log.e(TAG, "Market data fetch failed", e)
            return Result.failure(e)
        }
        Log.d(TAG, "Market data: ${marketData.klines.size} klines, last price: ${marketData.ticker24h.lastPrice}")

        // Step 3: AI Analysis
        Log.d(TAG, "Step 3: Running AI analysis...")
        val aiResult = geminiTextService.analyzeMarketData(
            marketData = marketData,
            assetName = null
        ).getOrElse { e ->
            Log.e(TAG, "AI analysis failed", e)
            return Result.failure(e)
        }
        Log.d(TAG, "AI result: ${aiResult.signal} confidence=${aiResult.confidenceScore}")

        // Step 4: Build Analysis with strategy reference
        val analysis = Analysis(
            userId = userId,
            strategyId = strategy?.id,
            assetSymbol = marketData.symbol,
            assetName = recognition.asset,
            signal = aiResult.signal,
            confidenceScore = aiResult.confidenceScore,
            actionPlan = aiResult.actionPlan,
            riskAssessment = aiResult.riskAssessment,
            aiExplanation = aiResult.aiExplanation,
            marketContext = aiResult.marketContext,
            strategyName = strategy?.name,
            currentPrice = marketData.ticker24h.lastPrice,
            analysisType = AnalysisType.IMAGE,
            timeframe = marketData.interval
        )

        // Step 5: Save to Supabase
        Log.d(TAG, "Step 5: Saving to Supabase...")
        val savedAnalysis = analysisRepository.saveAnalysis(analysis, marketData, strategy).getOrElse { e ->
            Log.e(TAG, "Save failed (non-fatal)", e)
            analysis // Return unsaved analysis
        }

        // Step 6: Upload image (fire-and-forget)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlResult = imageUploadService.uploadImage(imageData)
                urlResult.onSuccess { url ->
                    Log.d(TAG, "Image uploaded: $url")
                    analysisRepository.updateImageUrl(savedAnalysis.id, url)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Image upload failed (non-fatal)", e)
            }
        }

        return Result.success(savedAnalysis)
    }
}
