package com.example.qrscanner.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette lifted directly from the HTML design mock (the CSS :root variables).
 */
object AppColors {
    val Ink = Color(0xFF0B1118)       // camera surface
    val Ink2 = Color(0xFF121A24)
    val Panel = Color(0xFFF6F8FA)     // light screen background
    val Card = Color(0xFFFFFFFF)
    val Laser = Color(0xFFFF3B30)     // barcode laser red
    val LaserSoft = Color(0xFFFF7A72)
    val Teal = Color(0xFF0FB5A6)      // successful scan
    val Text = Color(0xFF101828)
    val TextDim = Color(0xFF667085)
    val Line = Color(0xFFE6EAF0)

    // Dark-screen specific tints
    val ScanTopText = Color(0xFFDCE5EE)
    val ScanHint = Color(0xFF9FB0C2)
    val FinderStroke = Color(0xFFEAF2FA)
    val GradientTop = Color(0xFF16222F)

    // Bottom nav (dark, translucent)
    val NavBg = Color(0xE60D131B)
    val NavIconIdle = Color(0xFF8E9BAB)
    val NavIconActive = Color(0xFFFFFFFF)

    // History list icon backgrounds
    val IconBgUrl = Color(0xFFE8F1FF)
    val IconBgWifi = Color(0xFFEAFBF4)
    val IconBgBar = Color(0xFFFFF3E8)
    val IconBgTxt = Color(0xFFF2EDFF)
}
