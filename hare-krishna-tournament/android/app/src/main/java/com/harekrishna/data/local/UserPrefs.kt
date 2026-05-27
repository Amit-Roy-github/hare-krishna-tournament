package com.harekrishna.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.harekrishna.domain.util.TimeUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Non-secret user-controlled preferences. Plain DataStore (not encrypted) —
// these flags don't need protection.
private val Context.userPrefsDataStore by preferencesDataStore(name = "user_prefs")

class UserPrefs(context: Context) {

    private val store = context.applicationContext.userPrefsDataStore

    // ── Bounce on tap ───────────────────────────────

    val bounceEnabled: Flow<Boolean> = store.data.map { it[BOUNCE_ENABLED] ?: DEFAULT_BOUNCE }

    suspend fun setBounceEnabled(enabled: Boolean) {
        store.edit { it[BOUNCE_ENABLED] = enabled }
    }

    // ── Tap anywhere to count ───────────────────────

    val tapAnywhereEnabled: Flow<Boolean> = store.data.map { it[TAP_ANYWHERE_ENABLED] ?: DEFAULT_TAP_ANYWHERE }

    suspend fun setTapAnywhereEnabled(enabled: Boolean) {
        store.edit { it[TAP_ANYWHERE_ENABLED] = enabled }
    }

    // ── Today reset offset (client-side, server unchanged) ──
    // Stored per bhaktName so different users on the same device don't share.
    // Auto-clears at midnight UTC: the stored timestamp must be ≥ today's
    // start; otherwise the offset is treated as 0 (yesterday's reset doesn't
    // hide today's chants).

    suspend fun getTodayResetOffset(bhaktName: String): Int {
        val data   = store.data.first()
        val setAt  = data[resetSetAtKey(bhaktName)]  ?: 0L
        val offset = data[resetOffsetKey(bhaktName)] ?: 0
        return if (setAt >= TimeUtil.todayStartUtcMs()) offset else 0
    }

    suspend fun setTodayResetOffset(bhaktName: String, offset: Int) {
        store.edit {
            it[resetOffsetKey(bhaktName)] = offset
            it[resetSetAtKey(bhaktName)]  = System.currentTimeMillis()
        }
    }

    private fun resetOffsetKey(bhaktName: String) = intPreferencesKey ("today_reset_offset_$bhaktName")
    private fun resetSetAtKey (bhaktName: String) = longPreferencesKey("today_reset_set_at_$bhaktName")

    private companion object {
        val BOUNCE_ENABLED            = booleanPreferencesKey("bounce_enabled")
        val TAP_ANYWHERE_ENABLED      = booleanPreferencesKey("tap_anywhere_enabled")
        const val DEFAULT_BOUNCE         = true
        const val DEFAULT_TAP_ANYWHERE   = false
    }
}
