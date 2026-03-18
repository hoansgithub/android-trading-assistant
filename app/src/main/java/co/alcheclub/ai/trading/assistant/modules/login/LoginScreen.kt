package co.alcheclub.ai.trading.assistant.modules.login

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.core.AppLinks
import co.alcheclub.ai.trading.assistant.domain.model.AuthProvider
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.BgPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextTertiary

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    activity: Activity,
    onAuthenticated: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is LoginState.Authenticated) {
            onAuthenticated()
        }
    }
    if (state is LoginState.Authenticated) return

    LoginScreenContent(
        state = state,
        onGoogleClick = { viewModel.signInWithGoogle(activity) }
    )
}

@Composable
private fun LoginScreenContent(
    state: LoginState = LoginState.Idle,
    onGoogleClick: () -> Unit = {}
) {
    val dimens = AppDimens.current
    val isAuthenticating = state is LoginState.Authenticating
    val authenticatingProvider = (state as? LoginState.Authenticating)?.provider

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimens.spaceLg)
                .padding(bottom = dimens.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Branding section
            BrandingSection()

            Spacer(modifier = Modifier.weight(1f))

            // Auth buttons section
            AuthButtonsSection(
                isAuthenticating = isAuthenticating,
                authenticatingProvider = authenticatingProvider,
                errorMessage = (state as? LoginState.Error)?.message,
                onGoogleClick = onGoogleClick
            )

            Spacer(modifier = Modifier.weight(0.3f))

            // Legal section
            LegalSection()
        }
    }
}

// MARK: - Branding Section

@Composable
private fun BrandingSection() {
    val dimens = AppDimens.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // App icon (large, above name — matching iOS 120x120 with cornerRadius 26)
        Image(
            painter = painterResource(id = R.drawable.ic_splash),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(26.dp))
        )

        Spacer(modifier = Modifier.height(dimens.spaceXl))

        // App name
        Text(
            text = stringResource(R.string.app_name),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(dimens.spaceXxl))

        // Welcome text
        Text(
            text = stringResource(R.string.login_title),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        // Subtitle
        Text(
            text = stringResource(R.string.login_subtitle),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

// MARK: - Auth Buttons Section

@Composable
private fun AuthButtonsSection(
    isAuthenticating: Boolean,
    authenticatingProvider: AuthProvider?,
    errorMessage: String?,
    onGoogleClick: () -> Unit
) {
    val dimens = AppDimens.current

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header text (matching iOS "Please login to get full access from us")
        Text(
            text = stringResource(R.string.login_prompt),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = TextPrimary,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(dimens.spaceLg))

        // Continue with Google
        AuthButton(
            text = stringResource(R.string.login_continue_google),
            iconResId = R.drawable.ic_google,
            isLoading = isAuthenticating && authenticatingProvider == AuthProvider.GOOGLE,
            enabled = !isAuthenticating,
            onClick = onGoogleClick
        )

        // Error message
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(dimens.spaceSm))
            Text(
                text = errorMessage,
                fontFamily = PoppinsFontFamily,
                fontSize = 13.sp,
                color = Color(0xFFF87171),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimens.spaceSm)
            )
        }
    }
}

// MARK: - Auth Button

@Composable
private fun AuthButton(
    text: String,
    iconResId: Int,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.White.copy(alpha = 0.7f),
            disabledContentColor = Color.Black.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (enabled) Color.Black else Color.Black.copy(alpha = 0.5f)
            )
        }
    }
}

// MARK: - Legal Section

@Composable
private fun LegalSection() {
    val uriHandler = LocalUriHandler.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.login_terms_prefix),
            fontFamily = PoppinsFontFamily,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.login_terms_of_service),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.clickable { uriHandler.openUri(AppLinks.TERMS_OF_SERVICE) }
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = stringResource(R.string.login_and),
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = stringResource(R.string.login_privacy_policy),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.clickable { uriHandler.openUri(AppLinks.PRIVACY_POLICY) }
            )
        }
    }
}

// MARK: - Preview

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    co.alcheclub.ai.trading.assistant.ui.theme.AlphaProfitTheme {
        LoginScreenContent()
    }
}
