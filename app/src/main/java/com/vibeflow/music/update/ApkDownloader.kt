package com.vibeflow.music.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

object ApkDownloader {

    /**
     * Enqueue APK download via Android DownloadManager.
     * Returns the download ID (used by [ApkInstallReceiver] to match completion).
     */
    fun download(context: Context, apkUrl: String, versionName: String): Long {
        val fileName = "VibeFlow_v${versionName}.apk"

        val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("VibeFlow Update")
            setDescription("Downloading v$versionName...")
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            addRequestHeader("User-Agent", "VibeFlow/$versionName")
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return dm.enqueue(request)
    }
}
