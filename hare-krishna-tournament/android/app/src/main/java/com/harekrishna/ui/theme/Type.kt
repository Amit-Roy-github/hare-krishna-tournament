package com.harekrishna.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// V1 maps to system fonts to keep the APK small. DESIGN.md specifies the
// intended families (Spectral for display warmth, Inter for body). When font
// files are dropped into res/font, swap the two `*Family` consts below — no
// other change needed.

private val DisplayFamily = FontFamily.Serif
private val BodyFamily    = FontFamily.SansSerif

val AppTypography = Typography(
    displayLarge   = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.SemiBold, fontSize = 64.sp, lineHeight = 72.sp),
    displayMedium  = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.SemiBold, fontSize = 48.sp, lineHeight = 56.sp),
    displaySmall   = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge  = TextStyle(fontFamily = BodyFamily,    fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = BodyFamily,    fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall  = TextStyle(fontFamily = BodyFamily,    fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge     = TextStyle(fontFamily = BodyFamily,    fontWeight = FontWeight.Medium,   fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium    = TextStyle(fontFamily = BodyFamily,    fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge      = TextStyle(fontFamily = BodyFamily,    fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = BodyFamily,    fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = BodyFamily,    fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontFamily = BodyFamily,    fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = BodyFamily,    fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = BodyFamily,    fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp),
)
