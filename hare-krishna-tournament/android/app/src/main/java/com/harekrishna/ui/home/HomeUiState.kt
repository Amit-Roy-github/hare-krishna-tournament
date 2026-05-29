package com.harekrishna.ui.home

import com.harekrishna.domain.model.CounterState

// The landing page only needs a read-only glance of the counter totals plus
// the bhakta's name. The actual counting lives on the Counter page.
data class HomeUiState(
    val bhaktName:     String  = "",
    val todayCount:    Int     = 0,
    val weekTotal:     Int     = 0,
    val lifetimeTotal: Int     = 0,
    val isLoading:     Boolean = false,
)

fun CounterState.toHomeUiState(bhaktName: String) = HomeUiState(
    bhaktName     = bhaktName,
    todayCount    = todayServer,
    weekTotal     = weekTotal,
    lifetimeTotal = lifetimeTotal,
    isLoading     = isLoading,
)
