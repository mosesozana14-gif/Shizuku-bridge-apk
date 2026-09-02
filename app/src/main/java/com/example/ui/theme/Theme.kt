package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NaturalColorScheme = lightColorScheme(
    primary = NaturalPrimary,
    onPrimary = NaturalOnPrimary,
    primaryContainer = NaturalPrimaryContainer,
    onPrimaryContainer = NaturalOnPrimaryContainer,
    secondary = NaturalSecondary,
    onSecondary = NaturalOnSecondary,
    secondaryContainer = NaturalSecondaryContainer,
    onSecondaryContainer = NaturalOnSecondaryContainer,
    tertiary = NaturalTertiary,
    onTertiary = NaturalOnTertiary,
    tertiaryContainer = NaturalTertiaryContainer,
    onTertiaryContainer = NaturalOnTertiaryContainer,
    background = NaturalBackground,
    onBackground = NaturalOnBackground,
    surface = NaturalSurface,
    onSurface = NaturalOnSurface,
    surfaceVariant = NaturalSurfaceVariant,
    onSurfaceVariant = NaturalOnSurfaceVariant,
    outline = NaturalOutline,
    outlineVariant = NaturalOutlineVariant,
    error = NaturalDanger,
    errorContainer = NaturalDangerContainer,
    onError = NaturalOnDanger
)

private val NaturalDarkColorScheme = darkColorScheme(
    primary = NaturalPrimaryContainer,
    onPrimary = NaturalOnPrimaryContainer,
    primaryContainer = NaturalPrimary,
    onPrimaryContainer = NaturalPrimaryContainer,
    secondary = NaturalSecondaryContainer,
    onSecondary = NaturalOnSecondaryContainer,
    secondaryContainer = NaturalSecondary,
    onSecondaryContainer = NaturalSecondaryContainer,
    tertiary = NaturalTertiaryContainer,
    onTertiary = NaturalOnTertiaryContainer,
    tertiaryContainer = NaturalTertiary,
    onTertiaryContainer = NaturalTertiaryContainer,
    background = Color(0xFF1C1917),
    onBackground = Color(0xFFEDE0D4),
    surface = Color(0xFF292524),
    onSurface = Color(0xFFEDE0D4),
    surfaceVariant = Color(0xFF38332E),
    onSurfaceVariant = Color(0xFFD7C4B7),
    outline = Color(0xFF857467),
    outlineVariant = Color(0xFF53483E),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NaturalDarkColorScheme else NaturalColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
