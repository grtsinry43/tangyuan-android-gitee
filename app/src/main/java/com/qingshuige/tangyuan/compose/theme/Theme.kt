package com.qingshuige.tangyuan.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 糖圆主题色彩
private val TangyuanBlue = Color(0xFF1976D2)
private val TangyuanLightBlue = Color(0xFF63A4FF)
private val TangyuanDarkBlue = Color(0xFF004BA0)

private val TangyuanOrange = Color(0xFFFF6D00)
private val TangyuanLightOrange = Color(0xFFFF9E40)
private val TangyuanDarkOrange = Color(0xFFC43E00)

private val DarkColorScheme = darkColorScheme(
    primary = TangyuanLightBlue,
    secondary = TangyuanLightOrange,
    tertiary = Color(0xFF82B1FF),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = TangyuanBlue,
    secondary = TangyuanOrange,
    tertiary = Color(0xFF2196F3),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun TangyuanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}