package com.wordbook.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalAccentColor = staticCompositionLocalOf { AccentColor.Purple }

private fun lightScheme(accent: AccentColor) = lightColorScheme(
    primary = accent.light,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private fun darkScheme(accent: AccentColor) = darkColorScheme(
    primary = accent.dark,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

@Composable
fun WordBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    accentColor: AccentColor = AccentColor.Purple,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkScheme(accentColor)
        else -> lightScheme(accentColor)
    }

    CompositionLocalProvider(LocalAccentColor provides accentColor) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
