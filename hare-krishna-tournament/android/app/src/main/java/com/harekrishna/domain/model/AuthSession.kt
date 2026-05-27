package com.harekrishna.domain.model

// The token is opaque to the client — the server signs it, the server validates
// it. We don't parse the JWT here; 401s from the API tell us it's expired.
data class AuthSession(
    val bhaktName: String,
    val role:      Role,
    val token:     String,
)
