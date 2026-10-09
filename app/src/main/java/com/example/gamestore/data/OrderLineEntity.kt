package com.example.gamestore.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "order_lines")
data class OrderLineEntity(
    @PrimaryKey val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)
