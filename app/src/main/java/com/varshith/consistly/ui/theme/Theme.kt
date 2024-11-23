package com.varshith.consistly.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom Purple Colors
private val PurplePrimary = Color(0xFF6750A4)
private val PurpleSecondary = Color(0xFF9F85ED)
private val PurpleContainer = Color(0xFFEADDFF)
private val PurpleOnContainer = Color(0xFF21005E)
private val Background = Color(0xFFFFFBFF)
private val Surface = Color(0xFFFFFBFF)
private val OnBackground = Color(0xFF1C1B1F)
private val OnSurface = Color(0xFF1C1B1F)

// Custom Color Scheme
private val CustomColorScheme = lightColorScheme(
    primary = PurplePrimary,
    secondary = PurpleSecondary,
    tertiary = PurpleSecondary.copy(alpha = 0.7f),
    background = Background,
    surface = Surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = OnBackground,
    onSurface = OnSurface,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = PurpleOnContainer,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F)
)

@Composable
fun ConsistlyTheme(
    content: @Composable () -> Unit
) {
    // Force light theme regardless of system settings
    val colorScheme = CustomColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}