# Error Handling Rules

## No Force Unwrap
```kotlin
// FORBIDDEN
val value = nullable!!

// REQUIRED - Safe handling with logging
val value = nullable ?: run {
    Logger.e("Value was null")
    return
}

// OR with default
val value = nullable ?: defaultValue
```

## Result Type Pattern
```kotlin
// Repository returns Result
suspend fun getData(): Result<Data> {
    return try {
        val data = api.fetch()
        Result.success(data)
    } catch (e: Exception) {
        Log.e("Repository", "Failed: ${e.message}", e)
        Result.failure(e)
    }
}

// ViewModel handles Result
when (val result = useCase()) {
    is Result.Success -> _uiState.value = UiState.Success(result.data)
    is Result.Error -> _uiState.value = UiState.Error(result.message)
}
```

## Never Embed Activities as Composables
```kotlin
// FORBIDDEN - Bypasses Activity lifecycle!
composable<AppDestination.Feature> {
    FeatureScreen()  // Activity's onBackPressed, ads NEVER called!
}

// REQUIRED - Launch Activity directly
private fun navigateToFeature() {
    startActivity(Intent(this, FeatureActivity::class.java))
    finish()
}
```
