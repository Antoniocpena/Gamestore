package com.example.gamestore.ui.state

import com.example.gamestore.data.OrderLineEntity
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct

data class StoreUiState(
    val products: List<GameProduct> = emptyList(),
    val profiles: List<DeveloperProfile> = emptyList(),
    val searchQuery: String = "",
    val orderLines: List<OrderLineEntity> = emptyList(),
    val isDarkTheme: Boolean? = null,
)
