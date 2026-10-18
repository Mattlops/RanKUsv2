package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RanKUsColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = NeonGreenDark,
    primaryContainer = NeonGreenContainer,
    onPrimaryContainer = NeonGreenDark,
    secondary = NeonViolet,
    onSecondary = NeonVioletDark,
    secondaryContainer = NeonVioletContainer,
    onSecondaryContainer = NeonViolet,
    tertiary = ElectricCyan,
    onTertiary = ElectricCyanDark,
    tertiaryContainer = ElectricCyanContainer,
    onTertiaryContainer = ElectricCyanDark,
    background = DarkBgSurface,
    onBackground = TextPrimary,
    surface = DarkBgSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextSecondary,
    outline = OutlineMuted,
    outlineVariant = OutlineBorder,
    error = ErrorRed,
    onError = ErrorContainer,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceBright = SurfaceBright
)

@Composable
fun RanKUsTheme(
    content: @Composable () -> Unit
) {
    // RanKUs uses an intentional, custom Dark Mode Gamer theme
    MaterialTheme(
        colorScheme = RanKUsColorScheme,
        typography = Typography,
        content = content
    )
}
