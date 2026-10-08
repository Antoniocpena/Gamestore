package com.example.gamestore.ui.state

import com.example.gamestore.model.BillingType
import com.example.gamestore.model.OrderReceipt
import com.example.gamestore.model.PaymentMethod

data class CheckoutUiState(
    val productId: String? = null,
    val name: String = "",
    val phone: String = "",
    val billingType: BillingType = BillingType.CF,
    val nit: String = "",
    val businessName: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val touchedFields: Set<CheckoutField> = emptySet(),
    val nameError: String? = null,
    val phoneError: String? = null,
    val nitError: String? = null,
    val businessNameError: String? = null,
    val isFormValid: Boolean = false,
    val receipt: OrderReceipt? = null,
)
