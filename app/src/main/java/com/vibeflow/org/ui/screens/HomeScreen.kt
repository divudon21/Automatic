package com.vibeflow.org.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeflow.org.R
import com.vibeflow.org.data.DownloadQualityPref
import com.vibeflow.org.data.models.AudioQuality
import com.vibeflow.org.data.models.Track
import com.vibeflow.org.ui.components.DownloadBottomSheet
import com.vibeflow.org.ui.components.TrackHorizontalCard
import com.vibeflow.org.ui.components.TrackListItem
import com.vibeflow.org.viewmodel.SaavnHomeState
import com.vibeflow.org.viewmodel.SearchUiState
import com.vibeflow.org.viewmodel.SourceTab
import com.vibeflow.org.viewmodel.YtHomeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activeSource: SourceTab,
    saavnHome: SaavnHomeState,
    ytHome: YtHomeState,
    searchState: SearchUiState,
    currentTrackId: String?,
    isPlaying: Boolean,
    defaultDownloadQuality: DownloadQualityPref,
    onSourceChange: (SourceTab) -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onDownload: (Track, AudioQuality) -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onRefreshSaavn: () -> Unit,
    onRefreshYt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var downloadTrack by remember { mutableStateOf<Track?>(null) }

    val handleDownloadClick: (Track) -> Unit = { track ->
        when (defaultDownloadQuality) {
            DownloadQualityPref.ALWAYS_ASK -> downloadTrack = track
            DownloadQualityPref.LOW        -> onDownload(track, AudioQuality.LOW)
            DownloadQualityPref.MEDIUM     -> onDownload(track, AudioQuality.MEDIUM)
            DownloadQualityPref.HIGH       -> onDownload(track, AudioQuality.HIGH)
        }
    }

    val searchPlaceholder = when (activeSource) {
        SourceTab.JIOSAAVN  -> "Search JioSaavn..."
        SourceTab.YOUTUBE   -> "Search YouTube Music..."
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

            // ── Header ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(0.9f),
                            MaterialTheme.colorScheme.background
                        ))
                    )
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    // Logo
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher),
                            contentDescription = "App Icon",
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("VibeFlow", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(searchPlaceholder) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""; onClearSearch(); focusManager.clearFocus()
                                }) { Icon(Icons.Default.Clear, null) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            onSearch(searchQuery); focusManager.clearFocus()
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            val selectedForDownload = downloadTrack?.id

            if (searchState.hasSearched) {
                SearchResultsScreen(searchState, currentTrackId, isPlaying,
                    selectedForDownload, onTrackClick, handleDownloadClick)
            } else {
                when (activeSource) {
                    SourceTab.JIOSAAVN  -> SaavnHomeContent(saavnHome, currentTrackId, isPlaying,
                        selectedForDownload, onTrackClick, handleDownloadClick, onRefreshSaavn)
                    SourceTab.YOUTUBE   -> YtHomeContent(ytHome, currentTrackId, isPlaying,
                        selectedForDownload, onTrackClick, handleDownloadClick, onRefreshYt)
                }
            }
        }

        // Download sheet
        downloadTrack?.let { track ->
            DownloadBottomSheet(
                track = track,
                onDismiss = { downloadTrack = null },
                onDownload = { quality -> onDownload(track, quality); downloadTrack = null }
            )
        }
    }
}

// ── Search Results ────────────────────────────────────────────────────────────

@Composable
fun SearchResultsScreen(
    searchState: SearchUiState, currentTrackId: String?, isPlaying: Boolean,
    selectedForDownload: String?, onTrackClick: (Track, List<Track>) -> Unit,
    onDownloadClick: (Track) -> Unit
) {
    when {
        searchState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text("Searching...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        searchState.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.WifiOff, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text(searchState.error, color = MaterialTheme.colorScheme.error)
            }
        }
        searchState.results.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(8.dp))
                Text("No results found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        else -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(searchState.results) { track ->
                TrackListItem(
                    track = track,
                    isPlaying = isPlaying && currentTrackId == track.id,
                    isBlurred = selectedForDownload != null && selectedForDownload != track.id,
                    onClick = { onTrackClick(track, searchState.results) },
                    onDownloadClick = onDownloadClick
                )
            }
        }
    }
}

