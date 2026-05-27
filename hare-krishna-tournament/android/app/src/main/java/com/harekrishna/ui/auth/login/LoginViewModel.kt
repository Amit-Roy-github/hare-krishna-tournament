package com.harekrishna.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.data.repository.ContestantRepository
import com.harekrishna.domain.auth.AuthError
import com.harekrishna.domain.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository:       AuthRepository,
    private val contestantRepository: ContestantRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState(isLoading = true))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init { loadContestants() }

    fun onNameChange(name: String)            = _uiState.update { it.copy(bhaktName = name, error = null) }
    fun onPasswordChange(pwd: String)         = _uiState.update { it.copy(password  = pwd,  error = null) }
    fun onTogglePasswordVisibility()          = _uiState.update { it.copy(showPassword = !it.showPassword) }

    fun onSubmit() {
        val s = _uiState.value
        if (!s.canSubmit) return
        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            authRepository.login(s.bhaktName, s.password)
                .onFailure { err ->
                    val msg = (err as? AuthError)?.userMessage ?: err.message ?: "Login failed"
                    _uiState.update { it.copy(isSubmitting = false, error = msg) }
                }
                .onSuccess {
                    // SessionPrefs.flow emits the new session → AppNavigation routes to Home.
                    _uiState.update { it.copy(isSubmitting = false) }
                }
        }
    }

    private fun loadContestants() {
        viewModelScope.launch {
            runCatching { contestantRepository.list() }
                .onSuccess { list ->
                    _uiState.update { it.copy(contestants = list, isLoading = false) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to load contestants")
                    }
                }
        }
    }
}
