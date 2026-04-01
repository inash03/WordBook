package com.wordbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.wordbook.data.preferences.UserPreferencesRepository
import com.wordbook.presentation.navigation.NavGraph
import com.wordbook.presentation.theme.AccentColor
import com.wordbook.presentation.theme.WordBookTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs by preferencesRepository.preferences.collectAsState(
                initial = com.wordbook.data.preferences.AppPreferences()
            )
            val isSystemDark = isSystemInDarkTheme()
            val isDark = if (prefs.followSystemTheme) isSystemDark else prefs.isDarkMode
            val accent = AccentColor.entries.find { it.displayName == prefs.accentColor }
                ?: AccentColor.Purple

            WordBookTheme(
                darkTheme = isDark,
                dynamicColor = prefs.followSystemTheme,
                accentColor = accent
            ) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
