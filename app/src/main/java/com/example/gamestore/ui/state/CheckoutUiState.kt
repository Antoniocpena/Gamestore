package com.example.gamestore.ui.state

import com.example.gamestore.model.BillingType
<<<<<<< Updated upstream
import com.example.gamestore.model.OrderReceipt
import com.example.gamestore.model.PaymentMethod
import com.example.gamestore.validation.CheckoutValidators

enum class CheckoutField {
    NAME,
    PHONE,
    NIT,
    BUSINESS_NAME
}

data class CheckoutTouched(
    val name: Boolean = false,
    val phone: Boolean = false,
    val nit: Boolean = false,
    val businessName: Boolean = false
)

data class CheckoutUiState(
    val name: String = "",
    val phone: String = "",
    val nit: String = "",
    val businessName: String = "",
    val billingType: BillingType = BillingType.CF,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val isTouched: CheckoutTouched = CheckoutTouched(),
    val productId: String? = null,
    val receipt: OrderReceipt? = null
) {

    val isFormValid: Boolean
        get() =
            CheckoutValidators.name(name) == null &&
                    CheckoutValidators.phone(phone) == null &&
                    (
                            billingType == BillingType.CF ||
                                    (
                                            CheckoutValidators.nit(nit) == null &&
                                                    CheckoutValidators.businessName(businessName) == null
                                            )
                            )

    val nameError: String?
        get() = if (isTouched.name) {
            CheckoutValidators.name(name)
        } else {
            null
        }

    val phoneError: String?
        get() = if (isTouched.phone) {
            CheckoutValidators.phone(phone)
        } else {
            null
        }

    val nitError: String?
        get() = if (
            billingType == BillingType.NIT && isTouched.nit
        ) {
            CheckoutValidators.nit(nit)
        } else {
            null
        }

    val businessNameError: String?
        get() = if (
            billingType == BillingType.NIT && isTouched.businessName
        ) {
            CheckoutValidators.businessName(businessName)
        } else {
            null
        }

    fun touch(field: CheckoutField): CheckoutUiState {
        val updatedTouched = when (field) {
            CheckoutField.NAME ->
                isTouched.copy(name = true)

            CheckoutField.PHONE ->
                isTouched.copy(phone = true)

            CheckoutField.NIT ->
                isTouched.copy(nit = billingType == BillingType.NIT)

            CheckoutField.BUSINESS_NAME ->
                isTouched.copy(
                    businessName = billingType == BillingType.NIT
                )
        }

        return copy(isTouched = updatedTouched)
    }

    fun withBillingType(type: BillingType): CheckoutUiState {
        return copy(
            billingType = type,
            isTouched = if (type == BillingType.CF) {
                isTouched.copy(
                    nit = false,
                    businessName = false
                )
            } else {
                isTouched
            }
        )
    }
}
=======
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
>>>>>>> Stashed changes
