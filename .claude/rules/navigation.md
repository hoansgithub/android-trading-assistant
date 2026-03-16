# Navigation Rules (Navigation 3)

## Type-Safe Routes
```kotlin
// Routes as @Serializable + NavKey
sealed interface AppRoute : NavKey {
    @Serializable data object Home : AppRoute
    @Serializable data class Profile(val userId: String) : AppRoute
}
```

## Navigation Setup
```kotlin
@Composable
fun AppNavigation(
    backStack: NavBackStack<NavKey> = rememberNavBackStack(AppRoute.Home)
) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<AppRoute.Home> { HomeScreen(onProfileClick = { backStack.add(AppRoute.Profile(it)) }) }
            entry<AppRoute.Profile> { route -> ProfileScreen(userId = route.userId) }
        }
    )
}
```

## Navigation Events - Channel Pattern (REQUIRED)
```kotlin
// FORBIDDEN - State-based navigation
LaunchedEffect(uiState) { if (uiState is Success) navigate() }  // Re-triggers!

// REQUIRED - Channel for one-time events
private val _navigationEvent = Channel<NavigationEvent>(Channel.BUFFERED)
val navigationEvent = _navigationEvent.receiveAsFlow()

// REQUIRED - LaunchedEffect(Unit) for collection
LaunchedEffect(Unit) {
    viewModel.navigationEvent.collect { event ->
        when (event) { is NavigationEvent.GoToNext -> navigateNext() }
    }
}
```

## Activity Navigation
```kotlin
// REQUIRED - Always finish() for forward nav
startActivity(intent)
finish()
```
