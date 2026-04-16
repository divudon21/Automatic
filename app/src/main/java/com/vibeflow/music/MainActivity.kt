package com.vibeflow.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vibeflow.music.data.ThemePref
import com.vibeflow.music.ui.components.MiniPlayer
import com.vibeflow.music.ui.components.UpdateDialog
import com.vibeflow.music.update.UpdateInfo
import com.vibeflow.music.ui.screens.FullScreenPlayer
import com.vibeflow.music.ui.screens.HomeScreen
import com.vibeflow.music.ui.screens.SettingsScreen
import com.vibeflow.music.ui.theme.VibeFlowTheme
import com.vibeflow.music.update.UpdateUiState
import com.vibeflow.music.viewmodel.MusicViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { VibeFlowTheme { VibeFlowApp() } }
    }
}

@Composable
fun VibeFlowApp() {
    val viewModel: MusicViewModel = viewModel()

    val playerState       by viewModel.playerState.collectAsState()
    val playbackUiState   by viewModel.playbackState.collectAsState()
    val saavnHome         by viewModel.saavnHome.collectAsState()
    val ytHome            by viewModel.ytHome.collectAsState()
    val amHome            by viewModel.amHome.collectAsState()
    val searchState       by viewModel.searchState.collectAsState()
    val activeSource      by viewModel.activeSource.collectAsState()
    val currentPosition   by viewModel.currentPosition.collectAsState()
    val duration          by viewModel.duration.collectAsState()
    val audioQuality      by viewModel.audioQuality.collectAsState()
    val downloadQuality   by viewModel.downloadQuality.collectAsState()
    val themePref         by viewModel.themePref.collectAsState()
    val autoPlayNext      by viewModel.autoPlayNext.collectAsState()
    val rememberLast      by viewModel.rememberLastPlayed.collectAsState()
    val updateUiState     by viewModel.updateUiState.collectAsState()

    var selectedTab    by remember { mutableStateOf(0) }
    var showFullPlayer by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isDownloading  by remember { mutableStateOf(false) }

    // Auto-check update on launch (after 2s)
    LaunchedEffect(Unit) {
        delay(2000)
        viewModel.checkForUpdate()
    }

    // Show update dialog when update is available
    LaunchedEffect(updateUiState) {
        if (updateUiState is UpdateUiState.UpdateAvailable) {
            showUpdateDialog = true
        }
    }

    // ── Update Dialog (auto-launch) ──────────────────────────────────────────
    if (showUpdateDialog && updateUiState is UpdateUiState.UpdateAvailable) {
        val info = (updateUiState as UpdateUiState.UpdateAvailable).info
        UpdateDialog(
            info = info,
            isDownloading = isDownloading,
            onDownload = {
                isDownloading = true
                viewModel.downloadUpdate()
                isDownloading = false
                showUpdateDialog = false
            },
            onDismiss = {
                showUpdateDialog = false
                viewModel.dismissUpdateDialog()
            }
        )
    }

    // ── Main UI ──────────────────────────────────────────────────────────────
    AnimatedContent(
        targetState = showFullPlayer,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label = "fullPlayer"
    ) { showFull ->
        if (showFull && playerState.currentTrack != null) {
            FullScreenPlayer(
                playerState = playerState,
                playbackUiState = playbackUiState,
                currentPosition = currentPosition,
                duration = duration,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.skipNext() },
                onSkipPrevious = { viewModel.skipPrevious() },
                onSeekTo = { viewModel.seekTo(it) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onCycleRepeat = { viewModel.cycleRepeatMode() },
                onCollapse = { showFullPlayer = false },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    Column {
                        MiniPlayer(
                            playerState = playerState,
                            playbackUiState = playbackUiState,
                            currentPosition = currentPosition,
                            duration = duration,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onSkipNext = { viewModel.skipNext() },
                            onExpandPlayer = { showFullPlayer = true }
                        )
                        VibeFlowBottomNav(selectedTab) { selectedTab = it }
                    }
                },
                containerColor = Color.Transparent
            ) { innerPadding ->
                Box(Modifier.fillMaxSize().padding(innerPadding)) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            activeSource = activeSource,
                            saavnHome = saavnHome,
                            ytHome = ytHome,
                            amHome = amHome,
                            searchState = searchState,
                            currentTrackId = playerState.currentTrack?.id,
                            isPlaying = playerState.isPlaying,
                            defaultDownloadQuality = downloadQuality,
                            onSourceChange = { viewModel.setActiveSource(it) },
                            onTrackClick = { track, playlist -> viewModel.playTrack(track, playlist) },
                            onDownload = { track, quality -> viewModel.downloadTrackNow(track, quality) },
                            onSearch = { viewModel.search(it) },
                            onClearSearch = { viewModel.clearSearch() },
                            onRefreshSaavn = { viewModel.loadSaavnHome() },
                            onRefreshYt = { viewModel.loadYtHome() },
                            onRefreshAm = { viewModel.loadAmHome() }
                        )
                        1 -> SettingsScreen(
                            currentQuality = audioQuality,
                            onQualityChange = { viewModel.setAudioQuality(it) },
                            currentDownloadQuality = downloadQuality,
                            onDownloadQualityChange = { viewModel.setDownloadQuality(it) },
                            currentTheme = themePref,
                            onThemeChange = { viewModel.setTheme(it) },
                            autoPlayNext = autoPlayNext,
                            onAutoPlayChange = { viewModel.setAutoPlayNext(it) },
                            rememberLastPlayed = rememberLast,
                            onRememberLastPlayedChange = { viewModel.setRememberLastPlayed(it) },
                            updateUiState = updateUiState,
                            onCheckUpdate = { viewModel.checkForUpdate() },
                            onDownloadUpdate = { viewModel.downloadUpdate() },
                            onDismissUpdateDialog = { viewModel.dismissUpdateDialog() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VibeFlowBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, "Home") },
            label = { Text("Home") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            colors = NavigationBarItemDefaults.colors()
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, "Settings") },
            label = { Text("Settings") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            colors = NavigationBarItemDefaults.colors()
        )
    }
}
