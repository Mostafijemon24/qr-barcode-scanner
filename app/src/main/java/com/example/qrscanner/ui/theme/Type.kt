package com.example.qrscanner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

/**
 * The mock uses Inter for UI and JetBrains Mono for payloads/links.
 *
 * To match those typefaces exactly: drop `inter_*.ttf` / `jetbrains_mono_*.ttf`
 * into res/font and replace the two families below with FontFamily(Font(R.font.inter, ...)).
 * The system sans-serif / monospace substitutes here keep the app fully offline
 * and visually very close.
 */
val AppFont: FontFamily = FontFamily.SansSerif
val MonoFont: FontFamily = FontFamily.Monospace

val AppTypography = Typography()
