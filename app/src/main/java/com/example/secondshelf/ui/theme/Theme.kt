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
    primary = Color(0xFF388AF6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF9E9E9E),
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFF2F2F7),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFF2F2F7),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFE5E5EA),
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
