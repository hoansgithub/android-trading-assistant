package co.alcheclub.ai.trading.assistant.domain.repository

import co.alcheclub.ai.trading.assistant.domain.model.Analysis
import co.alcheclub.ai.trading.assistant.domain.model.MarketData
import java.util.UUID

interface AnalysisRepository {
    suspend fun fetchAnalyses(userId: UUID): Result<List<Analysis>>
    suspend fun saveAnalysis(analysis: Analysis, marketData: MarketData): Result<Analysis>
    suspend fun deleteAnalysis(analysisId: UUID): Result<Unit>
    suspend fun updateImageUrl(analysisId: UUID, imageUrl: String): Result<Unit>
}
