package com.cloudreel.mobile.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── CloudReel Official Brand Palette ──────────────────────────────────────────
val CRMagenta = Color(0xFFD300EA)       // Vibrant brand magenta
val CRPurple = Color(0xFF3031CB)        // Deep royal purple/indigo
val CRViolet = Color(0xFF7928CA)        // Mid gradient tone
val CRIndigo = Color(0xFF4F46E5)        // Modern accent indigo

// Signature CloudReel Linear Gradient
val CRBrandGradient = Brush.horizontalGradient(
    colors = listOf(CRMagenta, CRPurple)
)

val CRBrandGradientVertical = Brush.verticalGradient(
    colors = listOf(CRMagenta, CRPurple)
)

// ── Light Theme Neutrals ──────────────────────────────────────────────────────
val BackgroundLight = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFF9FAFB)
val SurfaceSecondaryLight = Color(0xFFF3F4F6)
val BorderLight = Color(0xFFE5E7EB)
val BorderFocusedLight = Color(0xFF9333EA)
val TextPrimaryLight = Color(0xFF111111)
val TextSecondaryLight = Color(0xFF6B7280)
val TextMutedLight = Color(0xFF9CA3AF)

// ── Dark Theme Neutrals ───────────────────────────────────────────────────────
val BackgroundDark = Color(0xFF0F0F14)
val SurfaceDark = Color(0xFF1A1A22)
val SurfaceSecondaryDark = Color(0xFF23232E)
val BorderDark = Color(0xFF2C2C3A)
val BorderFocusedDark = Color(0xFFC084FC)
val TextPrimaryDark = Color(0xFFF9FAFB)
val TextSecondaryDark = Color(0xFF9CA3AF)
val TextMutedDark = Color(0xFF6B7280)

// ── Status & Functional Colors ────────────────────────────────────────────────
val SuccessGreen = Color(0xFF10B981)
val ErrorRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)
val InfoBlue = Color(0xFF3B82F6)

// Pill Badge Colors
val BadgeBackgroundLight = Color(0xFFF5E8FF)
val BadgeBorderLight = Color(0xFFE9D5FF)
val BadgeTextLight = Color(0xFF7E22CE)

val BadgeBackgroundDark = Color(0xFF2E1065)
val BadgeBorderDark = Color(0xFF581C87)
val BadgeTextDark = Color(0xFFE9D5FF)
