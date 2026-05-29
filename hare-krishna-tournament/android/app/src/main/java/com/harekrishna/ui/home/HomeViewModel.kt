package com.harekrishna.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.data.local.SessionPrefs
import com.harekrishna.data.local.UserPrefs
import com.harekrishna.data.repository.CounterRepository
import com.harekrishna.ui.theme.PaletteId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val counterRepository: CounterRepository,
    private val userPrefs:         UserPrefs,
    sessionPrefs:                  SessionPrefs,
) : ViewModel() {

    private val bhaktName: String = sessionPrefs.read()?.bhaktName.orEmpty()

    val uiState: StateFlow<HomeUiState> = counterRepository.state
        .map { it.toHomeUiState(bhaktName) }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeUiState(bhaktName = bhaktName, isLoading = true),
        )

    fun refresh() = viewModelScope.launch {
        counterRepository.loadInitial()
    }

    fun selectPalette(id: PaletteId) = viewModelScope.launch {
        userPrefs.setSelectedPalette(id.name)
    }

    init { refresh() }
}
