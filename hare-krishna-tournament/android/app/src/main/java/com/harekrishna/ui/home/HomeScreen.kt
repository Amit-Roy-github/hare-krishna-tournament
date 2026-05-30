package com.harekrishna.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harekrishna.ui.common.GlassIconChip
import com.harekrishna.ui.common.StatTile
import com.harekrishna.ui.theme.LocalAppPalette
import com.harekrishna.ui.theme.PaletteId
import com.harekrishna.ui.theme.Palettes
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
    var showPalettePicker by remember { mutableStateOf(false) }

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

        // Settings (top-right) — premium glass chip switches the colour palette.
        // statusBarsPadding() drops it clear of the system bar so it never
        // crowds the battery/clock icons on edge-to-edge devices.
        GlassIconChip(
            icon               = Icons.Filled.Settings,
            contentDescription = "Theme colour settings",
            onClick            = { showPalettePicker = true },
            modifier           = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 16.dp),
        )

        // Greeting + stats + CTA, vertically centered.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "Hare Krishna",
                style     = MaterialTheme.typography.headlineMedium,
                color     = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                state.bhaktName.ifBlank { " " },
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatTile("Today",    state.todayCount,    Modifier.weight(1f))
                StatTile("Week",     state.weekTotal,     Modifier.weight(1f))
                StatTile("Lifetime", state.lifetimeTotal, Modifier.weight(1f))
            }

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
        }
    }

    if (showPalettePicker) {
        PalettePickerSheet(
            current   = accent.id,
            onSelect  = {
                viewModel.selectPalette(it)
                showPalettePicker = false
            },
            onDismiss = { showPalettePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PalettePickerSheet(
    current:   PaletteId,
    onSelect:  (PaletteId) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Theme colour",
                style    = MaterialTheme.typography.titleMedium,
                color    = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Palettes.all.forEach { p ->
                val selected = p.id == current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(p.id) }
                        .padding(vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(p.primary.linear()),
                    )
                    Text(
                        p.displayName,
                        style    = MaterialTheme.typography.bodyLarge,
                        color    = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (selected) {
                        Icon(
                            imageVector        = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint               = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
