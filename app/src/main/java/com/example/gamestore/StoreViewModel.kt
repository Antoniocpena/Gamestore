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
import com.example.gamestore.model.BillingType
import com.example.gamestore.model.CreateOrderDto
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct
import com.example.gamestore.model.OrderCustomerDto
import com.example.gamestore.model.OrderLineDto
import com.example.gamestore.model.OrderReceipt
import com.example.gamestore.model.PaymentMethod
import com.example.gamestore.preferences.PreferencesManager
import com.example.gamestore.ui.state.CheckoutField
import com.example.gamestore.ui.state.CheckoutUiState
import com.example.gamestore.ui.state.StoreUiState
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
    val preferenceFlow = preferencesManager.preferenceFlow

    private val _checkoutState = MutableStateFlow(CheckoutUiState())
    val checkoutState: StateFlow<CheckoutUiState> = _checkoutState

    init {
        viewModelScope.launch {
            orderLineDao.observeOrderLines().collect { orderLines ->
                _checkoutState.update { state ->
                    state.copy(
                        lines = orderLines.map { entity ->
                            OrderLineDto(
                                productId = entity.productId,
                                productName = entity.productName,
                                quantity = entity.quantity,
                                unitPrice = entity.unitPrice,
                                subtotal = entity.subtotal,
                            )
                        },
                    )
                }
            }
        }
    }

    fun setPreference(value: String) {
        viewModelScope.launch {
            preferencesManager.setPreference(value)
        }
    }

    fun startCheckout(productId: String) {
        if (_checkoutState.value.isSubmitting) return

        val product = _allProducts.value.find {
            (it.id == productId) && it.isAvailable
        } ?: return

        viewModelScope.launch {
            val existing = _checkoutState.value.lines.find { it.productId == productId }
            val newQty = (existing?.quantity ?: 0) + 1
            val subtotal = product.price * newQty
            orderLineDao.insertLine(
                OrderLineEntity(
                    productId = product.id,
                    productName = product.name,
                    quantity = newQty,
                    unitPrice = product.price,
                    subtotal = subtotal,
                ),
            )
        }
    }

    fun changeQuantity(productId: String, quantity: Int) {
        if (_checkoutState.value.isSubmitting) return

        viewModelScope.launch {
            if (quantity <= 0) {
                orderLineDao.deleteLine(
                    OrderLineEntity(
                        productId = productId,
                        productName = "",
                        quantity = 0,
                        unitPrice = 0.0,
                        subtotal = 0.0,
                    ),
                )
            } else {
                val product = _allProducts.value.find { it.id == productId } ?: return@launch
                val subtotal = product.price * quantity
                orderLineDao.insertLine(
                    OrderLineEntity(
                        productId = productId,
                        productName = product.name,
                        quantity = quantity,
                        unitPrice = product.price,
                        subtotal = subtotal,
                    ),
                )
            }
        }
    }

    fun confirmOrder() {
        viewModelScope.launch {
            orderLineDao.clearAll()
        }
    }

    fun onCheckoutFieldChange(
        field: CheckoutField,
        value: String,
    ) {
        _checkoutState.update { currentState ->
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
        _checkoutState.update { currentState ->
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
        _checkoutState.update { currentState ->
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
        _checkoutState.update {
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
            if ((state.billingType == BillingType.NIT) &&
                state.isNitTouched
            ) {
                CheckoutValidators.nit(state.nit)
            } else {
                null
            }

        val businessNameError =
            if ((state.billingType == BillingType.NIT) &&
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
        val currentState = _checkoutState.value

        if (currentState.isSubmitting ||
            currentState.receipt != null
        ) {
            return
        }

        val nameErr =
            CheckoutValidators.name(currentState.name)
        val phoneErr =
            CheckoutValidators.phone(currentState.phone)

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

        if (nameErr != null ||
            phoneErr != null ||
            nitErr != null ||
            businessErr != null ||
            currentState.lines.isEmpty()
        ) {
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

        _checkoutState.update {
            it.copy(
                isSubmitting = true,
                submitError = null,
            )
        }

        viewModelScope.launch {
            try {
                val created = orderApi.create(order)

                orderLineDao.clearAll()

                _checkoutState.update { state ->
                    state.copy(
                        lines = emptyList(),
                        isSubmitting = false,
                        receipt = OrderReceipt(
                            id = created.id,
                            lines = order.lines,
                            total = order.total,
                            customerName = order.customer.name,
                            phone = order.customer.phone,
                            billingType =
                                order.customer.billingType,
                            nit = order.customer.nit,
                            businessName =
                                order.customer.businessName,
                            paymentMethod =
                                order.customer.paymentMethod,
                        ),
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: OrderHttpException) {
                _checkoutState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "No se pudo confirmar el pedido. Inténtalo de nuevo.",
                    )
                }
            } catch (_: SerializationException) {
                _checkoutState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "No pudimos leer la confirmación. Inténtalo más tarde.",
                    )
                }
            } catch (error: IOException) {
                _checkoutState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "Sin conexión. Revisa internet e inténtalo de nuevo.",
                    )
                }
            } catch (error: Exception) {
                _checkoutState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "Ocurrió un problema. Inténtalo de nuevo.",
                    )
                }
            }
        }
    }

    // Catálogo y búsqueda existentes.

    private val profiles = listOf(
        DeveloperProfile(
            "dev01", "Nebula Forge", "Estudio independiente",
            "Guatemala",
            "Especialistas en aventuras y mundos de fantasía.",
        ),
        DeveloperProfile(
            "dev02", "Pixel Jaguar", "Desarrollador",
            "México",
            "Crea juegos de acción inspirados en Latinoamérica.",
        ),
        DeveloperProfile(
            "dev03", "Aurora Byte", "Desarrollador",
            "Canadá",
            "Produce experiencias de estrategia y ciencia ficción.",
        ),
        DeveloperProfile(
            "dev04", "Sakura Circuit", "Desarrollador",
            "Japón",
            "Enfocado en carreras y juegos competitivos.",
        ),
        DeveloperProfile(
            "dev05", "Andes Interactive", "Desarrollador",
            "Chile",
            "Desarrolla experiencias cooperativas y de exploración.",
        ),
        DeveloperProfile(
            "dev06", "Emerald Owl Games", "Desarrollador",
            "Irlanda",
            "Diseña rompecabezas y aventuras narrativas.",
        ),
        DeveloperProfile(
            "dev07", "Solaris Works", "Productor",
            "España",
            "Publica juegos de deportes y simulación.",
        ),
        DeveloperProfile(
            "dev08", "Crimson Kraken", "Estudio independiente",
            "Australia",
            "Crea juegos de supervivencia y acción.",
        ),
        DeveloperProfile(
            "dev09", "Nordic Lantern", "Desarrollador",
            "Suecia",
            "Especialistas en estrategia y construcción.",
        ),
        DeveloperProfile(
            "dev10", "Quetzal Labs", "Desarrollador",
            "Guatemala",
            "Estudio de juegos educativos y familiares.",
        ),
    )

    private val _allProducts =
        MutableStateFlow(createTestCatalog(profiles))

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<StoreUiState> = combine(
        _allProducts,
        _searchQuery,
        favoriteDao.observeFavoriteIds(),
        orderLineDao.observeOrderLines(),
        preferencesManager.preferenceFlow,
    ) { products, query, favoriteIds, orderLines, pref ->
        val filtered = if (query.isBlank()) {
            products
        } else {
            products.filter {
                it.name.contains(
                    query,
                    ignoreCase = true,
                )
            }
        }

        StoreUiState(
            products = filtered.map { product ->
                product.copy(isFavorite = favoriteIds.contains(product.id))
            },
            profiles = profiles,
            searchQuery = query,
            orderLines = orderLines,
            userPreference = pref,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StoreUiState(
            products = _allProducts.value,
            profiles = profiles,
        ),
    )

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            val isFav = uiState.value.products.find { it.id == productId }?.isFavorite ?: false
            if (isFav) {
                favoriteDao.deleteFavorite(FavoriteEntity(productId))
            } else {
                favoriteDao.insertFavorite(FavoriteEntity(productId))
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearQuery() {
        _searchQuery.value = ""
    }

    private fun createTestCatalog(
        availableProfiles: List<DeveloperProfile>,
    ): List<GameProduct> {
        val themes = listOf(
            "Crónicas", "Horizonte", "Leyendas",
            "Reinos", "Circuito", "Guardianes",
            "Ecos", "Expedición", "Arena", "Misterios",
        )
        val worlds = listOf(
            "de Aether", "del Jaguar", "Neón",
            "del Norte", "Solar", "Abisal",
            "de Jade", "Andina", "Estelar", "Esmeralda",
        )
        val genres = listOf(
            "aventura", "acción", "estrategia",
            "carreras", "rompecabezas", "simulación",
            "rol", "deportes", "supervivencia",
            "plataformas",
        )

        return List(500) { index ->
            val number = index + 1
            val profile =
                availableProfiles[index % availableProfiles.size]
            val genre = genres[index % genres.size]

            GameProduct(
                id = "game-%03d".format(number),
                name =
                    "${themes[index % themes.size]} " +
                            "${worlds[(index / themes.size) % worlds.size]} " +
                            "#$number",
                description =
                    "Videojuego de $genre desarrollado por " +
                            "${profile.name}.",
                price = 9.99 + ((index * 7) % 60),
                developerId = profile.id,
                imageUrl = "https://picsum.photos/seed/gamestore-$number/600/400",
                isAvailable = number % 7 != 0,
            )
        }
    }
}
