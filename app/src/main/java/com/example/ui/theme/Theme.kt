package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val StudioDarkColorScheme = darkColorScheme(
    primary = CyanWavePrimary,
    onPrimary = Color(0xFF002A32),
    primaryContainer = CyanWaveContainer,
    onPrimaryContainer = OnCyanWaveContainer,
    secondary = CoralCloneSecondary,
    onSecondary = Color(0xFF2B0E00),
    secondaryContainer = CoralCloneContainer,
    onSecondaryContainer = OnCoralCloneContainer,
    tertiary = VioletAiTertiary,
    onTertiary = Color(0xFF1F0A38),
    tertiaryContainer = VioletAiContainer,
    onTertiaryContainer = OnVioletAiContainer,
    background = StudioBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = StudioSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = StudioSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderSubtleDark,
    error = CrimsonError,
    onError = Color.White
)

private val StudioLightColorScheme = lightColorScheme(
    primary = CyanPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF003640),
    secondary = CoralSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFCCBC),
    onSecondaryContainer = Color(0xFF3E1000),
    tertiary = VioletTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE7F6),
    onTertiaryContainer = Color(0xFF21005D),
    background = StudioBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = StudioSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = StudioSurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight
)

val StudioShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StudioDarkColorScheme else StudioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = StudioShapes,
        content = content
    )
}
