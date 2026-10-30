package com.example.gamestore.data.repository

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {

    fun observeFavoriteIds():
            Flow<List<String>>

    suspend fun addFavorite(
        productId: String
    ): Result<Unit>

    suspend fun removeFavorite(
        productId: String
    ): Result<Unit>
}