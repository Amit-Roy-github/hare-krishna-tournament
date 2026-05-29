package com.harekrishna.data.repository

import com.harekrishna.data.local.CounterStore
import com.harekrishna.data.local.DayEntry
import com.harekrishna.data.local.SessionPrefs
import com.harekrishna.data.remote.ApiService
import com.harekrishna.data.remote.dto.NaamSyncDto
import com.harekrishna.domain.model.CounterState
import com.harekrishna.domain.util.TimeUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Device-snapshot sync. Each tap bumps today's per-day high-water mark and the
// continuous display counter. sync() sends each un-synced day's absolute total;
// the server adds only the new part (idempotent). The display counter is the
// user's own — only resetToday() zeroes it, never the day boundary.
class CounterRepository(
    private val api:          ApiService,
    private val sessionPrefs: SessionPrefs,
    private val store:        CounterStore,
) {
    private val _state = MutableStateFlow(CounterState())
    val state: StateFlow<CounterState> = _state.asStateFlow()

    private var loadedFor:    String = ""
    private var deviceId:     String = ""
    private var displayCount: Int    = 0
    private val days = mutableListOf<DayEntry>()

    private fun bhakt(): String? = sessionPrefs.read()?.bhaktName

    suspend fun loadInitial() {
        val bhaktName = bhakt() ?: return
        _state.update { it.copy(isLoading = true, error = null) }

        // Local first — display shows instantly, even offline.
        if (deviceId.isEmpty()) deviceId = store.deviceId()
        displayCount = store.getDisplayCount(bhaktName)
        days.clear()
        days.addAll(store.getDays(bhaktName))
        loadedFor = bhaktName
        emit()

        // Then reconcile the server-truth tiles.
        runCatching {
            val scores  = api.getScores()
            val stats   = api.getStats()
            val overall = stats.overall.firstOrNull { it.bhaktName == bhaktName }
            Triple(
                scores.firstOrNull { it.bhaktName == bhaktName }?.todayNaam ?: 0,
                overall?.totalNaamCount    ?: 0,
                overall?.lifetimeNaamCount ?: 0,
            )
        }.onSuccess { (today, week, lifetime) ->
            _state.update {
                it.copy(
                    todayServer   = today,
                    weekTotal     = week,
                    lifetimeTotal = lifetime,
                    isLoading     = false,
                    error         = null,
                    syncedAt      = System.currentTimeMillis(),
                )
            }
        }.onFailure {
            _state.update { it.copy(isLoading = false, error = "Couldn't load — pull to retry") }
        }
    }

    // RAM-only — instant. Durability comes from flush()/sync().
    fun increment() {
        val today = TimeUtil.todayKeyUtc()
        val idx = days.indexOfFirst { it.date == today }
        if (idx >= 0) days[idx] = days[idx].copy(dayTotal = days[idx].dayTotal + 1)
        else days.add(DayEntry(date = today, dayTotal = 1, daySynced = 0))
        displayCount += 1
        emit()
    }

    // Persist local state without touching the network (durability backstop).
    suspend fun flush() {
        val bhaktName = loadedFor.ifEmpty { bhakt() ?: return }
        store.setDisplayCount(bhaktName, displayCount)
        store.setDays(bhaktName, days.toList())
    }

    suspend fun sync(): Result<Unit> {
        val bhaktName = bhakt() ?: return Result.failure(IllegalStateException("No session"))
        if (deviceId.isEmpty()) deviceId = store.deviceId()

        val pending = days.filter { it.dayTotal > it.daySynced }
        if (pending.isEmpty()) {
            flush()
            return Result.success(Unit)
        }

        _state.update { it.copy(isSyncing = true, error = null) }
        val flushing = pending.map { it.date to it.dayTotal }   // snapshot what we send

        return runCatching {
            api.syncNaam(flushing.map { (date, total) -> NaamSyncDto(deviceId, date, total) })
        }.onSuccess { resp ->
            // Advance each flushed day's synced mark to the total we sent. Taps
            // that arrived during the POST stay pending and roll into next sync.
            for ((date, total) in flushing) {
                val i = days.indexOfFirst { it.date == date }
                if (i >= 0) days[i] = days[i].copy(daySynced = maxOf(days[i].daySynced, total))
            }
            val today = TimeUtil.todayKeyUtc()
            days.removeAll { it.date < today && it.dayTotal == it.daySynced }

            val todayServer = resp.days.firstOrNull { it.date == today }?.naamJaapCount
                ?: resp.scores.firstOrNull { it.bhaktName == bhaktName }?.todayNaam

            _state.update {
                it.copy(
                    todayServer  = todayServer ?: it.todayServer,
                    pendingCount = days.sumOf { d -> (d.dayTotal - d.daySynced).coerceAtLeast(0) },
                    isSyncing    = false,
                    syncedAt     = System.currentTimeMillis(),
                    error        = null,
                )
            }
            flush()
        }.onFailure {
            _state.update { it.copy(isSyncing = false, error = "Sync failed") }
        }.map { }
    }

    // Client-only display reset. The server never sees it — the per-day ledger
    // (dayTotal/daySynced) is untouched, so the true contribution is preserved.
    suspend fun resetToday() {
        val bhaktName = bhakt() ?: return
        displayCount = 0
        emit()
        store.setDisplayCount(bhaktName, 0)
    }

    private fun emit() {
        val pending = days.sumOf { (it.dayTotal - it.daySynced).coerceAtLeast(0) }
        _state.update { it.copy(displayCount = displayCount, pendingCount = pending) }
    }
}
