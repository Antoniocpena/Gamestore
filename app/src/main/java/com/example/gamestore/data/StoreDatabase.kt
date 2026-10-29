package com.example.gamestore.data

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [FavoriteEntity::class, OrderLineEntity::class],
    version = 1
)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun orderLineDao(): OrderLineDao
}
