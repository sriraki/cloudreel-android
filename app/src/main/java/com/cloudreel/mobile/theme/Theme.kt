package com.cloudreel.mobile.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CRPurple,
    onPrimary = BackgroundLight,
    primaryContainer = BadgeBackgroundLight,
    onPrimaryContainer = BadgeTextLight,
    secondary = CRMagenta,
    onSecondary = BackgroundLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceSecondaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = ErrorRed,
    onError = BackgroundLight
)

private val DarkColorScheme = darkColorScheme(
    primary = CRViolet.liftedForDark(),
    onPrimary = BadgeBackgroundDark,
    primaryContainer = BadgeBackgroundDark,
    onPrimaryContainer = BadgeTextDark,
    secondary = CRMagenta,
    onSecondary = BackgroundLight,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceSecondaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = ErrorRed,
    onError = BackgroundLight
)

/** This accent as drawn in the current theme: unchanged in light mode, lifted in dark mode. */
@Composable
fun Color.forTheme(): Color = if (LocalThemeController.current.isDark) liftedForDark() else this

/** Theme controller to allow manual in-app toggle as seen in header moon icon */
val LocalThemeController = compositionLocalOf {
    ThemeController(
        isDark = false,
        toggleTheme = {}
    )
}

data class ThemeController(
    val isDark: Boolean,
    val toggleTheme: () -> Unit
)

@Composable
fun CloudReelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    onToggleTheme: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalThemeController provides ThemeController(
            isDark = darkTheme,
            toggleTheme = onToggleTheme
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
