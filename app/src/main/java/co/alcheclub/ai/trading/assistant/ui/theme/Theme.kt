package co.alcheclub.ai.trading.assistant.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark color scheme - the only theme for this app.
 * Matches iOS Alpha Profit AI color palette.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.Black,
    primaryContainer = PrimaryDark,
    secondary = Secondary,
    background = BgPrimary,          // 0xFF0B0F0E
    onBackground = TextPrimary,
    surface = BgSecondary,           // 0xFF111C27
    onSurface = TextPrimary,
    surfaceVariant = BgCard,         // 0xFF162230
    onSurfaceVariant = TextSecondary,
    error = Error
)

/**
 * App theme with:
 * - Dark mode only (matching iOS)
 * - Poppins font family
 * - Scalable typography based on screen size
 * - Emerald green (#2EDBA3) primary accent
 *
 * Note: Edge-to-edge display is handled by enableEdgeToEdge() in Activities.
 */
@Composable
fun AlphaProfitTheme(
    content: @Composable () -> Unit
) {
    // Configure system bar icons for dark theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(view) {
            val activity = view.context as? Activity
            if (activity != null) {
                val insetsController = WindowCompat.getInsetsController(activity.window, view)
                // Light icons on dark background
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
            onDispose { }
        }
    }

    // Provide scalable dimensions
    ProvideDimens {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = scalableTypography(),
            content = content
        )
    }
}
