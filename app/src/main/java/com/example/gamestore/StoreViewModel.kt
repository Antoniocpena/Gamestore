package com.example.gamestore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room3.Room
import com.example.gamestore.data.FavoriteEntity
import com.example.gamestore.data.OrderApi
import com.example.gamestore.data.OrderHttpException
import com.example.gamestore.data.OrderLineEntity
import com.example.gamestore.data.StoreDatabase
import com.example.gamestore.data.TestCatalog
import com.example.gamestore.model.BillingType
import com.example.gamestore.model.CreateOrderDto
import com.example.gamestore.model.OrderCustomerDto
import com.example.gamestore.model.OrderLineDto
import com.example.gamestore.model.OrderReceipt
import com.example.gamestore.model.PaymentMethod
import com.example.gamestore.preferences.PreferencesManager
import com.example.gamestore.ui.state.CheckoutField
import com.example.gamestore.ui.state.CheckoutUiState
import com.example.gamestore.ui.state.StoreUiState
import com.example.gamestore.ui.state.buildStoreUiState
import com.example.gamestore.validation.CheckoutValidators
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException

class StoreViewModel @JvmOverloads constructor(
    application: Application,
    private val orderApi: OrderApi = OrderApi(),
) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        StoreDatabase::class.java,
        "store.db",
    ).build()

    private val favoriteDao = db.favoriteDao()
    private val orderLineDao = db.orderLineDao()
    private val preferencesManager = PreferencesManager(application)

    private val orderMutex = Mutex()
    private val favoriteMutex = Mutex()

    // El pedido se sigue observando aunque el catálogo no esté visible.
    private val orderLines = orderLineDao.observeOrderLines().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    private val _checkoutForm = MutableStateFlow(CheckoutUiState())

    val checkoutState: StateFlow<CheckoutUiState> = combine(
        _checkoutForm,
        orderLines,
    ) { form, lines ->
        form.copy(
            lines = lines.map { entity ->
                OrderLineDto(
                    productId = entity.productId,
                    productName = entity.productName,
                    quantity = entity.quantity,
                    unitPrice = entity.unitPrice,
                    subtotal = entity.subtotal,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CheckoutUiState(),
    )

    // Catálogo generado únicamente en memoria.
    private val profiles = TestCatalog.profiles
    private val _allProducts = MutableStateFlow(TestCatalog.createProducts())
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<StoreUiState> = combine(
        _allProducts,
        _searchQuery,
        favoriteDao.observeFavoriteIds(),
        orderLines,
        preferencesManager.darkThemeFlow,
    ) { products, query, favoriteIds, lines, darkTheme ->
        buildStoreUiState(
            products = products,
            profiles = profiles,
            query = query,
            favoriteIds = favoriteIds,
            orderLines = lines,
            darkTheme = darkTheme,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StoreUiState(
            products = _allProducts.value,
            catalog = _allProducts.value,
            profiles = profiles,
        ),
    )

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkTheme(enabled)
        }
    }

    fun toggleFavorite(productId: String) {
        if (_allProducts.value.none { it.id == productId }) return

        viewModelScope.launch {
            favoriteMutex.withLock {
                val favorite = FavoriteEntity(productId)

                if (favoriteDao.isFavorite(productId)) {
                    favoriteDao.deleteFavorite(favorite)
                } else {
                    favoriteDao.insertFavorite(favorite)
                }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearQuery() {
        _searchQuery.value = ""
    }

    fun startCheckout(productId: String) {
        if (_checkoutForm.value.isSubmitting) return

        val product = _allProducts.value.find {
            it.id == productId && it.isAvailable
        } ?: return

        viewModelScope.launch {
            orderMutex.withLock {
                if (_checkoutForm.value.isSubmitting) return@withLock

                _checkoutForm.update {
                    it.copy(receipt = null, submitError = null)
                }

                val newQty =
                    (orderLineDao.getLine(productId)?.quantity ?: 0) + 1

                orderLineDao.insertLine(
                    OrderLineEntity(
                        productId = product.id,
                        productName = product.name,
                        quantity = newQty,
                        unitPrice = product.price,
                        subtotal = product.price * newQty,
                    ),
                )
            }
        }
    }

    fun changeQuantity(productId: String, quantity: Int) {
        if (_checkoutForm.value.isSubmitting) return

        viewModelScope.launch {
            orderMutex.withLock {
                if (_checkoutForm.value.isSubmitting) return@withLock

                if (quantity <= 0) {
                    orderLineDao.getLine(productId)?.let {
                        orderLineDao.deleteLine(it)
                    }
                } else {
                    val product = _allProducts.value.find {
                        it.id == productId
                    } ?: return@withLock

                    orderLineDao.insertLine(
                        OrderLineEntity(
                            productId = product.id,
                            productName = product.name,
                            quantity = quantity,
                            unitPrice = product.price,
                            subtotal = product.price * quantity,
                        ),
                    )
                }
            }
        }
    }

    fun onCheckoutFieldChange(
        field: CheckoutField,
        value: String,
    ) {
        _checkoutForm.update { currentState ->
            val updated = when (field) {
                CheckoutField.NAME ->
                    currentState.copy(name = value)

                CheckoutField.PHONE ->
                    currentState.copy(phone = value)

                CheckoutField.NIT ->
                    currentState.copy(nit = value)

                CheckoutField.BUSINESS_NAME ->
                    currentState.copy(businessName = value)
            }

            revalidateAll(updated)
        }
    }

    fun onCheckoutFieldTouched(field: CheckoutField) {
        _checkoutForm.update { currentState ->
            val updated = when (field) {
                CheckoutField.NAME ->
                    currentState.copy(isNameTouched = true)

                CheckoutField.PHONE ->
                    currentState.copy(isPhoneTouched = true)

                CheckoutField.NIT ->
                    currentState.copy(isNitTouched = true)

                CheckoutField.BUSINESS_NAME ->
                    currentState.copy(isBusinessNameTouched = true)
            }

            revalidateAll(updated)
        }
    }

    fun onBillingTypeChange(type: BillingType) {
        _checkoutForm.update { currentState ->
            val updated = if (type == BillingType.CF) {
                currentState.copy(
                    billingType = type,
                    nit = "",
                    businessName = "",
                    nitError = null,
                    businessNameError = null,
                    isNitTouched = false,
                    isBusinessNameTouched = false,
                )
            } else {
                currentState.copy(billingType = type)
            }

            revalidateAll(updated)
        }
    }

    fun onPaymentMethodChange(method: PaymentMethod) {
        _checkoutForm.update {
            it.copy(paymentMethod = method)
        }
    }

    private fun revalidateAll(
        state: CheckoutUiState,
    ): CheckoutUiState {
        val nameError = if (state.isNameTouched) {
            CheckoutValidators.name(state.name)
        } else {
            null
        }

        val phoneError = if (state.isPhoneTouched) {
            CheckoutValidators.phone(state.phone)
        } else {
            null
        }

        val nitError =
            if (
                state.billingType == BillingType.NIT &&
                state.isNitTouched
            ) {
                CheckoutValidators.nit(state.nit)
            } else {
                null
            }

        val businessNameError =
            if (
                state.billingType == BillingType.NIT &&
                state.isBusinessNameTouched
            ) {
                CheckoutValidators.businessName(state.businessName)
            } else {
                null
            }

        return state.copy(
            nameError = nameError,
            phoneError = phoneError,
            nitError = nitError,
            businessNameError = businessNameError,
        )
    }

    fun submitOrder() {
        val currentState = checkoutState.value

        if (
            _checkoutForm.value.isSubmitting ||
            _checkoutForm.value.receipt != null
        ) {
            return
        }

        val nameErr = CheckoutValidators.name(currentState.name)
        val phoneErr = CheckoutValidators.phone(currentState.phone)

        val nitErr =
            if (currentState.billingType == BillingType.NIT) {
                CheckoutValidators.nit(currentState.nit)
            } else {
                null
            }

        val businessErr =
            if (currentState.billingType == BillingType.NIT) {
                CheckoutValidators.businessName(
                    currentState.businessName,
                )
            } else {
                null
            }

        if (
            nameErr != null ||
            phoneErr != null ||
            nitErr != null ||
            businessErr != null ||
            currentState.lines.isEmpty()
        ) {
            _checkoutForm.update {
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

        val order = CreateOrderDto(
            lines = currentState.lines.toList(),
            total = currentState.total,
            customer = OrderCustomerDto(
                name = currentState.name.trim(),
                phone = currentState.phone.trim(),
                billingType = currentState.billingType,
                nit = currentState.nit.trim().takeIf {
                    currentState.billingType == BillingType.NIT
                },
                businessName =
                    currentState.businessName.trim().takeIf {
                        currentState.billingType == BillingType.NIT
                    },
                paymentMethod = currentState.paymentMethod,
            ),
        )

        _checkoutForm.update {
            it.copy(
                isSubmitting = true,
                submitError = null,
            )
        }

        viewModelScope.launch {
            try {
                val created = orderApi.create(order)

                orderMutex.withLock {
                    orderLineDao.clearAll()
                }

                _checkoutForm.update { state ->
                    state.copy(
                        isSubmitting = false,
                        receipt = OrderReceipt(
                            id = created.id,
                            lines = order.lines,
                            total = order.total,
                            customerName = order.customer.name,
                            phone = order.customer.phone,
                            billingType = order.customer.billingType,
                            nit = order.customer.nit,
                            businessName = order.customer.businessName,
                            paymentMethod = order.customer.paymentMethod,
                        ),
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: OrderHttpException) {
                _checkoutForm.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "No se pudo confirmar el pedido. Inténtalo de nuevo.",
                    )
                }
            } catch (_: SerializationException) {
                _checkoutForm.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "No pudimos leer la confirmación. Inténtalo más tarde.",
                    )
                }
            } catch (error: IOException) {
                _checkoutForm.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "Sin conexión. Revisa internet e inténtalo de nuevo.",
                    )
                }
            } catch (error: Exception) {
                _checkoutForm.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "Ocurrió un problema. Inténtalo de nuevo.",
                    )
                }
            }
        }
    }
}