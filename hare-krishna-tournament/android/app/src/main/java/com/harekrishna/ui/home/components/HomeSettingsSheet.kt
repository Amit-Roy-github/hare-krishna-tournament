package com.harekrishna.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.harekrishna.ui.common.ActionCard
import com.harekrishna.ui.common.GlassCard
import com.harekrishna.ui.common.SectionLabel
import com.harekrishna.ui.common.SignOutRow
import com.harekrishna.ui.theme.AppPalette
import com.harekrishna.ui.theme.LocalAppPalette
import com.harekrishna.ui.theme.PaletteId
import com.harekrishna.ui.theme.Palettes
import com.harekrishna.ui.theme.linear

// Home settings BottomSheet.
//
// Account-level + appearance actions:
//   1. Theme colour       — palette picker (live preview swatches)
//   2. Change password    — opens a focused dialog
//   3. Sign out           — clears session, drops back to Login
//
// Chant-time actions (reset today, bounce, tap-anywhere) live on the Counter
// sheet so they can't be tapped accidentally outside a japa session.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSettingsSheet(
    currentPalette:    PaletteId,
    onSelectPalette:   (PaletteId) -> Unit,
    onChangePassword:  () -> Unit,
    onSignOut:         () -> Unit,
    onDismiss:         () -> Unit,
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

            SectionLabel("Theme colour")
            Spacer(Modifier.height(8.dp))

            GlassCard(accentColor = accent.primary.solid) {
                Palettes.all.forEachIndexed { index, p ->
                    if (index > 0) {
                        HorizontalDivider(
                            color    = accent.primary.solid.copy(alpha = 0.12f),
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                    PaletteRow(
                        palette  = p,
                        selected = p.id == currentPalette,
                        onClick  = { onSelectPalette(p.id) },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionLabel("Account")
            Spacer(Modifier.height(8.dp))

            ActionCard(
                icon         = Icons.Filled.Lock,
                iconTint     = MaterialTheme.colorScheme.primary,
                iconBgColor  = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                borderColor  = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                title        = "Change password",
                titleColor   = MaterialTheme.colorScheme.onSurface,
                subtitle     = "Update the password you use to sign in.",
                onClick      = onChangePassword,
            )

            Spacer(Modifier.height(20.dp))

            SignOutRow(onClick = onSignOut)
        }
    }
}

@Composable
private fun PaletteRow(
    palette:  AppPalette,
    selected: Boolean,
    onClick:  () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(palette.primary.linear()),
        )
        Text(
            palette.displayName,
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
