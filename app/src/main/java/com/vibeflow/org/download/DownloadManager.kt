package com.vibeflow.org.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.vibeflow.org.data.models.AudioQuality
import com.vibeflow.org.data.models.MusicSource
import com.vibeflow.org.data.models.Track
import com.vibeflow.org.data.models.getPlayUrl
import com.vibeflow.org.extractor.YouTubeStreamResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "VibeDownload"

// ── Size estimators ────────────────────────────────────────────────────────────

fun estimatedSizeMB(durationSec: Int, quality: AudioQuality): String {
    val min = durationSec / 60.0
    val mb = min * when (quality) {
        AudioQuality.LOW    -> 0.09   // 12 kbps
        AudioQuality.MEDIUM -> 0.72   // 96 kbps
        AudioQuality.HIGH   -> 2.4    // 320 kbps
    }
    return if (mb < 1.0) "~${(mb * 1024).toInt()} KB" else "~%.1f MB".format(mb)
}

fun estimatedSizeMBYt(durationSec: Int, tier: String = "high"): String {
    val min = durationSec / 60.0
    val mb = min * when (tier) {
        "low"    -> 0.36   // ~48 kbps
        "medium" -> 0.96   // ~128 kbps
        else     -> 1.44   // ~192 kbps best
    }
    return if (mb < 1.0) "~${(mb * 1024).toInt()} KB" else "~%.1f MB".format(mb)
}

// ── Main download function ─────────────────────────────────────────────────────

suspend fun downloadTrack(
    context: Context,
    track: Track,
    quality: AudioQuality
) = withContext(Dispatchers.IO) {

    Log.d(TAG, "Download: ${track.title} | source=${track.source} | quality=$quality")
    Log.d(TAG, "downloadUrls count: ${track.downloadUrls.size}")
    track.downloadUrls.forEachIndexed { i, u ->
        Log.d(TAG, "  [$i] quality=${u.quality} url=${u.url.take(60)}")
    }

    // ── Resolve URL ────────────────────────────────────────────────────────────
    val streamUrl: String = when (track.source) {

        MusicSource.JIOSAAVN -> {
            val url = track.getPlayUrl(quality)
            Log.d(TAG, "JioSaavn URL ($quality): ${url.take(80)}")
            if (url.isBlank()) {
                // Fallback: try other qualities
                track.downloadUrls.lastOrNull()?.url ?: ""
            } else url
        }

        MusicSource.YOUTUBE -> {
            val url = YouTubeStreamResolver.resolveWithQuality(track.videoId, quality)
            Log.d(TAG, "YouTube URL: ${url?.take(80) ?: "null"}")
            url ?: ""
        }
    }

    if (streamUrl.isBlank()) {
        Log.e(TAG, "No URL found for ${track.title}")
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "❌ Download failed: Could not get audio URL for '${track.title}'",
                Toast.LENGTH_LONG
            ).show()
        }
        return@withContext
    }

    // ── Build filename ─────────────────────────────────────────────────────────
    val safeTitle = track.title
        .replace(Regex("[^a-zA-Z0-9 _\\-]"), "")
        .trim().take(60).ifBlank { "track" }

    val qualityTag = when (track.source) {
        MusicSource.JIOSAAVN -> when (quality) {
            AudioQuality.LOW    -> "_12k"
            AudioQuality.MEDIUM -> "_96k"
            AudioQuality.HIGH   -> "_320k"
        }
        MusicSource.YOUTUBE -> when (quality) {
            AudioQuality.LOW    -> "_48k"
            AudioQuality.MEDIUM -> "_128k"
            AudioQuality.HIGH   -> "_best"
        }
    }

    val fileName = "${safeTitle}${qualityTag}.m4a"
    val subPath  = "VibeFlow/$fileName"

    // ── Enqueue with DownloadManager ───────────────────────────────────────────
    try {
        val request = DownloadManager.Request(Uri.parse(streamUrl)).apply {
            setTitle(track.title)
            setDescription("${track.artist} • VibeFlow")
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, subPath)
            addRequestHeader(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36"
            )
            addRequestHeader("Accept", "audio/*,*/*;q=0.9")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            setMimeType("audio/mp4")
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)

        Log.d(TAG, "✅ Download enqueued: $fileName")

        withContext(Dispatchers.Main) {
            val qualStr = when (track.source) {
                MusicSource.JIOSAAVN -> when (quality) {
                    AudioQuality.LOW -> "12kbps"; AudioQuality.MEDIUM -> "96kbps"; AudioQuality.HIGH -> "320kbps"
                }
                MusicSource.YOUTUBE -> when (quality) {
                    AudioQuality.LOW -> "48kbps"; AudioQuality.MEDIUM -> "128kbps"; AudioQuality.HIGH -> "Best"
                }
            }
            Toast.makeText(
                context,
                "⬇️ Downloading: ${track.title} ($qualStr)",
                Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: Exception) {
        Log.e(TAG, "Download enqueue failed: ${e.message}")
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "❌ Download error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
