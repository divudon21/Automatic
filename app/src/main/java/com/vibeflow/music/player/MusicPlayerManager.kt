package com.vibeflow.music.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.vibeflow.music.data.models.AudioQuality
import com.vibeflow.music.data.models.Track
import com.vibeflow.music.data.models.getPlayUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerState(
    val currentTrack: Track? = null,
    val playlist: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isMiniPlayerVisible: Boolean = false,
    val isShuffleOn: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)

/**
 * Direct ExoPlayer for playback (always available immediately).
 * MediaController connects to service separately for notification/background.
 * This ensures songs play instantly on click — no async wait.
 */
class MusicPlayerManager(private val context: Context) {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // ── Direct ExoPlayer — used for actual playback ────────────────────────────
    // This is ALWAYS ready immediately, no async connect needed
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true  // handle audio focus automatically
        )
        .setHandleAudioBecomingNoisy(true)  // pause on headphone unplug
        .build()
        .also { player ->
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                }
                override fun onPlaybackStateChanged(state: Int) {
                    _playerState.value = _playerState.value.copy(
                        isLoading = state == Player.STATE_BUFFERING
                    )
                }
                override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                    val idx = player.currentMediaItemIndex
                    val pl  = _playerState.value.playlist
                    if (idx in pl.indices) {
                        _playerState.value = _playerState.value.copy(
                            currentTrack = pl[idx], currentIndex = idx
                        )
                    }
                }
                override fun onShuffleModeEnabledChanged(enabled: Boolean) {
                    _playerState.value = _playerState.value.copy(isShuffleOn = enabled)
                }
                override fun onRepeatModeChanged(mode: Int) {
                    _playerState.value = _playerState.value.copy(repeatMode = mode)
                }
            })
        }

    // ── MediaSession — wraps exoPlayer for background notification ────────────
    private val mediaSession: MediaSession = MediaSession.Builder(context, exoPlayer)
        .setId("VibeFlowSession")
        .build()

    // ── Position helpers ──────────────────────────────────────────────────────
    fun getCurrentPosition(): Long = exoPlayer.currentPosition
    fun getDuration(): Long = exoPlayer.duration.coerceAtLeast(0L)

    // ── Show track in mini player immediately (before YT stream resolves) ─────
    fun setLoadingTrack(track: Track, playlist: List<Track>) {
        val index = playlist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        _playerState.value = _playerState.value.copy(
            currentTrack = track,
            playlist = playlist,
            currentIndex = index,
            isMiniPlayerVisible = true,
            isLoading = true
        )
    }

    // ── YouTube: play with resolved direct URL ────────────────────────────────
    fun playWithUrl(track: Track, resolvedUrl: String, playlist: List<Track>, indexInPlaylist: Int) {
        Log.d("PlayerManager", "playWithUrl: ${track.title} → $resolvedUrl")
        val item = buildMediaItem(track, resolvedUrl)
        exoPlayer.setMediaItem(item)
        exoPlayer.prepare()
        exoPlayer.play()
        _playerState.value = _playerState.value.copy(
            currentTrack = track,
            playlist = playlist,
            currentIndex = indexInPlaylist,
            isMiniPlayerVisible = true,
            isLoading = false
        )
    }

    // ── JioSaavn: play full playlist (all URLs known upfront) ─────────────────
    fun playSaavnPlaylist(track: Track, playlist: List<Track>, quality: AudioQuality) {
        Log.d("PlayerManager", "playSaavnPlaylist: ${track.title}")
        val index = playlist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        val items = playlist.mapNotNull { t ->
            val url = t.getPlayUrl(quality)
            if (url.isBlank()) null else buildMediaItem(t, url)
        }
        if (items.isEmpty()) {
            Log.w("PlayerManager", "No valid URLs in playlist")
            return
        }
        exoPlayer.setMediaItems(items, index, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
        _playerState.value = _playerState.value.copy(
            currentTrack = track,
            playlist = playlist,
            currentIndex = index,
            isMiniPlayerVisible = true,
            isLoading = false
        )
    }

    private fun buildMediaItem(track: Track, url: String): MediaItem =
        MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album.takeIf { it.isNotBlank() })
                    .setArtworkUri(Uri.parse(track.thumbnailUrl))
                    .build()
            )
            .build()

    // ── Controls ──────────────────────────────────────────────────────────────
    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }
    fun skipNext() {
        if (exoPlayer.hasNextMediaItem()) exoPlayer.seekToNextMediaItem()
    }
    fun skipPrevious() {
        if (exoPlayer.currentPosition > 3000L) exoPlayer.seekTo(0L)
        else if (exoPlayer.hasPreviousMediaItem()) exoPlayer.seekToPreviousMediaItem()
    }
    fun seekTo(pos: Long) = exoPlayer.seekTo(pos)
    fun toggleShuffle() {
        val new = !exoPlayer.shuffleModeEnabled
        exoPlayer.shuffleModeEnabled = new
        _playerState.value = _playerState.value.copy(isShuffleOn = new)
    }
    fun cycleRepeatMode() {
        val next = when (exoPlayer.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer.repeatMode = next
        _playerState.value = _playerState.value.copy(repeatMode = next)
    }

    fun release() {
        mediaSession.release()
        exoPlayer.release()
    }
}
