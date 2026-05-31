package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CustomColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    secondary = AquaSecondary,
    onSecondary = Color.Black,
    tertiary = CoolCyanAccent,
    background = SpaceObsidian,
    onBackground = HighContrastWhite,
    surface = MidnightSurface,
    onSurface = HighContrastWhite,
    surfaceVariant = BorderIndigo,
    onSurfaceVariant = SteelLavender
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    // We enforce our highly polished custom twilight style for optimal visual contrast and rest.
    MaterialTheme(
        colorScheme = CustomColorScheme,
        typography = Typography,
        content = content
    )
}
