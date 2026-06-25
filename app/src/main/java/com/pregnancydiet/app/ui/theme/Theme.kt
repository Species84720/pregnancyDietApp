package com.pregnancydiet.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = RosePrimary,
    onPrimary = RoseOnPrimary,
    primaryContainer = RosePrimaryContainer,
    onPrimaryContainer = RoseOnPrimaryContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    background = CreamBackground,
    surface = CreamBackground,
    onBackground = DeepText,
    onSurface = DeepText,
)

private val DarkColorScheme = darkColorScheme(
    primary = RosePrimaryContainer,
    onPrimary = RoseOnPrimaryContainer,
    primaryContainer = RosePrimary,
    onPrimaryContainer = RoseOnPrimary,
    secondary = SageOnSecondary,
    onSecondary = SageSecondary,
)

@Composable
fun PregnancyDietTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PregnancyTypography,
        content = content,
    )
}