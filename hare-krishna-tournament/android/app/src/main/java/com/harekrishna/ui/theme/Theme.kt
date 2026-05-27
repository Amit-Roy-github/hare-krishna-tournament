package com.harekrishna.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary          = Saffron,
    onPrimary        = SaffronDeep,
    secondary        = DevoPurple,
    onSecondary      = WarmWhite,
    tertiary         = FestiveGold,
    onTertiary       = SaffronDeep,
    background       = InkBlack,
    onBackground     = WarmWhite,
    surface          = Surface,
    onSurface        = WarmWhite,
    surfaceVariant   = SurfaceRaised,
    onSurfaceVariant = MutedSand,
    error            = ErrorRed,
    onError          = WarmWhite,
)

private val LightScheme = lightColorScheme(
    primary       = Saffron,
    onPrimary     = SaffronDeep,
    secondary     = DevoPurple,
    onSecondary   = WarmWhite,
    tertiary      = FestiveGold,
    background    = WarmWhite,
    onBackground  = InkBlack,
    surface       = WarmWhite,
    onSurface     = InkBlack,
    error         = ErrorRed,
)

// Dark-first. We ignore the system setting in v1 — the design is tuned for
// dark and the light scheme exists only as a safety net.
@Composable
fun HareKrishnaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography  = AppTypography,
        content     = content,
    )
}
