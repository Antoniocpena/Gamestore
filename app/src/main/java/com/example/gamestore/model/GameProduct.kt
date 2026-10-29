package com.example.gamestore.model

import kotlin.math.roundToLong

data class GameProduct(
    val id: String,
    val name: String,
    val description: String,
    val price: Double = 0.0,
    val developerId: String,
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val isFavorite: Boolean = false,
    val priceCents: Long = (price * 100).roundToLong(),
    val stock: Int = if (isAvailable) 10 else 0,
    val coverColorIndex: Int = 0,
)
