package com.example.gamestore.model

data class OrderReceipt(
    val id: String,
    val productId: String,
    val productName: String,
    val total: Double,
    val customerName: String,
    val phone: String,
    val billingType: BillingType,
    val nit: String?,
    val businessName: String?,
    val paymentMethod: PaymentMethod
)