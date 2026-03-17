package co.alcheclub.ai.trading.assistant.modules.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import co.alcheclub.ai.trading.assistant.MainActivity
import co.alcheclub.ai.trading.assistant.core.extensions.applyFadeTransition
import co.alcheclub.ai.trading.assistant.di.AppModule
import co.alcheclub.ai.trading.assistant.ui.theme.AlphaProfitTheme

/**
 * OnboardingActivity - First-time user onboarding flow
 *
 * Separate Activity for onboarding because:
 * - One-time flow that doesn't need to be in main navigation stack
 * - Cleaner separation from main app flow
 * - Follows single-responsibility principle
 *
 * Flow (matching iOS):
 * 1. Experience Level
 * 2. Time Availability
 * 3. Risk Comfort
 * 4. Primary Goal
 * 5. Learning Style
 * 6. Rate Us
 * 7. Processing
 * 8. Disclaimer
 * 9. All Set
 *
 * After completion -> Launch MainActivity and finish
 */
class OnboardingActivity : AppCompatActivity() {

    private val viewModel by lazy {
        OnboardingViewModel(
            onboardingRepository = AppModule.onboardingRepository,
            authRepository = AppModule.authRepository,
            analyzeChartUseCase = AppModule.createAnalyzeChartUseCase()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AlphaProfitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showExitDialog by remember { mutableStateOf(false) }

                    BackHandler(enabled = true) { showExitDialog = true }

                    OnboardingScreen(
                        viewModel = viewModel,
                        activity = this@OnboardingActivity,
                        onComplete = { navigateToMain() }
                    )

                    if (showExitDialog) {
                        AlertDialog(
                            onDismissRequest = { showExitDialog = false },
                            title = { Text("Leave Onboarding?") },
                            text = { Text("You can always come back to complete setup later.") },
                            confirmButton = {
                                TextButton(onClick = { finish() }) {
                                    Text("Exit")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showExitDialog = false }) {
                                    Text("Keep Going")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        applyFadeTransition()
        finish()
    }
}
