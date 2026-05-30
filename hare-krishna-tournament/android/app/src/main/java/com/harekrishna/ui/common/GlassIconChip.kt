package com.harekrishna.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.harekrishna.ui.theme.LocalAppPalette

// Premium glass icon button: a circular surface with an accent-tinted
// gradient, a thin accent border, and a soft drop shadow. Picks up the
// active palette automatically via LocalAppPalette, so it stays on-theme
// across Saffron Ember / Aurora Vivid / Feather Vivid.
@Composable
fun GlassIconChip(
    icon:               ImageVector,
    contentDescription: String?,
    onClick:            () -> Unit,
    modifier:           Modifier = Modifier,
    size:               Dp       = 44.dp,
    iconSize:           Dp       = 20.dp,
) {
    val accent = LocalAppPalette.current
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 8.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accent.primary.solid.copy(alpha = 0.22f),
                        accent.primary.solid.copy(alpha = 0.06f),
                    )
                )
            )
            .border(
                width = 1.dp,
                color = accent.primary.solid.copy(alpha = 0.35f),
                shape = CircleShape,
            )
            .clickable(
                role    = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = contentDescription,
            tint               = MaterialTheme.colorScheme.onSurface,
            modifier           = Modifier.size(iconSize),
        )
    }
}
