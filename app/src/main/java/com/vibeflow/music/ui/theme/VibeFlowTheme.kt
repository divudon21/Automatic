package com.vibeflow.music.ui.theme

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

val VibePurple = Color(0xFF8B5CF6)
val VibePurpleLight = Color(0xFFA78BFA)
val VibePink = Color(0xFFEC4899)
val VibeDeepPurple = Color(0xFF6D28D9)
val VibeDark = Color(0xFF0F0F1A)
val VibeDarkSurface = Color(0xFF1A1A2E)
val VibeDarkCard = Color(0xFF16213E)
val VibeAccent = Color(0xFF06B6D4)

private val DarkColorScheme = darkColorScheme(
    primary = VibePurpleLight,
    onPrimary = Color(0xFF1A0040),
    primaryContainer = VibeDeepPurple,
    onPrimaryContainer = Color(0xFFEDE0FF),
    secondary = VibePink,
    onSecondary = Color(0xFF3E001D),
    secondaryContainer = Color(0xFF5C1133),
    onSecondaryContainer = Color(0xFFFFD9E4),
    tertiary = VibeAccent,
    onTertiary = Color(0xFF003544),
    background = VibeDark,
    onBackground = Color(0xFFE8E0F0),
    surface = VibeDarkSurface,
    onSurface = Color(0xFFE8E0F0),
    surfaceVariant = VibeDarkCard,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF534361),
    inverseSurface = Color(0xFFE8E0F0),
    inverseOnSurface = VibeDark,
    inversePrimary = VibeDeepPurple
)

private val LightColorScheme = lightColorScheme(
    primary = VibeDeepPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE0FF),
    onPrimaryContainer = Color(0xFF21005E),
    secondary = VibePink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E4),
    onSecondaryContainer = Color(0xFF3E001D),
    tertiary = Color(0xFF0097A7),
    onTertiary = Color.White,
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFEDE0FF),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF7A7289)
)

@Composable
fun VibeFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        content = content
    )
}
