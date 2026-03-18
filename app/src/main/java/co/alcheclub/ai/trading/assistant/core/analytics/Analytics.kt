package co.alcheclub.ai.trading.assistant.core.analytics

import co.alcheclub.lib.acccore.analytics.AnalyticsCoordinator
import co.alcheclub.lib.acccore.di.ACCDI

/**
 * Analytics Helper
 *
 * Simple wrapper around ACCCore's AnalyticsCoordinator.
 * Automatically broadcasts to all registered analytics platforms (Firebase, Facebook, etc.).
 *
 * Usage:
 * ```kotlin
 * // Track event with parameters
 * Analytics.track(
 *     name = AnalyticsEvent.ANALYSIS_COMPLETE,
 *     params = mapOf(
 *         AnalyticsEvent.Param.ASSET to "BTC",
 *         AnalyticsEvent.Param.SIGNAL to "buy"
 *     )
 * )
 *
 * // Track event without parameters
 * Analytics.track(AnalyticsEvent.AUTH_SIGN_OUT)
 *
 * // Track with sanitized user input
 * Analytics.track(
 *     name = AnalyticsEvent.AUTH_ERROR,
 *     params = mapOf(
 *         AnalyticsEvent.Param.ERROR to Analytics.sanitize(errorMessage)
 *     )
 * )
 * ```
 */
object Analytics {

    /**
     * Sanitize a string for analytics parameters.
     *
     * - Removes special characters (keeps alphanumeric, spaces, hyphens, underscores)
     * - Truncates to maxLength characters
     *
     * @param value The string to sanitize
     * @param maxLength Maximum length (default 100)
     * @return Sanitized string safe for analytics
     */
    fun sanitize(value: String, maxLength: Int = 100): String {
        return value
            .replace(Regex("[^a-zA-Z0-9\\s\\-_]"), "")
            .take(maxLength)
            .trim()
    }

    /**
     * Track an analytics event.
     *
     * Broadcasts to all registered analytics platforms via ACCCore's AnalyticsCoordinator.
     *
     * @param name Event name (use AnalyticsEvent constants)
     * @param params Event parameters (use AnalyticsEvent.Param constants for keys)
     */
    fun track(name: String, params: Map<String, Any>? = null) {
        val coordinator = ACCDI.get<AnalyticsCoordinator>()
        coordinator.track(name, params)
    }

    /**
     * Track a screen view event.
     *
     * Calls AnalyticsCoordinator.trackScreenView() which broadcasts to all
     * registered analytics platforms.
     *
     * @param screenName The screen name (use AnalyticsEvent.Screen constants)
     * @param screenClass The screen class name (e.g., "HomeTab", "LoginScreen")
     */
    fun trackScreenView(screenName: String, screenClass: String) {
        val coordinator = ACCDI.get<AnalyticsCoordinator>()
        coordinator.trackScreenView(screenName, screenClass)
    }
}