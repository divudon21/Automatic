package com.vibeflow.music.data.api

import com.vibeflow.music.data.models.ChartResponse
import com.vibeflow.music.data.models.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SaavnService {

    @GET("api/search/songs")
    suspend fun searchSongs(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<SearchResponse>

    @GET("api/search/songs")
    suspend fun getTrending(
        @Query("query") query: String = "top hits 2024",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<SearchResponse>

    @GET("api/search/songs")
    suspend fun getCharts(
        @Query("query") query: String = "bollywood hits",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<SearchResponse>
}
