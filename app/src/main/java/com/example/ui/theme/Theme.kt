package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = NeonIris,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E1B4B),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = NeonAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF451A03),
    onSecondaryContainer = Color(0xFFFFEDD5),
    tertiary = NeonCyan,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF083344),
    onTertiaryContainer = Color(0xFFCFFAFE),
    background = VoidBlack,
    onBackground = Color(0xFFF1F5F9),
    surface = VoidSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = VoidSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = GlassBorder,
    outlineVariant = Color(0x1AFFFFFF)
)

private val CyberLightColorScheme = lightColorScheme(
    primary = NeonIris,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFFEA580C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF7C2D12),
    tertiary = Color(0xFF0891B2),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFFAFE),
    onTertiaryContainer = Color(0xFF164E63),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF090D16),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to stunning dark cyber-zen aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CyberDarkColorScheme else CyberLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
