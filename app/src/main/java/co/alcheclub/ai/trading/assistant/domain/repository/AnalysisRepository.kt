package co.alcheclub.ai.trading.assistant.domain.repository

import co.alcheclub.ai.trading.assistant.domain.model.Analysis
import co.alcheclub.ai.trading.assistant.domain.model.MarketData
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import java.util.UUID

interface AnalysisRepository {
    suspend fun fetchAnalyses(userId: UUID, offset: Int = 0, limit: Int = 20): Result<List<Analysis>>
    suspend fun saveAnalysis(analysis: Analysis, marketData: MarketData, strategy: Strategy? = null): Result<Analysis>
    suspend fun deleteAnalysis(analysisId: UUID): Result<Unit>
    suspend fun updateImageUrl(analysisId: UUID, imageUrl: String): Result<Unit>
}
