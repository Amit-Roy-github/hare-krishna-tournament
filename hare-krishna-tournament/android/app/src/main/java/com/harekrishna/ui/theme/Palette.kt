package com.harekrishna.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────
// Scalable gradient-palette system. Everything premium in the app (counter,
// buttons, chips, mesh backgrounds) draws its accents from an AppPalette.
//
// To ADD a new palette in the future:
//   1. Add a stable key to the PaletteId enum.
//   2. Add a `private val` AppPalette below.
//   3. Register it in Palettes.all.
// Nothing else needs to change — the lab and the theme both read the registry.
// ─────────────────────────────────────────────────────────────────────────

/** Stable identity for a palette — safe to persist as the user's choice. */
enum class PaletteId { SAFFRON_EMBER, AURORA_VIVID, FEATHER_VIVID }

/**
 * A gradient variant: two or more colour stops. This is the unit of "colour"
 * in the app — we use gradients, not flat fills, for every accent surface.
 */
@Immutable
data class Gradient(val stops: List<Color>) {
    init { require(stops.size >= 2) { "A gradient needs at least two stops" } }

    /** A single representative colour, for the rare spot that can't take a brush. */
    val solid: Color get() = stops[stops.size / 2]
}

fun Gradient.linear(): Brush = Brush.linearGradient(stops)

fun Gradient.radial(radiusPx: Float): Brush = Brush.radialGradient(stops, radius = radiusPx)

/**
 * A full premium palette: a dark, hue-tinted canvas plus gradient accents.
 * Surfaces step up in lightness (background → surface → surfaceRaised) to convey
 * elevation without shadows.
 */
@Immutable
data class AppPalette(
    val id:            PaletteId,
    val displayName:   String,
    val background:    Color,
    val surface:       Color,
    val surfaceRaised: Color,
    val onPrimary:     Color,
    val text:          Color,
    val muted:         Color,
    val primary:       Gradient,
    val secondary:     Gradient,
    val tertiary:      Color,
)

private val SaffronEmber = AppPalette(
    id            = PaletteId.SAFFRON_EMBER,
    displayName   = "Saffron Ember",
    background    = Color(0xFF0E0B14),
    surface       = Color(0xFF19131F),
    surfaceRaised = Color(0xFF241C2D),
    onPrimary     = Color(0xFF1A0F00),
    text          = Color(0xFFF5F1E8),
    muted         = Color(0xFFA19A8E),
    primary       = Gradient(listOf(Color(0xFFFFC861), Color(0xFFFFA62B), Color(0xFFFF7A1A))),
    secondary     = Gradient(listOf(Color(0xFFA78BFA), Color(0xFF8B5CF6), Color(0xFF6D5CF6))),
    tertiary      = Color(0xFFFFD700),
)

private val AuroraVivid = AppPalette(
    id            = PaletteId.AURORA_VIVID,
    displayName   = "Aurora Vivid",
    background    = Color(0xFF060A12),
    surface       = Color(0xFF0F1622),
    surfaceRaised = Color(0xFF18233A),
    onPrimary     = Color(0xFF04121A),
    text          = Color(0xFFEAF2F4),
    muted         = Color(0xFF9DB0B8),
    primary       = Gradient(listOf(Color(0xFF34F5C5), Color(0xFF22B8E6), Color(0xFF5B7CFF), Color(0xFFC16BFF))),
    secondary     = Gradient(listOf(Color(0xFFFF9EC4), Color(0xFFD17DFF), Color(0xFF8B7CFF))),
    tertiary      = Color(0xFFFFD86B),
)

private val FeatherVivid = AppPalette(
    id            = PaletteId.FEATHER_VIVID,
    displayName   = "Feather Vivid",
    background    = Color(0xFF07100E),
    surface       = Color(0xFF0E1A18),
    surfaceRaised = Color(0xFF16302B),
    onPrimary     = Color(0xFF04140F),
    text          = Color(0xFFE9F4F0),
    muted         = Color(0xFF98ADA6),
    primary       = Gradient(listOf(Color(0xFF1746A8), Color(0xFF0E86C2), Color(0xFF00C2A8), Color(0xFF1FD68C))),
    secondary     = Gradient(listOf(Color(0xFFFFD27A), Color(0xFFE0A93B), Color(0xFFB07A1E))),
    tertiary      = Color(0xFF2E7D6B),
)

/** The registry — the single source of truth for every palette in the app. */
object Palettes {
    val all: List<AppPalette> = listOf(SaffronEmber, AuroraVivid, FeatherVivid)
    val default: AppPalette = SaffronEmber
    fun byId(id: PaletteId): AppPalette = all.firstOrNull { it.id == id } ?: default

    // Resolve a persisted PaletteId name (see UserPrefs.selectedPalette) to a
    // palette, falling back to the default for null/unknown values.
    fun byName(name: String?): AppPalette = all.firstOrNull { it.id.name == name } ?: default
}

// The active palette, provided by HareKrishnaTheme. Composables read this for
// gradient variants (the counter fill, the CTA) that M3's flat colorScheme
// can't express. Neutrals/text still come from MaterialTheme.colorScheme.
val LocalAppPalette = staticCompositionLocalOf { Palettes.default }
