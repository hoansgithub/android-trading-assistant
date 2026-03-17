package co.alcheclub.ai.trading.assistant.modules.root

/**
 * Navigation events emitted by RootActivity.
 * Uses sealed class for type-safe navigation routing.
 */
sealed class RootNavigationEvent {
    data class NavigateTo(val destination: Destination) : RootNavigationEvent()

    enum class Destination {
        LOGIN,
        ONBOARDING,
        MAIN
    }
}
