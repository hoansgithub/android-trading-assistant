package co.alcheclub.ai.trading.assistant.modules.main

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.alcheclub.ai.trading.assistant.domain.model.Analysis
import co.alcheclub.ai.trading.assistant.domain.model.RiskLevel
import co.alcheclub.ai.trading.assistant.domain.model.TradingSignal
import co.alcheclub.ai.trading.assistant.modules.main.components.AssetIconView
import co.alcheclub.ai.trading.assistant.modules.main.components.ChartImageGuideOverlay
import co.alcheclub.ai.trading.assistant.modules.main.components.isChartGuideDisabled
import co.alcheclub.ai.trading.assistant.modules.onboarding.steps.AnalyzingChartView
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.Bearish
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.BgElevated
import co.alcheclub.ai.trading.assistant.ui.theme.BgPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.Black60
import co.alcheclub.ai.trading.assistant.ui.theme.Caution
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.EmeraldDark
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextMuted
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary
import co.alcheclub.ai.trading.assistant.ui.theme.Warning
import java.io.File
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analyzingProgress by viewModel.analyzingProgress.collectAsStateWithLifecycle()
    val analysisError by viewModel.analysisError.collectAsStateWithLifecycle()
    val dimens = AppDimens.current
    val context = LocalContext.current

    var showMenu by remember { mutableStateOf(false) }
    var showChartGuide by remember { mutableStateOf(false) }
    var selectedAnalysis by remember { mutableStateOf<Analysis?>(null) }
    var analysisToDelete by remember { mutableStateOf<Analysis?>(null) }

    LaunchedEffect(Unit) { viewModel.onViewAppear() }

    // Camera setup
    val cameraImageFile = remember(context) {
        File(context.cacheDir, "camera").apply { mkdirs() }.resolve("home_capture.jpg")
    }
    val cameraImageUri = remember(cameraImageFile) {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cameraImageFile)
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val bytes = context.contentResolver.openInputStream(cameraImageUri)?.use { it.readBytes() }
            if (bytes != null && bytes.isNotEmpty()) viewModel.onImageCaptured(bytes)
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(cameraImageUri)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null && bytes.isNotEmpty()) viewModel.onImageCaptured(bytes)
        }
    }

    // Analyzing overlay
    if (isAnalyzing) {
        Box(Modifier.fillMaxSize().background(BgPrimary)) {
            AnalyzingChartView(progress = analyzingProgress)
        }
        return
    }

    // Error screen
    if (analysisError != null) {
        val parts = (analysisError ?: "").split("\n", limit = 2)
        Box(Modifier.fillMaxSize().background(BgPrimary), contentAlignment = Alignment.Center) {
            Column(Modifier.padding(dimens.spaceXxl), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), tint = Warning)
                Spacer(Modifier.height(dimens.spaceXxl))
                Text(parts.getOrElse(0) { "Analysis Failed" }, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(dimens.spaceMd))
                Text(parts.getOrElse(1) { "" }, fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 20.sp)
                Spacer(Modifier.height(dimens.space2Xl))
                Button(
                    onClick = { viewModel.dismissAnalysisError() },
                    modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.Black)
                ) { Text("OK", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
            }
        }
        return
    }

    // Detail screen
    if (selectedAnalysis != null) {
        AnalysisDetailScreen(
            analysis = selectedAnalysis!!,
            onBack = { selectedAnalysis = null },
            onDelete = {
                viewModel.deleteAnalysis(selectedAnalysis!!.id)
                selectedAnalysis = null
            }
        )
        return
    }

    // Inline delete confirmation
    if (analysisToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { analysisToDelete = null },
            title = { Text("Delete Analysis", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold) },
            text = { Text("Are you sure you want to delete this analysis? This action cannot be undone.", fontFamily = PoppinsFontFamily) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAnalysis(analysisToDelete!!.id); analysisToDelete = null }) {
                    Text("Delete", color = co.alcheclub.ai.trading.assistant.ui.theme.Danger, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { analysisToDelete = null }) {
                    Text("Cancel", fontFamily = PoppinsFontFamily)
                }
            }
        )
    }

    // Main content
    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Emerald, modifier = Modifier.size(40.dp))
                    }
                }
                is HomeUiState.Empty -> EmptyAnalysesView()
                is HomeUiState.Loaded -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = dimens.spaceLg, vertical = dimens.spaceMd),
                        verticalArrangement = Arrangement.spacedBy(dimens.spaceMd)
                    ) {
                        items(state.analyses, key = { it.id }) { analysis ->
                            AnalysisCard(
                                analysis = analysis,
                                onClick = { selectedAnalysis = analysis },
                                onDelete = { analysisToDelete = analysis }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
                is HomeUiState.Error -> {
                    Column(
                        Modifier.fillMaxSize().padding(dimens.spaceXxl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(state.message, fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(dimens.spaceLg))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.Black)
                        ) { Text("Retry", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Emerald, EmeraldDark)))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (!isChartGuideDisabled(context)) {
                        showChartGuide = true
                    } else {
                        showMenu = !showMenu
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, "New Analysis", Modifier.size(28.dp), tint = Color.White)
        }

        // Dark overlay behind menu
        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                Modifier.fillMaxSize().background(Black60).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showMenu = false }
            )
        }

        // Camera/Gallery popup menu
        AnimatedVisibility(
            visible = showMenu,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 84.dp),
            enter = scaleIn(tween(200), transformOrigin = TransformOrigin(1f, 1f)) + fadeIn(tween(200)),
            exit = scaleOut(tween(150), transformOrigin = TransformOrigin(1f, 1f)) + fadeOut(tween(150))
        ) {
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgElevated)
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        showMenu = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(cameraImageUri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CameraAlt, null, Modifier.size(22.dp), tint = Emerald)
                    Spacer(Modifier.width(12.dp))
                    Text("Take Photo", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        showMenu = false
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PhotoLibrary, null, Modifier.size(22.dp), tint = Emerald)
                    Spacer(Modifier.width(12.dp))
                    Text("Choose from Gallery", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
                }
            }
        }

        // Chart image guide overlay
        ChartImageGuideOverlay(
            isVisible = showChartGuide,
            onDismiss = { showChartGuide = false; showMenu = true },
            onDontShowAgain = { showChartGuide = false; showMenu = true }
        )
    }
}

