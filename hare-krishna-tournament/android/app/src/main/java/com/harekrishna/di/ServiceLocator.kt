package com.harekrishna.di

import android.content.Context
import com.harekrishna.data.auth.RealAuthRepository
import com.harekrishna.data.local.CounterStore
import com.harekrishna.data.local.SessionPrefs
import com.harekrishna.data.local.UserPrefs
import com.harekrishna.data.remote.ApiClient
import com.harekrishna.data.repository.ContestantRepository
import com.harekrishna.data.repository.CounterRepository
import com.harekrishna.domain.auth.AuthRepository

// Manual DI for v1 — see android/PRINCIPLES.md for when to introduce Hilt.
// All instances are app-scoped singletons; this class is held by HareKrishnaApp.
class ServiceLocator(context: Context) {

    private val appContext = context.applicationContext

    val sessionPrefs: SessionPrefs by lazy { SessionPrefs(appContext) }
    val userPrefs:    UserPrefs    by lazy { UserPrefs(appContext) }
    val counterStore: CounterStore by lazy { CounterStore(appContext) }

    private val api by lazy { ApiClient.create(sessionPrefs) }

    val authRepository: AuthRepository by lazy { RealAuthRepository(api, sessionPrefs) }

    val contestantRepository: ContestantRepository by lazy { ContestantRepository(api) }

    val counterRepository:    CounterRepository    by lazy { CounterRepository(api, sessionPrefs, counterStore) }
}
