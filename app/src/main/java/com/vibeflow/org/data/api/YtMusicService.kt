package com.vibeflow.org.data.api

import com.vibeflow.org.data.models.YtSearchResponse
import com.vibeflow.org.data.models.YtStreamResponse
import com.vibeflow.org.data.models.YtTrendingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YtMusicService {

    /** Search YouTube Music songs */
    @GET("api/search")
    suspend fun searchSongs(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): Response<YtSearchResponse>

    /** Trending songs by genre */
    @GET("api/trending")
    suspend fun getTrending(
        @Query("genre") genre: String = "bollywood"
    ): Response<YtTrendingResponse>

    /** Resolve stream URL for a videoId */
    @GET("api/stream/{videoId}")
    suspend fun getStreamUrl(
        @Path("videoId") videoId: String
    ): Response<YtStreamResponse>
}
