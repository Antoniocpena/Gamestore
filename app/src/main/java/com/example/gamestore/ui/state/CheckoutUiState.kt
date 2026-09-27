package com.example.gamestore.ui.state

import com.example.gamestore.model.BillingType
import com.example.gamestore.model.PaymentMethod

data class CheckoutUiState(
    val fullName: String = "",
    val phone: String = "",
    val billingType: BillingType = BillingType.CF,
    val nit: String = "",
    val razonSocial: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val errors: Map<String, String?> = emptyMap(),
    val isTouched: Map<String, Boolean> = emptyMap(),
    val isFormValid: Boolean = false
)
