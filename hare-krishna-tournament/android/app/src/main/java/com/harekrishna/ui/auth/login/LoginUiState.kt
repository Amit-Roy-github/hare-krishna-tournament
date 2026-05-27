package com.harekrishna.ui.auth.login

import com.harekrishna.domain.model.Contestant

data class LoginUiState(
    val contestants:  List<Contestant> = emptyList(),
    val bhaktName:    String  = "",
    val password:     String  = "",
    val showPassword: Boolean = false,
    val isLoading:    Boolean = false,  // initial contestant fetch
    val isSubmitting: Boolean = false,  // login POST in flight
    val error:        String? = null,
) {
    val canSubmit: Boolean
        get() = bhaktName.isNotBlank() && password.isNotBlank() && !isSubmitting
}
