package co.alcheclub.ai.trading.assistant.domain.repository

import co.alcheclub.ai.trading.assistant.domain.model.AssetType
import co.alcheclub.ai.trading.assistant.domain.model.MarketData

interface MarketDataRepository {
    suspend fun fetchMarketData(symbol: String, interval: String, limit: Int = 100): Result<MarketData>
    suspend fun fetchMarketData(symbol: String, assetType: AssetType, interval: String, limit: Int = 100): Result<MarketData>
}
