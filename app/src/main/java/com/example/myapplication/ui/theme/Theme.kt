package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import android.graphics.Color as AndroidColor

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF0B3D91),

    secondary = SecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A8A),

    tertiary = TertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4E6),
    onTertiaryContainer = Color(0xFF9F1239),

    background = BackgroundLight,
    onBackground = Color(0xFF111827),

    surface = SurfaceLight,
    onSurface = Color(0xFF111827),

    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF4B5563),

    outline = Color(0xFF6B7280),
    outlineVariant = Color(0xFFD1D5DB),

    error = ErrorLight,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFEFF6FF),

    secondary = SecondaryDark,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E40AF),
    onSecondaryContainer = Color(0xFFDBEAFE),

    tertiary = TertiaryDark,
    onTertiary = Color(0xFF0F172A),
    tertiaryContainer = Color(0xFF881337),
    onTertiaryContainer = Color(0xFFFFE4E6),

    background = BackgroundDark,
    onBackground = Color(0xFFE5E7EB),

    surface = SurfaceDark,
    onSurface = Color(0xFFE5E7EB),

    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFCBD5E1),

    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFF334155),

    error = ErrorDark,
    onError = Color(0xFF0F172A)
)

@Composable
fun NewsAppTheme(
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

            // Cho phép app vẽ tràn ra phía sau status bar/navigation bar
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Làm status bar trong suốt
            window.statusBarColor = AndroidColor.TRANSPARENT

            // Làm navigation bar trong suốt nếu muốn tràn cả dưới
            window.navigationBarColor = AndroidColor.TRANSPARENT

            val insetsController = WindowCompat.getInsetsController(window, view)

            // Light theme thì icon status bar nên màu tối
            // Dark theme thì icon status bar nên màu sáng
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NewsTypography,
        shapes = NewsShapes,
        content = content
    )
}

// Custom Shapes cho ứng dụng tin tức
val NewsShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)