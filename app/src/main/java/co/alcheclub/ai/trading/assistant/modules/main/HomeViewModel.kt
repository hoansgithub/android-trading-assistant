package co.alcheclub.ai.trading.assistant.modules.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.alcheclub.ai.trading.assistant.domain.model.Analysis
import co.alcheclub.ai.trading.assistant.domain.repository.AnalysisRepository
import co.alcheclub.ai.trading.assistant.domain.repository.AuthRepository
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
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HomeVM"
    }

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var hasLoaded = false

    fun onViewAppear() {
        if (!hasLoaded) {
            hasLoaded = true
            loadAnalyses()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadAnalysesInternal()
            _isRefreshing.value = false
        }
    }

    fun deleteAnalysis(analysisId: java.util.UUID) {
        viewModelScope.launch {
            analysisRepository.deleteAnalysis(analysisId).onSuccess {
                // Remove from local state
                val currentState = _uiState.value
                if (currentState is HomeUiState.Loaded) {
                    val updated = currentState.analyses.filter { it.id != analysisId }
                    _uiState.value = if (updated.isEmpty()) HomeUiState.Empty else HomeUiState.Loaded(updated)
                }
            }.onFailure { e ->
                Log.e(TAG, "Delete failed", e)
            }
        }
    }

    private fun loadAnalyses() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            loadAnalysesInternal()
        }
    }

    private suspend fun loadAnalysesInternal() {
        val userId = authRepository.getCurrentUserId()
        analysisRepository.fetchAnalyses(userId).onSuccess { analyses ->
            _uiState.value = if (analyses.isEmpty()) HomeUiState.Empty else HomeUiState.Loaded(analyses)
        }.onFailure { e ->
            Log.e(TAG, "Load analyses failed", e)
            _uiState.value = HomeUiState.Error(e.message ?: "Failed to load analyses")
        }
    }
}
