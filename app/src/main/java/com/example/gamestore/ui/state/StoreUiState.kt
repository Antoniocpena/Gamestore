package com.example.gamestore.ui.state

import com.example.gamestore.data.OrderLineEntity
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct

data class StoreUiState(
    val products: List<GameProduct> = emptyList(),
    val catalog: List<GameProduct> = emptyList(),
    val profiles: List<DeveloperProfile> = emptyList(),
    val searchQuery: String = "",
    val orderLines: List<OrderLineEntity> = emptyList(),
    val isDarkTheme: Boolean? = null,
)

fun buildStoreUiState(
    products: List<GameProduct>,
    profiles: List<DeveloperProfile>,
    query: String,
    favoriteIds: List<String>,
    orderLines: List<OrderLineEntity>,
    darkTheme: Boolean,
): StoreUiState {
    val favorites = favoriteIds.toSet()

    val catalog = products.map { product ->
        product.copy(isFavorite = product.id in favorites)
    }

    val filtered =
        if (query.isBlank()) {
            catalog
        } else {
            catalog.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
        }

    return StoreUiState(
        products = filtered,
        catalog = catalog,
        profiles = profiles,
        searchQuery = query,
        orderLines = orderLines,
        isDarkTheme = darkTheme,
    )
}
