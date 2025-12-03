package com.eventify.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette claire
private val LightColors = lightColorScheme(
    primary = Color(0xFFFF9800),          // Orange vif
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFB74D), // Orange clair
    onPrimaryContainer = Color(0xFF5D2600),
    secondary = Color(0xFF2196F3),         // Bleu vif
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF90CAF9),// Bleu clair
    onSecondaryContainer = Color(0xFF0D47A1),
    background = Color(0xFFFFFFFF),        // Blanc pur
    onBackground = Color(0xFF1A237E),      // Bleu foncé
    surface = Color(0xFFFFFFFF),           // Blanc surface
    onSurface = Color(0xFF1A237E),
    error = Color(0xFFD32F2F),
    onError = Color.White
)

// Palette sombre
private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF5D2600),
    primaryContainer = Color(0xFFFF9800),
    onPrimaryContainer = Color(0xFFFFB74D),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF0D47A1),
    secondaryContainer = Color(0xFF2196F3),
    onSecondaryContainer = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE3F2FD),
    surface = Color(0xFF1A237E),
    onSurface = Color(0xFFE3F2FD),
    error = Color(0xFFCF6679),
    onError = Color(0xFF330000)
)

@Composable
fun EventifyTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
