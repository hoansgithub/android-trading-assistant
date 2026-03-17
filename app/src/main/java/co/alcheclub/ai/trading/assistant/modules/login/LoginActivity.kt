package co.alcheclub.ai.trading.assistant.modules.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import co.alcheclub.ai.trading.assistant.MainActivity
import co.alcheclub.ai.trading.assistant.core.extensions.applyFadeTransition
import co.alcheclub.ai.trading.assistant.di.AppModule
import co.alcheclub.ai.trading.assistant.modules.onboarding.OnboardingActivity
import co.alcheclub.ai.trading.assistant.ui.theme.AlphaProfitTheme

class LoginActivity : AppCompatActivity() {

    private val viewModel by lazy {
        LoginViewModel(AppModule.authRepository)
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
        val isOnboardingComplete = AppModule.onboardingRepository.isOnboardingCompleted()
        val destination = if (isOnboardingComplete) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, OnboardingActivity::class.java)
        }
        startActivity(destination)
        applyFadeTransition()
        finish()
    }
}
