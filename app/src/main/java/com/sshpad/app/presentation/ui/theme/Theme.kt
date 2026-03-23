package com.sshpad.app.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * App Theme Colors
 * Week 7: Fixed per UI requirements
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2563EB),  // Fixed: Blue #2563EB
    secondary = Color(0xFF10B981),
    tertiary = Color(0xFFF59E0B),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),  // Fixed: Blue #2563EB
    secondary = Color(0xFF10B981),
    tertiary = Color(0xFFF59E0B),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

/**
 * Dracula Theme Colors for Terminal
 * Week 7: Dracula theme support
 */
object DraculaTheme {
    val background = Color(0xFF282A36)
    val foreground = Color(0xFFF8F8F2)
    val comment = Color(0xFF6272A4)
    val cyan = Color(0xFF8BE9FD)
    val green = Color(0xFF50FA7B)
    val orange = Color(0xFFFFB86C)
    val pink = Color(0xFFFF79C6)
    val purple = Color(0xFFBD93F9)
    val red = Color(0xFFFF5555)
    val yellow = Color(0xFFF1FA8C)
}

@Composable
fun SSHPadTheme(
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
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
