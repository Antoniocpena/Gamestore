package com.example.gamestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestore.model.GameProduct
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.ui.state.StoreUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        StoreUiState(
            products = listOf(
                GameProduct("1", "Elder Quest", "RPG épico", 59.99, "dev1"),
                GameProduct("2", "Speed Racer X", "Juego de carreras futurista", 39.99, "dev2"),
                GameProduct("3", "Puzzle Master", "Rompecabezas desafiante", 19.99, "dev1")
            ),
            profiles = listOf(
                DeveloperProfile("dev1", "Epic Studios", "Desarrollador", "USA", "Expertos en RPGs"),
                DeveloperProfile("dev2", "FastGames", "Desarrollador", "Japón", "Especialistas en juegos de carreras")
            )
        )
    )
    val uiState: StateFlow<StoreUiState> = _uiState

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            _uiState.update { current ->
                val updatedProducts = current.products.map { product ->
                    if (product.id == productId) {
                        product.copy(isFavorite = !product.isFavorite)
                    } else product
                }
                current.copy(products = updatedProducts)
            }
        }
    }
}
