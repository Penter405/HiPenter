package com.example.hipenter.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val HiPenterDarkColorScheme = darkColorScheme(
    primary = Teal,
    onPrimary = TextOnTeal,
    primaryContainer = TealDark,
    onPrimaryContainer = TealLight,
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = CoralDark,
    onSecondaryContainer = CoralLight,
    tertiary = GoldAccent,
    background = NavyDark,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF4A4B6A),
    outlineVariant = Color(0xFF3A3B5A),
    surfaceContainerLowest = NavyDark,
    surfaceContainerLow = Color(0xFF1E1F34),
    surfaceContainer = NavySurface,
    surfaceContainerHigh = NavyCard,
    surfaceContainerHighest = NavyElevated,
)

private val HiPenterLightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2F5EA),
    onPrimaryContainer = TealDark,
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = CoralDark,
    tertiary = GoldAccent,
    background = OffWhite,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFFCCCEDA),
    outlineVariant = Color(0xFFE0E2EE),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5F6FA),
    surfaceContainer = Color(0xFFF0F1F6),
    surfaceContainerHigh = LightCard,
    surfaceContainerHighest = Color(0xFFE8E9F0),
)

@Composable
fun HiPenterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) HiPenterDarkColorScheme else HiPenterLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
