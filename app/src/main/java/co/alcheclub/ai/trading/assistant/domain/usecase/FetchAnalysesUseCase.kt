package co.alcheclub.ai.trading.assistant.domain.usecase

import co.alcheclub.ai.trading.assistant.domain.model.Analysis
import co.alcheclub.ai.trading.assistant.domain.repository.AnalysisRepository
import java.util.UUID

/**
 * Fetch user's analysis history from Supabase.
 */
class FetchAnalysesUseCase(
    private val analysisRepository: AnalysisRepository
) {
    suspend fun execute(userId: UUID): Result<List<Analysis>> {
        return analysisRepository.fetchAnalyses(userId)
    }
}
