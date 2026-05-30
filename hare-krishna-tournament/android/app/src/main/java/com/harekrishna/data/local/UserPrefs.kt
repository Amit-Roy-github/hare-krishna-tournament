package com.harekrishna.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
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

    // ── Selected colour palette ─────────────────────
    // Stored as the PaletteId name (a String) so this data-layer class stays
    // free of any UI/theme types. The UI maps the name back to a palette.

    val selectedPalette: Flow<String?> = store.data.map { it[SELECTED_PALETTE] }

    suspend fun setSelectedPalette(id: String) {
        store.edit { it[SELECTED_PALETTE] = id }
    }

    private companion object {
        val BOUNCE_ENABLED            = booleanPreferencesKey("bounce_enabled")
        val TAP_ANYWHERE_ENABLED      = booleanPreferencesKey("tap_anywhere_enabled")
        val SELECTED_PALETTE          = stringPreferencesKey("selected_palette")
        const val DEFAULT_BOUNCE         = true
        const val DEFAULT_TAP_ANYWHERE   = false
    }
}
