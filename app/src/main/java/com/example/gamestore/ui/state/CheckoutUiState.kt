package com.example.gamestore.ui.state

import com.example.gamestore.model.BillingType
import com.example.gamestore.model.OrderReceipt
import com.example.gamestore.model.PaymentMethod

enum class CheckoutField {
    NAME,
    PHONE,
    NIT,
    BUSINESS_NAME
}

data class CheckoutUiState(
    val productId: String? = null,
    val name: String = "",
    val phone: String = "",
    val billingType: BillingType = BillingType.CF,
    val nit: String = "",
    val businessName: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val nameError: String? = null,
    val phoneError: String? = null,
    val nitError: String? = null,
    val businessNameError: String? = null,
    val isNameTouched: Boolean = false,
    val isPhoneTouched: Boolean = false,
    val isNitTouched: Boolean = false,
    val isBusinessNameTouched: Boolean = false,
    val receipt: OrderReceipt? = null
) {
    val isFormValid: Boolean
        get() {
            if (nameError != null || name.isBlank()) return false
            if (phoneError != null || phone.isBlank()) return false
            if (billingType == BillingType.NIT) {
                if (nitError != null || nit.isBlank()) return false
                if (businessNameError != null || businessName.isBlank()) return false
            }
            return true
        }
}
