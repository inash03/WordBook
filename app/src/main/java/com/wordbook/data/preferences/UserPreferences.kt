package com.wordbook.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class AppPreferences(
    val isDarkMode: Boolean = false,
    val followSystemTheme: Boolean = true,
    val accentColor: String = "Purple",
    val defaultTestMode: String = "SEQUENTIAL"
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DARK_MODE = booleanPreferencesKey("dark_mode")
    private val FOLLOW_SYSTEM = booleanPreferencesKey("follow_system")
    private val ACCENT_COLOR = stringPreferencesKey("accent_color")
    private val DEFAULT_TEST_MODE = stringPreferencesKey("default_test_mode")

    val preferences: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            isDarkMode = prefs[DARK_MODE] ?: false,
            followSystemTheme = prefs[FOLLOW_SYSTEM] ?: true,
            accentColor = prefs[ACCENT_COLOR] ?: "Purple",
            defaultTestMode = prefs[DEFAULT_TEST_MODE] ?: "SEQUENTIAL"
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setFollowSystemTheme(follow: Boolean) {
        context.dataStore.edit { it[FOLLOW_SYSTEM] = follow }
    }

    suspend fun setAccentColor(color: String) {
        context.dataStore.edit { it[ACCENT_COLOR] = color }
    }

    suspend fun setDefaultTestMode(mode: String) {
        context.dataStore.edit { it[DEFAULT_TEST_MODE] = mode }
    }
}
