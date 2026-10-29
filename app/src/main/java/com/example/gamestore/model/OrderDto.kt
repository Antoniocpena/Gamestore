package com.example.gamestore.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderLineDto(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)

@Serializable
data class OrderCustomerDto(
    val name: String,
    val phone: String,
    val billingType: BillingType,
    val nit: String? = null,
    val businessName: String? = null,
    val paymentMethod: PaymentMethod
)

@Serializable
data class CreateOrderDto(
    val lines: List<OrderLineDto>,
    val total: Double,
    val customer: OrderCustomerDto
)

@Serializable
data class CreatedOrderDto(
    val id: String
)
