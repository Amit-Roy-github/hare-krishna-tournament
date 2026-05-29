package com.harekrishna.ui.counter

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harekrishna.ui.common.StatTile
import com.harekrishna.ui.counter.components.CounterButton
import com.harekrishna.ui.counter.components.SettingsSheet
import com.harekrishna.ui.counter.components.SyncStatusBar
import com.harekrishna.ui.theme.LocalAppPalette
import com.harekrishna.ui.theme.linear

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
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Header: back + bhaktName + settings ──
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                state.bhaktName.ifBlank { " " },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            IconButton(onClick = { showSettings = true }) {
                Icon(
                    imageVector        = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Stats (server truth) ────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatTile(label = "Today",    value = state.todayServer,   modifier = Modifier.weight(1f))
            StatTile(label = "Week",     value = state.weekTotal,     modifier = Modifier.weight(1f))
            StatTile(label = "Lifetime", value = state.lifetimeTotal, modifier = Modifier.weight(1f))
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
            if (state.isLoading && state.displayCount == 0) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                CounterButton(
                    count         = state.displayCount,
                    bounceEnabled = state.bounceEnabled,
                    clickable     = !state.tapAnywhereEnabled,
                    onTap         = onCounterTap,
                )
            }
        }

        // ── Offer to Krishna ────────────────────
        OfferButton(
            pending   = state.pendingCount,
            isSyncing = state.isSyncing,
            onClick   = viewModel::offerToKrishna,
        )
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

@Composable
private fun OfferButton(
    pending:   Int,
    isSyncing: Boolean,
    onClick:   () -> Unit,
) {
    val palette = LocalAppPalette.current
    val enabled = pending > 0 && !isSyncing
    val label = when {
        isSyncing     -> "Offering…  🙏"
        pending > 0   -> "Send to Krishna  ·  $pending"
        else          -> "All offered  🙏"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .then(
                if (enabled) Modifier.background(palette.primary.linear())
                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text      = label,
            color     = if (enabled) palette.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style     = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}
