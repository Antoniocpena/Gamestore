package com.example.gamestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gamestore.data.repository.FavoritesRepository
import com.example.gamestore.data.repository.OrderRepository
import com.example.gamestore.data.repository.PreferencesRepository

class StoreViewModelFactory(
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
                favoritesRepository,
                orderRepository,
                preferencesRepository,
            ) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
