package com.harekrishna.ui.counter.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.harekrishna.ui.common.ActionCard
import com.harekrishna.ui.common.GlassCard
import com.harekrishna.ui.common.SectionLabel
import com.harekrishna.ui.common.ToggleRow
import com.harekrishna.ui.theme.LocalAppPalette

// Counter settings BottomSheet — chant-time only actions:
//   1. Reset today's count — destructive, prominent at the top
//   2. Preferences         — bounce + tap-anywhere
//
// Account-level actions (Change password, Sign out) live on the Home settings
// sheet so they can't be accidentally hit mid-japa.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    bounceEnabled:        Boolean,
    tapAnywhereEnabled:   Boolean,
    onToggleBounce:       () -> Unit,
    onToggleTapAnywhere:  () -> Unit,
    onResetToday:         () -> Unit,
    onDismiss:            () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val accent     = LocalAppPalette.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 20.dp),
        ) {
            Text(
                "Settings",
                style    = MaterialTheme.typography.headlineSmall,
                color    = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp),
            )

            // Direct reset on tap — no confirm dialog. The action is local-only
            // (server count, week and lifetime stay intact), so an accidental
            // tap is recoverable just by chanting again.
            ActionCard(
                icon         = Icons.Filled.Refresh,
                iconTint     = MaterialTheme.colorScheme.error,
                iconBgColor  = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                borderColor  = MaterialTheme.colorScheme.error.copy(alpha = 0.28f),
                title        = "Reset today's count",
                titleColor   = MaterialTheme.colorScheme.error,
                subtitle     = "Start from 0 on this device. Your server count, week and lifetime totals stay unchanged.",
                onClick      = {
                    onResetToday()
                    onDismiss()
                },
            )

            Spacer(Modifier.height(20.dp))

            SectionLabel("Preferences")

            Spacer(Modifier.height(8.dp))

            GlassCard(accentColor = accent.primary.solid) {
                ToggleRow(
                    title    = "Bounce on tap",
                    subtitle = "Counter springs back with each chant",
                    checked  = bounceEnabled,
                    onChange = onToggleBounce,
                )
                HorizontalDivider(
                    color    = accent.primary.solid.copy(alpha = 0.12f),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                ToggleRow(
                    title    = "Tap anywhere to count",
                    subtitle = "Tap any part of the screen, not just the bead",
                    checked  = tapAnywhereEnabled,
                    onChange = onToggleTapAnywhere,
                )
            }
        }
    }
}
