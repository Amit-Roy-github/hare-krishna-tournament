package com.harekrishna.ui.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harekrishna.ui.home.components.CounterButton
import com.harekrishna.ui.home.components.SettingsSheet
import com.harekrishna.ui.home.components.StatTile
import com.harekrishna.ui.home.components.SyncStatusBar

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Refresh on every resume — picks up admin edits, new days, week rollovers.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Header: bhaktName + settings ────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                state.bhaktName.ifBlank { " " },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            IconButton(onClick = { showSettings = true }) {
                Icon(
                    imageVector       = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint              = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Stats ───────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatTile(label = "Today",    value = state.todayCount,    modifier = Modifier.weight(1f))
            StatTile(label = "Week",     value = state.weekTotal,     modifier = Modifier.weight(1f))
            StatTile(label = "Lifetime", value = state.lifetimeTotal, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        // ── Sync status (moved here, just below stats) ──
        SyncStatusBar(
            isSyncing = state.isSyncing,
            isPending = state.hasPending,
            syncedAt  = state.syncedAt,
            error     = state.error,
        )

        // ── Tap zone: the rest of the screen ────
        // When tap-anywhere is on, the whole zone increments. When off, only
        // the counter circle does. Nothing below the counter by design.
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
            if (state.isLoading && state.todayCount == 0) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                CounterButton(
                    count         = state.todayCount,
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
            onSignOut           = viewModel::signOut,
            onDismiss           = { showSettings = false },
        )
    }
}
