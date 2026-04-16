package com.vibeflow.music.data.audiomack

import android.util.Log
import com.vibeflow.music.data.models.MusicSource
import com.vibeflow.music.data.models.Track
import com.vibeflow.music.data.repository.Result

private const val TAG = "DeezerRepo"

/**
 * DeezerRepository — completely isolated from JioSaavn & YouTube logic.
 *
 * Deezer Public API (api.deezer.com):
 *  - No API key required
 *  - No OAuth needed
 *  - preview field = direct .mp3 URL (30-sec clip) — plays directly in ExoPlayer
 *  - cover_xl / cover_big = high-res album art (up to 1000x1000)
 *
 * Maps DeezerTrack → unified Track (source = AUDIOMACK) so the
 * existing player/download system works without any changes.
 */
class DeezerRepository {

    private val service = DeezerClient.service

    // ── Search ────────────────────────────────────────────────────────────────

    suspend fun search(query: String, limit: Int = 30): Result<List<Track>> {
        return try {
            val resp = service.searchTracks(query, limit)
            if (resp.isSuccessful) {
                val tracks = resp.body()?.data?.map { it.toTrack() } ?: emptyList()
                Log.d(TAG, "Search '$query' → ${tracks.size} tracks")
                Result.Success(tracks)
            } else {
                Result.Error("Deezer search failed: ${resp.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search error: ${e.message}")
            Result.Error(e.message ?: "Search failed")
        }
    }

    // ── Home sections ─────────────────────────────────────────────────────────

    /** Global trending chart */
    suspend fun getGlobalChart(): Result<List<Track>> {
        return try {
            val resp = service.getGlobalChart(25)
            if (resp.isSuccessful) {
                Result.Success(resp.body()?.data?.map { it.toTrack() } ?: emptyList())
            } else Result.Error("Chart failed: ${resp.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Chart failed")
        }
    }

    /** Hip-Hop / Rap chart (genre 132) */
    suspend fun getHipHopChart(): Result<List<Track>> = getGenreChart(132)

    /** Pop chart (genre 116) */
    suspend fun getPopChart(): Result<List<Track>> = getGenreChart(116)

    /** Dance / Electronic chart (genre 113) */
    suspend fun getDanceChart(): Result<List<Track>> = getGenreChart(113)

    /** R&B chart (genre 152) */
    suspend fun getRnbChart(): Result<List<Track>> = getGenreChart(152)

    private suspend fun getGenreChart(genreId: Int): Result<List<Track>> {
        return try {
            val resp = service.getGenreChart(genreId, 20)
            if (resp.isSuccessful) {
                Result.Success(resp.body()?.data?.map { it.toTrack() } ?: emptyList())
            } else Result.Error("Genre chart failed: ${resp.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Genre chart failed")
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    /**
     * Convert [DeezerTrack] → unified [Track].
     *
     * stream URL = track.preview (direct .mp3, no auth needed)
     * This is stored in videoId field (reused as streamUrl for AUDIOMACK source)
     */
    private fun DeezerTrack.toTrack(): Track = Track(
        id          = "dz_$id",
        title       = title,
        artist      = artist.name,
        album       = album.title,
        durationSec = duration,
        thumbnailUrl = bestThumbnail(),
        source      = MusicSource.AUDIOMACK,
        downloadUrls = emptyList(),
        videoId     = preview   // ← direct MP3 stream URL stored here
    )
}
