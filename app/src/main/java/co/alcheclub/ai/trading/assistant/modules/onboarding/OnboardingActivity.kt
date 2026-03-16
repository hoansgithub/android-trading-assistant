package co.alcheclub.ai.trading.assistant.modules.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.alcheclub.ai.trading.assistant.MainActivity
import co.alcheclub.ai.trading.assistant.core.extensions.applyFadeTransition
import co.alcheclub.ai.trading.assistant.ui.theme.AlphaProfitTheme
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens

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
 * After completion → Launch MainActivity and finish
 */
class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AlphaProfitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showExitDialog by remember { mutableStateOf(false) }

                    BackHandler(enabled = true) { showExitDialog = true }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        // TODO: Replace with OnboardingScreen composable
                        val dimens = AppDimens.current
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimens.spaceLg)
                        ) {
                            Text(
                                text = "Onboarding",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Survey flow coming soon",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // TODO: Add exit dialog
                }
            }
        }
    }

    private fun completeOnboardingAndNavigate() {
        // TODO: Mark onboarding as complete via use case
        // TODO: Track analytics event
        navigateToMain()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        applyFadeTransition()
        finish()
    }
}
