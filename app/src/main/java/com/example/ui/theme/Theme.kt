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
    primary = Color(0xFFA5C8F7),
    onPrimary = Color(0xFF00315B),
    primaryContainer = Color(0xFF163E66),
    onPrimaryContainer = Color(0xFFD4E3FF),
    secondary = AmberGoldLight,
    onSecondary = Color(0xFF452200),
    secondaryContainer = Color(0xFF623400),
    onSecondaryContainer = AmberGoldContainer,
    tertiary = CypressEmeraldLight,
    onTertiary = Color(0xFF00382B),
    background = SurfaceDark,
    surface = CardSurfaceDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = LapisNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E7FD),
    onPrimaryContainer = Color(0xFF001B37),
    secondary = AmberGoldSecondary,
    onSecondary = Color.White,
    secondaryContainer = AmberGoldContainer,
    onSecondaryContainer = Color(0xFF331600),
    tertiary = CypressEmeraldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = CypressEmeraldContainer,
    onTertiaryContainer = Color(0xFF002117),
    background = SurfaceLight,
    surface = CardSurfaceLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight
)

@Composable
fun ClusterHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted Bahá'í palette for distinct identity
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
