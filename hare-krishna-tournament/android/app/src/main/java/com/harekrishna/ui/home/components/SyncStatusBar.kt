package com.harekrishna.ui.home.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SyncStatusBar(
    isSyncing: Boolean,
    isPending: Boolean,
    syncedAt:  Long?,
    error:     String?,
    modifier:  Modifier = Modifier,
) {
    val dotColor: Color = when {
        error != null -> MaterialTheme.colorScheme.error
        else          -> MaterialTheme.colorScheme.primary
    }

    val animatedDotSize by if (isSyncing || isPending) {
        rememberInfiniteTransition(label = "syncDot").animateFloat(
            initialValue = 4f,
            targetValue  = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "syncDotSize",
        )
    } else {
        animateFloatAsState(targetValue = 4f, label = "idleDotSize")
    }

    Row(
        modifier = modifier,
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(animatedDotSize.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Text(
            text  = label(isSyncing, isPending, syncedAt, error),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun label(
    isSyncing: Boolean,
    isPending: Boolean,
    syncedAt:  Long?,
    error:     String?,
): String = when {
    error != null -> "Sync failed — will retry"
    isSyncing     -> "Syncing…"
    isPending     -> "Sync pending…"
    syncedAt != null -> "Synced ${ago(syncedAt)}"
    else          -> "Not synced yet"
}

private fun ago(timestampMs: Long): String {
    val secs = (System.currentTimeMillis() - timestampMs) / 1000
    return when {
        secs < 5     -> "just now"
        secs < 60    -> "${secs}s ago"
        secs < 3600  -> "${secs / 60}m ago"
        else         -> "${secs / 3600}h ago"
    }
}
