package co.alcheclub.ai.trading.assistant.modules.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.alcheclub.ai.trading.assistant.domain.model.Analysis
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.repository.AnalysisRepository
import co.alcheclub.ai.trading.assistant.domain.repository.AuthRepository
import co.alcheclub.ai.trading.assistant.domain.repository.StrategyRepository
import co.alcheclub.ai.trading.assistant.domain.usecase.AnalyzeChartUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data object Empty : HomeUiState()
    data class Loaded(val analyses: List<Analysis>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val analysisRepository: AnalysisRepository,
    private val authRepository: AuthRepository,
    private val strategyRepository: StrategyRepository,
    private val analyzeChartUseCase: AnalyzeChartUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "HomeVM"
        private const val PAGE_SIZE = 20
    }

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    // New analysis flow
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analyzingProgress = MutableStateFlow(0f)
    val analyzingProgress: StateFlow<Float> = _analyzingProgress.asStateFlow()

    private val _analysisError = MutableStateFlow<String?>(null)
    val analysisError: StateFlow<String?> = _analysisError.asStateFlow()

    // Strategy picker
    private val _showStrategyPicker = MutableStateFlow(false)
    val showStrategyPicker: StateFlow<Boolean> = _showStrategyPicker.asStateFlow()

    private val _strategies = MutableStateFlow<List<Strategy>>(emptyList())
    val strategies: StateFlow<List<Strategy>> = _strategies.asStateFlow()

    private val _selectedStrategy = MutableStateFlow<Strategy?>(null)
    val selectedStrategy: StateFlow<Strategy?> = _selectedStrategy.asStateFlow()

    private var pendingImageData: ByteArray? = null
    private var hasLoaded = false
    private var allAnalyses = mutableListOf<Analysis>()

    fun onViewAppear() {
        if (!hasLoaded) {
            hasLoaded = true
            loadFirstPage()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            allAnalyses.clear()
            _canLoadMore.value = true
            loadPage(0)
            _isRefreshing.value = false
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value || !_canLoadMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            loadPage(allAnalyses.size)
            _isLoadingMore.value = false
        }
    }

    fun deleteAnalysis(analysisId: java.util.UUID) {
        viewModelScope.launch {
            analysisRepository.deleteAnalysis(analysisId).onSuccess {
                allAnalyses.removeAll { it.id == analysisId }
                _uiState.value = if (allAnalyses.isEmpty()) HomeUiState.Empty else HomeUiState.Loaded(allAnalyses.toList())
            }.onFailure { e ->
                Log.e(TAG, "Delete failed", e)
            }
        }
    }

    fun onImageCaptured(imageData: ByteArray) {
        pendingImageData = imageData
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            Log.d(TAG, "Loading strategies for userId=$userId")
            strategyRepository.fetchStrategies(userId).onSuccess { list ->
                Log.d(TAG, "Loaded ${list.size} strategies")
                _strategies.value = list
                _selectedStrategy.value = list.firstOrNull()
            }.onFailure { e ->
                Log.e(TAG, "Failed to load strategies", e)
                _strategies.value = emptyList()
                _selectedStrategy.value = null
            }
            _showStrategyPicker.value = true
        }
    }

    fun selectStrategy(strategy: Strategy) {
        _selectedStrategy.value = strategy
    }

    fun dismissStrategyPicker() {
        _showStrategyPicker.value = false
        pendingImageData = null
    }

    fun startAnalysisWithStrategy() {
        val imageData = pendingImageData ?: return
        val strategy = _selectedStrategy.value
        _showStrategyPicker.value = false

        viewModelScope.launch {
            _isAnalyzing.value = true
            _analyzingProgress.value = 0f
            _analysisError.value = null

            val progressJob = launch {
                val steps = 90
                val stepDelay = 110L
                for (i in 1..steps) {
                    delay(stepDelay)
                    _analyzingProgress.value = i / 100f
                }
                while (_isAnalyzing.value) { delay(100) }
            }

            val userId = authRepository.getCurrentUserId()
            val result = analyzeChartUseCase.execute(
                imageData = imageData,
                userId = userId,
                strategy = strategy
            )

            progressJob.cancel()
            _analyzingProgress.value = 1f
            pendingImageData = null

            result.onSuccess {
                Log.d(TAG, "Analysis complete: ${it.signal} ${it.confidenceScore}%")
                _isAnalyzing.value = false
                refresh()
            }.onFailure { error ->
                Log.e(TAG, "Analysis failed: ${error.message}", error)
                _analysisError.value = mapAnalysisError(error)
                _isAnalyzing.value = false
            }
        }
    }

    fun dismissAnalysisError() {
        _analysisError.value = null
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            allAnalyses.clear()
            _canLoadMore.value = true
            loadPage(0)
        }
    }

    private suspend fun loadPage(offset: Int) {
        val userId = authRepository.getCurrentUserId()
        analysisRepository.fetchAnalyses(userId, offset, PAGE_SIZE).onSuccess { page ->
            Log.d(TAG, "Fetched ${page.size} analyses (offset=$offset)")
            if (offset == 0) allAnalyses.clear()
            allAnalyses.addAll(page)
            _canLoadMore.value = page.size >= PAGE_SIZE
            _uiState.value = if (allAnalyses.isEmpty()) HomeUiState.Empty else HomeUiState.Loaded(allAnalyses.toList())
        }.onFailure { e ->
            Log.e(TAG, "Load analyses failed", e)
            if (offset == 0) {
                _uiState.value = HomeUiState.Error("Couldn't load your analyses. Check your connection and pull to refresh.")
            }
        }
    }

    private fun mapAnalysisError(error: Throwable): String {
        val msg = error.message?.lowercase() ?: ""
        return when {
            msg.contains("chart recognition") || msg.contains("could not determine") || msg.contains("image optimization") ->
                "Chart Recognition Failed\nWe couldn't read your chart. Don't worry — you can try again."
            msg.contains("market data") || msg.contains("symbol not found") ->
                "Market Data Unavailable\nCouldn't fetch price data for this asset. Please try again."
            else ->
                "AI Analysis Failed\nOur AI couldn't complete the analysis. Please try again."
        }
    }
}
