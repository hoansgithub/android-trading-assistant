package co.alcheclub.ai.trading.assistant

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.alcheclub.ai.trading.assistant.ui.theme.AlphaProfitTheme
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens

/**
 * MainActivity - Main App Content
 *
 * Hosts the main app navigation including:
 * - Home screen (My Analyses)
 * - Strategy screen
 * - Profile screen
 *
 * Note: Onboarding is handled by OnboardingActivity (separate flow)
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Guard: redirect to RootActivity if onboarding not complete
        // TODO: Check onboarding status from repository
        // if (!onboardingRepository.isOnboardingCompleted()) {
        //     startActivity(Intent(this, RootActivity::class.java))
        //     finish()
        //     return
        // }

        setContent {
            AlphaProfitTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        // TODO: Replace with AppNavigation composable
                        val dimens = AppDimens.current
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(dimens.spaceLg)
                            ) {
                                Text(
                                    text = "Alpha Profit AI",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Home Screen",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
