package com.harekrishna.ui.counter

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

private const val SYNC_DEBOUNCE_MS    = 300_000L  // 5 min after the last tap, auto-sync
private const val PERSIST_DEBOUNCE_MS = 2_000L    // local durability flush, independent of network
private const val SYNC_TAP_THRESHOLD  = 108       // one mala — force a sync immediately

class CounterViewModel(
    private val counterRepository: CounterRepository,
    private val authRepository:    AuthRepository,
    private val userPrefs:         UserPrefs,
    sessionPrefs:                  SessionPrefs,
) : ViewModel() {

    private val bhaktName: String = sessionPrefs.read()?.bhaktName.orEmpty()

    val uiState: StateFlow<CounterUiState> = combine(
        counterRepository.state,
        userPrefs.bounceEnabled,
        userPrefs.tapAnywhereEnabled,
    ) { counter, bounce, tapAnywhere ->
        counter.toUiState(bhaktName, bounce, tapAnywhere)
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000L),
        initialValue = CounterUiState(bhaktName = bhaktName, isLoading = true),
    )

    private val syncTrigger    = MutableSharedFlow<Unit>(extraBufferCapacity = 64)
    private val persistTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 64)

    init {
        refresh()

        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            syncTrigger.debounce(SYNC_DEBOUNCE_MS).collect { counterRepository.sync() }
        }
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            persistTrigger.debounce(PERSIST_DEBOUNCE_MS).collect { counterRepository.flush() }
        }
    }

    fun onTap() {
        counterRepository.increment()
        persistTrigger.tryEmit(Unit)
        if (counterRepository.state.value.pendingCount >= SYNC_TAP_THRESHOLD) {
            viewModelScope.launch { counterRepository.sync() }
        } else {
            syncTrigger.tryEmit(Unit)
        }
    }

    // "Send count to Krishna" — explicit immediate offer.
    fun offerToKrishna() = viewModelScope.launch {
        counterRepository.sync()
    }

    // Flush local state + best-effort sync when the screen is backgrounded, so
    // leaving the app sends the count without waiting for the 5-min window.
    fun onLeave() = viewModelScope.launch {
        counterRepository.flush()
        counterRepository.sync()
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
        counterRepository.sync()          // flush pending taps before letting go
        authRepository.signOut()
    }
}
