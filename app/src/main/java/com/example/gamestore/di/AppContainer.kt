package com.example.gamestore.di

import android.content.Context
import androidx.room3.Room
import com.example.gamestore.data.OrderApi
import com.example.gamestore.data.StoreDatabase
import com.example.gamestore.data.repository.FavoritesRepository
import com.example.gamestore.data.repository.OrderRepository
import com.example.gamestore.data.repository.PreferencesRepository
import com.example.gamestore.data.repository.ProductRepository
import com.example.gamestore.data.repository.impl.FirestoreProductRepository
import com.example.gamestore.data.repository.impl.LocalFavoritesRepository
import com.example.gamestore.data.repository.impl.LocalPreferencesRepository
import com.example.gamestore.data.repository.impl.MockApiOrderRepository
import com.example.gamestore.preferences.PreferencesManager

class AppContainer(
    context: Context,
) {

    private val database = Room.databaseBuilder(
        context,
        StoreDatabase::class.java,
        "store.db",
    ).build()

    private val preferencesManager = PreferencesManager(context)

    private val orderApi = OrderApi()

    val productRepository: ProductRepository =
        FirestoreProductRepository()

    val favoritesRepository: FavoritesRepository =
        LocalFavoritesRepository(
            database.favoriteDao()
        )

    val preferencesRepository: PreferencesRepository =
        LocalPreferencesRepository(
            preferencesManager
        )

    val orderRepository: OrderRepository =
        MockApiOrderRepository(
            orderApi
        )
}