package com.example.gamestore.data.repository.impl

import com.example.gamestore.data.FavoriteDao
import com.example.gamestore.data.FavoriteEntity
import com.example.gamestore.data.repository.FavoritesRepository

class LocalFavoritesRepository(
    private val dao: FavoriteDao
) : FavoritesRepository {

    override fun observeFavoriteIds() =
        dao.observeFavoriteIds()

    override suspend fun addFavorite(
        productId: String
    ): Result<Unit> =
        runCatching {
            dao.insertFavorite(
                FavoriteEntity(productId)
            )
        }

    override suspend fun removeFavorite(
        productId: String
    ): Result<Unit> =
        runCatching {
            dao.deleteFavorite(
                FavoriteEntity(productId)
            )
        }
}