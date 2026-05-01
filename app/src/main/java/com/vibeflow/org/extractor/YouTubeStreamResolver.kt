package com.vibeflow.org.extractor

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * YouTube audio stream resolver.
 *
 * Strategy (tries in order until one works):
 *  1. Cobalt API (fast, reliable proxy)
 *  2. YouTube InnerTube ANDROID_MUSIC client  — direct URLs, no cipher
 *  3. YouTube InnerTube WEB client            — fallback
 *  4. Piped public instances (x5)             — community proxy fallback
 *
 * This multi-layer approach ensures maximum reliability.
 */
object YouTubeStreamResolver {

    private const val TAG = "YTResolver"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val PIPED_INSTANCES = listOf(
        "https://pipedapi.kavin.rocks",
        "https://piped-api.garudalinux.org",
        "https://api.piped.projectsegfau.lt",
        "https://pipedapi.in.projectsegfau.lt",
        "https://piped.video/api"
    )

    /** Resolve with quality hint (YouTube always returns best available, hint is informational) */
    suspend fun resolveWithQuality(videoId: String, quality: com.vibeflow.org.data.models.AudioQuality): String? =
        resolve(videoId)

    suspend fun resolve(videoId: String): String? {
        Log.d(TAG, "Resolving: $videoId")

        // 1. Cobalt API (Very reliable for music)
        cobalt(videoId)?.let {
            Log.d(TAG, "✅ Cobalt API: $videoId")
            return it
        }

        // 2. InnerTube Android Music client
        innerTube(videoId, clientAndroid())?.let {
            Log.d(TAG, "✅ InnerTube ANDROID_MUSIC: $videoId")
            return it
        }

        // 3. InnerTube Web client
        innerTube(videoId, clientWeb())?.let {
            Log.d(TAG, "✅ InnerTube WEB: $videoId")
            return it
        }

        // 4. Piped fallback
        piped(videoId)?.let {
            Log.d(TAG, "✅ Piped: $videoId")
            return it
        }

        Log.e(TAG, "❌ All methods failed for $videoId")
        return null
    }

    // ── Cobalt API ────────────────────────────────────────────────────────────

    private suspend fun cobalt(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("url", "https://music.youtube.com/watch?v=$videoId")
                put("isAudioOnly", true)
                put("aFormat", "best")
            }.toString()

            val req = Request.Builder()
                .url("https://api.cobalt.tools/api/json")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build()

            val resp = client.newCall(req).execute()
            val body = resp.body?.string() ?: return@withContext null
            if (!resp.isSuccessful) return@withContext null

