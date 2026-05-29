package com.harekrishna.ui.counter

import com.harekrishna.domain.model.CounterState

data class CounterUiState(
    val bhaktName:          String  = "",
    val displayCount:       Int     = 0,    // the big number (personal, continuous)
    val todayServer:        Int     = 0,    // tile: server's today total
    val weekTotal:          Int     = 0,
    val lifetimeTotal:      Int     = 0,
    val pendingCount:       Int     = 0,
    val bounceEnabled:      Boolean = true,
    val tapAnywhereEnabled: Boolean = false,
    val isSyncing:          Boolean = false,
    val isLoading:          Boolean = false,
    val syncedAt:           Long?   = null,
    val error:              String? = null,
) {
    val hasPending: Boolean get() = pendingCount > 0
}

fun CounterState.toUiState(
    bhaktName:          String,
    bounceEnabled:      Boolean,
    tapAnywhereEnabled: Boolean,
) = CounterUiState(
    bhaktName          = bhaktName,
    displayCount       = displayCount,
    todayServer        = todayServer,
    weekTotal          = weekTotal,
    lifetimeTotal      = lifetimeTotal,
    pendingCount       = pendingCount,
    bounceEnabled      = bounceEnabled,
    tapAnywhereEnabled = tapAnywhereEnabled,
    isSyncing          = isSyncing,
    isLoading          = isLoading,
    syncedAt           = syncedAt,
    error              = error,
)
