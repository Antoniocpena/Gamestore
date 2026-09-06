package com.example.gamestore.ui.state

import com.example.gamestore.model.GameProduct
import com.example.gamestore.model.DeveloperProfile

data class StoreUiState(
    val products: List<GameProduct> = emptyList(),
    val profiles: List<DeveloperProfile> = emptyList()
)
