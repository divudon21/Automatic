package com.vibeflow.org.ui.theme

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
import com.vibeflow.org.data.AppColor

val VibeDark = Color(0xFF0F0F1A)
val VibeDarkSurface = Color(0xFF1A1A2E)
val VibeDarkCard = Color(0xFF16213E)

// Base color schemes
private fun createDarkColorScheme(primaryColor: Color, primaryContainerColor: Color) = darkColorScheme(
    primary = primaryColor,
    onPrimary = Color(0xFF1A0040),
    primaryContainer = primaryContainerColor,
    onPrimaryContainer = Color(0xFFEDE0FF),
    secondary = primaryColor,
    onSecondary = Color(0xFF3E001D),
    secondaryContainer = primaryContainerColor,
    onSecondaryContainer = Color(0xFFFFD9E4),
    tertiary = primaryColor,
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
    inversePrimary = primaryContainerColor
)

private fun createLightColorScheme(primaryColor: Color, primaryContainerColor: Color) = lightColorScheme(
    primary = primaryColor,
    onPrimary = Color.White,
    primaryContainer = primaryContainerColor,
    onPrimaryContainer = Color(0xFF21005E),
    secondary = primaryColor,
    onSecondary = Color.White,
    secondaryContainer = primaryContainerColor,
    onSecondaryContainer = Color(0xFF3E001D),
    tertiary = primaryColor,
    onTertiary = Color.White,
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF3EDF7),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF7A7289)
)

@Composable
fun VibeFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appColor: AppColor = AppColor.PURPLE,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val (primary, primaryContainer) = when (appColor) {
        AppColor.PURPLE -> Pair(if (darkTheme) Color(0xFFA78BFA) else Color(0xFF6D28D9), if (darkTheme) Color(0xFF6D28D9) else Color(0xFFEDE0FF))
        AppColor.BLUE -> Pair(if (darkTheme) Color(0xFF60A5FA) else Color(0xFF2563EB), if (darkTheme) Color(0xFF1D4ED8) else Color(0xFFDBEAFE))
        AppColor.GREEN -> Pair(if (darkTheme) Color(0xFF34D399) else Color(0xFF059669), if (darkTheme) Color(0xFF047857) else Color(0xFFD1FAE5))
        AppColor.RED -> Pair(if (darkTheme) Color(0xFFF87171) else Color(0xFFDC2626), if (darkTheme) Color(0xFFB91C1C) else Color(0xFFFEE2E2))
        AppColor.ORANGE -> Pair(if (darkTheme) Color(0xFFFB923C) else Color(0xFFEA580C), if (darkTheme) Color(0xFFC2410C) else Color(0xFFFFEDD5))
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> createDarkColorScheme(primary, primaryContainer)
        else -> createLightColorScheme(primary, primaryContainer)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
