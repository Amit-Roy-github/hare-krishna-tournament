package com.harekrishna.ui.preview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harekrishna.ui.theme.AppPalette
import com.harekrishna.ui.theme.CounterFill
import com.harekrishna.ui.theme.Gradient
import com.harekrishna.ui.theme.Palettes
import com.harekrishna.ui.theme.counterBrush
import com.harekrishna.ui.theme.linear

// ─────────────────────────────────────────────────────────────────────────
// TEMPORARY design playground. Shows the final option matrix — every palette
// in the Palettes registry × every CounterFill — so we can confirm the look.
// The counter fill comes from AppPalette.counterBrush(), the same code the
// real CounterButton will use, so there's no drift. Delete this file (and flip
// SHOW_COLOR_LAB in MainActivity) once the look is locked.
// ─────────────────────────────────────────────────────────────────────────

private fun Color.toHex(): String = "#%06X".format(0xFFFFFF and toArgb())

@Composable
fun ColorLabScreen() {
    var paletteIndex by remember { mutableIntStateOf(0) }
    var fill         by remember { mutableStateOf(CounterFill.GRADIENT) }
    var count        by remember { mutableIntStateOf(108) }

    val palette = Palettes.all[paletteIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(palette.background)
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(palette.primary.solid.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.maxDimension * 0.7f,
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(palette.secondary.solid.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width, size.height * 0.25f),
                        radius = size.maxDimension * 0.6f,
                    )
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Color Lab",
                color      = palette.text,
                fontSize   = 22.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                modifier   = Modifier.fillMaxWidth(),
            )
            Text(
                "3 palettes × 4 fills. Press the circle for the glow.",
                color    = palette.muted,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )

            Spacer(Modifier.height(20.dp))

            PaletteChips(
                palettes = Palettes.all,
                selected = paletteIndex,
                active   = palette,
                onSelect = { paletteIndex = it },
            )

            Spacer(Modifier.height(10.dp))

            FillChips(
                fills    = CounterFill.entries,
                selected = fill,
                palette  = palette,
                onSelect = { fill = it },
            )

            Spacer(Modifier.height(28.dp))

            CounterDemo(count = count, palette = palette, fill = fill, onTap = { count++ })

            Spacer(Modifier.height(28.dp))

            SwatchStrip(palette)

            Spacer(Modifier.height(20.dp))

            SampleComponents(palette)

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaletteChips(
    palettes: List<AppPalette>,
    selected: Int,
    active:   AppPalette,
    onSelect: (Int) -> Unit,
) {
    FlowRow(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp),
    ) {
        palettes.forEachIndexed { index, p ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (isSelected) Modifier.background(p.primary.linear())
                        else Modifier.background(active.surface)
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = p.displayName,
                    color      = if (isSelected) p.onPrimary else active.text,
                    fontSize   = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun FillChips(
    fills:    List<CounterFill>,
    selected: CounterFill,
    palette:  AppPalette,
    onSelect: (CounterFill) -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        fills.forEach { f ->
            val isSelected = f == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (isSelected) Modifier.background(palette.primary.linear())
                        else Modifier.background(palette.surface)
                    )
                    .clickable { onSelect(f) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = f.label,
                    color      = if (isSelected) palette.onPrimary else palette.text,
                    fontSize   = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun CounterDemo(
    count:   Int,
    palette: AppPalette,
    fill:    CounterFill,
    onTap:   () -> Unit,
) {
    val circleSize = 260.dp

    var pressed by remember { mutableStateOf(false) }
    val glow by animateFloatAsState(
        targetValue   = if (pressed && fill == CounterFill.GLOW) 1f else 0f,
        animationSpec = tween(260),
        label         = "glow",
    )

    val bounce = remember { Animatable(1f) }
    val sweep  = remember { Animatable(0f) }
    LaunchedEffect(count) {
        if (count > 0) {
            bounce.animateTo(1.08f, tween(120))
            bounce.animateTo(1.00f, tween(120))
        }
    }
    LaunchedEffect(count) {
        if (fill == CounterFill.SHEEN && count > 0) {
            sweep.snapTo(0f)
            sweep.animateTo(1f, tween(600))
        }
    }

    Box(contentAlignment = Alignment.Center) {
        // Glow halo — only for the GLOW treatment, intensifies while pressed.
        Box(
            modifier = Modifier
                .size(circleSize)
                .scale(1f + 0.30f * glow)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(palette.primary.solid.copy(alpha = 0.55f * glow), Color.Transparent),
                    )
                ),
        )

        Box(
            modifier = Modifier
                .size(circleSize)
                .scale(bounce.value)
                .clip(CircleShape)
                .background(palette.counterBrush(fill))
                .pointerInput(fill) {
                    detectPressAndTap(onPressChange = { pressed = it }, onTap = onTap)
                },
            contentAlignment = Alignment.Center,
        ) {
            if (fill == CounterFill.SHEEN) {
                val s = sweep.value
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colorStops = arrayOf(
                                    (s - 0.18f).coerceIn(0f, 1f) to Color.Transparent,
                                    s.coerceIn(0f, 1f)          to Color.White.copy(alpha = 0.35f),
                                    (s + 0.18f).coerceIn(0f, 1f) to Color.Transparent,
                                ),
                            )
                        ),
                )
            }
            Text(
                text       = "%,d".format(count),
                color      = palette.onPrimary,
                fontSize   = 60.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
            )
        }
    }
}

private suspend fun PointerInputScope.detectPressAndTap(
    onPressChange: (Boolean) -> Unit,
    onTap: () -> Unit,
) {
    detectTapGestures(
        onPress = {
            onPressChange(true)
            tryAwaitRelease()
            onPressChange(false)
        },
        onTap = { onTap() },
    )
}

@Composable
private fun SwatchStrip(palette: AppPalette) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GradientBar("Primary variant",   palette.primary,   palette)
        GradientBar("Secondary variant", palette.secondary, palette)

        Spacer(Modifier.height(4.dp))

        val solids = listOf(
            "Background"    to palette.background,
            "Surface"       to palette.surface,
            "SurfaceRaised" to palette.surfaceRaised,
            "Tertiary"      to palette.tertiary,
            "Text"          to palette.text,
            "Muted"         to palette.muted,
        )
        solids.forEach { (name, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color),
                )
                Spacer(Modifier.size(12.dp))
                Text(name, color = palette.text, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(color.toHex(), color = palette.muted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun GradientBar(label: String, gradient: Gradient, palette: AppPalette) {
    Column {
        Text(label, color = palette.muted, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(gradient.linear()),
        )
    }
}

@Composable
private fun SampleComponents(palette: AppPalette) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTileDemo("Today", "108", palette, Modifier.weight(1f))
            StatTileDemo("Week",  "742", palette, Modifier.weight(1f))
            StatTileDemo("Lifetime", "12,406", palette, Modifier.weight(1f))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(palette.primary.linear())
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Primary action", color = palette.onPrimary, fontWeight = FontWeight.SemiBold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ChipDemo("Secondary", palette.secondary.solid)
            ChipDemo("Tertiary",  palette.tertiary)
        }
    }
}

@Composable
private fun StatTileDemo(label: String, value: String, palette: AppPalette, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .padding(16.dp),
    ) {
        Text(label, color = palette.muted, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = palette.text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ChipDemo(label: String, fg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fg.copy(alpha = 0.18f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ColorLabPreview() {
    ColorLabScreen()
}