            val root = JSONObject(body)
            if (root.optString("status") == "stream") {
                return@withContext root.optString("url").takeIf { it.isNotBlank() }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "Cobalt API error: ${e.message}")
            null
        }
    }

    // ── InnerTube ─────────────────────────────────────────────────────────────

    private fun clientAndroid() = JSONObject().apply {
        put("clientName", "ANDROID_MUSIC")
        put("clientVersion", "6.42.52")
        put("androidSdkVersion", 30)
        put("hl", "en")
        put("gl", "US")
    }

    private fun clientWeb() = JSONObject().apply {
        put("clientName", "WEB_REMIX")
        put("clientVersion", "1.20231101.01.00")
        put("hl", "en")
        put("gl", "US")
    }

    private suspend fun innerTube(videoId: String, clientCtx: JSONObject): String? =
        withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("videoId", videoId)
                    put("context", JSONObject().apply {
                        put("client", clientCtx)
                    })
                }.toString()

                val clientName = clientCtx.optString("clientName", "UNKNOWN")
                val url = if (clientName.contains("ANDROID"))
                    "https://www.youtube.com/youtubei/v1/player?key=AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc"
                else
                    "https://music.youtube.com/youtubei/v1/player?key=AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"

                val userAgent = if (clientName.contains("ANDROID"))
                    "com.google.android.apps.youtube.music/6.42.52 (Linux; U; Android 11) gzip"
                else
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"

                val req = Request.Builder()
                    .url(url)
                    .post(payload.toRequestBody("application/json".toMediaType()))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", userAgent)
                    .addHeader("X-Goog-Api-Format-Version", "1")
                    .build()

                val resp = client.newCall(req).execute()
                val body = resp.body?.string() ?: return@withContext null
                if (!resp.isSuccessful) return@withContext null

                parseInnerTubeAudio(body, videoId)
            } catch (e: Exception) {
                Log.w(TAG, "InnerTube ${clientCtx.optString("clientName")} error: ${e.message}")
                null
            }
        }

    private fun parseInnerTubeAudio(json: String, videoId: String): String? {
        return try {
            val root = JSONObject(json)

            // Check if playable
            val status = root.optJSONObject("playabilityStatus")?.optString("status") ?: ""
            if (status == "ERROR" || status == "LOGIN_REQUIRED") {
                Log.w(TAG, "Not playable: $status")
                return null
            }

            val streamingData = root.optJSONObject("streamingData") ?: return null
            val adaptive = streamingData.optJSONArray("adaptiveFormats")
            val muxed = streamingData.optJSONArray("formats")

            data class Opt(val url: String, val bitrate: Int, val mime: String)

            val opts = mutableListOf<Opt>()

            // adaptiveFormats = audio-only (best)
            if (adaptive != null) {
                for (i in 0 until adaptive.length()) {
                    val f = adaptive.getJSONObject(i)
                    val mime = f.optString("mimeType", "")
                    if (!mime.startsWith("audio/")) continue
                    val u = f.optString("url", "")
                    if (u.isBlank()) continue  // cipher protected, skip
                    opts += Opt(u, f.optInt("averageBitrate", f.optInt("bitrate", 0)), mime)
                }
            }

            // muxed formats = fallback (audio+video)
            if (opts.isEmpty() && muxed != null) {
                for (i in 0 until muxed.length()) {
                    val f = muxed.getJSONObject(i)
                    val u = f.optString("url", "")
                    if (u.isBlank()) continue
                    opts += Opt(u, f.optInt("averageBitrate", f.optInt("bitrate", 0)),
                        f.optString("mimeType", ""))
                }
            }

            if (opts.isEmpty()) {
                Log.w(TAG, "No direct URLs in InnerTube response (all ciphered) for $videoId")
                return null
            }

            // Best: m4a > webm > anything
            (opts.filter { it.mime.contains("mp4", true) }.maxByOrNull { it.bitrate }
                ?: opts.filter { it.mime.contains("webm", true) }.maxByOrNull { it.bitrate }
                ?: opts.maxByOrNull { it.bitrate })?.url
        } catch (e: Exception) {
            Log.e(TAG, "InnerTube parse error: ${e.message}")
            null
        }
    }

    // ── Piped fallback ────────────────────────────────────────────────────────

    private suspend fun piped(videoId: String): String? = withContext(Dispatchers.IO) {
        for (instance in PIPED_INSTANCES) {
            try {
                val req = Request.Builder()
                    .url("$instance/streams/$videoId")
                    .addHeader("User-Agent", "VibeFlow/1.0")
                    .build()

                val resp = client.newCall(req).execute()
                if (!resp.isSuccessful) continue

                val json = resp.body?.string() ?: continue
                val root = JSONObject(json)
                val streams = root.optJSONArray("audioStreams") ?: continue

                data class PA(val url: String, val bitrate: Int, val mime: String)
                val opts = mutableListOf<PA>()

                for (i in 0 until streams.length()) {
                    val s = streams.getJSONObject(i)
                    val u = s.optString("url", "")
                    if (u.isBlank()) continue
                    opts += PA(u, s.optInt("bitrate", 0), s.optString("mimeType", ""))
                }

                val best = opts.filter { it.mime.contains("mp4", true) }.maxByOrNull { it.bitrate }
                    ?: opts.filter { it.mime.contains("webm", true) }.maxByOrNull { it.bitrate }
                    ?: opts.maxByOrNull { it.bitrate }

                if (best != null) {
                    Log.d(TAG, "Piped $instance → ${best.mime} ${best.bitrate}bps")
                    return@withContext best.url
                }
            } catch (e: Exception) {
                Log.w(TAG, "Piped $instance: ${e.message}")
            }
        }
        null
    }
}
