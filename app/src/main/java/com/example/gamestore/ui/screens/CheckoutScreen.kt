package com.example.gamestore.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gamestore.model.BillingType
import com.example.gamestore.model.PaymentMethod
import com.example.gamestore.ui.state.CheckoutField
import com.example.gamestore.ui.state.CheckoutUiState

@Composable
fun CheckoutScreen(
    state: CheckoutUiState,
    onFieldChange: (CheckoutField, String) -> Unit,
    onFieldTouched: (CheckoutField) -> Unit,
    onBillingTypeChange: (BillingType) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Regresar")
        }

        Text(
            text = "Checkout",
            style = MaterialTheme.typography.headlineMedium
        )

        val receipt = state.receipt

        if (receipt != null) {
            Text("Pedido confirmado")
            Text("Comprobante: ${receipt.id}")
            Text(receipt.productName)
            Text("Total: \$${receipt.total}")
            Text("Cliente: ${receipt.customerName}")
            Text("Teléfono: ${receipt.phone}")
            Text("Facturación: ${receipt.billingType}")

            receipt.nit?.let {
                Text("NIT: $it")
            }

            receipt.businessName?.let {
                Text("Razón social: $it")
            }

            Text("Pago: ${receipt.paymentMethod.label}")
            Text("Comprobante local de demostración. No se realizó ningún cobro.")
        } else {
            CheckoutTextField(
                label = "Nombre",
                value = state.name,
                error = state.nameError,
                onValueChange = {
                    onFieldChange(CheckoutField.NAME, it)
                },
                onTouched = {
                    onFieldTouched(CheckoutField.NAME)
                }
            )

            CheckoutTextField(
                label = "Teléfono",
                value = state.phone,
                error = state.phoneError,
                onValueChange = {
                    onFieldChange(CheckoutField.PHONE, it)
                },
                onTouched = {
                    onFieldTouched(CheckoutField.PHONE)
                },
                keyboardType = KeyboardType.Phone
            )

            Text("Tipo de facturación")

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BillingType.entries.forEach { type ->
                    FilterChip(
                        selected = state.billingType == type,
                        onClick = {
                            onBillingTypeChange(type)
                        },
                        label = {
                            Text(type.name)
                        }
                    )
                }
            }

            if (state.billingType == BillingType.NIT) {
                CheckoutTextField(
                    label = "NIT",
                    value = state.nit,
                    error = state.nitError,
                    onValueChange = {
                        onFieldChange(CheckoutField.NIT, it)
                    },
                    onTouched = {
                        onFieldTouched(CheckoutField.NIT)
                    },
                    keyboardType = KeyboardType.Number
                )

                CheckoutTextField(
                    label = "Razón social",
                    value = state.businessName,
                    error = state.businessNameError,
                    onValueChange = {
                        onFieldChange(CheckoutField.BUSINESS_NAME, it)
                    },
                    onTouched = {
                        onFieldTouched(CheckoutField.BUSINESS_NAME)
                    }
                )
            }

            Text("Método de pago")

            PaymentMethod.entries.forEach { method ->
                FilterChip(
                    selected = state.paymentMethod == method,
                    onClick = {
                        onPaymentMethodChange(method)
                    },
                    label = {
                        Text(method.label)
                    }
                )
            }

            Button(
                onClick = onSubmit,
                enabled = state.isFormValid && state.productId != null
            ) {
                Text("Confirmar pedido")
            }
        }
    }
}

@Composable
private fun CheckoutTextField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    onTouched: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var hadFocus by remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        singleLine = true,
        isError = error != null,
        supportingText = {
            error?.let {
                Text(it)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    hadFocus = true
                } else if (hadFocus) {
                    hadFocus = false
                    onTouched()
                }
            }
    )
}