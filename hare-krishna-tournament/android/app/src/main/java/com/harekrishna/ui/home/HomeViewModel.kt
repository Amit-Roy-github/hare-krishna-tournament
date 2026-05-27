package com.harekrishna.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.data.local.SessionPrefs
import com.harekrishna.data.local.UserPrefs
import com.harekrishna.data.repository.CounterRepository
import com.harekrishna.domain.auth.AuthRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SYNC_DEBOUNCE_MS    = 3_000L  // settle window after the last tap
private const val SYNC_TAP_THRESHOLD  = 25      // force a sync after this many unsynced taps

class HomeViewModel(
    private val counterRepository: CounterRepository,
    private val authRepository:    AuthRepository,
    private val userPrefs:         UserPrefs,
    sessionPrefs:                  SessionPrefs,
) : ViewModel() {

    private val bhaktName: String = sessionPrefs.read()?.bhaktName.orEmpty()

    val uiState: StateFlow<HomeUiState> = combine(
        counterRepository.state,
        userPrefs.bounceEnabled,
        userPrefs.tapAnywhereEnabled,
    ) { counter, bounce, tapAnywhere ->
        counter.toUiState(bhaktName, bounce, tapAnywhere)
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState(bhaktName = bhaktName, isLoading = true),
    )

    private val tapTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 64)

    init {
        refresh()

        // Sync 3s after the last tap (resets on every new tap).
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            tapTrigger.debounce(SYNC_DEBOUNCE_MS).collect {
                counterRepository.sync()
            }
        }
    }

    fun onTap() {
        counterRepository.increment()
        val deltaNow = counterRepository.state.value.sessionDelta
        if (deltaNow >= SYNC_TAP_THRESHOLD) {
            viewModelScope.launch { counterRepository.sync() }
        } else {
            tapTrigger.tryEmit(Unit)
        }
    }

    fun onToggleBounce() = viewModelScope.launch {
        userPrefs.setBounceEnabled(!userPrefs.bounceEnabled.first())
    }

    fun onToggleTapAnywhere() = viewModelScope.launch {
        userPrefs.setTapAnywhereEnabled(!userPrefs.tapAnywhereEnabled.first())
    }

    fun onResetToday() = viewModelScope.launch {
        counterRepository.resetToday()
    }

    fun refresh() = viewModelScope.launch {
        counterRepository.loadInitial()
    }

    fun signOut() = viewModelScope.launch {
        counterRepository.sync()          // flush any pending taps before letting go
        authRepository.signOut()
    }
}
