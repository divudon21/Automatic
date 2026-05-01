package com.vibeflow.org

import android.app.Application
import com.vibeflow.org.extractor.NewPipeInitializer

class VibeFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize NewPipeExtractor v0.26.1 once at app startup
        // This sets up the OkHttp downloader for all YouTube requests
        NewPipeInitializer.init(this)
    }
}
