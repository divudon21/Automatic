package com.vibeflow.music.data.repository

import com.vibeflow.music.data.api.RetrofitClient
import com.vibeflow.music.data.models.Track
import com.vibeflow.music.data.models.toTrack
import com.vibeflow.music.extractor.resolveYouTubeAudioStream
import com.vibeflow.music.extractor.searchYouTubeMusic
import com.vibeflow.music.extractor.toTrack

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class MusicRepository {

    private val saavnService = RetrofitClient.saavnService

    // ── JioSaavn ──────────────────────────────────────────────────────────────

    suspend fun saavnSearch(query: String): Result<List<Track>> = safeCall {
        saavnService.searchSongs(query).body()?.data?.results?.map { it.toTrack() } ?: emptyList()
    }

    suspend fun saavnTrending(): Result<List<Track>> = safeCall {
        val q = listOf("arijit singh hits", "top bollywood 2024", "hindi hits").random()
        saavnService.getTrending(q).body()?.data?.results?.map { it.toTrack() } ?: emptyList()
    }

    suspend fun saavnNewReleases(): Result<List<Track>> = safeCall {
        saavnService.getCharts("new hindi songs 2024").body()?.data?.results?.map { it.toTrack() } ?: emptyList()
    }

    suspend fun saavnTopCharts(): Result<List<Track>> = safeCall {
        saavnService.getCharts("top chart songs india").body()?.data?.results?.map { it.toTrack() } ?: emptyList()
    }

    // ── YouTube Music (NewPipeExtractor v0.26.1) ──────────────────────────────

    /**
     * Search YouTube Music songs.
     * Uses MUSIC_SONGS filter via NewPipeExtractor v0.26.1.
     */
    suspend fun ytSearch(query: String): Result<List<Track>> {
        return try {
            val results = searchYouTubeMusic(query, limit = 25)
            if (results.isNotEmpty()) Result.Success(results.map { it.toTrack() })
            else Result.Error("No results for \"$query\"")
        } catch (e: Exception) {
            Result.Error(e.message ?: "YouTube search failed")
        }
    }

    suspend fun ytTrending(genre: String): Result<List<Track>> {
        val queryMap = mapOf(
            "bollywood"     to "top bollywood songs hindi hits 2024",
            "phonk"         to "phonk music trending 2024",
            "international" to "top international pop hits 2024",
            "pop"           to "best pop songs 2024",
            "hiphop"        to "hip hop rap hits 2024",
            "lofi"          to "lofi chill beats 2024"
        )
        return ytSearch(queryMap[genre.lowercase()] ?: "top $genre songs 2024")
    }

    /**
     * Resolve direct audio stream URL for a YouTube videoId.
     * Uses NewPipeExtractor v0.26.1 StreamInfo.getInfo().
     * Returns M4A > WebM > any available stream.
     */
    suspend fun resolveYtStreamUrl(videoId: String): Result<String> {
        return try {
            val url = resolveYouTubeAudioStream(videoId)
            if (!url.isNullOrBlank()) Result.Success(url)
            else Result.Error("Could not resolve stream for $videoId")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Stream resolution failed")
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
        return try {
            Result.Success(block())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}
