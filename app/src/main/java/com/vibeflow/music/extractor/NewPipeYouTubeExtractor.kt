package com.vibeflow.music.extractor

import android.util.Log
import com.vibeflow.music.data.models.MusicSource
import com.vibeflow.music.data.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

private const val TAG = "NewPipeYT"

// ─── Search Result Model ──────────────────────────────────────────────────────

data class YtMusicSearchResult(
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val durationSec: Int,
    val album: String = ""
)

fun YtMusicSearchResult.toTrack(): Track = Track(
    id = videoId,
    title = title,
    artist = artist,
    album = album,
    durationSec = durationSec,
    thumbnailUrl = thumbnailUrl,
    source = MusicSource.YOUTUBE,
    videoId = videoId
)

// ─── Search ───────────────────────────────────────────────────────────────────

suspend fun searchYouTubeMusic(
    query: String,
    limit: Int = 25
): List<YtMusicSearchResult> = withContext(Dispatchers.IO) {
    if (!NewPipeInitializer.isReady()) {
        Log.w(TAG, "NewPipe not initialized")
        return@withContext emptyList()
    }
    try {
        val yt = ServiceList.YouTube
        val qh = yt.searchQHFactory.fromQuery(
            query,
            listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS),
            ""
        )
        val searchInfo = SearchInfo.getInfo(yt, qh)
        val results = mutableListOf<YtMusicSearchResult>()

        for (item in searchInfo.relatedItems) {
            if (results.size >= limit) break
            val stream = item as? StreamInfoItem ?: continue
            val videoId = extractVideoId(stream.url) ?: continue

            val thumbnail = stream.thumbnails
                .maxByOrNull { it.width }?.url
                ?: stream.thumbnails.firstOrNull()?.url
                ?: ""

            results += YtMusicSearchResult(
                videoId = videoId,
                title = stream.name.trim(),
                artist = stream.uploaderName?.trim()
                    ?.takeIf { it.isNotBlank() } ?: "Unknown Artist",
                thumbnailUrl = thumbnail,
                durationSec = stream.duration.toInt().coerceAtLeast(0)
            )
        }
        Log.d(TAG, "Search '$query' → ${results.size} results")
        results
    } catch (e: Exception) {
        Log.e(TAG, "Search failed: $query → ${e.message}")
        emptyList()
    }
}

// ─── Stream Resolution ────────────────────────────────────────────────────────

/**
 * Resolve direct audio stream URL using NewPipeExtractor v0.26.1.
 *
 * v0.26.1 fixes:
 *  - YouTube InnerTube API changes
 *  - Signature cipher decryption
 *  - n-parameter descrambling (throttling fix)
 *
 * Priority: Progressive M4A > Progressive WebM > DASH > HLS
 */
suspend fun resolveYouTubeAudioStream(videoId: String): String? = withContext(Dispatchers.IO) {
    if (!NewPipeInitializer.isReady()) {
        Log.w(TAG, "NewPipe not initialized")
        return@withContext null
    }
    try {
        val watchUrl = "https://www.youtube.com/watch?v=$videoId"
        Log.d(TAG, "Resolving stream: $watchUrl")

        val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, watchUrl)
        val audioStreams: List<AudioStream> = streamInfo.audioStreams

        if (audioStreams.isEmpty()) {
            Log.w(TAG, "No audio streams for $videoId")
            return@withContext null
        }

        // Log available streams
        audioStreams.forEachIndexed { i, s ->
            Log.d(TAG, "Stream[$i] format=${s.format?.name} " +
                    "delivery=${s.deliveryMethod} " +
                    "bitrate=${s.averageBitrate} " +
                    "content=${s.content?.take(80) ?: "NULL"}")
        }

        // Helper — get URL, handle both content field and manifestUrl
        fun AudioStream.resolvedUrl(): String? =
            content?.takeIf { it.isNotBlank() }

        val usable = audioStreams.filter { it.resolvedUrl() != null }

        if (usable.isEmpty()) {
            Log.w(TAG, "All streams have null content for $videoId")
            return@withContext null
        }

        // 1. Progressive M4A — best direct play
        usable.filter {
            it.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP &&
            (it.format?.mimeType?.contains("mp4", true) == true ||
             it.format?.name?.contains("M4A", true) == true ||
             it.format?.name?.contains("MPEG_4", true) == true)
        }.maxByOrNull { it.averageBitrate }?.resolvedUrl()?.let {
            Log.d(TAG, "✅ Progressive M4A")
            return@withContext it
        }

        // 2. Progressive WebM/Opus
        usable.filter {
            it.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP
        }.maxByOrNull { it.averageBitrate }?.resolvedUrl()?.let {
            Log.d(TAG, "✅ Progressive WebM")
            return@withContext it
        }

        // 3. DASH audio (ExoPlayer handles natively)
        usable.filter {
            it.deliveryMethod == DeliveryMethod.DASH
        }.maxByOrNull { it.averageBitrate }?.resolvedUrl()?.let {
            Log.d(TAG, "✅ DASH stream")
            return@withContext it
        }

        // 4. HLS
        usable.filter {
            it.deliveryMethod == DeliveryMethod.HLS
        }.maxByOrNull { it.averageBitrate }?.resolvedUrl()?.let {
            Log.d(TAG, "✅ HLS stream")
            return@withContext it
        }

        // 5. Any usable stream
        val fallback = usable.maxByOrNull { it.averageBitrate }?.resolvedUrl()
        Log.d(TAG, if (fallback != null) "✅ Fallback stream" else "❌ No usable stream")
        fallback

    } catch (e: Exception) {
        Log.e(TAG, "Stream resolution failed for $videoId: ${e.message}")
        null
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

private fun extractVideoId(url: String): String? = try {
    when {
        url.contains("watch?v=") ->
            url.substringAfter("watch?v=").substringBefore("&")
                .takeIf { it.length in 10..12 }
        url.contains("youtu.be/") ->
            url.substringAfter("youtu.be/").substringBefore("?")
                .takeIf { it.length in 10..12 }
        else -> null
    }
} catch (e: Exception) { null }
