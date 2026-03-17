package co.alcheclub.ai.trading.assistant.domain.repository

import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import java.util.UUID

interface StrategyRepository {
    suspend fun fetchStrategies(userId: UUID): Result<List<Strategy>>
    suspend fun saveStrategy(strategy: Strategy, userId: UUID): Result<Strategy>
    suspend fun deleteStrategy(strategyId: UUID): Result<Unit>
}
