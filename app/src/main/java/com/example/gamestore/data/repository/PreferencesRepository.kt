package com.example.gamestore.data.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    fun observeDarkTheme():
            Flow<Boolean>

    suspend fun setDarkTheme(
        enabled: Boolean
    ): Result<Unit>
}