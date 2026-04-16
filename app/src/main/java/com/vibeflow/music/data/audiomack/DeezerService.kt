package com.vibeflow.music.data.audiomack

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Deezer Public API — No API key required.
 * Base URL: https://api.deezer.com/
 *
 * All endpoints are public and rate-limited by IP only.
 * preview field = direct 30-sec MP3 stream URL (always works in ExoPlayer).
 */
interface DeezerService {

    /** Search tracks by query */
    @GET("search")
    suspend fun searchTracks(
        @Query("q")      query:  String,
        @Query("limit")  limit:  Int = 30,
        @Query("index")  offset: Int = 0,
        @Query("output") output: String = "json"
    ): Response<DeezerSearchResponse>

    /** Global top chart tracks */
    @GET("chart/0/tracks")
    suspend fun getGlobalChart(
        @Query("limit") limit: Int = 25
    ): Response<DeezerChartResponse>

    /** Genre-specific chart — genre IDs below:
     *  0   = All
     *  116 = Pop
     *  132 = Rap/Hip-Hop
     *  152 = R&B
     *  113 = Dance
     *  165 = Alternative
     *  197 = Electro
     *  144 = Reggae
     */
    @GET("chart/{genreId}/tracks")
    suspend fun getGenreChart(
        @retrofit2.http.Path("genreId") genreId: Int,
        @Query("limit") limit: Int = 25
    ): Response<DeezerChartResponse>

    /** Artist top tracks */
    @GET("artist/{artistId}/top")
    suspend fun getArtistTop(
        @retrofit2.http.Path("artistId") artistId: Long,
        @Query("limit") limit: Int = 20
    ): Response<DeezerSearchResponse>
}
