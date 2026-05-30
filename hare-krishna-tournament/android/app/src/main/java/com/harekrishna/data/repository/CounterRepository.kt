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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    // Serializes sync() calls. Without this, every tap above the 108 threshold
    // spawns a parallel POST with the same payload — wasting bandwidth, racing
    // the snapshot on the server, and overloading the Vercel function until
    // it times out.
    private val syncMutex = Mutex()

    private fun bhakt(): String? = sessionPrefs.read()?.bhaktName

    suspend fun loadInitial() {
        val bhaktName = bhakt() ?: return

        // Hydrate local state ONCE per user (or after a process restart). The
        // in-memory repo is the source of truth afterwards, so re-entering the
        // screen never reloads from disk and never erases the live count.
        if (loadedFor != bhaktName) {
            if (deviceId.isEmpty()) deviceId = store.deviceId()
            displayCount = store.getDisplayCount(bhaktName)
            days.clear()
            days.addAll(store.getDays(bhaktName))
            loadedFor = bhaktName
            emit()
        }

        // Refresh server-truth tiles in the background using the standalone
        // per-user endpoint — NOT the leaderboard (`/api/scores`,`/api/stats`),
        // which filters by `includeInKeliKunj` and would hide a user who isn't
        // in the active tournament. The counter itself runs entirely offline
        // from local state; a failed fetch only leaves the tiles stale.
        _state.update { it.copy(isLoading = true) }
        runCatching { api.getMyStats() }
            .onSuccess { stats ->
                _state.update {
                    it.copy(
                        todayServer   = stats.todayNaam,
                        weekTotal     = stats.weekTotal,
                        lifetimeTotal = stats.lifetimeNaam,
                        isLoading     = false,
                        syncedAt      = System.currentTimeMillis(),
                    )
                }
            }
            .onFailure {
                _state.update { it.copy(isLoading = false) }
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

    // Mutex-guarded so concurrent triggers (per-tap threshold, debounce, leave,
    // sign-out) can't pile up parallel POSTs of the same payload. If a sync is
    // already in flight, additional callers wait their turn — they'll usually
    // find pending=empty and short-circuit, since the in-flight one already
    // covered the data.
    suspend fun sync(): Result<Unit> = syncMutex.withLock {
        val bhaktName = bhakt() ?: return@withLock Result.failure(IllegalStateException("No session"))
        if (deviceId.isEmpty()) deviceId = store.deviceId()

        val pending = days.filter { it.dayTotal > it.daySynced }
        if (pending.isEmpty()) {
            flush()
            return@withLock Result.success(Unit)
        }

        _state.update { it.copy(isSyncing = true, error = null) }
        val flushing = pending.map { it.date to it.dayTotal }   // snapshot what we send

        runCatching {
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

            // Prefer the per-day naamJaapCount the server echoed back for
            // today; fall back to the user's own stats block. Both are this
            // user only — no leaderboard data anywhere in this path.
            val todayServer = resp.days.firstOrNull { it.date == today }?.naamJaapCount
                ?: resp.stats?.todayNaam

            _state.update {
                it.copy(
                    todayServer   = todayServer       ?: it.todayServer,
                    weekTotal     = resp.stats?.weekTotal    ?: it.weekTotal,
                    lifetimeTotal = resp.stats?.lifetimeNaam ?: it.lifetimeTotal,
                    pendingCount  = days.sumOf { d -> (d.dayTotal - d.daySynced).coerceAtLeast(0) },
                    isSyncing     = false,
                    syncedAt      = System.currentTimeMillis(),
                    error         = null,
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
