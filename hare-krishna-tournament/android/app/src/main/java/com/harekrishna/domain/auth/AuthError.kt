package com.harekrishna.domain.auth

// Modeled as a sealed Throwable so it flows through Result.failure cleanly.
sealed class AuthError(val userMessage: String) : RuntimeException(userMessage) {
    data object InvalidCredentials : AuthError("Invalid credentials")
    data object NoPasswordSet      : AuthError("No password set — contact admin")
    data object Network            : AuthError("Network error — try again")
    data class  Unknown(val msg: String) : AuthError(msg)
}
