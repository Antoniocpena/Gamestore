package com.example.gamestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gamestore.data.repository.FavoritesRepository
import com.example.gamestore.data.repository.OrderRepository
import com.example.gamestore.data.repository.PreferencesRepository
import com.example.gamestore.data.repository.ProductRepository

class StoreViewModelFactory(
    private val productRepository: ProductRepository,
    private val favoritesRepository: FavoritesRepository,
    private val orderRepository: OrderRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoreViewModel(
                productRepository = productRepository,
                favoritesRepository = favoritesRepository,
                orderRepository = orderRepository,
                preferencesRepository = preferencesRepository,
            ) as T
        }

        throw IllegalArgumentException(
            "Clase ViewModel desconocida: ${modelClass.name}"
        )
    }
}