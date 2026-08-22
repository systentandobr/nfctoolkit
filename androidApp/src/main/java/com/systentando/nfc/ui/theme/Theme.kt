package com.systentando.nfc.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = BrandTeal,
    onPrimary = TextPrimary,
    primaryContainer = Surface03,
    onPrimaryContainer = TextPrimary,
    secondary = BrandCyan,
    onSecondary = TextPrimary,
    secondaryContainer = Surface02,
    onSecondaryContainer = TextPrimary,
    tertiary = BrandTealLight,
    onTertiary = TextPrimary,
    background = BackgroundBase,
    onBackground = TextPrimary,
    surface = Surface01,
    onSurface = TextPrimary,
    surfaceVariant = Surface02,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = TextPrimary,
    outline = Surface03,
    outlineVariant = Surface02,
    scrim = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandTealDark,
    onPrimary = TextPrimary,
    primaryContainer = BrandTealLight,
    onPrimaryContainer = BackgroundBase,
    secondary = BrandCyan,
    onSecondary = TextPrimary,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = BackgroundBase,
    tertiary = BrandTeal,
    onTertiary = TextPrimary,
    background = Color(0xFFF8FAFA),
    onBackground = Color(0xFF061616),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF061616),
    surfaceVariant = Color(0xFFE3EBEA),
    onSurfaceVariant = Color(0xFF466B68),
    error = Error,
    onError = TextPrimary,
    outline = Color(0xFF9EC8C4),
    outlineVariant = Color(0xFFE3EBEA),
    scrim = Color(0xFF000000)
)

@Composable
fun NFCToolkitTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
