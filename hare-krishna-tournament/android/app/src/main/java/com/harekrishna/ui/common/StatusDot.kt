package com.harekrishna.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Small status indicator dot. Static by default; pulses while `animating`.
// Used by both the Home stats card (static, always-on) and the Counter stats
// card (animates + recolours from sync state). Centralised so the look stays
// in lockstep across screens.
@Composable
fun StatusDot(
    animating: Boolean = false,
    color:     Color   = MaterialTheme.colorScheme.primary,
    modifier:  Modifier = Modifier,
) {
    val size by if (animating) {
        rememberInfiniteTransition(label = "statusDot").animateFloat(
            initialValue  = 6f,
            targetValue   = 9f,
            animationSpec = infiniteRepeatable(
                animation  = tween(durationMillis = 1200),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "statusDotSize",
        )
    } else {
        animateFloatAsState(targetValue = 8f, label = "idleStatusDot")
    }
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color),
    )
}
