package com.vibeflow.org.extractor

import android.content.Context
import android.util.Log
import org.schabi.newpipe.extractor.NewPipe
import java.util.concurrent.atomic.AtomicBoolean

object NewPipeInitializer {

    private val initialized = AtomicBoolean(false)
    private const val TAG = "NewPipeInit"

    fun init(context: Context) {
        if (initialized.getAndSet(true)) return
        try {
            NewPipe.init(NewPipeDownloader.getInstance())
            Log.d(TAG, "NewPipeExtractor v0.26.1 initialized")
        } catch (e: Exception) {
            initialized.set(false)
            Log.e(TAG, "Init failed", e)
        }
    }

    fun isReady() = initialized.get()
}
