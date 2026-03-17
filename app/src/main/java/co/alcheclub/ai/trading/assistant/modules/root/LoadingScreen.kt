package co.alcheclub.ai.trading.assistant.modules.root

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.Primary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

/**
 * Loading/splash screen shown during app initialization.
 * Matches iOS SplashView: logo + app name + spinner.
 */
@Composable
fun LoadingScreen(
    isLoading: Boolean = true,
    loadingStep: String = ""
) {
    val dimens = AppDimens.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.spaceXl)
        ) {
            // App icon (matching iOS: 120x120, cornerRadius 26)
            Image(
                painter = painterResource(id = R.drawable.ic_splash),
                contentDescription = "Alpha Profit AI",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(26.dp))
            )

            // App name
            Text(
                text = "Alpha Profit AI",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Primary,
                    strokeWidth = 3.dp
                )
            }

            if (loadingStep.isNotEmpty()) {
                Text(
                    text = loadingStep,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
