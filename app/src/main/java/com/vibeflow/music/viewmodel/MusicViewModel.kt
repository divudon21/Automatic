package com.vibeflow.music.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vibeflow.music.BuildConfig
import com.vibeflow.music.data.AppPreferences
import com.vibeflow.music.data.DownloadQualityPref
import com.vibeflow.music.data.ThemePref
import com.vibeflow.music.update.ApkDownloader
import com.vibeflow.music.update.ApkInstallReceiver
import com.vibeflow.music.update.UpdateChecker
import com.vibeflow.music.update.UpdateResult
import com.vibeflow.music.update.UpdateUiState
import com.vibeflow.music.data.audiomack.DeezerRepository
import com.vibeflow.music.data.models.AudioQuality
import com.vibeflow.music.data.models.MusicSource
import com.vibeflow.music.data.models.Track
import com.vibeflow.music.data.repository.MusicRepository
import com.vibeflow.music.data.repository.Result
import com.vibeflow.music.download.downloadTrack
import com.vibeflow.music.player.MusicPlayerManager
import com.vibeflow.music.player.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SourceTab { JIOSAAVN, YOUTUBE, AUDIOMACK }

data class SaavnHomeState(
    val trending: List<Track> = emptyList(),
    val newReleases: List<Track> = emptyList(),
    val topCharts: List<Track> = emptyList(),
    val isLoadingTrending: Boolean = false,
    val isLoadingNew: Boolean = false,
    val isLoadingCharts: Boolean = false,
    val error: String? = null
)

data class YtHomeState(
    val bollywood: List<Track> = emptyList(),
    val phonk: List<Track> = emptyList(),
    val international: List<Track> = emptyList(),
    val isLoadingBollywood: Boolean = false,
    val isLoadingPhonk: Boolean = false,
    val isLoadingInternational: Boolean = false,
    val error: String? = null
)

data class AmHomeState(
    val globalChart: List<Track> = emptyList(),
    val hipHop:      List<Track> = emptyList(),
    val pop:         List<Track> = emptyList(),
    val dance:       List<Track> = emptyList(),
    val isLoadingGlobal: Boolean = false,
    val isLoadingHipHop: Boolean = false,
    val isLoadingPop:    Boolean = false,
    val isLoadingDance:  Boolean = false,
    val error: String? = null
)

data class SearchUiState(
    val query: String = "",
    val results: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false
)

