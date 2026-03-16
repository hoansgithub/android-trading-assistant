package co.alcheclub.ai.trading.assistant.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import co.alcheclub.ai.trading.assistant.R

/**
 * Poppins font family - matching the iOS app.
 */
val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

/**
 * Create scalable Typography based on current dimensions.
 * Matches iOS font hierarchy: largeTitle(28), title(24), title2(20),
 * title3(17), body(16), callout(15), footnote(13), caption(12), caption2(11).
 */
@Composable
fun scalableTypography(): Typography {
    val dimens = AppDimens.current

    return Typography(
        // Display styles
        displayLarge = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = dimens.font4Xl // 32sp scaled
        ),
        displayMedium = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = dimens.font3Xl // 28sp scaled (iOS largeTitle)
        ),
        displaySmall = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = dimens.font2Xl // 24sp scaled (iOS title)
        ),

        // Headline styles
        headlineLarge = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = dimens.font3Xl // 28sp scaled
        ),
        headlineMedium = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = dimens.font2Xl // 24sp scaled
        ),
        headlineSmall = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = dimens.fontXxl // 20sp scaled (iOS title2)
        ),

        // Title styles
        titleLarge = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = dimens.fontXl // 18sp scaled
        ),
        titleMedium = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = dimens.fontLg // 16sp scaled (iOS body)
        ),
        titleSmall = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = dimens.fontMd // 14sp scaled
        ),

        // Body styles
        bodyLarge = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = dimens.fontLg // 16sp scaled (iOS body)
        ),
        bodyMedium = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = dimens.fontMd // 14sp scaled
        ),
        bodySmall = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = dimens.fontSm // 12sp scaled (iOS caption)
        ),

        // Label styles
        labelLarge = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = dimens.fontMd // 14sp scaled
        ),
        labelMedium = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = dimens.fontSm // 12sp scaled
        ),
        labelSmall = TextStyle(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = dimens.fontXs // 10sp scaled
        )
    )
}
