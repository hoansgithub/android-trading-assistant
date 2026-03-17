package co.alcheclub.ai.trading.assistant.modules.main

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.BuildConfig
import co.alcheclub.ai.trading.assistant.core.AppLinks
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.BgCard
import co.alcheclub.ai.trading.assistant.ui.theme.Caution
import co.alcheclub.ai.trading.assistant.ui.theme.Danger
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextMuted
import co.alcheclub.ai.trading.assistant.ui.theme.TextPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary
import co.alcheclub.ai.trading.assistant.ui.theme.TextTertiary

@Composable
fun ProfileTab(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AppDimens.current
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val showSignOutDialog by viewModel.showSignOutDialog.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val isDeleting by viewModel.isDeleting.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onViewAppear() }

    // Sign Out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSignOut() },
            title = { Text("Sign Out", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold) },
            text = { Text("Are you sure you want to sign out?", fontFamily = PoppinsFontFamily) },
            confirmButton = {
                TextButton(onClick = { viewModel.signOut(onLogout) }) {
                    Text("Sign Out", color = Danger, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSignOut() }) {
                    Text("Cancel", fontFamily = PoppinsFontFamily)
                }
            }
        )
    }

    // Delete Account confirmation dialog
    if (showDeleteDialog) {
        var deleteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteAccount() },
            title = { Text("Delete Account", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text(
                        "This will permanently delete your account and all associated data. This action cannot be undone.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = deleteText,
                        onValueChange = { deleteText = it },
                        placeholder = { Text("Type \"delete\" to confirm") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteAccount(onLogout) },
                    enabled = deleteText.lowercase() == "delete"
                ) {
                    Text("Delete", color = if (deleteText.lowercase() == "delete") Danger else TextMuted,
                        fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteAccount() }) {
                    Text("Cancel", fontFamily = PoppinsFontFamily)
                }
            }
        )
    }

    // Message dialog
    if (message != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissMessage() },
            title = { Text("Notice", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold) },
            text = { Text(message ?: "", fontFamily = PoppinsFontFamily) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissMessage() }) {
                    Text("OK", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.spaceLg)
                .padding(top = dimens.space3Xl, bottom = dimens.space2Xl)
        ) {
            // Profile Header
            ProfileHeader(userProfile = userProfile)

            Spacer(Modifier.height(dimens.spaceLg))

            // Preferences Section
            ProfileSection(title = "PREFERENCES") {
                ProfileRow(
                    icon = Icons.Default.CreditCard,
                    iconColor = Emerald,
                    title = "Restore Purchases",
                    onClick = { viewModel.restorePurchases() }
                )
            }

            Spacer(Modifier.height(dimens.spaceLg))

            // About Section
            ProfileSection(title = "ABOUT") {
                ProfileRow(
                    icon = Icons.Default.Description,
                    iconColor = TextSecondary,
                    title = "Terms of Service",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppLinks.TERMS_OF_SERVICE)))
                    }
                )
                ProfileRow(
                    icon = Icons.Default.PrivacyTip,
                    iconColor = TextSecondary,
                    title = "Privacy Policy",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppLinks.PRIVACY_POLICY)))
                    }
                )
                ProfileRow(
                    icon = Icons.Default.Star,
                    iconColor = Caution,
                    title = "Rate the App",
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=${context.packageName}")))
                        } catch (_: Exception) {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                        }
                    }
                )
                ProfileRow(
                    icon = Icons.Default.Email,
                    iconColor = TextSecondary,
                    title = "Send Feedback",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@alcheclub.co"))
                            putExtra(Intent.EXTRA_SUBJECT, "Alpha Profit AI Android - Feedback (v${BuildConfig.VERSION_NAME})")
                        }
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )
            }

            Spacer(Modifier.height(dimens.spaceLg))

            // Danger Zone - Sign Out
            DangerRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Sign Out",
                color = TextPrimary,
                onClick = { viewModel.requestSignOut() }
            )

            Spacer(Modifier.height(dimens.spaceMd))

            // Danger Zone - Delete Account
            DangerRow(
                icon = Icons.Default.Delete,
                title = "Delete Account",
                color = TextTertiary,
                onClick = { viewModel.requestDeleteAccount() }
            )

            Spacer(Modifier.height(dimens.spaceXxl))

            // App Version Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Alpha Profit AI",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Loading overlay
        if (isProcessing || isDeleting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Emerald)
            }
        }
    }
}

@Composable
private fun ProfileHeader(userProfile: UserProfile) {
    val dimens = AppDimens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(vertical = 20.dp, horizontal = dimens.spaceLg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (userProfile.displayName != null) {
            Text(
                text = userProfile.displayName,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
        }
        Text(
            text = userProfile.email,
            fontFamily = PoppinsFontFamily,
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        if (userProfile.memberSince.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Member since ${userProfile.memberSince}",
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BgCard)
        ) {
            content()
        }
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = iconColor
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontSize = 15.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = TextMuted
        )
    }
}

@Composable
private fun DangerRow(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = color
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontSize = 15.sp,
            color = color
        )
    }
}
