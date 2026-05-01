package com.vibeflow.org.update

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object UpdateChecker {

    private const val TAG = "UpdateChecker"
    private const val RELEASES_URL =
        "https://api.github.com/repos/divudon21/Automatic/releases/latest"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Check GitHub Releases API for a newer version.
     * Compares [currentVersionCode] (Int) with latest release tag.
     */
    suspend fun checkForUpdate(currentVersionCode: Int): UpdateResult =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(RELEASES_URL)
                    .addHeader("Accept", "application/vnd.github+json")
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .addHeader("User-Agent", "VibeFlow-Android/$currentVersionCode")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext UpdateResult.Error("GitHub API: ${response.code}")
                }

                val body = response.body?.string()
                    ?: return@withContext UpdateResult.Error("Empty response")
                val json = JSONObject(body)

                val tagName      = json.optString("tag_name", "")
                val releaseNotes = json.optString("body", "")
                val publishedAt  = json.optString("published_at", "")

                // Parse version from tag: "v1.2-build.abc1234" → name="1.2", code from tag
                val versionName = tagName.removePrefix("v").substringBefore("-build").trim()
                // Try to parse a numeric versionCode from the tag suffix, fallback to 0
                val remoteCode = tagName
                    .substringAfter("-build.", "")
                    .filter { it.isDigit() }
                    .take(6)
                    .toIntOrNull() ?: 0

                Log.d(TAG, "Current code: $currentVersionCode | Remote: $versionName (code~$remoteCode, tag: $tagName)")

                // Find APK asset
                val assets = json.optJSONArray("assets")
                    ?: return@withContext UpdateResult.Error("No assets in release")
                var apkUrl = ""
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.optString("name").endsWith(".apk")) {
                        apkUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
                if (apkUrl.isBlank()) {
                    return@withContext UpdateResult.Error("No APK asset in release $tagName")
                }

                // Compare: if remote versionCode > current, update available
                // Since our build tags include build number, compare versionName parts
                val isNewer = isNewerVersion(versionName, currentVersionCode.toString())

                if (isNewer) {
                    Log.d(TAG, "Update available: $tagName")
                    UpdateResult.UpdateAvailable(
                        UpdateInfo(
                            latestVersionName = versionName,
                            latestVersionCode = remoteCode,
                            tagName = tagName,
                            apkDownloadUrl = apkUrl,
                            releaseNotes = releaseNotes,
                            publishedAt = publishedAt
                        )
                    )
                } else {
                    Log.d(TAG, "App is up to date")
                    UpdateResult.UpToDate
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update check failed: ${e.message}")
                UpdateResult.Error(e.message ?: "Unknown error")
            }
        }

    private fun isNewerVersion(remote: String, current: String): Boolean {
        return try {
            val r = remote.split(".").map { it.trim().toIntOrNull() ?: 0 }
            val c = current.split(".").map { it.trim().toIntOrNull() ?: 0 }
            val maxLen = maxOf(r.size, c.size)
            for (i in 0 until maxLen) {
                val rv = r.getOrElse(i) { 0 }
                val cv = c.getOrElse(i) { 0 }
                if (rv > cv) return true
                if (rv < cv) return false
            }
            false
        } catch (e: Exception) { false }
    }
}
