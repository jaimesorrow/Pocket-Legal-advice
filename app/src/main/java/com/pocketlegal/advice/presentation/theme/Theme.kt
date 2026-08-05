package com.pocketlegal.advice.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = OnNavy,
    primaryContainer = NavyLight,
    secondary = Gold,
    onSecondary = Navy,
    secondaryContainer = GoldLight,
    background = Surface,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldLight,
    onPrimary = Navy,
    primaryContainer = NavyLight,
    secondary = Gold,
    onSecondary = Navy,
    background = Navy,
    surface = NavyLight,
    surfaceVariant = NavyLight,
    error = ErrorRed
)

@Composable
fun PocketLawyerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
