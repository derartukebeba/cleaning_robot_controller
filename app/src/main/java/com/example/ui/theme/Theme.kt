package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CleanBotDarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Navy950,
    primaryContainer = Navy800,
    onPrimaryContainer = CyanBright,
    secondary = ElectricBlue,
    onSecondary = TextPrimary,
    secondaryContainer = Navy700,
    onSecondaryContainer = TextPrimary,
    tertiary = StatusGreen,
    onTertiary = Navy950,
    background = Navy900,
    onBackground = TextPrimary,
    surface = Navy800,
    onSurface = TextPrimary,
    surfaceVariant = Navy700,
    onSurfaceVariant = TextSecondary,
    outline = Navy600,
    error = StatusRose,
    onError = TextPrimary,
    errorContainer = StatusRoseBg,
    onErrorContainer = TextPrimary
)

private val CleanBotLightColorScheme = lightColorScheme(
    primary = BlueDark,
    onPrimary = TextPrimary,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = CyanAccent,
    onSecondary = Navy950,
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1),
    error = StatusRose,
    onError = TextPrimary
)

@Composable
fun CleanBotTheme(
    darkTheme: Boolean = true, // Default to sleek industrial dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CleanBotDarkColorScheme else CleanBotLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
