package com.harekrishna.ui.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.harekrishna.ui.theme.Saffron
import com.harekrishna.ui.theme.SaffronDeep
import com.harekrishna.ui.util.formatThousands

// Hero element — see android/DESIGN.md §"Counter button (the hero)".
// `clickable` is false when tap-anywhere mode is on (HomeScreen wraps the
// whole area in its own tap detector). Haptic + viewModel.onTap() live in the
// caller, not here — keeps this composable pure visual.
@Composable
fun CounterButton(
    count:         Int,
    bounceEnabled: Boolean,
    clickable:     Boolean,
    onTap:         () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(count, bounceEnabled) {
        if (count > 0 && bounceEnabled) {
            scale.animateTo(1.08f, animationSpec = tween(120))
            scale.animateTo(1.00f, animationSpec = tween(120))
        } else {
            scale.snapTo(1f)
        }
    }

    val clickModifier =
        if (clickable) {
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,    // no ripple — pulse + haptic do the talking
            ) { onTap() }
        } else {
            Modifier
        }

    Box(
        modifier = modifier
            .size(280.dp)
            .scale(scale.value)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Saffron, Saffron.copy(alpha = 0.78f)),
                    radius = 600f,
                )
            )
            .then(clickModifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = count.formatThousands(),
            style = MaterialTheme.typography.displayLarge,
            color = SaffronDeep,
        )
    }
}
