package com.example.gamestore.data.repository.impl

import com.example.gamestore.data.repository.PreferencesRepository
import com.example.gamestore.preferences.PreferencesManager

class LocalPreferencesRepository(
    private val preferences: PreferencesManager
) : PreferencesRepository {

    override fun observeDarkTheme() =
        preferences.darkThemeFlow

    override suspend fun setDarkTheme(
        enabled: Boolean
    ): Result<Unit> =
        runCatching {
            preferences.setDarkTheme(enabled)
        }
}