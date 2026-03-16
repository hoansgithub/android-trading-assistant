# State Management Rules

## Sealed Class State Machines (REQUIRED)
```kotlin
// FORBIDDEN - Multiple boolean flags
var isLoading = false
var hasError = false

// REQUIRED - Single state source
sealed class FeatureUiState {
    data object Loading : FeatureUiState()
    data class Success(val data: Data) : FeatureUiState()
    data class Error(val message: String) : FeatureUiState()
}
```

## StateFlow Collection
```kotlin
// FORBIDDEN
val uiState by viewModel.uiState.collectAsState()

// REQUIRED - Lifecycle-aware
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

## ViewModel Pattern
```kotlin
class FeatureViewModel(private val useCase: UseCase) : ViewModel() {
    // UI State - StateFlow
    private val _uiState = MutableStateFlow<FeatureUiState>(FeatureUiState.Loading)
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    // Navigation Events - Channel (one-time)
    private val _navigationEvent = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onItemClick(id: String) {
        viewModelScope.launch {
            _navigationEvent.send(NavigationEvent.NavigateToDetail(id))
        }
    }
}
```
