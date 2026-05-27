package com.harekrishna.domain.auth

import com.harekrishna.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Reactive stream of the current session. Emits null when signed out. */
    val session: Flow<AuthSession?>

    suspend fun login(bhaktName: String, password: String): Result<AuthSession>
    suspend fun changePassword(current: String, new: String): Result<Unit>
    suspend fun currentSession(): AuthSession?
    suspend fun signOut()
}
