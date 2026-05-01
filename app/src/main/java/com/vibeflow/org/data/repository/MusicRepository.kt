package com.vibeflow.org.data.repository

import com.vibeflow.org.data.api.RetrofitClient
import com.vibeflow.org.data.models.Track
import com.vibeflow.org.data.models.toTrack
import com.vibeflow.org.extractor.resolveYouTubeAudioStream
import com.vibeflow.org.extractor.searchYouTubeMusic
import com.vibeflow.org.extractor.toTrack

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class MusicRepository {

    // ── JioSaavn ──────────────────────────────────────────────────────────────

    // Try multiple fallback endpoints if one fails
    private suspend fun safeSaavnCall(block: suspend (com.vibeflow.org.data.api.SaavnService) -> retrofit2.Response<com.vibeflow.org.data.models.SearchResponse>): Result<List<Track>> {
        var lastError: Exception? = null
        for (url in RetrofitClient.SAAVN_BASE_URLS) {
            try {
                val service = RetrofitClient.createSaavnService(url)
                val response = block(service)
                if (response.isSuccessful) {
                    val tracks = response.body()?.data?.results?.map { it.toTrack() } ?: emptyList()
                    return Result.Success(tracks)
                }
            } catch (e: Exception) {
                lastError = e
                continue
            }
        }
        return Result.Error(lastError?.message ?: "All JioSaavn fallback APIs failed")
    }

    suspend fun saavnSearch(query: String): Result<List<Track>> = safeSaavnCall { service ->
        service.searchSongs(query)
    }

    suspend fun saavnTrending(): Result<List<Track>> = safeSaavnCall { service ->
        val q = listOf("arijit singh hits", "top bollywood 2024", "hindi hits").random()
        service.getTrending(q)
    }

    suspend fun saavnNewReleases(): Result<List<Track>> = safeSaavnCall { service ->
        service.getCharts("new hindi songs 2024")
    }

    suspend fun saavnTopCharts(): Result<List<Track>> = safeSaavnCall { service ->
        service.getCharts("top chart songs india")
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
