package com.harekrishna.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harekrishna.ui.common.GlassIconChip
import com.harekrishna.ui.common.ServerStatsCard
import com.harekrishna.ui.common.StatusDot
import com.harekrishna.ui.home.components.ChangePasswordDialog
import com.harekrishna.ui.home.components.HomeSettingsSheet
import com.harekrishna.ui.theme.LocalAppPalette
import com.harekrishna.ui.theme.linear

@Composable
fun HomeScreen(
    viewModel:   HomeViewModel,
    onBeginJapa: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Refresh totals whenever the landing resumes (e.g. back from Counter).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val accent = LocalAppPalette.current
    var showSettings        by remember { mutableStateOf(false) }
    var showChangePassword  by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Soft accent bloom top-left for premium depth.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.primary.solid.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 900f,
                    )
                )
        )

        // Settings (top-right). statusBarsPadding() drops it clear of the
        // system bar so it never crowds the battery/clock icons.
        GlassIconChip(
            icon               = Icons.Filled.Settings,
            contentDescription = "Settings",
            onClick            = { showSettings = true },
            modifier           = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 16.dp),
        )

        // Greeting at upper-mid, then stats and CTA in the lower half.
        // Weighted spacers keep the title floating in the top third regardless
        // of screen height; the cards below stay anchored as a unit.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.9f))

            // "Hare Krishna" hero — display-sized Serif painted with the live
            // accent gradient, so switching palettes recolours it instantly.
            Text(
                "Hare Krishna",
                style = MaterialTheme.typography.displayMedium.copy(
                    brush = Brush.horizontalGradient(accent.primary.stops),
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                state.bhaktName.ifBlank { " " },
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.2.sp),
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1.4f))

            ServerStatsCard(
                todayValue    = state.todayCount,
                weekValue     = state.weekTotal,
                lifetimeValue = state.lifetimeTotal,
                leading       = { StatusDot() },
            )

            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(accent.primary.linear())
                    .clickable { onBeginJapa() },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accent.onPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("◉", color = accent.onPrimary, style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text(
                            "Begin Japa",
                            style = MaterialTheme.typography.headlineSmall,
                            color = accent.onPrimary,
                        )
                        Text(
                            "Tap to open your counter  →",
                            style = MaterialTheme.typography.bodySmall,
                            color = accent.onPrimary.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            Spacer(Modifier.weight(0.9f))
        }
    }

    if (showSettings) {
        HomeSettingsSheet(
            currentPalette   = accent.id,
            onSelectPalette  = { viewModel.selectPalette(it) },
            onChangePassword = {
                showSettings       = false
                showChangePassword = true
            },
            onSignOut        = {
                showSettings = false
                viewModel.signOut()
            },
            onDismiss        = { showSettings = false },
        )
    }

    if (showChangePassword) {
        ChangePasswordDialog(
            onSubmit  = viewModel::changePassword,
            onSuccess = { showChangePassword = false },
            onDismiss = { showChangePassword = false },
        )
    }
}
