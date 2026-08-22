package com.systentando.nfc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnSurface,
    primaryContainer = PrimaryTealDark,
    onPrimaryContainer = OnSurface,
    secondary = SecondaryGreen,
    onSecondary = OnSurface,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = OnSurface,
    tertiary = AccentCyan,
    onTertiary = OnSurface,
    background = BackgroundDark,
    onBackground = OnSurface,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceMuted,
    error = ErrorRed,
    onError = OnSurface,
    outline = PrimaryTealDark
)

@Composable
fun NFCToolkitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
