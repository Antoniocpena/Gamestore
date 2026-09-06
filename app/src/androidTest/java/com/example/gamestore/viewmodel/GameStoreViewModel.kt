package com.example.gamestore.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct
import com.example.gamestore.ui.GameStoreUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameStoreViewModel : ViewModel() {

    private val initialProfiles = listOf(
        DeveloperProfile(
            id = "studio_santa_monica",
            name = "Santa Monica Studio",
            role = "Estudio desarrollador",
            location = "Los Ángeles, Estados Unidos",
            description = "Estudio reconocido por desarrollar aventuras de acción con narrativas cinematográficas."
        ),
        DeveloperProfile(
            id = "nintendo_epd",
            name = "Nintendo EPD",
            role = "Desarrollador y productor",
            location = "Kioto, Japón",
            description = "División de Nintendo dedicada al desarrollo de algunas de sus principales franquicias."
        )
    )

    private val initialProducts = listOf(
        GameProduct(
            id = "god_of_war_ragnarok",
            name = "God of War Ragnarök",
            description = "Aventura de acción inspirada en la mitología nórdica.",
            price = 69.99,
            developerId = "studio_santa_monica"
        ),
        GameProduct(
            id = "zelda_tears_of_the_kingdom",
            name = "The Legend of Zelda: Tears of the Kingdom",
            description = "Aventura de mundo abierto con exploración y construcción.",
            price = 69.99,
            developerId = "nintendo_epd"
        ),
        GameProduct(
            id = "super_mario_odyssey",
            name = "Super Mario Odyssey",
            description = "Juego de plataformas y exploración alrededor del mundo.",
            price = 59.99,
            developerId = "nintendo_epd"
        )
    )

    private val _uiState = MutableStateFlow(
        GameStoreUiState(
            products = initialProducts,
            profiles = initialProfiles
        )
    )

    val uiState: StateFlow<GameStoreUiState> = _uiState.asStateFlow()

    fun toggleFavorite(productId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                products = currentState.products.map { product ->
                    if (product.id == productId) {
                        product.copy(isFavorite = !product.isFavorite)
                    } else {
                        product
                    }
                }
            )
        }
    }

    fun findProduct(productId: String): GameProduct? {
        return _uiState.value.products.find { product ->
            product.id == productId
        }
    }

    fun findProfile(profileId: String): DeveloperProfile? {
        return _uiState.value.profiles.find { profile ->
            profile.id == profileId
        }
    }
}