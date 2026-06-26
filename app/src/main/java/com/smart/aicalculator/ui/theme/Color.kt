package com.smart.aicalculator.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Brand accent — coral / red-orange. Used intentionally: primary actions,
// selected navigation, result emphasis. Not as a background everywhere.
// ---------------------------------------------------------------------------
val CoralPrimary = Color(0xFFFF5630)   // brand accent
val CoralDark = Color(0xFFE24420)      // pressed / deeper accent
val CoralLight = Color(0xFFFFB8A3)     // accent on dark surfaces
val CoralBgTint = Color(0xFFFFEAE3)    // soft container tint (light)
val CoralOnTint = Color(0xFFB23A18)    // strong content on coral tint

// ---------------------------------------------------------------------------
// Light theme neutrals — soft gray canvas + crisp white cards.
// ---------------------------------------------------------------------------
val BgLight = Color(0xFFF3F4F6)        // app canvas
val SurfaceLight = Color(0xFFFFFFFF)   // cards / sheets
val SurfaceMutedLight = Color(0xFFEDEFF3) // neutral keys, chips, fills
val TextPrimary = Color(0xFF14171C)    // strong near-black headings/values
val TextSecondary = Color(0xFF566070)  // readable secondary text (no alpha dimming)
val TextTertiary = Color(0xFF8B94A3)   // hints / placeholders
val BorderLight = Color(0xFFE3E7ED)    // hairline borders / dividers
val OnMutedLight = Color(0xFF2D3340)   // content on muted neutral fills

// ---------------------------------------------------------------------------
// Dark theme neutrals.
// ---------------------------------------------------------------------------
val BgDark = Color(0xFF0F1115)
val SurfaceDark = Color(0xFF181B21)
val SurfaceMutedDark = Color(0xFF262A33)
val TextPrimaryDark = Color(0xFFF4F6F9)
val TextSecondaryDark = Color(0xFFA4AEBE)
val TextTertiaryDark = Color(0xFF6F7889)
val BorderDark = Color(0xFF2C313B)
val OnMutedDark = Color(0xFFE4E8EF)
val CoralBgTintDark = Color(0xFF3A1E15)
val CoralOnTintDark = Color(0xFFFFC2AE)

// ---------------------------------------------------------------------------
// Shared semantic colors (status / categories). Same in both themes.
// ---------------------------------------------------------------------------
val SuccessGreen = Color(0xFF1FAA6B)
val WarningAmber = Color(0xFFE9A100)
val InfoBlue = Color(0xFF2E7CF6)
