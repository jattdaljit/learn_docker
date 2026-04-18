package com.dmarts.learndocker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NeoCyan,
    onPrimary = Color.White,
    primaryContainer = NeoCyanContainer,
    onPrimaryContainer = NeoCyanDim,
    secondary = NeoGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = NeoGreenDim,
    tertiary = NeoPurple,
    onTertiary = Color.White,
    tertiaryContainer = NeoPurpleContainer,
    onTertiaryContainer = NeoPurpleDim,
    background = NeoBackground,
    onBackground = NeoTextPrimary,
    surface = NeoSurface,
    onSurface = NeoTextPrimary,
    surfaceVariant = NeoSurfaceVariant,
    onSurfaceVariant = NeoTextSecondary,
    error = NeoRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = NeoRedDim,
    outline = NeoBorder,
    outlineVariant = NeoTextMuted,
)

@Composable
fun LearndockerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = NeoTypography,
        content = content
    )
}
