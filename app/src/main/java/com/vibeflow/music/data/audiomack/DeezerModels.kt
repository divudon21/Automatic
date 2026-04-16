package com.vibeflow.music.data.audiomack

import com.google.gson.annotations.SerializedName

// ── Deezer Search Response ─────────────────────────────────────────────────────
data class DeezerSearchResponse(
    @SerializedName("data")  val data:  List<DeezerTrack> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("next")  val next:  String? = null
)

// ── Deezer Chart Response ──────────────────────────────────────────────────────
data class DeezerChartResponse(
    @SerializedName("data") val data: List<DeezerTrack> = emptyList()
)

// ── Track ──────────────────────────────────────────────────────────────────────
data class DeezerTrack(
    @SerializedName("id")               val id:             Long,
    @SerializedName("title")            val title:          String,
    @SerializedName("title_short")      val titleShort:     String?,
    @SerializedName("duration")         val duration:       Int,           // seconds
    @SerializedName("rank")             val rank:           Long = 0,
    @SerializedName("explicit_lyrics")  val explicit:       Boolean = false,
    @SerializedName("preview")          val preview:        String,        // 30-sec MP3 preview URL
    @SerializedName("md5_image")        val md5Image:       String?,
    @SerializedName("artist")           val artist:         DeezerArtist,
    @SerializedName("album")            val album:          DeezerAlbum
)

data class DeezerArtist(
    @SerializedName("id")             val id:           Long,
    @SerializedName("name")           val name:         String,
    @SerializedName("picture_xl")     val pictureXl:    String? = null,
    @SerializedName("picture_big")    val pictureBig:   String? = null,
    @SerializedName("picture_medium") val pictureMedium:String? = null
)

data class DeezerAlbum(
    @SerializedName("id")           val id:         Long,
    @SerializedName("title")        val title:      String,
    @SerializedName("cover_xl")     val coverXl:    String? = null,
    @SerializedName("cover_big")    val coverBig:   String? = null,
    @SerializedName("cover_medium") val coverMedium:String? = null
)

// ── Best thumbnail helper ──────────────────────────────────────────────────────
fun DeezerTrack.bestThumbnail(): String =
    album.coverXl
        ?: album.coverBig
        ?: album.coverMedium
        ?: "https://cdn-images.dzcdn.net/images/cover/${md5Image}/500x500-000000-80-0-0.jpg"
        .takeIf { !md5Image.isNullOrBlank() }
        ?: ""

fun DeezerTrack.formattedDuration(): String {
    val m = duration / 60
    val s = duration % 60
    return "%d:%02d".format(m, s)
}
