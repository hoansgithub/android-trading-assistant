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
import co.alcheclub.ai.trading.assistant.di.AppModule
import co.alcheclub.ai.trading.assistant.modules.main.MainScreen
import co.alcheclub.ai.trading.assistant.ui.theme.AlphaProfitTheme
import kotlinx.coroutines.launch

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

    private val homeViewModel by lazy { AppModule.createHomeViewModel() }
    private val strategyViewModel by lazy { AppModule.createStrategyViewModel() }
    private val profileViewModel by lazy { AppModule.createProfileViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AlphaProfitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        homeViewModel = homeViewModel,
                        strategyViewModel = strategyViewModel,
                        profileViewModel = profileViewModel,
                        onLogout = { performLogout() }
                    )
                }
            }
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            AppModule.authRepository.signOut()
            startActivity(Intent(this@MainActivity, RootActivity::class.java))
            applyFadeTransition()
            finish()
        }
    }
}
