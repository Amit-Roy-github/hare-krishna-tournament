package com.harekrishna.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harekrishna.ui.theme.AppPalette
import com.harekrishna.ui.theme.Palettes
import com.harekrishna.ui.theme.linear

// ─────────────────────────────────────────────────────────────────────────
// TEMPORARY wireframe for the new Home landing page. Home is a calm launcher:
// greeting + quick stats glance + a big gradient CTA that routes to the
// Counter page (the current counter screen, unchanged). Render on-device via
// MainActivity's DEV_SCREEN switch. Translate into ui/home once the layout is
// agreed, then delete this file.
// ─────────────────────────────────────────────────────────────────────────

@Composable
fun HomeWireframeScreen(palette: AppPalette = Palettes.default) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        // Soft accent bloom top-left — same premium mesh feel as the counter.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(palette.primary.solid.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 900f,
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Settings (top-right) ────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(palette.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("⚙", color = palette.muted, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Greeting (centered) ─────────────────
            Text(
                "Hare Krishna",
                color      = palette.text,
                fontSize   = 24.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
            )
            Text("Bhakta Name", color = palette.muted, fontSize = 14.sp)

            Spacer(Modifier.height(28.dp))

            // ── Quick stats glance ──────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Today",    "108",    palette, Modifier.weight(1f))
                StatTile("Week",     "742",    palette, Modifier.weight(1f))
                StatTile("Lifetime", "12,406", palette, Modifier.weight(1f))
            }

            Spacer(Modifier.height(28.dp))

            // ── Primary CTA → Counter ───────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(palette.primary.linear()),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(palette.onPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("◉", color = palette.onPrimary, fontSize = 22.sp)
                    }
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text(
                            "Begin Japa",
                            color      = palette.onPrimary,
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                        )
                        Text("Tap to open your counter  →", color = palette.onPrimary.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Future zone (leaderboard / streaks) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surface.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("Leaderboard · coming in v2", color = palette.muted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, palette: AppPalette, modifier: Modifier) {
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

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeWireframePreview() {
    HomeWireframeScreen()
}
