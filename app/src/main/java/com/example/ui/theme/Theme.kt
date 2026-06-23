package com.example.ui.theme

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
    primary = MinimalPrimaryDark,
    secondary = MinimalSecondaryDark,
    tertiary = AccentAmber,
    background = CharcoalDark,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    primaryContainer = CardDark,
    secondaryContainer = CardDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = MinimalPrimary,
    secondary = MinimalSecondary,
    tertiary = AccentAmber,
    background = OffWhiteLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    primaryContainer = CardLight,
    secondaryContainer = CardLight,
    surfaceVariant = CardLight,
    onSurfaceVariant = TextSecondaryLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamic color toggleable, default to false so our beautiful brand colors are showcased
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
