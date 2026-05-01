package com.vibeflow.org.data.models

import com.google.gson.annotations.SerializedName

/** Response from GET /api/search */
data class YtSearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("total") val total: Int,
    @SerializedName("results") val results: List<YtTrack>
)

/** Response from GET /api/trending */
data class YtTrendingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("genre") val genre: String,
    @SerializedName("results") val results: List<YtTrack>
)

/** Response from GET /api/stream/:videoId */
data class YtStreamResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("videoId") val videoId: String,
    @SerializedName("streamUrl") val streamUrl: String,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("source") val source: String
)

/** A single YouTube Music track from our backend */
data class YtTrack(
    @SerializedName("videoId") val videoId: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("album") val album: String
)

/** Convert YtTrack → unified Track */
fun YtTrack.toTrack(): Track = Track(
    id = videoId,
    title = title,
    artist = artist,
    album = album,
    durationSec = duration,
    thumbnailUrl = thumbnail,
    source = MusicSource.YOUTUBE,
    downloadUrls = emptyList(),
    videoId = videoId
)
