package com.vibeflow.org.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore extension — single instance per app
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vibeflow_prefs")

enum class DownloadQualityPref { ALWAYS_ASK, LOW, MEDIUM, HIGH }
enum class ThemePref { SYSTEM, LIGHT, DARK }

enum class AppColor { PURPLE, BLUE, GREEN, RED, ORANGE }

object AppPreferences {

    private val DOWNLOAD_QUALITY_KEY   = stringPreferencesKey("download_quality")
    private val THEME_KEY              = stringPreferencesKey("theme_pref")
    private val APP_COLOR_KEY          = stringPreferencesKey("app_color_pref")
    private val AUTOPLAY_KEY           = androidx.datastore.preferences.core.booleanPreferencesKey("autoplay_next")
    private val REMEMBER_LAST_KEY      = androidx.datastore.preferences.core.booleanPreferencesKey("remember_last_played")
    private val LAST_TRACK_ID_KEY      = stringPreferencesKey("last_track_id")
    private val LAST_TRACK_SOURCE_KEY  = stringPreferencesKey("last_track_source")

    fun getDownloadQuality(context: Context): Flow<DownloadQualityPref> =
        context.dataStore.data.map { prefs ->
            when (prefs[DOWNLOAD_QUALITY_KEY]) {
                "LOW"    -> DownloadQualityPref.LOW
                "MEDIUM" -> DownloadQualityPref.MEDIUM
                "HIGH"   -> DownloadQualityPref.HIGH
                else     -> DownloadQualityPref.ALWAYS_ASK
            }
        }

    suspend fun setDownloadQuality(context: Context, pref: DownloadQualityPref) {
        context.dataStore.edit { it[DOWNLOAD_QUALITY_KEY] = pref.name }
    }

    fun getTheme(context: Context): Flow<ThemePref> =
        context.dataStore.data.map { prefs ->
            when (prefs[THEME_KEY]) {
                "LIGHT" -> ThemePref.LIGHT
                "DARK"  -> ThemePref.DARK
                else    -> ThemePref.SYSTEM
            }
        }

    suspend fun setTheme(context: Context, pref: ThemePref) {
        context.dataStore.edit { it[THEME_KEY] = pref.name }
    }
    
    fun getAppColor(context: Context): Flow<AppColor> =
        context.dataStore.data.map { prefs ->
            when (prefs[APP_COLOR_KEY]) {
                "BLUE" -> AppColor.BLUE
                "GREEN" -> AppColor.GREEN
                "RED" -> AppColor.RED
                "ORANGE" -> AppColor.ORANGE
                else -> AppColor.PURPLE
            }
        }

    suspend fun setAppColor(context: Context, color: AppColor) {
        context.dataStore.edit { it[APP_COLOR_KEY] = color.name }
    }

    fun getAutoPlay(context: Context): kotlinx.coroutines.flow.Flow<Boolean> =
        context.dataStore.data.map { it[AUTOPLAY_KEY] ?: true }

    suspend fun setAutoPlay(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[AUTOPLAY_KEY] = enabled }
    }

    fun getRememberLastPlayed(context: Context): kotlinx.coroutines.flow.Flow<Boolean> =
        context.dataStore.data.map { it[REMEMBER_LAST_KEY] ?: true }

    suspend fun setRememberLastPlayed(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[REMEMBER_LAST_KEY] = enabled }
    }

    suspend fun saveLastTrack(context: Context, trackId: String, source: String) {
        context.dataStore.edit {
            it[LAST_TRACK_ID_KEY]     = trackId
            it[LAST_TRACK_SOURCE_KEY] = source
        }
    }

    fun getLastTrackId(context: Context): kotlinx.coroutines.flow.Flow<String> =
        context.dataStore.data.map { it[LAST_TRACK_ID_KEY] ?: "" }
}
