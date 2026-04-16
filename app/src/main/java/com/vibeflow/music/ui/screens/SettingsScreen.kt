package com.vibeflow.music.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeflow.music.BuildConfig
import com.vibeflow.music.data.DownloadQualityPref
import com.vibeflow.music.data.ThemePref
import com.vibeflow.music.data.models.AudioQuality
import com.vibeflow.music.update.UpdateUiState

@Composable
fun SettingsScreen(
    currentQuality: AudioQuality,
    onQualityChange: (AudioQuality) -> Unit,
    currentDownloadQuality: DownloadQualityPref,
    onDownloadQualityChange: (DownloadQualityPref) -> Unit,
    currentTheme: ThemePref,
    onThemeChange: (ThemePref) -> Unit,
    autoPlayNext: Boolean,
    onAutoPlayChange: (Boolean) -> Unit,
    rememberLastPlayed: Boolean,
    onRememberLastPlayedChange: (Boolean) -> Unit,
    updateUiState: UpdateUiState,
    onCheckUpdate: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onDismissUpdateDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Settings, null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.size(10.dp))
            Text("Settings", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            // ── Theme ──────────────────────────────────────────────────────────
            SectionLabel("THEME")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeCard(Icons.Default.BrightnessAuto, "System",
                    currentTheme == ThemePref.SYSTEM, { onThemeChange(ThemePref.SYSTEM) }, Modifier.weight(1f))
                ThemeCard(Icons.Default.BrightnessHigh, "Light",
                    currentTheme == ThemePref.LIGHT, { onThemeChange(ThemePref.LIGHT) }, Modifier.weight(1f))
                ThemeCard(Icons.Default.Brightness4, "Dark",
                    currentTheme == ThemePref.DARK, { onThemeChange(ThemePref.DARK) }, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // ── Streaming Quality ──────────────────────────────────────────────
            SectionLabel("JIOSAAVN STREAMING QUALITY")
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    QualityOption("Low", "12 kbps · Saves data", Icons.Default.Speed,
                        currentQuality == AudioQuality.LOW) { onQualityChange(AudioQuality.LOW) }
                    SettingsDivider()
                    QualityOption("Medium", "96 kbps · Balanced", Icons.Default.GraphicEq,
                        currentQuality == AudioQuality.MEDIUM) { onQualityChange(AudioQuality.MEDIUM) }
                    SettingsDivider()
                    QualityOption("High", "320 kbps · Best audio", Icons.Default.HighQuality,
                        currentQuality == AudioQuality.HIGH) { onQualityChange(AudioQuality.HIGH) }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Download Quality ───────────────────────────────────────────────
            SectionLabel("DOWNLOAD QUALITY")
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    QualityOption("Always Ask", "Show picker every time", Icons.Default.HelpOutline,
                        currentDownloadQuality == DownloadQualityPref.ALWAYS_ASK) { onDownloadQualityChange(DownloadQualityPref.ALWAYS_ASK) }
                    SettingsDivider()
                    QualityOption("Low", "12 kbps · Smallest size", Icons.Default.Speed,
                        currentDownloadQuality == DownloadQualityPref.LOW) { onDownloadQualityChange(DownloadQualityPref.LOW) }
                    SettingsDivider()
                    QualityOption("Medium", "96 kbps · Balanced", Icons.Default.GraphicEq,
                        currentDownloadQuality == DownloadQualityPref.MEDIUM) { onDownloadQualityChange(DownloadQualityPref.MEDIUM) }
                    SettingsDivider()
                    QualityOption("High", "320 kbps · Best quality", Icons.Default.HighQuality,
                        currentDownloadQuality == DownloadQualityPref.HIGH) { onDownloadQualityChange(DownloadQualityPref.HIGH) }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f), modifier = Modifier.size(13.dp))
                Spacer(Modifier.size(4.dp))
                Text("Downloads saved to Music/VibeFlow/",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
            }

            Spacer(Modifier.height(24.dp))

            // ── Playback Settings ────────────────────────────────────────────
            SectionLabel("PLAYBACK")
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    ToggleOption(
                        icon = Icons.Default.SkipNext,
                        title = "Auto Play Next",
                        description = "Automatically play next song when current ends",
                        checked = autoPlayNext,
                        onCheckedChange = onAutoPlayChange
                    )
                    SettingsDivider()
                    ToggleOption(
                        icon = Icons.Default.History,
                        title = "Remember Last Played",
                        description = "Resume last played song on app restart",
                        checked = rememberLastPlayed,
                        onCheckedChange = onRememberLastPlayedChange
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── App Update ─────────────────────────────────────────────────────
            SectionLabel("APP UPDATE")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Current version info row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(0.12f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SystemUpdate, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("VibeFlow",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    SettingsDivider()
                    Spacer(Modifier.height(14.dp))

                    // Check Update button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                enabled = updateUiState !is UpdateUiState.Checking,
                                onClick = onCheckUpdate
                            ),
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = if (updateUiState is UpdateUiState.Checking) 0.6f else 1f
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (updateUiState is UpdateUiState.Checking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.size(10.dp))
                                Text("Checking...", color = Color.White, fontWeight = FontWeight.SemiBold)
                            } else {
                                Icon(Icons.Default.Refresh, null,
                                    tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text("Check for Update",
                                    color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Status message below button
                    when (updateUiState) {
                        is UpdateUiState.UpToDate -> {
                            Spacer(Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CheckCircle, null,
                                    tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.size(6.dp))
                                Text("App is already at the latest version",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4CAF50))
                            }
                        }
                        is UpdateUiState.Error -> {
                            Spacer(Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Info, null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp))
                                Spacer(Modifier.size(6.dp))
                                Text(updateUiState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error)
                            }
                        }
                        else -> {}
                    }
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }

    // ── Update Available Dialog ────────────────────────────────────────────────
    if (updateUiState is UpdateUiState.UpdateAvailable) {
        val info = updateUiState.info
        AlertDialog(
            onDismissRequest = onDismissUpdateDialog,
            icon = {
                Icon(Icons.Default.SystemUpdate, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Update Available 🎉",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("v${BuildConfig.VERSION_NAME}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("→", fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterVertically))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("New", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("v${info.latestVersionName}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (info.releaseNotes.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text("What's new:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text(info.releaseNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("The APK will be downloaded in the background. You'll be notified when it's ready to install.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDownloadUpdate()
                        onDismissUpdateDialog()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Update Now")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissUpdateDialog) {
                    Text("Later")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
private fun ThemeCard(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(0.3f),
        tween(250), label = "border")
    val bgColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary.copy(0.12f) else MaterialTheme.colorScheme.surfaceVariant,
        tween(250), label = "bg")
    val iconColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        tween(250), label = "icon")
    Box(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(bgColor)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp, modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp))
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(0.3f))
}

@Composable
fun ToggleOption(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (checked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
            )
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.size(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun QualityOption(title: String, description: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(selected = selected, onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
    }
}
