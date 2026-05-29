package com.harekrishna.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

// One day's tap ledger on this device.
//   dayTotal  = true taps this device made that day (monotonic high-water mark)
//   daySynced = how much of dayTotal the server has confirmed
@Serializable
data class DayEntry(
    val date:      String,
    val dayTotal:  Int = 0,
    val daySynced: Int = 0,
)

// Durable counter state: a stable per-install deviceId, the user's continuous
// personal display counter, and the per-day sync ledger. Plain (unencrypted)
// DataStore — none of this is secret. Per-bhaktName so two users sharing a
// device don't collide.
private val Context.counterDataStore by preferencesDataStore(name = "counter_store")

class CounterStore(context: Context) {

    private val store = context.applicationContext.counterDataStore
    private val json  = Json { ignoreUnknownKeys = true }

    // ── Device id (one per install) ─────────────────
    suspend fun deviceId(): String {
        store.data.first()[DEVICE_ID]?.let { return it }
        val id = UUID.randomUUID().toString()
        store.edit { it[DEVICE_ID] = id }
        return id
    }

    // ── Continuous personal display counter ─────────
    suspend fun getDisplayCount(bhaktName: String): Int =
        store.data.first()[displayKey(bhaktName)] ?: 0

    suspend fun setDisplayCount(bhaktName: String, value: Int) {
        store.edit { it[displayKey(bhaktName)] = value }
    }

    // ── Per-day sync ledger ─────────────────────────
    suspend fun getDays(bhaktName: String): List<DayEntry> {
        val raw = store.data.first()[daysKey(bhaktName)] ?: return emptyList()
        return runCatching { json.decodeFromString<List<DayEntry>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun setDays(bhaktName: String, days: List<DayEntry>) {
        store.edit { it[daysKey(bhaktName)] = json.encodeToString(days) }
    }

    private fun displayKey(bhaktName: String) = intPreferencesKey("display_count_$bhaktName")
    private fun daysKey(bhaktName: String)    = stringPreferencesKey("days_$bhaktName")

    private companion object {
        val DEVICE_ID = stringPreferencesKey("device_id")
    }
}
