package com.vibeflow.org.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ─── JioSaavn ──────────────────────────────────────────────────────────────
    // Fallback URLs in case one goes down
    val SAAVN_BASE_URLS = listOf(
        "https://saavn.sumit.co/",
        "https://saavn.me/",
        "https://jiosaavn-api-privatecvc2.vercel.app/"
    )

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun createSaavnService(baseUrl: String): SaavnService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SaavnService::class.java)
    }

    // Note: YouTube Music is now handled entirely on-device via NewPipeExtractor.
    // No Retrofit service needed for YouTube.
}
