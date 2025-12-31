package com.mobile.soundscape

import android.content.Context
import android.util.Log

object PreferenceManager {
    private const val PREF_NAME = "soundscape_prefs"
    private const val KEY_EXPERIENCE = "is_playlist_experienced"

    fun setPlaylistExperienced(context: android.content.Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_EXPERIENCE, value).apply()
    }

    fun isPlaylistExperienced(context: android.content.Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_EXPERIENCE, false) // 기본값 false!
    }
}