package co.alcheclub.ai.trading.assistant.modules.onboarding.steps

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.domain.model.MockStrategy
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.BgElevated
import co.alcheclub.ai.trading.assistant.ui.theme.Black60
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

@Composable
fun AllSetView(
    strategy: MockStrategy?,
    onImageCaptured: (ByteArray) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AppDimens.current
    val context = LocalContext.current
    val displayStrategy = strategy ?: MockStrategy(
        name = "Steady Grower",
        style = "Swing Trading",
        timeframe = "4H - Daily",
        riskPerTrade = "1-2%",
        direction = "Long Bias"
    )

    var showMenu by remember { mutableStateOf(false) }

    // Stable file path for camera capture — survives activity recreation.
    // Uses fixed filename since only one capture is active at a time.
    val cameraImageFile = remember(context) {
        File(context.cacheDir, "camera").apply { mkdirs() }
            .resolve("onboarding_capture.jpg")
    }
    val cameraImageUri = remember(cameraImageFile) {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cameraImageFile)
    }

    // Camera launcher — TakePicture saves full-res image to URI, then we read it
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            try {
                val bytes = context.contentResolver.openInputStream(cameraImageUri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    onImageCaptured(bytes)
                }
            } catch (e: Exception) {
                android.util.Log.e("AllSetView", "Failed to read camera image", e)
            }
        }
    }

    // Camera permission launcher — requests CAMERA permission then launches camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(cameraImageUri)
        }
    }

    // Gallery launcher — reads Uri content to ByteArray
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    onImageCaptured(bytes)
                }
            } catch (e: Exception) {
                android.util.Log.e("AllSetView", "Failed to read gallery image", e)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimens.spaceXxl)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(dimens.space2Xl))

            // All set image
            Image(
                painter = painterResource(id = R.drawable.ic_all_set),
                contentDescription = "All set",
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(dimens.spaceXxl))

            Text(
                text = "You're all set!",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimens.spaceSm))

            Text(
                text = "We've created the \"${displayStrategy.name}\" strategy for you.",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimens.spaceXxl))

            // Strategy summary card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .padding(dimens.spaceLg),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceMd)
            ) {
                StrategyRow(label = "Strategy", value = displayStrategy.name)
                StrategyRow(label = "Style", value = displayStrategy.style)
                StrategyRow(label = "Timeframe", value = displayStrategy.timeframe)
                StrategyRow(label = "Risk/Trade", value = displayStrategy.riskPerTrade)
                StrategyRow(label = "Direction", value = displayStrategy.direction)
            }

            Spacer(modifier = Modifier.height(dimens.spaceLg))

            // Footer text (matching iOS)
            Row {
                Text(
                    text = "You can change this anytime in the ",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Strategy tab.",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Button at bottom
            Button(
                onClick = { showMenu = !showMenu },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Start Your First Analysis",
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

            Spacer(modifier = Modifier.height(dimens.space2Xl))
        }

        // Layer 2: Dark overlay (behind menu, above content)
        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black60)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showMenu = false }
            )
        }

        // Layer 3: Popup menu (on top of everything)
        AnimatedVisibility(
            visible = showMenu,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    horizontal = dimens.spaceXxl,
                    vertical = dimens.space2Xl + 56.dp + dimens.spaceSm
                ),
            enter = scaleIn(
                animationSpec = tween(200),
                transformOrigin = TransformOrigin(0.5f, 1f)
            ) + fadeIn(tween(200)),
            exit = scaleOut(
                animationSpec = tween(150),
                transformOrigin = TransformOrigin(0.5f, 1f)
            ) + fadeOut(tween(150))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgElevated)
                    .padding(vertical = 4.dp)
            ) {
                // Take Photo option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMenu = false
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                cameraLauncher.launch(cameraImageUri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Emerald
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Take Photo",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }

                // Choose from Gallery option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMenu = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Emerald
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Choose from Gallery",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun StrategyRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Emerald
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}
