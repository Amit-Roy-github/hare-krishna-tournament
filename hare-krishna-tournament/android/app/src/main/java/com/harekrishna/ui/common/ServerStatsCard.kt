package com.harekrishna.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.harekrishna.ui.theme.LocalAppPalette

// Shared "server-truth" stats panel. One source for the look so the Home
// landing and the Counter screen can never visually drift apart.
// `leading` lets the caller drop in a sync indicator (Counter does); Home
// passes null and gets a plain label.
@Composable
fun ServerStatsCard(
    todayValue:    Int,
    weekValue:     Int,
    lifetimeValue: Int,
    modifier:      Modifier = Modifier,
    label:         String = "Lalju server",
    leading:       (@Composable () -> Unit)? = null,
) {
    val accent = LocalAppPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accent.primary.solid.copy(alpha = 0.10f),
                        accent.primary.solid.copy(alpha = 0.03f),
                    )
                )
            )
            .border(
                width = 1.dp,
                color = accent.primary.solid.copy(alpha = 0.18f),
                shape = RoundedCornerShape(24.dp),
            )
            .padding(14.dp),
    ) {
        Row(
            modifier          = Modifier.padding(start = 4.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.size(8.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatTile(label = "Today",    value = todayValue,    modifier = Modifier.weight(1f))
            StatTile(label = "Week",     value = weekValue,     modifier = Modifier.weight(1f))
            StatTile(label = "Lifetime", value = lifetimeValue, modifier = Modifier.weight(1f))
        }
    }
}
