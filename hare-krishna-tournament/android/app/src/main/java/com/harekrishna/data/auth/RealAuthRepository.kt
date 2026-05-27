package com.harekrishna.data.auth

import com.harekrishna.data.local.SessionPrefs
import com.harekrishna.data.mapper.toDomain
import com.harekrishna.data.remote.ApiService
import com.harekrishna.data.remote.HttpStatus
import com.harekrishna.data.remote.dto.ChangePasswordRequestDto
import com.harekrishna.data.remote.dto.LoginRequestDto
import com.harekrishna.domain.auth.AuthError
import com.harekrishna.domain.auth.AuthRepository
import com.harekrishna.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

class RealAuthRepository(
    private val api:   ApiService,
    private val prefs: SessionPrefs,
) : AuthRepository {

    override val session: Flow<AuthSession?> = prefs.flow

    override suspend fun login(bhaktName: String, password: String): Result<AuthSession> = runCatching {
        api.login(LoginRequestDto(bhaktName.trim(), password))
            .toDomain()
            .also { prefs.save(it) }
    }.recoverCatching { throw mapError(it) }

    override suspend fun changePassword(current: String, new: String): Result<Unit> = runCatching {
        api.changePassword(ChangePasswordRequestDto(current, new))
    }.recoverCatching { throw mapError(it) }

    override suspend fun currentSession(): AuthSession? = prefs.read()

    override suspend fun signOut() {
        prefs.clear()
    }

    private fun mapError(t: Throwable): AuthError = when {
        t is HttpException && t.code() == HttpStatus.UNAUTHORIZED.code -> AuthError.InvalidCredentials
        t is HttpException && t.code() == HttpStatus.BAD_REQUEST.code  -> AuthError.NoPasswordSet
        t is IOException                                                -> AuthError.Network
        else                                                            -> AuthError.Unknown(t.message ?: "Login failed")
    }
}
