package com.harekrishna.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// Dark-first. The color scheme is derived from the active AppPalette, so
// switching palettes recolours the whole app. Gradient accents (counter, CTA)
// read LocalAppPalette directly — see Palette.kt.
@Composable
fun HareKrishnaTheme(
    palette: AppPalette = Palettes.default,
    content: @Composable () -> Unit,
) {
    val scheme = darkColorScheme(
        primary          = palette.primary.solid,
        onPrimary        = palette.onPrimary,
        secondary        = palette.secondary.solid,
        onSecondary      = palette.text,
        tertiary         = palette.tertiary,
        onTertiary       = palette.onPrimary,
        background       = palette.background,
        onBackground     = palette.text,
        surface          = palette.surface,
        onSurface        = palette.text,
        surfaceVariant   = palette.surfaceRaised,
        onSurfaceVariant = palette.muted,
        error            = ErrorRed,
        onError          = palette.text,
    )

    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography  = AppTypography,
            content     = content,
        )
    }
}
