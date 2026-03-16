# Memory Safety Rules

## Coroutine Scopes
```kotlin
// FORBIDDEN - Manual scope
private val scope = CoroutineScope(Dispatchers.Main)

// FORBIDDEN - GlobalScope
GlobalScope.launch { }

// REQUIRED - Auto-cancelled with ViewModel
viewModelScope.launch { }
```

## Context Retention
```kotlin
// FORBIDDEN - Storing Activity context
class Manager(private val context: Context) {
    private val prefs = context.getSharedPreferences(...)  // Could be Activity!
}

// REQUIRED - Always use applicationContext
class Manager(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(...)
}
```

## DisposableEffect with Safe Cast
```kotlin
// FORBIDDEN - Memory leak risk
SideEffect {
    val window = (view.context as Activity).window  // Unsafe cast
}

// REQUIRED - Safe and lifecycle-aware
DisposableEffect(view) {
    val activity = view.context as? Activity ?: return@DisposableEffect onDispose {}
    val window = activity.window
    // ... configure window
    onDispose { }
}
```
