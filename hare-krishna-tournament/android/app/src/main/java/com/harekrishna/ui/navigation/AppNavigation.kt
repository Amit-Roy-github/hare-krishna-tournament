package com.harekrishna.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.harekrishna.di.ServiceLocator
import com.harekrishna.ui.auth.login.LoginScreen
import com.harekrishna.ui.auth.login.LoginViewModel
import com.harekrishna.ui.home.HomeScreen
import com.harekrishna.ui.home.HomeViewModel

// Single source of truth for which top-level screen renders. Observes the
// encrypted session — null → Login, present → Home.
@Composable
fun AppNavigation(services: ServiceLocator) {
    val session by services.sessionPrefs.flow.collectAsStateWithLifecycle()
    val current = session

    if (current == null) {
        val vm: LoginViewModel = viewModel(
            factory = viewModelFactory {
                initializer {
                    LoginViewModel(services.authRepository, services.contestantRepository)
                }
            },
        )
        LoginScreen(viewModel = vm)
    } else {
        // Keying by bhaktName ensures a fresh ViewModel after signing in as a
        // different user (otherwise the previous user's counter would briefly
        // flash before loadInitial overwrites it).
        val vm: HomeViewModel = viewModel(
            key = "home-${current.bhaktName}",
            factory = viewModelFactory {
                initializer {
                    HomeViewModel(
                        counterRepository = services.counterRepository,
                        authRepository    = services.authRepository,
                        userPrefs         = services.userPrefs,
                        sessionPrefs      = services.sessionPrefs,
                    )
                }
            },
        )
        HomeScreen(viewModel = vm)
    }
}
