package com.example.weatherapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1EB980),
    secondary = Color(0xFF045D56),
    background = Color(0xFF121212),
    surface = Color(0xFF1D1D1D),
    onPrimary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1EB980),
    secondary = Color(0xFF045D56),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.Black
)

@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colors, content = content)
}