// ── JioSaavn Home ─────────────────────────────────────────────────────────────

@Composable
fun SaavnHomeContent(
    state: SaavnHomeState, currentTrackId: String?, isPlaying: Boolean,
    selectedForDownload: String?, onTrackClick: (Track, List<Track>) -> Unit,
    onDownloadClick: (Track) -> Unit, onRefresh: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        item { HomeSectionHeader("🔥 Trending Now", state.isLoadingTrending, onRefresh) }
        item { HorizontalTrackRow(state.trending, state.isLoadingTrending, currentTrackId, isPlaying, selectedForDownload, "Could not load trending", onTrackClick, onDownloadClick) }
        item { Spacer(Modifier.height(20.dp)) }
        item { HomeSectionHeader("✨ New Releases", state.isLoadingNew) }
        item { HorizontalTrackRow(state.newReleases, state.isLoadingNew, currentTrackId, isPlaying, selectedForDownload, "Could not load new releases", onTrackClick, onDownloadClick) }
        item { Spacer(Modifier.height(20.dp)) }
        item { HomeSectionHeader("🎵 Top Charts", state.isLoadingCharts) }
        if (state.isLoadingCharts) {
            item { Box(Modifier.fillMaxWidth().height(60.dp), Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } }
        } else {
            items(state.topCharts) { track ->
                TrackListItem(track, isPlaying && currentTrackId == track.id,
                    selectedForDownload != null && selectedForDownload != track.id,
                    { onTrackClick(track, state.topCharts) }, onDownloadClick)
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── YouTube Home ──────────────────────────────────────────────────────────────

@Composable
fun YtHomeContent(
    state: YtHomeState, currentTrackId: String?, isPlaying: Boolean,
    selectedForDownload: String?, onTrackClick: (Track, List<Track>) -> Unit,
    onDownloadClick: (Track) -> Unit, onRefresh: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        item { HomeSectionHeader("🇮🇳 Bollywood Hits", state.isLoadingBollywood, onRefresh) }
        item { HorizontalTrackRow(state.bollywood, state.isLoadingBollywood, currentTrackId, isPlaying, selectedForDownload, "Could not load Bollywood", onTrackClick, onDownloadClick) }
        item { Spacer(Modifier.height(20.dp)) }
        item { HomeSectionHeader("💥 Phonk", state.isLoadingPhonk) }
        item { HorizontalTrackRow(state.phonk, state.isLoadingPhonk, currentTrackId, isPlaying, selectedForDownload, "Could not load Phonk", onTrackClick, onDownloadClick) }
        item { Spacer(Modifier.height(20.dp)) }
        item { HomeSectionHeader("🌍 International", state.isLoadingInternational) }
        item { HorizontalTrackRow(state.international, state.isLoadingInternational, currentTrackId, isPlaying, selectedForDownload, "Could not load International", onTrackClick, onDownloadClick) }
        if (!state.isLoadingInternational && state.international.isNotEmpty()) {
            item { Spacer(Modifier.height(20.dp)) }
            item { HomeSectionHeader("🎶 International Top") }
            items(state.international) { track ->
                TrackListItem(track, isPlaying && currentTrackId == track.id,
                    selectedForDownload != null && selectedForDownload != track.id,
                    { onTrackClick(track, state.international) }, onDownloadClick)
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
fun HorizontalTrackRow(
    tracks: List<Track>, isLoading: Boolean, currentTrackId: String?,
    isPlaying: Boolean, selectedForDownload: String?, emptyMsg: String,
    onTrackClick: (Track, List<Track>) -> Unit, onDownloadClick: (Track) -> Unit
) {
    when {
        isLoading -> Box(Modifier.fillMaxWidth().height(180.dp), Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        tracks.isNotEmpty() -> LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tracks) { track ->
                TrackHorizontalCard(
                    track = track,
                    isPlaying = isPlaying && currentTrackId == track.id,
                    isBlurred = selectedForDownload != null && selectedForDownload != track.id,
                    onClick = { onTrackClick(track, tracks) },
                    onDownloadClick = onDownloadClick
                )
            }
        }
        else -> Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
            Text(emptyMsg, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
fun HomeSectionHeader(title: String, isLoading: Boolean = false, onRefresh: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        if (onRefresh != null && !isLoading) {
            IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
