package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PulseIndigoPrimary,
    onPrimary = PulseIndigoOnPrimary,
    primaryContainer = PulseIndigoContainerDark,
    onPrimaryContainer = PulseIndigoOnContainerDark,
    secondary = PulseCoralSecondary,
    onSecondary = PulseCoralOnSecondary,
    secondaryContainer = PulseCoralContainerDark,
    onSecondaryContainer = PulseCoralOnContainerDark,
    tertiary = PulseCyanTertiary,
    onTertiary = PulseCyanOnTertiary,
    tertiaryContainer = PulseCyanContainerDark,
    onTertiaryContainer = PulseCyanOnContainerDark,
    background = PulseDarkBackground,
    onBackground = PulseDarkOnSurface,
    surface = PulseDarkSurface,
    onSurface = PulseDarkOnSurface,
    surfaceVariant = PulseDarkSurfaceVariant,
    onSurfaceVariant = PulseDarkOnSurfaceVariant,
    outline = PulseDarkOutline,
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = PulseIndigoPrimary,
    onPrimary = PulseIndigoOnPrimary,
    primaryContainer = PulseIndigoContainerLight,
    onPrimaryContainer = PulseIndigoOnContainerLight,
    secondary = PulseCoralSecondary,
    onSecondary = PulseCoralOnSecondary,
    secondaryContainer = PulseCoralContainerLight,
    onSecondaryContainer = PulseCoralOnContainerLight,
    tertiary = PulseCyanTertiary,
    onTertiary = PulseCyanOnTertiary,
    tertiaryContainer = PulseCyanContainerLight,
    onTertiaryContainer = PulseCyanOnContainerLight,
    background = PulseLightBackground,
    onBackground = PulseLightOnSurface,
    surface = PulseLightSurface,
    onSurface = PulseLightOnSurface,
    surfaceVariant = PulseLightSurfaceVariant,
    onSurfaceVariant = PulseLightOnSurfaceVariant,
    outline = PulseLightOutline,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve our custom branded aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
