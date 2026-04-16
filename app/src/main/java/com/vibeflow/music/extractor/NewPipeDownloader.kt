package com.vibeflow.music.extractor

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.util.concurrent.TimeUnit

class NewPipeDownloader private constructor(
    private val client: OkHttpClient
) : Downloader() {

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/124.0.0.0 Safari/537.36"

        @Volatile private var INSTANCE: NewPipeDownloader? = null

        fun getInstance(): NewPipeDownloader = INSTANCE ?: synchronized(this) {
            INSTANCE ?: NewPipeDownloader(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()
            ).also { INSTANCE = it }
        }
    }

    override fun execute(request: Request): Response {
        val body: RequestBody? = request.dataToSend()?.toRequestBody()

        val reqBuilder = okhttp3.Request.Builder()
            .method(request.httpMethod(), body)
            .url(request.url())
            .header("User-Agent", USER_AGENT)

        for ((name, values) in request.headers()) {
            if (values.size > 1) {
                reqBuilder.removeHeader(name)
                values.forEach { reqBuilder.addHeader(name, it) }
            } else if (values.size == 1) {
                reqBuilder.header(name, values[0])
            }
        }

        val response = client.newCall(reqBuilder.build()).execute()

        if (response.code == 429) {
            response.close()
            throw ReCaptchaException("reCaptcha requested", request.url())
        }

        val responseBody = response.body?.string()
        val latestUrl   = response.request.url.toString()

        return Response(
            response.code,
            response.message,
            response.headers.toMultimap(),
            responseBody,
            latestUrl
        )
    }
}
