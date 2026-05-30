package com.harekrishna.ui.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harekrishna.ui.common.GlassIconChip
import com.harekrishna.ui.common.StatTile
import com.harekrishna.ui.counter.components.CounterButton
import com.harekrishna.ui.counter.components.SettingsSheet
import com.harekrishna.ui.counter.components.SyncStatusBar
import com.harekrishna.ui.theme.LocalAppPalette

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
    val accent = LocalAppPalette.current

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

        Spacer(Modifier.height(16.dp))

        // ── Stats card — server-truth tiles inside a labelled glass card ──
        // "Lalju server" makes it clear these numbers are what the server has,
        // distinct from the personal display counter on the tap zone below.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accent.primary.solid.copy(alpha = 0.10f),
                            accent.primary.solid.copy(alpha = 0.03f),
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = accent.primary.solid.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(14.dp),
        ) {
            Row(
                modifier          = Modifier.padding(start = 4.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent.primary.solid),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "Lalju server",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatTile(label = "Today",    value = state.todayServer,   modifier = Modifier.weight(1f))
                StatTile(label = "Week",     value = state.weekTotal,     modifier = Modifier.weight(1f))
                StatTile(label = "Lifetime", value = state.lifetimeTotal, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(12.dp))

        SyncStatusBar(
            isSyncing = state.isSyncing,
            isPending = state.hasPending,
            syncedAt  = state.syncedAt,
            error     = state.error,
        )

        // ── Tap zone (the counter) ──────────────
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
            // Always render from local state — the counter works offline; the
            // server fetch only updates the tiles, it must never gate the count.
            CounterButton(
                count         = state.displayCount,
                bounceEnabled = state.bounceEnabled,
                clickable     = !state.tapAnywhereEnabled,
                onTap         = onCounterTap,
            )
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
