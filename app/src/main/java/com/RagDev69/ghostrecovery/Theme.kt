package com.RagDev69.ghostrecovery

import android.content.Context
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/* ── DataStore ─────────────────────────────── */
val Context.dataStore by preferencesDataStore(name = "ghost_prefs")
val THEME_KEY = stringPreferencesKey("selected_theme")

/* ── Enum ──────────────────────────────────── */
enum class AppTheme(val displayName: String, val emoji: String, val description: String) {
    DARK("Dark Mode",        "🌑", "Clean obsidian darkness"),
    ANIME("Anime",           "🌸", "Sakura pink & soft lavender"),
    COMIC("Comic",           "💥", "Bold ink & pop-art punch"),
    GHOST("Ghost / Horror",  "👻", "Eerie green on void black"),
    NEON("Neon / Cyberpunk", "⚡", "Electric violet neon city")
}

/* ── Color palettes ────────────────────────── */
object ThemeColors {

    val darkScheme = darkColorScheme(
        primary          = Color(0xFFCCCCCC),
        onPrimary        = Color(0xFF0A0A0A),
        primaryContainer = Color(0xFF2A2A2A),
        secondary        = Color(0xFF888888),
        background       = Color(0xFF0D0D0D),
        surface          = Color(0xFF1A1A1A),
        onBackground     = Color(0xFFEEEEEE),
        onSurface        = Color(0xFFDDDDDD),
        error            = Color(0xFFCF6679)
    )

    val animeScheme = lightColorScheme(
        primary          = Color(0xFFE91E8C),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD6EC),
        secondary        = Color(0xFF9C6FDE),
        background       = Color(0xFFFFF0F8),
        surface          = Color(0xFFFFE4F4),
        onBackground     = Color(0xFF3A003A),
        onSurface        = Color(0xFF4A1042),
        error            = Color(0xFFE53935)
    )

    val comicScheme = lightColorScheme(
        primary          = Color(0xFFFFD600),
        onPrimary        = Color(0xFF1A1A1A),
        primaryContainer = Color(0xFFFFEE58),
        secondary        = Color(0xFFFF1744),
        background       = Color(0xFFFAFAFA),
        surface          = Color(0xFFFFFFFF),
        onBackground     = Color(0xFF0D0D0D),
        onSurface        = Color(0xFF1A1A1A),
        error            = Color(0xFFFF1744)
    )

    val ghostScheme = darkColorScheme(
        primary          = Color(0xFF00E676),
        onPrimary        = Color(0xFF000000),
        primaryContainer = Color(0xFF003319),
        secondary        = Color(0xFF69F0AE),
        background       = Color(0xFF020A04),
        surface          = Color(0xFF071209),
        onBackground     = Color(0xFFCCFFD6),
        onSurface        = Color(0xFFB2DFCA),
        error            = Color(0xFFFF1744)
    )

    val neonScheme = darkColorScheme(
        primary          = Color(0xFFE040FB),
        onPrimary        = Color(0xFF0A0010),
        primaryContainer = Color(0xFF2D0040),
        secondary        = Color(0xFF00E5FF),
        background       = Color(0xFF06000F),
        surface          = Color(0xFF110020),
        onBackground     = Color(0xFFF0CCFF),
        onSurface        = Color(0xFFDDBBFF),
        error            = Color(0xFFFF1744)
    )

    fun colorSchemeFor(theme: AppTheme) = when (theme) {
        AppTheme.DARK  -> darkScheme
        AppTheme.ANIME -> animeScheme
        AppTheme.COMIC -> comicScheme
        AppTheme.GHOST -> ghostScheme
        AppTheme.NEON  -> neonScheme
    }

    fun accentFor(theme: AppTheme): Color = when (theme) {
        AppTheme.DARK  -> Color(0xFFCCCCCC)
        AppTheme.ANIME -> Color(0xFFE91E8C)
        AppTheme.COMIC -> Color(0xFFFFD600)
        AppTheme.GHOST -> Color(0xFF00E676)
        AppTheme.NEON  -> Color(0xFFE040FB)
    }

    fun cardFor(theme: AppTheme): Color = when (theme) {
        AppTheme.DARK  -> Color(0xFF1E1E1E)
        AppTheme.ANIME -> Color(0xFFFFD6EC)
        AppTheme.COMIC -> Color(0xFFFFF9C4)
        AppTheme.GHOST -> Color(0xFF0A1F0C)
        AppTheme.NEON  -> Color(0xFF1A0030)
    }
}

/* ── DataStore helpers ─────────────────────── */
fun themeFlow(context: Context): Flow<AppTheme> =
    context.dataStore.data.map { prefs ->
        runCatching { AppTheme.valueOf(prefs[THEME_KEY] ?: AppTheme.GHOST.name) }
            .getOrDefault(AppTheme.GHOST)
    }

suspend fun saveTheme(context: Context, theme: AppTheme) {
    context.dataStore.edit { it[THEME_KEY] = theme.name }
}