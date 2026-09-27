package com.example.gamestore.ui.screens

<<<<<<< Updated upstream
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
=======
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gamestore.StoreViewModel
import com.example.gamestore.model.BillingType
import com.example.gamestore.model.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: StoreViewModel,
    onConfirmOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkoutState = viewModel.checkoutUiState.collectAsStateWithLifecycle()
    val uiState = checkoutState.value

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val nitFocusRequester = remember { FocusRequester() }

    val isConfirmEnabled by remember(checkoutState) {
        derivedStateOf { uiState.isFormValid }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = viewModel::onNameChange,
            label = { Text("Nombre completo") },
            singleLine = true,
            isError = uiState.errors["name"] != null && (uiState.isTouched["name"] == true),
            supportingText = { uiState.errors["name"]?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            )
        )

        OutlinedTextField(
            value = uiState.phone,
            onValueChange = viewModel::onPhoneChange,
            label = { Text("Teléfono / WhatsApp") },
            singleLine = true,
            isError = uiState.errors["phone"] != null && (uiState.isTouched["phone"] == true),
            supportingText = { uiState.errors["phone"]?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = if (uiState.billingType == BillingType.NIT) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = { nitFocusRequester.requestFocus() },
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            )
        )

        Column(Modifier.selectableGroup()) {
            listOf(BillingType.CF, BillingType.NIT).forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .selectable(
                            selected = uiState.billingType == type,
                            role = Role.RadioButton,
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                viewModel.onBillingTypeChange(type)
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.billingType == type,
                        onClick = null
                    )
                    Text(if (type == BillingType.CF) "Consumidor Final (CF)" else "Factura con NIT")
                }
            }
        }

        AnimatedVisibility(visible = uiState.billingType == BillingType.NIT) {
            Column {
                OutlinedTextField(
                    value = uiState.nit,
                    onValueChange = viewModel::onNitChange,
                    modifier = Modifier.focusRequester(nitFocusRequester),
                    label = { Text("NIT") },
                    singleLine = true,
                    isError = uiState.errors["nit"] != null && (uiState.isTouched["nit"] == true),
                    supportingText = { uiState.errors["nit"]?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                    )
                )

                OutlinedTextField(
                    value = uiState.razonSocial,
                    onValueChange = viewModel::onRazonSocialChange,
                    label = { Text("Razón Social") },
                    singleLine = true,
                    isError = uiState.errors["razon"] != null && (uiState.isTouched["razon"] == true),
                    supportingText = { uiState.errors["razon"]?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    )
                )
            }
        }

        Column(Modifier.selectableGroup()) {
            listOf(PaymentMethod.CASH, PaymentMethod.TRANSFER).forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .selectable(
                            selected = uiState.paymentMethod == method,
                            role = Role.RadioButton,
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                viewModel.onPaymentMethodChange(method)
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.paymentMethod == method,
                        onClick = null
                    )
                    Text(if (method == PaymentMethod.CASH) "Efectivo contra entrega" else "Transferencia bancaria")
                }
            }
        }

        Button(
            onClick = onConfirmOrder,
            enabled = isConfirmEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar pedido")
        }
    }
}
>>>>>>> Stashed changes
