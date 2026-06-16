package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GolaDarkColorScheme = darkColorScheme(
    primary = GolaCyan,
    onPrimary = Color(0xFF0C0C0F),
    secondary = GolaMagenta,
    onSecondary = Color.White,
    tertiary = GolaOrange,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor
)

@Composable
fun GolaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GolaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
