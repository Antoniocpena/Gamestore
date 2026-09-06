package com.example.gamestore.ui

import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct

data class GameStoreUiState(
    val products: List<GameProduct> = emptyList(),
    val profiles: List<DeveloperProfile> = emptyList()
)