package com.vibeflow.music.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

/**
 * Listens for DownloadManager completion and auto-triggers APK install.
 *
 * Handles:
 *  - Android 7+ FileProvider (no exposed file:// URIs)
 *  - Android 8+ REQUEST_INSTALL_PACKAGES check
 *  - Matches only our own download IDs via shared prefs
 */
class ApkInstallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ApkInstall"
        const val PREF_NAME = "vibeflow_update"
        const val KEY_DOWNLOAD_ID = "pending_download_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return

        val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (completedId == -1L) return

        // Check if this is our update download
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val pendingId = prefs.getLong(KEY_DOWNLOAD_ID, -1L)
        if (completedId != pendingId) return

        Log.d(TAG, "Update APK download complete (id=$completedId)")

        // Clear stored id
        prefs.edit().remove(KEY_DOWNLOAD_ID).apply()

        // Get downloaded file path from DownloadManager
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(completedId)
        val cursor = dm.query(query)

        if (!cursor.moveToFirst()) {
            Log.w(TAG, "DownloadManager cursor empty")
            cursor.close()
            return
        }

        val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status = cursor.getInt(statusCol)

        if (status != DownloadManager.STATUS_SUCCESSFUL) {
            Log.w(TAG, "Download not successful: $status")
            cursor.close()
            return
        }

        val uriCol = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
        val localUri = cursor.getString(uriCol)
        cursor.close()

        if (localUri.isNullOrBlank()) {
            Log.w(TAG, "Local URI is null")
            return
        }

        installApk(context, localUri)
    }

    private fun installApk(context: Context, localUri: String) {
        try {
            // Android 8+: check install permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val canInstall = context.packageManager
                    .canRequestPackageInstalls()
                if (!canInstall) {
                    Log.w(TAG, "REQUEST_INSTALL_PACKAGES not granted — opening settings")
                    val settingsIntent = Intent(
                        android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(settingsIntent)
                    return
                }
            }

            val file = File(Uri.parse(localUri).path ?: return)

            // Android 7+: use FileProvider instead of file:// URI
            val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Log.d(TAG, "Launching install for: $apkUri")
            context.startActivity(installIntent)

        } catch (e: Exception) {
            Log.e(TAG, "Install failed: ${e.message}")
        }
    }
}
