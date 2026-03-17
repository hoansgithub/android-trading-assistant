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
    }

    private val _uiState = MutableStateFlow<StrategyUiState>(StrategyUiState.Loading)
    val uiState: StateFlow<StrategyUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var hasLoaded = false

    fun onViewAppear() {
        if (!hasLoaded) {
            hasLoaded = true
            loadStrategies()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadStrategiesInternal()
            _isRefreshing.value = false
        }
    }

    fun deleteStrategy(strategyId: java.util.UUID) {
        viewModelScope.launch {
            strategyRepository.deleteStrategy(strategyId).onSuccess {
                val currentState = _uiState.value
                if (currentState is StrategyUiState.Loaded) {
                    val updated = currentState.strategies.filter { it.id != strategyId }
                    _uiState.value = if (updated.isEmpty()) StrategyUiState.Empty else StrategyUiState.Loaded(updated)
                }
            }.onFailure { e ->
                Log.e(TAG, "Delete strategy failed", e)
            }
        }
    }

    private fun loadStrategies() {
        viewModelScope.launch {
            _uiState.value = StrategyUiState.Loading
            loadStrategiesInternal()
        }
    }

    private suspend fun loadStrategiesInternal() {
        val userId = authRepository.getCurrentUserId()
        strategyRepository.fetchStrategies(userId).onSuccess { strategies ->
            _uiState.value = if (strategies.isEmpty()) StrategyUiState.Empty else StrategyUiState.Loaded(strategies)
        }.onFailure { e ->
            Log.e(TAG, "Load strategies failed", e)
            _uiState.value = StrategyUiState.Error(e.message ?: "Failed to load strategies")
        }
    }
}
