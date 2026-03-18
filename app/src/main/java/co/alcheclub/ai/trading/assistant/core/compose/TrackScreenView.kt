package co.alcheclub.ai.trading.assistant.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import co.alcheclub.ai.trading.assistant.core.analytics.Analytics

/**
 * Tracks screen view on ON_RESUME lifecycle event.
 *
 * Per Firebase documentation, screen views should be tracked in onResume (Android).
 * This composable provides that behavior for Jetpack Compose.
 *
 * Usage:
 * ```kotlin
 * @Composable
 * fun HomeTab() {
 *     TrackScreenView(
 *         screenName = AnalyticsEvent.Screen.HOME,
 *         screenClass = "HomeTab"
 *     )
 *
 *     // ... rest of your screen content
 * }
 * ```
 *
 * @param screenName The screen name (use AnalyticsEvent.Screen constants)
 * @param screenClass The screen class name (typically the composable function name)
 */
@Composable
fun TrackScreenView(
    screenName: String,
    screenClass: String
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val isPreview = LocalInspectionMode.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !isPreview) {
                Analytics.trackScreenView(screenName, screenClass)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}