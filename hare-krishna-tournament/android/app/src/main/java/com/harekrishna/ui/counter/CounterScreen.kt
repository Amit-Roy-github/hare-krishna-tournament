package com.harekrishna.ui.counter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harekrishna.ui.common.GlassIconChip
import com.harekrishna.ui.common.ServerStatsCard
import com.harekrishna.ui.common.StatusDot
import com.harekrishna.ui.counter.components.CounterButton
import com.harekrishna.ui.counter.components.SettingsSheet

@Composable
fun CounterScreen(
    viewModel: CounterViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Refresh on resume; flush + sync when leaving (background or navigating away).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.refresh()
                Lifecycle.Event.ON_PAUSE  -> viewModel.onLeave()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onLeave()
        }
    }

    var showSettings by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val tapAnywhereSource = remember { MutableInteractionSource() }

    val onCounterTap: () -> Unit = {
        viewModel.onTap()
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Header: back + bhaktName + settings ──
        // Glass chips match Home's premium settings button and clear the
        // status bar so they never crowd the system icons.
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            GlassIconChip(
                icon               = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick            = onBack,
            )
            Text(
                state.bhaktName.ifBlank { " " },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            GlassIconChip(
                icon               = Icons.Filled.Settings,
                contentDescription = "Settings",
                onClick            = { showSettings = true },
            )
        }

        // ── Stats + counter as a vertically-centered group below the header ──
        // Wrapping in a weighted Box lets the stats float into the available
        // space rather than hugging the header. tap-anywhere covers this whole
        // zone so any tap (outside system UI / the back/settings chips) counts.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(
                    if (state.tapAnywhereEnabled) {
                        Modifier.clickable(
                            interactionSource = tapAnywhereSource,
                            indication        = null,
                        ) { onCounterTap() }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier            = Modifier.fillMaxWidth(),
            ) {
                // Stats card — server-truth tiles. The leading dot encodes sync
                // state via color + pulse, so we can drop the separate
                // "Synced…/Sync pending…" text below.
                ServerStatsCard(
                    todayValue    = state.todayServer,
                    weekValue     = state.weekTotal,
                    lifetimeValue = state.lifetimeTotal,
                    leading = {
                        StatusDot(
                            animating = state.isSyncing || state.hasPending,
                            color     = if (state.error != null)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary,
                        )
                    },
                )

                Spacer(Modifier.height(36.dp))

                // Always render from local state — the counter works offline;
                // the server fetch only updates the tiles, it must never gate
                // the count.
                CounterButton(
                    count         = state.displayCount,
                    bounceEnabled = state.bounceEnabled,
                    clickable     = !state.tapAnywhereEnabled,
                    onTap         = onCounterTap,
                )
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            bounceEnabled       = state.bounceEnabled,
            tapAnywhereEnabled  = state.tapAnywhereEnabled,
            onToggleBounce      = viewModel::onToggleBounce,
            onToggleTapAnywhere = viewModel::onToggleTapAnywhere,
            onResetToday        = viewModel::onResetToday,
            onDismiss           = { showSettings = false },
        )
    }
}
