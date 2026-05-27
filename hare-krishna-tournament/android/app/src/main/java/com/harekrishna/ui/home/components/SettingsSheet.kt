package com.harekrishna.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    bounceEnabled:        Boolean,
    tapAnywhereEnabled:   Boolean,
    onToggleBounce:       () -> Unit,
    onToggleTapAnywhere:  () -> Unit,
    onResetToday:         () -> Unit,
    onSignOut:            () -> Unit,
    onDismiss:            () -> Unit,
) {
    var showResetConfirm by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Settings",
                style    = MaterialTheme.typography.titleMedium,
                color    = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            ToggleRow(
                label    = "Bounce on tap",
                checked  = bounceEnabled,
                onChange = onToggleBounce,
            )
            ToggleRow(
                label    = "Tap anywhere to count",
                checked  = tapAnywhereEnabled,
                onChange = onToggleTapAnywhere,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            TextRow(
                label   = "Reset today's count",
                color   = MaterialTheme.colorScheme.error,
                onClick = { showResetConfirm = true },
            )
            TextRow(
                label   = "Sign out",
                color   = MaterialTheme.colorScheme.onSurface,
                onClick = onSignOut,
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title            = { Text("Reset today's display?") },
            text             = { Text("The counter starts from 0 again on this device. Your real count stays on the server — week and lifetime totals don't change. Resets at midnight automatically.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm = false
                    onResetToday()
                    onDismiss()
                }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ToggleRow(
    label:    String,
    checked:  Boolean,
    onChange: () -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Switch(checked = checked, onCheckedChange = { onChange() })
    }
}

@Composable
private fun TextRow(
    label:   String,
    color:   androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    TextButton(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = color,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        )
    }
}
