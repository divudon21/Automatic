package com.vibeflow.org.update

data class UpdateInfo(
    val latestVersionName: String,
    val latestVersionCode: Int,
    val tagName: String,
    val apkDownloadUrl: String,
    val releaseNotes: String,
    val publishedAt: String
)

/** Result from [UpdateChecker.checkForUpdate] */
sealed class UpdateResult {
    data class UpdateAvailable(val info: UpdateInfo) : UpdateResult()
    object UpToDate : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

/** UI state for the update section in Settings */
sealed class UpdateUiState {
    object Idle : UpdateUiState()
    object Checking : UpdateUiState()
    object UpToDate : UpdateUiState()
    data class UpdateAvailable(val info: UpdateInfo) : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}