data class PlaybackUiState(
    val isResolvingStream: Boolean = false,
    val streamError: String? = null
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    val playerManager = MusicPlayerManager(application)
    val playerState: StateFlow<PlayerState> = playerManager.playerState
    private val repository = MusicRepository()
    private val deezerRepository = DeezerRepository()

    private val _activeSource = MutableStateFlow(SourceTab.JIOSAAVN)
    val activeSource: StateFlow<SourceTab> = _activeSource.asStateFlow()

    private val _saavnHome = MutableStateFlow(SaavnHomeState())
    val saavnHome: StateFlow<SaavnHomeState> = _saavnHome.asStateFlow()

    private val _ytHome = MutableStateFlow(YtHomeState())
    val ytHome: StateFlow<YtHomeState> = _ytHome.asStateFlow()

    private val _amHome = MutableStateFlow(AmHomeState())
    val amHome: StateFlow<AmHomeState> = _amHome.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackUiState())
    val playbackState: StateFlow<PlaybackUiState> = _playbackState.asStateFlow()

    private val _audioQuality = MutableStateFlow(AudioQuality.HIGH)
    val audioQuality: StateFlow<AudioQuality> = _audioQuality.asStateFlow()

    val downloadQuality: StateFlow<DownloadQualityPref> =
        AppPreferences.getDownloadQuality(application)
            .stateIn(viewModelScope, SharingStarted.Eagerly, DownloadQualityPref.ALWAYS_ASK)

    val themePref: StateFlow<ThemePref> =
        AppPreferences.getTheme(application)
            .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePref.SYSTEM)

    val autoPlayNext: StateFlow<Boolean> =
        AppPreferences.getAutoPlay(application)
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val rememberLastPlayed: StateFlow<Boolean> =
        AppPreferences.getRememberLastPlayed(application)
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private var positionJob: Job? = null

    init {
        loadSaavnHome()
        loadYtHome()
        loadAmHome()
        startPositionTracker()
        setupAutoPlayListener()
        restoreLastPlayed()
    }

    private fun setupAutoPlayListener() {
        // When a song ends naturally, auto-play next if enabled
        playerManager.exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    if (autoPlayNext.value) {
                        skipNext()
                    }
                }
            }
        })
    }

    private fun restoreLastPlayed() {
        // Restore last played track info on startup if enabled
        viewModelScope.launch {
            if (rememberLastPlayed.value) {
                AppPreferences.getLastTrackId(getApplication()).collect { lastId ->
                    // Just store the ID — actual restore would need full track data
                    // This is used to highlight the last played track in the UI
                }
            }
        }
    }

    private fun startPositionTracker() {
        positionJob = viewModelScope.launch {
            while (isActive) {
                _currentPosition.value = playerManager.getCurrentPosition()
                val dur = playerManager.getDuration()
                if (dur > 0) _duration.value = dur
                delay(500L)
            }
        }
    }

    fun setActiveSource(src: SourceTab) {
        _activeSource.value = src
        _searchState.value = SearchUiState()
    }

    // ── JioSaavn ──────────────────────────────────────────────────────────────

    fun loadSaavnHome() {
        viewModelScope.launch {
            _saavnHome.value = _saavnHome.value.copy(isLoadingTrending = true)
            when (val r = repository.saavnTrending()) {
                is Result.Success -> _saavnHome.value = _saavnHome.value.copy(trending = r.data, isLoadingTrending = false)
                is Result.Error   -> _saavnHome.value = _saavnHome.value.copy(error = r.message, isLoadingTrending = false)
                else -> {}
            }
        }
        viewModelScope.launch {
            _saavnHome.value = _saavnHome.value.copy(isLoadingNew = true)
            when (val r = repository.saavnNewReleases()) {
                is Result.Success -> _saavnHome.value = _saavnHome.value.copy(newReleases = r.data, isLoadingNew = false)
                is Result.Error   -> _saavnHome.value = _saavnHome.value.copy(isLoadingNew = false)
                else -> {}
            }
        }
        viewModelScope.launch {
            _saavnHome.value = _saavnHome.value.copy(isLoadingCharts = true)
            when (val r = repository.saavnTopCharts()) {
                is Result.Success -> _saavnHome.value = _saavnHome.value.copy(topCharts = r.data, isLoadingCharts = false)
                is Result.Error   -> _saavnHome.value = _saavnHome.value.copy(isLoadingCharts = false)
                else -> {}
            }
        }
    }

    // ── YouTube ───────────────────────────────────────────────────────────────

    fun loadYtHome() {
        viewModelScope.launch {
            _ytHome.value = _ytHome.value.copy(isLoadingBollywood = true)
            when (val r = repository.ytTrending("bollywood")) {
                is Result.Success -> _ytHome.value = _ytHome.value.copy(bollywood = r.data, isLoadingBollywood = false)
                is Result.Error   -> _ytHome.value = _ytHome.value.copy(isLoadingBollywood = false)
                else -> {}
            }
        }
        viewModelScope.launch {
            _ytHome.value = _ytHome.value.copy(isLoadingPhonk = true)
            when (val r = repository.ytTrending("phonk")) {
                is Result.Success -> _ytHome.value = _ytHome.value.copy(phonk = r.data, isLoadingPhonk = false)
                is Result.Error   -> _ytHome.value = _ytHome.value.copy(isLoadingPhonk = false)
                else -> {}
            }
        }
        viewModelScope.launch {
            _ytHome.value = _ytHome.value.copy(isLoadingInternational = true)
            when (val r = repository.ytTrending("international")) {
                is Result.Success -> _ytHome.value = _ytHome.value.copy(international = r.data, isLoadingInternational = false)
                is Result.Error   -> _ytHome.value = _ytHome.value.copy(isLoadingInternational = false)
                else -> {}
            }
        }
    }

    // ── Audiomack (Deezer) ────────────────────────────────────────────────────

    fun loadAmHome() {
        viewModelScope.launch {
            _amHome.value = _amHome.value.copy(isLoadingGlobal = true)
            when (val r = deezerRepository.getGlobalChart()) {
                is Result.Success -> _amHome.value = _amHome.value.copy(globalChart = r.data, isLoadingGlobal = false)
                is Result.Error   -> _amHome.value = _amHome.value.copy(isLoadingGlobal = false)
                else -> {}
            }
        }
        viewModelScope.launch {
            _amHome.value = _amHome.value.copy(isLoadingHipHop = true)
            when (val r = deezerRepository.getHipHopChart()) {
                is Result.Success -> _amHome.value = _amHome.value.copy(hipHop = r.data, isLoadingHipHop = false)
                is Result.Error   -> _amHome.value = _amHome.value.copy(isLoadingHipHop = false)
                else -> {}
            }
        }
        viewModelScope.launch {
            _amHome.value = _amHome.value.copy(isLoadingPop = true)
            when (val r = deezerRepository.getPopChart()) {
                is Result.Success -> _amHome.value = _amHome.value.copy(pop = r.data, isLoadingPop = false)
                is Result.Error   -> _amHome.value = _amHome.value.copy(isLoadingPop = false)
                else -> {}
            }
        }
        viewModelScope.launch {
            _amHome.value = _amHome.value.copy(isLoadingDance = true)
            when (val r = deezerRepository.getDanceChart()) {
                is Result.Success -> _amHome.value = _amHome.value.copy(dance = r.data, isLoadingDance = false)
                is Result.Error   -> _amHome.value = _amHome.value.copy(isLoadingDance = false)
                else -> {}
            }
        }
    }

    // ── Search ────────────────────────────────────────────────────────────────

    fun search(query: String) {
        if (query.isBlank()) { _searchState.value = SearchUiState(); return }
        _searchState.value = SearchUiState(query = query, isLoading = true, hasSearched = true)
        viewModelScope.launch {
            val result = when (_activeSource.value) {
                SourceTab.JIOSAAVN  -> repository.saavnSearch(query)
                SourceTab.YOUTUBE   -> repository.ytSearch(query)
                SourceTab.AUDIOMACK -> deezerRepository.search(query)
            }
            when (result) {
                is Result.Success -> _searchState.value = _searchState.value.copy(results = result.data, isLoading = false)
                is Result.Error   -> _searchState.value = _searchState.value.copy(error = result.message, isLoading = false)
                else -> {}
            }
        }
    }

    fun clearSearch() { _searchState.value = SearchUiState() }

    // ── Playback ──────────────────────────────────────────────────────────────

    fun playTrack(track: Track, playlist: List<Track> = listOf(track)) {
        viewModelScope.launch {
            when (track.source) {
                MusicSource.JIOSAAVN -> {
                    playerManager.playSaavnPlaylist(track, playlist, _audioQuality.value)
                    if (rememberLastPlayed.value) {
                        AppPreferences.saveLastTrack(getApplication(), track.id, track.source.name)
                    }
                }
                MusicSource.AUDIOMACK -> {
                    // Deezer: direct MP3 URL stored in videoId field — play immediately
                    val url = track.videoId
                    if (url.isNotBlank()) {
                        val index = playlist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
                        playerManager.playWithUrl(track, url, playlist, index)
                    }
                }
                MusicSource.YOUTUBE -> {
                    playerManager.setLoadingTrack(track, playlist)
                    _playbackState.value = PlaybackUiState(isResolvingStream = true)
                    val index = playlist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
                    when (val r = repository.resolveYtStreamUrl(track.videoId)) {
                        is Result.Success -> {
                            _playbackState.value = PlaybackUiState(isResolvingStream = false)
                            playerManager.playWithUrl(track, r.data, playlist, index)
                        }
                        is Result.Error -> {
                            _playbackState.value = PlaybackUiState(
                                isResolvingStream = false,
                                streamError = r.message
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun togglePlayPause() = playerManager.togglePlayPause()

    fun skipNext() {
        val state = playerState.value
        val nextIdx = state.currentIndex + 1
        if (nextIdx < state.playlist.size) {
            val next = state.playlist[nextIdx]
            if (next.source == MusicSource.YOUTUBE || next.source == MusicSource.AUDIOMACK)
                playTrack(next, state.playlist)
            else playerManager.skipNext()
        }
    }

    fun skipPrevious() {
        val state = playerState.value
        if (playerManager.getCurrentPosition() > 3000L) {
            playerManager.seekTo(0L)
        } else {
            val prevIdx = state.currentIndex - 1
            if (prevIdx >= 0) {
                val prev = state.playlist[prevIdx]
                if (prev.source == MusicSource.YOUTUBE || prev.source == MusicSource.AUDIOMACK)
                    playTrack(prev, state.playlist)
                else playerManager.skipPrevious()
            }
        }
    }

    fun seekTo(pos: Long)  = playerManager.seekTo(pos)
    fun toggleShuffle()    = playerManager.toggleShuffle()
    fun cycleRepeatMode()  = playerManager.cycleRepeatMode()
    fun setAudioQuality(q: AudioQuality) { _audioQuality.value = q }

    fun setDownloadQuality(pref: DownloadQualityPref) {
        viewModelScope.launch { AppPreferences.setDownloadQuality(getApplication(), pref) }
    }

    fun setTheme(pref: ThemePref) {
        viewModelScope.launch { AppPreferences.setTheme(getApplication(), pref) }
    }

    fun setAutoPlayNext(enabled: Boolean) {
        viewModelScope.launch { AppPreferences.setAutoPlay(getApplication(), enabled) }
    }

    fun setRememberLastPlayed(enabled: Boolean) {
        viewModelScope.launch { AppPreferences.setRememberLastPlayed(getApplication(), enabled) }
    }

    fun downloadTrackNow(track: Track, quality: AudioQuality) {
        viewModelScope.launch { downloadTrack(getApplication(), track, quality) }
    }

    // ── In-App Update ─────────────────────────────────────────────────────────

    private val _updateUiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateUiState: StateFlow<UpdateUiState> = _updateUiState.asStateFlow()

    fun checkForUpdate() {
        if (_updateUiState.value is UpdateUiState.Checking) return
        viewModelScope.launch {
            _updateUiState.value = UpdateUiState.Checking
            when (val result = UpdateChecker.checkForUpdate(BuildConfig.VERSION_CODE)) {
                is UpdateResult.UpdateAvailable ->
                    _updateUiState.value = UpdateUiState.UpdateAvailable(result.info)
                is UpdateResult.UpToDate ->
                    _updateUiState.value = UpdateUiState.UpToDate
                is UpdateResult.Error ->
                    _updateUiState.value = UpdateUiState.Error(result.message)
            }
        }
    }

    fun downloadUpdate() {
        val state = _updateUiState.value
        if (state !is UpdateUiState.UpdateAvailable) return
        val app: Application = getApplication()
        val downloadId = ApkDownloader.download(
            context      = app,
            apkUrl       = state.info.apkDownloadUrl,
            versionName  = state.info.latestVersionName
        )
        // Store download ID so BroadcastReceiver can match it
        app.getSharedPreferences(ApkInstallReceiver.PREF_NAME, android.content.Context.MODE_PRIVATE)
            .edit()
            .putLong(ApkInstallReceiver.KEY_DOWNLOAD_ID, downloadId)
            .apply()
    }

    fun dismissUpdateDialog() {
        if (_updateUiState.value !is UpdateUiState.Checking) {
            _updateUiState.value = UpdateUiState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        positionJob?.cancel()
        playerManager.release()
    }
}
