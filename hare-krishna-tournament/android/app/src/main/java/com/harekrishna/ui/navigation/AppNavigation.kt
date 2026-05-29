package com.harekrishna.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.harekrishna.di.ServiceLocator
import com.harekrishna.ui.auth.login.LoginScreen
import com.harekrishna.ui.auth.login.LoginViewModel
import com.harekrishna.ui.counter.CounterScreen
import com.harekrishna.ui.counter.CounterViewModel
import com.harekrishna.ui.home.HomeScreen
import com.harekrishna.ui.home.HomeViewModel

private enum class AuthedScreen { HOME, COUNTER }

// Single source of truth for which top-level screen renders. Observes the
// encrypted session — null → Login, present → the authenticated Home/Counter
// flow. Navigation between the two authenticated screens is a lightweight
// state switch (no nav library needed for two screens — see PRINCIPLES.md).
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
        return
    }

    // Keyed by bhaktName so a different user gets fresh ViewModels after sign-in.
    val homeVm: HomeViewModel = viewModel(
        key = "home-${current.bhaktName}",
        factory = viewModelFactory {
            initializer { HomeViewModel(services.counterRepository, services.userPrefs, services.sessionPrefs) }
        },
    )
    val counterVm: CounterViewModel = viewModel(
        key = "counter-${current.bhaktName}",
        factory = viewModelFactory {
            initializer {
                CounterViewModel(
                    counterRepository = services.counterRepository,
                    authRepository    = services.authRepository,
                    userPrefs         = services.userPrefs,
                    sessionPrefs      = services.sessionPrefs,
                )
            }
        },
    )

    var screen by rememberSaveable { mutableStateOf(AuthedScreen.HOME) }

    when (screen) {
        AuthedScreen.HOME -> HomeScreen(
            viewModel   = homeVm,
            onBeginJapa = { screen = AuthedScreen.COUNTER },
        )
        AuthedScreen.COUNTER -> {
            BackHandler { screen = AuthedScreen.HOME }
            CounterScreen(
                viewModel = counterVm,
                onBack    = { screen = AuthedScreen.HOME },
            )
        }
    }
}
