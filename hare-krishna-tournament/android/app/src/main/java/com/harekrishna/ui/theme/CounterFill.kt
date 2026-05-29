package com.harekrishna.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp

// ─────────────────────────────────────────────────────────────────────────
// User-selectable fill treatment for the hero counter. Adding a new treatment
// later is one enum case + one branch in [counterBrush] (plus, if it animates,
// an overlay in the counter composable). The lab and the real CounterButton
// both call counterBrush, so the look can never drift between them.
// ─────────────────────────────────────────────────────────────────────────

enum class CounterFill(val label: String) {
    FLAT("Flat"),
    GRADIENT("Gradient"),
    GLOW("Glow on press"),
    SHEEN("Sheen"),
}

private fun Color.deepen(fraction: Float): Color = lerp(this, Color.Black, fraction)

/**
 * The static base brush each treatment paints onto the hero counter circle.
 * Tuned for the ~260–280dp hero. The press-glow halo (GLOW) and the sweeping
 * highlight (SHEEN) are animated overlays owned by the counter composable —
 * this provides only the base fill underneath them.
 */
fun AppPalette.counterBrush(fill: CounterFill): Brush = when (fill) {
    CounterFill.FLAT -> SolidColor(primary.solid)
    CounterFill.GLOW -> Brush.linearGradient(
        colors = primary.stops,
        start  = Offset(0f, 0f),
        end    = Offset(700f, 700f),
    )
    CounterFill.GRADIENT,
    CounterFill.SHEEN -> Brush.radialGradient(
        colors = primary.stops + primary.stops.last().deepen(0.15f),
        radius = 560f,
    )
}
