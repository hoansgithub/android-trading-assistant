package co.alcheclub.ai.trading.assistant.modules.onboarding.steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

@Composable
fun DisclaimerView(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AppDimens.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimens.spaceXxl)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(dimens.spaceXl))

        // Banner image
        Image(
            painter = painterResource(id = R.drawable.ic_banner_disclaimer),
            contentDescription = "Disclaimer banner",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(dimens.spaceXxl))

        Text(
            text = "Important Disclaimer",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimens.spaceSm))

        Text(
            text = "Please read before continuing",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimens.spaceXxl))

        // Disclaimer text card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BgCard)
                .padding(dimens.spaceLg)
        ) {
            val disclaimerText = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Alpha Profit AI")
                }
                append(" is an AI-powered ")
                withStyle(style = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                    append("informational")
                }
                append(" and ")
                withStyle(style = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                    append("educational")
                }
                append(" assistant designed to support trading and investment decisions.\n\n")
                append("⚠️ ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("It is not a financial advisor and does not guarantee results.")
                }
                append("\n\n")
                append("Users are responsible for their own ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("due diligence")
                }
                append(" and are encouraged to consult ")
                withStyle(style = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                    append("licensed financial professionals")
                }
                append(" before making any investment decisions.")
            }

            Text(
                text = disclaimerText,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(dimens.space2Xl))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Emerald,
                contentColor = Color.Black
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "I Understand & Continue",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(dimens.space2Xl))
    }
}
