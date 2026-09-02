package com.example.secondshelf.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = SurfaceWhite,
    secondary = TextSecondary,
    onSecondary = SurfaceWhite,
    background = AppBackgroundLight,
    onBackground = TextMain,
    surface = SurfaceWhite,
    onSurface = TextMain,
    error = DangerRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = PrimaryDark,
    background = TextMain,
    onBackground = SurfaceWhite,
    surface = Color(0xFF2C2C2E),
    onSurface = SurfaceWhite,
    error = DangerRed
)

@Composable
fun SecondShelfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
