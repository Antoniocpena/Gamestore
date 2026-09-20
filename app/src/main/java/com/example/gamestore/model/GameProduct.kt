package com.example.gamestore.model

data class GameProduct(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val developerId: String,
    val imageUrl: String,
    val isAvailable: Boolean,
    val isFavorite: Boolean = false
)