@Composable
private fun EmptyAnalysesView() {
    val dimens = AppDimens.current
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = dimens.spaceXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Search, null, Modifier.size(80.dp), tint = TextSecondary.copy(alpha = 0.5f))
        Spacer(Modifier.height(dimens.spaceXxl))
        Text("No Analyses Yet", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(dimens.spaceSm))
        Text("Capture a trading chart to get\nAI-powered analysis", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 20.sp)
    }
}

@Composable
private fun AnalysisCard(
    analysis: Analysis,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val dimens = AppDimens.current
    val signalColor = when (analysis.signal) {
        TradingSignal.BULLISH -> Emerald
        TradingSignal.BEARISH -> Bearish
        else -> TextSecondary
    }
    val riskColor = when (analysis.riskAssessment.level) {
        RiskLevel.LOW -> Emerald
        RiskLevel.MODERATE -> Caution
        RiskLevel.HIGH -> Warning
        RiskLevel.VERY_HIGH -> Bearish
    }

    Box {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BgCard)
            .clickable(onClick = onClick).padding(dimens.spaceLg)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AssetIconView(symbol = analysis.assetSymbol, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(analysis.assetSymbol, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (analysis.strategyName != null) {
                    Text(analysis.strategyName, fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                }
            }
            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(signalColor.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text(analysis.signal.value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = signalColor)
            }
        }
        Spacer(Modifier.height(dimens.spaceMd))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricItem("Confidence", "${analysis.confidenceScore}%")
            MetricItem("Price", formatPrice(analysis.currentPrice))
            MetricItem("Timeframe", analysis.timeframe.uppercase())
            MetricItem("Risk", analysis.riskAssessment.level.name.replace("_", " "), riskColor)
        }
        Spacer(Modifier.height(dimens.spaceSm))
        Text(relativeTime(analysis.analyzedAt), fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
    }

    // Three-dot delete menu (bottom-right overlay)
    Box(Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
        var showCardMenu by remember { mutableStateOf(false) }
        IconButton(onClick = { showCardMenu = true }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.MoreVert, "Menu", Modifier.size(18.dp), tint = TextMuted)
        }
        DropdownMenu(expanded = showCardMenu, onDismissRequest = { showCardMenu = false }) {
            DropdownMenuItem(
                text = { Text("Delete", fontFamily = PoppinsFontFamily, color = co.alcheclub.ai.trading.assistant.ui.theme.Danger) },
                onClick = { showCardMenu = false; onDelete() },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = co.alcheclub.ai.trading.assistant.ui.theme.Danger, modifier = Modifier.size(18.dp)) }
            )
        }
    }
    } // Close Box wrapper
}

@Composable
private fun MetricItem(label: String, value: String, valueColor: Color = TextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextSecondary)
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = valueColor, maxLines = 1)
    }
}

private fun formatPrice(price: java.math.BigDecimal): String {
    val d = price.toDouble()
    return when {
        d >= 1000 -> "%,.0f".format(d)
        d >= 1 -> "%,.2f".format(d)
        d >= 0.01 -> "%.4f".format(d)
        else -> "%.6f".format(d)
    }
}

private fun relativeTime(date: java.util.Date): String {
    val diffMs = System.currentTimeMillis() - date.time
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val days = TimeUnit.MILLISECONDS.toDays(diffMs)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "${days / 7}w ago"
    }
}
