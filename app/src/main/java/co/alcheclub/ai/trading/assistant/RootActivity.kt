package co.alcheclub.ai.trading.assistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import co.alcheclub.ai.trading.assistant.core.extensions.applyFadeTransition
import co.alcheclub.ai.trading.assistant.modules.onboarding.OnboardingActivity
import co.alcheclub.ai.trading.assistant.modules.root.LoadingScreen
import co.alcheclub.ai.trading.assistant.modules.root.RootNavigationEvent
import co.alcheclub.ai.trading.assistant.ui.theme.AlphaProfitTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * RootActivity - Entry point for the app
 *
 * This Activity handles:
 * - Splash/loading screen
 * - App initialization (DI, analytics, remote config)
 * - Routing to OnboardingActivity or MainActivity
 *
 * Flow:
 * 1. Show loading screen
 * 2. Initialize app services
 * 3. Check onboarding status:
 *    - If NOT complete → Launch OnboardingActivity
 *    - If complete → Launch MainActivity (Home)
 */
class RootActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AlphaProfitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoadingScreen(
                        isLoading = true,
                        loadingStep = "Initializing..."
                    )
                }
            }
        }

        // Initialize app and navigate
        initializeAndNavigate()
    }

    private fun initializeAndNavigate() {
        lifecycleScope.launch {
            // TODO: Initialize app services (analytics, remote config, etc.)
            // TODO: Check onboarding status from repository
            delay(500) // Brief loading for splash feel

            // TODO: Replace with actual onboarding check
            val isOnboardingComplete = false

            val event = if (isOnboardingComplete) {
                RootNavigationEvent.NavigateTo(RootNavigationEvent.Destination.MAIN)
            } else {
                RootNavigationEvent.NavigateTo(RootNavigationEvent.Destination.ONBOARDING)
            }

            handleNavigation(event)
        }
    }

    private fun handleNavigation(event: RootNavigationEvent.NavigateTo) {
        when (event.destination) {
            RootNavigationEvent.Destination.ONBOARDING -> {
                startActivity(Intent(this, OnboardingActivity::class.java))
                applyFadeTransition()
                finish()
            }
            RootNavigationEvent.Destination.MAIN -> {
                startActivity(Intent(this, MainActivity::class.java))
                applyFadeTransition()
                finish()
            }
        }
    }
}
