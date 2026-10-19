package com.notasparamibebe.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue900,
    secondary = RoseFavorite,
    onSecondary = Color.White,
    secondaryContainer = SoftPink,
    onSecondaryContainer = RoseDark,
    background = NeutralLight,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = Blue050,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight
)

@Composable
fun NotasParaMiBebeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
