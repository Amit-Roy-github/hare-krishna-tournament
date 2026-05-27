package com.harekrishna.domain.model

// Source of truth for the counter UI.
//   display today = max(0, serverBaseline + sessionDelta - todayResetOffset)
// The offset is a client-only display tweak — the server retains the true
// cumulative count. weekTotal / lifetimeTotal always show server truth.
data class CounterState(
    val serverBaseline:   Int  = 0,    // last value the server confirmed for today
    val sessionDelta:     Int  = 0,    // taps since last successful sync
    val todayResetOffset: Int  = 0,    // local "hide everything before this point" offset
    val weekTotal:        Int  = 0,    // Monday-to-today total (server)
    val lifetimeTotal:    Int  = 0,    // all-time total (server)
    val syncedAt:         Long? = null,
    val isLoading:        Boolean = false,
    val isSyncing:        Boolean = false,
    val error:            String? = null,
) {
    val todayCount: Int get() = (serverBaseline + sessionDelta - todayResetOffset).coerceAtLeast(0)
    val hasPending: Boolean   get() = sessionDelta > 0
}
