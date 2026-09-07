package com.example.gamestore

import androidx.lifecycle.ViewModel
import com.example.gamestore.model.GameProduct
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.ui.state.StoreUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun toggleFavorite(productId: String) {
        _uiState.update { currentState ->
            if (currentState.products.none { it.id == productId }) {
                currentState
            } else {
                val updatedFavorites = if (productId in currentState.favoriteProductIds) {
                    currentState.favoriteProductIds - productId
                } else {
                    currentState.favoriteProductIds + productId
                }
                currentState.copy(favoriteProductIds = updatedFavorites)
            }
        }
    }
}
