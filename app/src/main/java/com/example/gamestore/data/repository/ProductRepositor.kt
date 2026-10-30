package com.example.gamestore.data.repository

import com.example.gamestore.model.GameProduct
import kotlinx.coroutines.flow.Flow

interface ProductRepository {

    fun observeProducts(): Flow<List<GameProduct>>

    suspend fun toggleFavorite(
        productId: String
    ): Result<Unit>
}