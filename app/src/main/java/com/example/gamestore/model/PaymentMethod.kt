package com.example.gamestore.model

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentMethod(val label: String) {
    CASH("Efectivo contra entrega"),
    TRANSFER("Transferencia bancaria")
}
