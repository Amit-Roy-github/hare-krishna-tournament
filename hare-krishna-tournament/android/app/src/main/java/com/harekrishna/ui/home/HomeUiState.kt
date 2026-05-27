package com.harekrishna.ui.home

import com.harekrishna.domain.model.CounterState

data class HomeUiState(
    val bhaktName:          String  = "",
    val todayCount:         Int     = 0,
    val weekTotal:          Int     = 0,
    val lifetimeTotal:      Int     = 0,
    val bounceEnabled:      Boolean = true,
    val tapAnywhereEnabled: Boolean = false,
    val hasPending:         Boolean = false,
    val isSyncing:          Boolean = false,
    val isLoading:          Boolean = false,
    val syncedAt:           Long?   = null,
    val error:              String? = null,
)

fun CounterState.toUiState(
    bhaktName:          String,
    bounceEnabled:      Boolean,
    tapAnywhereEnabled: Boolean,
) = HomeUiState(
    bhaktName          = bhaktName,
    todayCount         = todayCount,
    weekTotal          = weekTotal,
    lifetimeTotal      = lifetimeTotal,
    bounceEnabled      = bounceEnabled,
    tapAnywhereEnabled = tapAnywhereEnabled,
    hasPending         = hasPending,
    isSyncing          = isSyncing,
    isLoading          = isLoading,
    syncedAt           = syncedAt,
    error              = error,
)
