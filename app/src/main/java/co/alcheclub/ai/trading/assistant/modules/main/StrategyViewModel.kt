package co.alcheclub.ai.trading.assistant.modules.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.alcheclub.ai.trading.assistant.domain.model.Strategy
import co.alcheclub.ai.trading.assistant.domain.repository.AuthRepository
import co.alcheclub.ai.trading.assistant.domain.repository.StrategyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class StrategyUiState {
    data object Loading : StrategyUiState()
    data object Empty : StrategyUiState()
    data class Loaded(val strategies: List<Strategy>) : StrategyUiState()
    data class Error(val message: String) : StrategyUiState()
}

class StrategyViewModel(
    private val strategyRepository: StrategyRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StrategyVM"
        private const val PAGE_SIZE = 20
    }

    private val _uiState = MutableStateFlow<StrategyUiState>(StrategyUiState.Loading)
    val uiState: StateFlow<StrategyUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private var hasLoaded = false
    private var allStrategies = mutableListOf<Strategy>()

    private val strategyCount: Int get() = allStrategies.size

    fun onViewAppear() {
        if (!hasLoaded) {
            hasLoaded = true
            loadFirstPage()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            allStrategies.clear()
            _canLoadMore.value = true
            loadPage(0)
            _isRefreshing.value = false
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value || !_canLoadMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            loadPage(allStrategies.size)
            _isLoadingMore.value = false
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    fun deleteStrategy(strategyId: java.util.UUID) {
        if (strategyCount <= 1) {
            _errorMessage.value = "You must have at least one strategy. Create a new strategy before deleting this one."
            return
        }
        viewModelScope.launch {
            _isProcessing.value = true
            strategyRepository.deleteStrategy(strategyId).onSuccess {
                allStrategies.removeAll { it.id == strategyId }
                _uiState.value = if (allStrategies.isEmpty()) StrategyUiState.Empty else StrategyUiState.Loaded(allStrategies.toList())
            }.onFailure { e ->
                Log.e(TAG, "Delete strategy failed", e)
                _errorMessage.value = "Couldn't delete this strategy. Please check your connection and try again."
            }
            _isProcessing.value = false
        }
    }

    fun duplicateStrategy(strategy: Strategy) {
        viewModelScope.launch {
            _isProcessing.value = true
            val copy = strategy.copy(
                id = java.util.UUID.randomUUID(),
                name = "${strategy.name} (Copy)",
                isPreset = false,
                isDefault = false
            )
            val userId = authRepository.getCurrentUserId()
            strategyRepository.saveStrategy(copy, userId).onSuccess {
                Log.d(TAG, "Strategy duplicated: ${copy.name}")
                refresh()
            }.onFailure { e ->
                Log.e(TAG, "Duplicate strategy failed", e)
                _errorMessage.value = "Couldn't duplicate this strategy. Please check your connection and try again."
            }
            _isProcessing.value = false
        }
    }

    fun createStrategy(strategy: Strategy) {
        viewModelScope.launch {
            _isProcessing.value = true
            val userId = authRepository.getCurrentUserId()
            strategyRepository.saveStrategy(strategy, userId).onSuccess {
                Log.d(TAG, "Strategy created: ${strategy.name}")
                refresh()
            }.onFailure { e ->
                Log.e(TAG, "Create strategy failed", e)
                _errorMessage.value = "Couldn't save your strategy. Please check your connection and try again."
            }
            _isProcessing.value = false
        }
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            _uiState.value = StrategyUiState.Loading
            allStrategies.clear()
            _canLoadMore.value = true
            loadPage(0)
        }
    }

    private suspend fun loadPage(offset: Int) {
        val userId = authRepository.getCurrentUserId()
        strategyRepository.fetchStrategies(userId, offset, PAGE_SIZE).onSuccess { page ->
            Log.d(TAG, "Fetched ${page.size} strategies (offset=$offset)")
            if (offset == 0) allStrategies.clear()
            allStrategies.addAll(page)
            _canLoadMore.value = page.size >= PAGE_SIZE
            _uiState.value = if (allStrategies.isEmpty()) StrategyUiState.Empty else StrategyUiState.Loaded(allStrategies.toList())
        }.onFailure { e ->
            Log.e(TAG, "Load strategies failed", e)
            if (offset == 0) {
                _uiState.value = StrategyUiState.Error("Couldn't load your strategies. Check your connection and pull to refresh.")
            }
        }
    }
}
