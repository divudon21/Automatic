package com.vibeflow.music.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ─── JioSaavn ──────────────────────────────────────────────────────────────
    private const val SAAVN_BASE_URL = "https://saavn.sumit.co/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val saavnService: SaavnService by lazy {
        Retrofit.Builder()
            .baseUrl(SAAVN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SaavnService::class.java)
    }

    // Note: YouTube Music is now handled entirely on-device via NewPipeExtractor.
    // No Retrofit service needed for YouTube.
}
