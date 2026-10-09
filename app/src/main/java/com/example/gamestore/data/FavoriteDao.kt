package com.example.gamestore.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT productId FROM favorites")
    fun observeFavoriteIds(): Flow<List<String>>

    @Query(
        "SELECT EXISTS(SELECT 1 FROM favorites WHERE productId = :productId)"
    )
    suspend fun isFavorite(productId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
}