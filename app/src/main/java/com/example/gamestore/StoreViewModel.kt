package com.example.gamestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestore.data.TestCatalog
import com.example.gamestore.data.repository.FavoritesRepository
import com.example.gamestore.data.repository.OrderRepository
import com.example.gamestore.data.repository.PreferencesRepository
import com.example.gamestore.model.BillingType
import com.example.gamestore.model.CreateOrderDto
import com.example.gamestore.model.OrderCustomerDto
import com.example.gamestore.model.OrderLineDto
import com.example.gamestore.model.OrderReceipt
import com.example.gamestore.model.PaymentMethod
import com.example.gamestore.ui.state.CheckoutField
import com.example.gamestore.ui.state.CheckoutUiState
import com.example.gamestore.ui.state.StoreUiState
import com.example.gamestore.validation.CheckoutValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val orderRepository: OrderRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val catalog = TestCatalog.createProducts()
    private val profiles = TestCatalog.profiles

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<StoreUiState> = combine(
        _searchQuery,
        favoritesRepository.observeFavoriteIds(),
        preferencesRepository.observeDarkTheme(),
    ) { query, favoriteIds, isDarkTheme ->
        val favoritesSet = favoriteIds.toSet()

        val updatedCatalog = catalog.map { product ->
            product.copy(isFavorite = product.id in favoritesSet)
        }

        val filtered = if (query.isBlank()) {
            updatedCatalog
        } else {
            updatedCatalog.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
        }

        StoreUiState(
            products = filtered,
            catalog = updatedCatalog,
            profiles = profiles,
            searchQuery = query,
            isDarkTheme = isDarkTheme,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StoreUiState(
            products = catalog,
            catalog = catalog,
            profiles = profiles,
            searchQuery = "",
            isDarkTheme = false,
        ),
    )

    private val _checkoutState = MutableStateFlow(CheckoutUiState())
    val checkoutState: StateFlow<CheckoutUiState> = _checkoutState.asStateFlow()

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearQuery() {
        onQueryChange("")
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            val currentProduct = uiState.value.catalog.find { it.id == productId }
            val isFavorite = currentProduct?.isFavorite ?: false
            if (isFavorite) {
                favoritesRepository.removeFavorite(productId)
            } else {
                favoritesRepository.addFavorite(productId)
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkTheme(enabled)
        }
    }

    fun startCheckout(productId: String) {
        val product = uiState.value.catalog.find { it.id == productId } ?: return

        _checkoutState.update { state ->
            val existing = state.lines.find { it.productId == productId }
            val updatedLines = if (existing == null) {
                state.lines + OrderLineDto(
                    productId = product.id,
                    productName = product.name,
                    quantity = 1,
                    unitPrice = product.price,
                    subtotal = product.price,
                )
            } else {
                state.lines.map { line ->
                    if (line.productId == productId) {
                        line.copy(
                            quantity = line.quantity + 1,
                            subtotal = line.unitPrice * (line.quantity + 1),
                        )
                    } else {
                        line
                    }
                }
            }
            state.copy(lines = updatedLines)
        }
    }

    fun changeQuantity(productId: String, quantity: Int) {
        _checkoutState.update { state ->
            state.copy(
                lines = state.lines.mapNotNull { line ->
                    when {
                        line.productId != productId -> line
                        quantity <= 0 -> null
                        else -> line.copy(
                            quantity = quantity,
                            subtotal = quantity * line.unitPrice,
                        )
                    }
                },
            )
        }
    }

    fun onCheckoutFieldChange(field: CheckoutField, value: String) {
        _checkoutState.update { state ->
            when (field) {
                CheckoutField.NAME -> state.copy(name = value)
                CheckoutField.PHONE -> state.copy(phone = value)
                CheckoutField.NIT -> state.copy(nit = value)
                CheckoutField.BUSINESS_NAME -> state.copy(businessName = value)
            }
        }
    }

    fun onCheckoutFieldTouched(field: CheckoutField) {
        _checkoutState.update { state ->
            when (field) {
                CheckoutField.NAME -> state.copy(isNameTouched = true)
                CheckoutField.PHONE -> state.copy(isPhoneTouched = true)
                CheckoutField.NIT -> state.copy(isNitTouched = true)
                CheckoutField.BUSINESS_NAME -> state.copy(isBusinessNameTouched = true)
            }
        }
    }

    fun onBillingTypeChange(type: BillingType) {
        _checkoutState.update { it.copy(billingType = type) }
    }

    fun onPaymentMethodChange(method: PaymentMethod) {
        _checkoutState.update { it.copy(paymentMethod = method) }
    }

    fun submitOrder() {
        val currentState = _checkoutState.value

        if ((currentState.isSubmitting) || (currentState.receipt != null)) return

        val nameErr = CheckoutValidators.name(currentState.name)
        val phoneErr = CheckoutValidators.phone(currentState.phone)
        val nitErr = if (currentState.billingType == BillingType.NIT) {
            CheckoutValidators.nit(currentState.nit)
        } else null
        val businessErr = if (currentState.billingType == BillingType.NIT) {
            CheckoutValidators.businessName(currentState.businessName)
        } else null

        if (nameErr != null || phoneErr != null || nitErr != null || businessErr != null || currentState.lines.isEmpty()) {
            _checkoutState.update {
                it.copy(
                    isNameTouched = true,
                    isPhoneTouched = true,
                    isNitTouched = true,
                    isBusinessNameTouched = true,
                    nameError = nameErr,
                    phoneError = phoneErr,
                    nitError = nitErr,
                    businessNameError = businessErr,
                )
            }
            return
        }

        val createOrderDto = CreateOrderDto(
            lines = currentState.lines,
            total = currentState.total,
            customer = OrderCustomerDto(
                name = currentState.name.trim(),
                phone = currentState.phone.trim(),
                billingType = currentState.billingType,
                nit = currentState.nit.trim().takeIf { currentState.billingType == BillingType.NIT },
                businessName = currentState.businessName.trim().takeIf { currentState.billingType == BillingType.NIT },
                paymentMethod = currentState.paymentMethod,
            ),
        )

        _checkoutState.update { it.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            val result = orderRepository.createOrder(createOrderDto)
            result.onSuccess { createdOrder ->
                _checkoutState.update { state ->
                    state.copy(
                        lines = emptyList(),
                        isSubmitting = false,
                        receipt = OrderReceipt(
                            id = createdOrder.id,
                            lines = createOrderDto.lines,
                            total = createOrderDto.total,
                            customerName = createOrderDto.customer.name,
                            phone = createOrderDto.customer.phone,
                            billingType = createOrderDto.customer.billingType,
                            nit = createOrderDto.customer.nit,
                            businessName = createOrderDto.customer.businessName,
                            paymentMethod = createOrderDto.customer.paymentMethod,
                        ),
                    )
                }
            }.onFailure { error ->
                _checkoutState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError = error.message ?: "Error al procesar el pedido.",
                    )
                }
            }
        }
    }
}
