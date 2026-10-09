package com.example.gamestore.model

import kotlin.math.roundToLong

data class GameProduct(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val developerId: String,
    val imageUrl: String,
    val isAvailable: Boolean,
    val isFavorite: Boolean = false,
    val stock: Int = if (isAvailable) Int.MAX_VALUE else 0,
    val coverColorIndex: Int = 0,
) {
    val priceCents: Long get() = (price * 100).roundToLong()
}
