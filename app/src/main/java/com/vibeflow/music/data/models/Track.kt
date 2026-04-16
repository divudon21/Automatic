package com.vibeflow.music.data.models

/**
 * Unified track model for JioSaavn + YouTube Music.
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSec: Int,
    val thumbnailUrl: String,
    val source: MusicSource,
    val downloadUrls: List<DownloadUrl> = emptyList(),
    val videoId: String = ""
)

enum class MusicSource { JIOSAAVN, YOUTUBE, AUDIOMACK }

fun Song.toTrack(): Track = Track(
    id = id,
    title = name,
    artist = getPrimaryArtists(),
    album = album?.name ?: "",
    durationSec = duration ?: 0,
    thumbnailUrl = getBestImageUrl(),
    source = MusicSource.JIOSAAVN,
    downloadUrls = downloadUrl ?: emptyList(),
    videoId = ""
)

fun Track.getFormattedDuration(): String {
    val minutes = durationSec / 60
    val seconds = durationSec % 60
    return "%d:%02d".format(minutes, seconds)
}

/**
 * JioSaavn downloadUrl array layout (by index):
 *  0 → 12kbps   (LOW)
 *  1 → 48kbps
 *  2 → 96kbps   (MEDIUM)
 *  3 → 160kbps
 *  4 → 320kbps  (HIGH)
 *
 * We pick by quality: LOW=first, MEDIUM=middle, HIGH=last
 */
fun Track.getPlayUrl(quality: AudioQuality = AudioQuality.HIGH): String {
    return when (source) {
        MusicSource.JIOSAAVN -> {
            if (downloadUrls.isEmpty()) return ""
            when (quality) {
                AudioQuality.LOW    -> downloadUrls.first().url
                AudioQuality.MEDIUM -> downloadUrls.getOrElse(downloadUrls.size / 2) { downloadUrls.last() }.url
                AudioQuality.HIGH   -> downloadUrls.last().url
            }
        }
        MusicSource.AUDIOMACK -> videoId // videoId holds direct MP3 stream URL for Deezer
        MusicSource.YOUTUBE   -> ""      // resolved dynamically
    }
}

/** Get the actual quality label from downloadUrls (e.g. "320kbps") */
fun Track.getQualityLabel(quality: AudioQuality): String {
    if (downloadUrls.isEmpty()) return quality.name
    val url = when (quality) {
        AudioQuality.LOW    -> downloadUrls.first()
        AudioQuality.MEDIUM -> downloadUrls.getOrElse(downloadUrls.size / 2) { downloadUrls.last() }
        AudioQuality.HIGH   -> downloadUrls.last()
    }
    return url.quality  // e.g. "320kbps", "96kbps", "12kbps"
}
