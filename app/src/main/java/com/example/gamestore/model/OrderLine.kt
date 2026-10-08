package com.example.gamestore.model

data class OrderLine(
    val productId: String,
    val productName: String,
    val unitPriceCents: Long,
    val quantity: Int,
)
