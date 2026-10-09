package com.example.gamestore.preferences
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesManager(context: Context) {
    private val appContext = context.applicationContext

    private companion object {
        val PREF_DARK_THEME = booleanPreferencesKey("pref_dark_theme")
    }

    val darkThemeFlow: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[PREF_DARK_THEME] ?: false
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[PREF_DARK_THEME] = enabled
        }
    }
}
