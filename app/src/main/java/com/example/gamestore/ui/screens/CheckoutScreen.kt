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
    onQuantityChange: (String, Int) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(
            onClick = onBack,
            enabled = !state.isSubmitting
        ) {
            Text("Regresar")
        }

        Text(
            text = "Checkout",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Pedido: ${state.itemCount} producto(s)",
            style = MaterialTheme.typography.titleMedium
        )

        state.lines.forEach { line ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(line.productName)
                    Text(
                        "$${"%.2f".format(line.unitPrice)} × " +
                                "${line.quantity} = " +
                                "$${"%.2f".format(line.subtotal)}"
                    )
                }

                TextButton(
                    onClick = {
                        onQuantityChange(
                            line.productId,
                            line.quantity - 1
                        )
                    },
                    enabled = !state.isSubmitting
                ) {
                    Text("−")
                }

                TextButton(
                    onClick = {
                        onQuantityChange(
                            line.productId,
                            line.quantity + 1
                        )
                    },
                    enabled = !state.isSubmitting
                ) {
                    Text("+")
                }
            }
        }

        Text("Total: $${"%.2f".format(state.total)}")

        if (state.lines.isEmpty()) {
            Text("Agrega un producto desde el catálogo.")
        }

        state.submitError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (state.isSubmitting) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Text("Enviando pedido…")
        }

        CompositionLocalProvider(
            LocalCheckoutEnabled provides !state.isSubmitting
        ) {
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

            Text(
                "Tipo de facturación",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BillingType.entries.forEach { type ->
                    FilterChip(
                        selected = state.billingType == type,
                        enabled = !state.isSubmitting,
                        onClick = {
                            onBillingTypeChange(type)
                        },
                        label = {
                            Text(type.label)
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
                        onFieldChange(
                            CheckoutField.BUSINESS_NAME,
                            it
                        )
                    },
                    onTouched = {
                        onFieldTouched(
                            CheckoutField.BUSINESS_NAME
                        )
                    }
                )
            }

            Text(
                "Método de pago",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentMethod.entries.forEach { method ->
                    FilterChip(
                        selected =
                            state.paymentMethod == method,
                        enabled = !state.isSubmitting,
                        onClick = {
                            onPaymentMethodChange(method)
                        },
                        label = {
                            Text(method.label)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSubmit,
                enabled =
                    state.isFormValid &&
                            state.lines.isNotEmpty() &&
                            !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar pedido")
            }
        }
    }
}

private val LocalCheckoutEnabled =
    staticCompositionLocalOf { true }

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
        mutableStateOf(value = false)
    }

    OutlinedTextField(
        value = value,
        enabled = LocalCheckoutEnabled.current,
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
