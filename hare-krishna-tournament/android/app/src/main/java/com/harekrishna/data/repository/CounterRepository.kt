package com.harekrishna.data.repository

import com.harekrishna.data.local.SessionPrefs
import com.harekrishna.data.local.UserPrefs
import com.harekrishna.data.remote.ApiService
import com.harekrishna.data.remote.dto.ScoreUpdateDto
import com.harekrishna.domain.model.CounterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CounterRepository(
    private val api:          ApiService,
    private val sessionPrefs: SessionPrefs,
    private val userPrefs:    UserPrefs,
) {
    private val _state = MutableStateFlow(CounterState())
    val state: StateFlow<CounterState> = _state.asStateFlow()

    suspend fun loadInitial() {
        val bhaktName = sessionPrefs.read()?.bhaktName ?: return
        _state.update { it.copy(isLoading = true, error = null) }
        runCatching {
            val scores  = api.getScores()
            val stats   = api.getStats()
            val overall = stats.overall.firstOrNull { it.bhaktName == bhaktName }
            LoadedCounts(
                today    = scores.firstOrNull { it.bhaktName == bhaktName }?.todayNaam ?: 0,
                week     = overall?.totalNaamCount    ?: 0,
                lifetime = overall?.lifetimeNaamCount ?: 0,
                offset   = userPrefs.getTodayResetOffset(bhaktName),
            )
        }.onSuccess { loaded ->
            _state.update {
                it.copy(
                    serverBaseline   = loaded.today,
                    sessionDelta     = 0,
                    weekTotal        = loaded.week,
                    lifetimeTotal    = loaded.lifetime,
                    todayResetOffset = loaded.offset,
                    syncedAt         = System.currentTimeMillis(),
                    isLoading        = false,
                    error            = null,
                )
            }
        }.onFailure {
            _state.update { it.copy(isLoading = false, error = "Couldn't load — pull to retry") }
        }
    }

    fun increment() {
        _state.update { it.copy(sessionDelta = it.sessionDelta + 1) }
    }

    suspend fun sync(): Result<Unit> {
        val snapshot = _state.value
        if (snapshot.sessionDelta == 0) return Result.success(Unit)
        val bhaktName = sessionPrefs.read()?.bhaktName
            ?: return Result.failure(IllegalStateException("No session"))

        // Capture the delta we're flushing — fresh taps during the POST stay
        // in sessionDelta and roll into the next sync. The server count is the
        // *true* cumulative (offset is purely a display thing).
        val flushedDelta = snapshot.sessionDelta
        val targetCount  = snapshot.serverBaseline + flushedDelta

        _state.update { it.copy(isSyncing = true, error = null) }
        return runCatching {
            api.updateScores(listOf(ScoreUpdateDto(bhaktName, targetCount)))
        }.onSuccess {
            _state.update {
                it.copy(
                    serverBaseline = targetCount,
                    sessionDelta   = it.sessionDelta - flushedDelta,
                    weekTotal      = it.weekTotal      + flushedDelta,
                    lifetimeTotal  = it.lifetimeTotal  + flushedDelta,
                    isSyncing      = false,
                    syncedAt       = System.currentTimeMillis(),
                    error          = null,
                )
            }
        }.onFailure {
            _state.update { it.copy(isSyncing = false, error = "Sync failed") }
        }.map { }
    }

    // Client-only reset. The server never sees this — true count stays put.
    // Stored under the user's bhaktName + a timestamp; auto-expires at the
    // next midnight UTC so yesterday's reset doesn't hide today's chants.
    suspend fun resetToday() {
        val bhaktName = sessionPrefs.read()?.bhaktName ?: return
        val current   = _state.value
        val newOffset = current.serverBaseline + current.sessionDelta
        userPrefs.setTodayResetOffset(bhaktName, newOffset)
        _state.update { it.copy(todayResetOffset = newOffset, error = null) }
    }

    private data class LoadedCounts(
        val today:    Int,
        val week:     Int,
        val lifetime: Int,
        val offset:   Int,
    )
}
