package com.harekrishna.domain.model

// Source of truth for the counter UI under the device-snapshot sync model.
//
//   displayCount  — the big on-screen number: a continuous personal counter
//                   that only the user's Reset zeroes (never midnight).
//   todayServer / weekTotal / lifetimeTotal — server truth, shown in the tiles.
//   hasPending    — there are taps not yet confirmed by the server.
//
// The per-day tap ledger (DayEntry list) lives in CounterRepository/CounterStore,
// not here — the UI never needs it.
data class CounterState(
    val displayCount:  Int     = 0,
    val todayServer:   Int     = 0,
    val weekTotal:     Int     = 0,
    val lifetimeTotal: Int     = 0,
    val pendingCount:  Int     = 0,    // taps not yet confirmed by the server
    val isLoading:     Boolean = false,
    val isSyncing:     Boolean = false,
    val error:         String? = null,
    val syncedAt:      Long?   = null,
) {
    val hasPending: Boolean get() = pendingCount > 0
}
