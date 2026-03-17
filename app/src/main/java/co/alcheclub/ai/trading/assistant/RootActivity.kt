package co.alcheclub.ai.trading.assistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import co.alcheclub.ai.trading.assistant.core.extensions.applyFadeTransition
import co.alcheclub.ai.trading.assistant.di.AppModule
import co.alcheclub.ai.trading.assistant.modules.login.LoginActivity
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
 * - Routing to LoginActivity, OnboardingActivity, or MainActivity
 *
 * Flow:
 * 1. Show loading screen
 * 2. Initialize app services
 * 3. Check auth + onboarding status:
 *    - If NOT authenticated → Launch LoginActivity
 *    - If authenticated but onboarding NOT complete → Launch OnboardingActivity
 *    - If both complete → Launch MainActivity (Home)
 */
class RootActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate()
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }

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
            delay(500) // Brief loading for splash feel

            val authRepository = AppModule.authRepository
            val onboardingRepository = AppModule.onboardingRepository

            // If authenticated, sync onboarding status from Supabase
            // (handles re-login after logout/reinstall where local prefs were cleared)
            if (authRepository.isAuthenticated()) {
                val userId = authRepository.getCurrentUserId()
                onboardingRepository.syncOnboardingStatus(userId)
            }

            val destination = when {
                !authRepository.isAuthenticated() ->
                    RootNavigationEvent.Destination.LOGIN
                !onboardingRepository.isOnboardingCompleted() ->
                    RootNavigationEvent.Destination.ONBOARDING
                else ->
                    RootNavigationEvent.Destination.MAIN
            }

            handleNavigation(RootNavigationEvent.NavigateTo(destination))
        }
    }

    private fun handleNavigation(event: RootNavigationEvent.NavigateTo) {
        when (event.destination) {
            RootNavigationEvent.Destination.LOGIN -> {
                startActivity(Intent(this, LoginActivity::class.java))
                applyFadeTransition()
                finish()
            }
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
