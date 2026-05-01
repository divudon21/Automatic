package com.vibeflow.org.data.models

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: SearchData?
)

data class SearchData(
    @SerializedName("total") val total: Int,
    @SerializedName("start") val start: Int,
    @SerializedName("results") val results: List<Song>
)

data class Song(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("year") val year: String?,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("label") val label: String?,
    @SerializedName("explicitContent") val explicitContent: Boolean,
    @SerializedName("playCount") val playCount: Long?,
    @SerializedName("language") val language: String?,
    @SerializedName("hasLyrics") val hasLyrics: Boolean,
    @SerializedName("url") val url: String?,
    @SerializedName("copyright") val copyright: String?,
    @SerializedName("album") val album: Album?,
    @SerializedName("artists") val artists: Artists?,
    @SerializedName("image") val image: List<ImageQuality>?,
    @SerializedName("downloadUrl") val downloadUrl: List<DownloadUrl>?
)

data class Album(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?
)

data class Artists(
    @SerializedName("primary") val primary: List<Artist>?,
    @SerializedName("featured") val featured: List<Artist>?,
    @SerializedName("all") val all: List<Artist>?
)

data class Artist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String?,
    @SerializedName("image") val image: List<ImageQuality>?,
    @SerializedName("type") val type: String?,
    @SerializedName("url") val url: String?
)

data class ImageQuality(
    @SerializedName("quality") val quality: String,
    @SerializedName("url") val url: String
)

data class DownloadUrl(
    @SerializedName("quality") val quality: String,
    @SerializedName("url") val url: String
)

// Trending / Charts response
data class TrendingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Song>?
)

data class ChartResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ChartData?
)

data class ChartData(
    @SerializedName("total") val total: Int?,
    @SerializedName("start") val start: Int?,
    @SerializedName("results") val results: List<Song>?
)

// Extension helpers
fun Song.getBestImageUrl(): String {
    return image?.maxByOrNull { it.quality.replace("x", "").split("x").firstOrNull()?.toIntOrNull() ?: 0 }?.url
        ?: image?.lastOrNull()?.url ?: ""
}

fun Song.getDownloadUrl(quality: AudioQuality = AudioQuality.HIGH): String {
    if (downloadUrl.isNullOrEmpty()) return ""
    return when (quality) {
        AudioQuality.LOW -> downloadUrl.firstOrNull()?.url ?: downloadUrl.last().url
        AudioQuality.MEDIUM -> downloadUrl.getOrNull(downloadUrl.size / 2)?.url ?: downloadUrl.last().url
        AudioQuality.HIGH -> downloadUrl.lastOrNull()?.url ?: ""
    }
}

fun Song.getPrimaryArtists(): String {
    return artists?.primary?.joinToString(", ") { it.name }
        ?: artists?.all?.filter { it.role == "singer" }?.joinToString(", ") { it.name }
        ?: "Unknown Artist"
}

fun Song.getFormattedDuration(): String {
    val d = duration ?: 0
    val minutes = d / 60
    val seconds = d % 60
    return "%d:%02d".format(minutes, seconds)
}

enum class AudioQuality { LOW, MEDIUM, HIGH }
