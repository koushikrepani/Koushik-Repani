package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Obsidian Void Base
val VoidBlack = Color(0xFF06080F)
val VoidSurface = Color(0xFF0C101B)
val VoidSurfaceVariant = Color(0xFF141A29)
val GlassSurface = Color(0x1FFFFFFF)
val GlassSurfaceActive = Color(0x2EFFFFFF)
val GlassBorder = Color(0x26FFFFFF)

// Neon Cyber & Aurora Accents
val NeonIris = Color(0xFF6366F1)
val NeonCyan = Color(0xFF06B6D4)
val NeonMint = Color(0xFF10B981)
val NeonAmber = Color(0xFFFF9800)
val NeonCoral = Color(0xFFFF4D4D)
val NeonPurple = Color(0xFFA855F7)
// Legacy aliases mapped to Cyber-Zen palette
val PulseIndigoPrimary = NeonIris
val PulseIndigoOnPrimary = Color.White

// Iridescent Glow Brushes
val AuroraBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFF06B6D4))
)

val FlameBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFFF4D4D), Color(0xFFFF9800))
)

val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(Color(0x406366F1), Color(0x1FFFFFFF), Color(0x4006B6D4))
)

// Priority Accents
val AccentUrgent = Color(0xFFFF453A)
val AccentHigh = Color(0xFFFF9F0A)
val AccentMedium = Color(0xFF0A84FF)
val AccentLow = Color(0xFF30D158)

val FlameGold = Color(0xFFFFB300)
val FocusPurple = Color(0xFFA855F7)
