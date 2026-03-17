package co.alcheclub.ai.trading.assistant.modules.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import co.alcheclub.ai.trading.assistant.MainActivity
import co.alcheclub.ai.trading.assistant.core.extensions.applyFadeTransition
import co.alcheclub.ai.trading.assistant.core.viewModelFactory
import co.alcheclub.ai.trading.assistant.di.AppModule
import co.alcheclub.ai.trading.assistant.modules.onboarding.OnboardingActivity
import co.alcheclub.ai.trading.assistant.ui.theme.AlphaProfitTheme
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels {
        viewModelFactory { LoginViewModel(AppModule.authRepository) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AlphaProfitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        viewModel = viewModel,
                        activity = this@LoginActivity,
                        onAuthenticated = { navigateAfterLogin() }
                    )
                }
            }
        }
    }

    private fun navigateAfterLogin() {
        lifecycleScope.launch {
            // Sync onboarding status from Supabase before routing
            // (local prefs were cleared on sign-out)
            val userId = AppModule.authRepository.getCurrentUserId()
            AppModule.onboardingRepository.syncOnboardingStatus(userId)

            val isOnboardingComplete = AppModule.onboardingRepository.isOnboardingCompleted()
            val destination = if (isOnboardingComplete) {
                Intent(this@LoginActivity, MainActivity::class.java)
            } else {
                Intent(this@LoginActivity, OnboardingActivity::class.java)
            }
            startActivity(destination)
            applyFadeTransition()
            finish()
        }
    }
}
