package com.pocketlawbook.alaska.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Alaska's flag is gold stars on a field of deep blue, so the app takes its two
// signal colours from it: blue carries Alaska law, gold carries federal law.
private val FlagBlue = Color(0xFF1B4570)
private val FlagBlueLight = Color(0xFF7FB0E4)
private val FlagGold = Color(0xFFA87209)
private val FlagGoldLight = Color(0xFFE0AE4B)

private val InkLight = Color(0xFF0E2233)
private val InkDark = Color(0xFFE5EEF5)
private val GroundLight = Color(0xFFEDF1F5)
private val GroundDark = Color(0xFF0A141D)
private val SurfaceLight = Color(0xFFFFFFFF)
private val SurfaceDark = Color(0xFF111F2B)

private val LightScheme = lightColorScheme(
    primary = FlagBlue,
    onPrimary = Color.White,
    secondary = FlagGold,
    onSecondary = Color.White,
    background = GroundLight,
    onBackground = InkLight,
    surface = SurfaceLight,
    onSurface = InkLight,
    surfaceVariant = Color(0xFFE3EAF1),
    onSurfaceVariant = Color(0xFF33495B),
    outline = Color(0xFFC9D6E1),
    error = Color(0xFFA3302A)
)

private val DarkScheme = darkColorScheme(
    primary = FlagBlueLight,
    onPrimary = Color(0xFF08192B),
    secondary = FlagGoldLight,
    onSecondary = Color(0xFF2A1E04),
    background = GroundDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = Color(0xFF172937),
    onSurfaceVariant = Color(0xFFC0D0DD),
    outline = Color(0xFF2A3F51),
    error = Color(0xFFE8776F)
)

/** Colour for a jurisdiction chip. Blue for Alaska, gold for federal. */
@Composable
fun jurisdictionColor(isFederal: Boolean): Color {
    val dark = isSystemInDarkTheme()
    return when {
        isFederal && dark -> FlagGoldLight
        isFederal -> FlagGold
        dark -> FlagBlueLight
        else -> FlagBlue
    }
}

@Composable
fun PocketLawbookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // The app draws edge-to-edge (enabled in MainActivity), so the status
            // bar itself is transparent; only its icon contrast is set here.
            // Setting Window.statusBarColor is deprecated and, on apps targeting
            // API 35+, ignored by the platform in favor of edge-to-edge.
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
