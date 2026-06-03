package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CosmicColorScheme = darkColorScheme(
    primary = SpaceNeonTeal,
    secondary = NeonPurple,
    tertiary = GoldYellow,
    background = DeepSpace,
    surface = SlateGray,
    onPrimary = DeepSpace,
    onSecondary = ActiveWhite,
    onTertiary = DeepSpace,
    onBackground = ActiveWhite,
    onSurface = ActiveWhite,
    surfaceContainer = SlateLight,
    outline = BorderSlate
)

private val LightColorScheme = lightColorScheme(
    primary = SpaceNeonTeal,
    secondary = NeonPurple,
    tertiary = GoldYellow,
    background = ActiveWhite,
    surface = ActiveWhite,
    onPrimary = ActiveWhite,
    onSecondary = DeepSpace,
    onBackground = DeepSpace,
    onSurface = DeepSpace
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark space theme by default for the premium "Mission Control" vibe
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) CosmicColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
