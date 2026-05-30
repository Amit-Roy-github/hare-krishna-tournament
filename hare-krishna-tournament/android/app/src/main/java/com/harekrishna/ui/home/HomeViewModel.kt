package com.harekrishna.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.data.local.SessionPrefs
import com.harekrishna.data.local.UserPrefs
import com.harekrishna.data.repository.CounterRepository
import com.harekrishna.domain.auth.AuthRepository
import com.harekrishna.ui.theme.PaletteId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val counterRepository: CounterRepository,
    private val authRepository:    AuthRepository,
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

    // Suspend so the dialog can await the result and surface success/error
    // inline (no extra StateFlow plumbing for a one-shot action).
    suspend fun changePassword(current: String, new: String): Result<Unit> =
        authRepository.changePassword(current, new)

    fun signOut() = viewModelScope.launch {
        counterRepository.sync()      // flush pending taps before letting go
        authRepository.signOut()
    }

    init { refresh() }
}
