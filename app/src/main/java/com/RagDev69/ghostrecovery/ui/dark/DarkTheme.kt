package com.RagDev69.ghostrecovery.ui.dark

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 🔹 Light Theme Colors
private val LightColors = lightColorScheme(
    primary = Color(0xFF0066FF),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF5F5F5)
)

// 🔹 Dark Theme Colors (Ghost vibe 👻)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF00FFAA),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

@Composable
fun GhostRecoveryTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}