package com.example.gamestore.model

data class GameProduct(
    val id: String,
    val name: String,
    val description: String,
    val priceCents: Long,
    val developerId: String,
    val stock: Int,
    val coverColorIndex: Int,
    val isFavorite: Boolean = false,
) {
    val isAvailable: Boolean
        get() = stock > 0
}
