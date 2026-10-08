package com.twinspace.cloner.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TwinSpaceColorScheme = lightColorScheme(
    primary = TwinPrimary,
    onPrimary = TwinTextInverse,
    primaryContainer = TwinPrimarySoft,
    onPrimaryContainer = TwinPrimaryDark,
    secondary = TwinCyan,
    onSecondary = TwinTextInverse,
    secondaryContainer = TwinCyanSoft,
    onSecondaryContainer = TwinTextPrimary,
    background = TwinBg,
    onBackground = TwinTextPrimary,
    surface = TwinSurface,
    onSurface = TwinTextPrimary,
    surfaceVariant = TwinSurfaceAlt,
    onSurfaceVariant = TwinTextSecondary,
    outline = TwinBorder
)

@Composable
fun TwinSpaceTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Light mode status bar and navigation bar with dark icons
                window.statusBarColor = TwinSurface.toArgb()
                window.navigationBarColor = TwinSurface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = TwinSpaceColorScheme,
        typography = Typography,
        content = content
    )
}
