package com.example.gamestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestore.data.OrderApi
import com.example.gamestore.data.OrderHttpException
import com.example.gamestore.model.BillingType
import com.example.gamestore.model.CreateOrderDto
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct
import com.example.gamestore.model.OrderCustomerDto
import com.example.gamestore.model.OrderLineDto
import com.example.gamestore.model.OrderReceipt
import com.example.gamestore.model.PaymentMethod
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

class StoreViewModel(
    private val orderApi: OrderApi = OrderApi()
) : ViewModel() {

    private val _checkoutState = MutableStateFlow(CheckoutUiState())
    val checkoutState: StateFlow<CheckoutUiState> = _checkoutState

    fun startCheckout(productId: String) {
        if (_checkoutState.value.isSubmitting) return

        val product = _allProducts.value.find {
            it.id == productId && it.isAvailable
        } ?: return

        _checkoutState.update { state ->
            val existing = state.lines.find {
                it.productId == productId
            }

            val updatedLines = if (existing == null) {
                state.lines + OrderLineDto(
                    productId = product.id,
                    productName = product.name,
                    quantity = 1,
                    unitPrice = product.price,
                    subtotal = product.price
                )
            } else {
                state.lines.map { line ->
                    if (line.productId == productId) {
                        line.copy(
                            quantity = line.quantity + 1,
                            subtotal = line.unitPrice *
                                    (line.quantity + 1)
                        )
                    } else {
                        line
                    }
                }
            }

            state.copy(
                lines = updatedLines,
                receipt = null,
                submitError = null
            )
        }
    }

    fun changeQuantity(productId: String, quantity: Int) {
        if (_checkoutState.value.isSubmitting) return

        _checkoutState.update { state ->
            state.copy(
                lines = state.lines.mapNotNull { line ->
                    when {
                        line.productId != productId -> line
                        quantity <= 0 -> null
                        else -> line.copy(
                            quantity = quantity,
                            subtotal = line.unitPrice * quantity
                        )
                    }
                },
                submitError = null
            )
        }
    }

    fun onCheckoutFieldChange(
        field: CheckoutField,
        value: String
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
                    isBusinessNameTouched = false
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
        state: CheckoutUiState
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
            if (state.billingType == BillingType.NIT &&
                state.isNitTouched
            ) {
                CheckoutValidators.nit(state.nit)
            } else {
                null
            }

        val businessNameError =
            if (state.billingType == BillingType.NIT &&
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
            businessNameError = businessNameError
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
                    currentState.businessName
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
                    businessNameError = businessErr
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
                paymentMethod = currentState.paymentMethod
            )
        )

        // Se marca antes de lanzar la corrutina:
        // un segundo toque no puede iniciar otro POST.
        _checkoutState.update {
            it.copy(
                isSubmitting = true,
                submitError = null
            )
        }

        viewModelScope.launch {
            try {
                val created = orderApi.create(order)

                // Solo después de recibir y leer el ID
                // se vacían las líneas del pedido.
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
                                order.customer.paymentMethod
                        )
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: OrderHttpException) {
                _checkoutState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "No se pudo confirmar el pedido. Inténtalo de nuevo."
                    )
                }
            } catch (error: SerializationException) {
                _checkoutState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "No pudimos leer la confirmación. Inténtalo más tarde."
                    )
                }
            } catch (error: IOException) {
                _checkoutState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "Sin conexión. Revisa internet e inténtalo de nuevo."
                    )
                }
            } catch (error: Exception) {
                _checkoutState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            "Ocurrió un problema. Inténtalo de nuevo."
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
            "Especialistas en aventuras y mundos de fantasía."
        ),
        DeveloperProfile(
            "dev02", "Pixel Jaguar", "Desarrollador",
            "México",
            "Crea juegos de acción inspirados en Latinoamérica."
        ),
        DeveloperProfile(
            "dev03", "Aurora Byte", "Desarrollador",
            "Canadá",
            "Produce experiencias de estrategia y ciencia ficción."
        ),
        DeveloperProfile(
            "dev04", "Sakura Circuit", "Desarrollador",
            "Japón",
            "Enfocado en carreras y juegos competitivos."
        ),
        DeveloperProfile(
            "dev05", "Andes Interactive", "Desarrollador",
            "Chile",
            "Desarrolla experiencias cooperativas y de exploración."
        ),
        DeveloperProfile(
            "dev06", "Emerald Owl Games", "Desarrollador",
            "Irlanda",
            "Diseña rompecabezas y aventuras narrativas."
        ),
        DeveloperProfile(
            "dev07", "Solaris Works", "Productor",
            "España",
            "Publica juegos de deportes y simulación."
        ),
        DeveloperProfile(
            "dev08", "Crimson Kraken", "Estudio independiente",
            "Australia",
            "Crea juegos de supervivencia y acción."
        ),
        DeveloperProfile(
            "dev09", "Nordic Lantern", "Desarrollador",
            "Suecia",
            "Especialistas en estrategia y construcción."
        ),
        DeveloperProfile(
            "dev10", "Quetzal Labs", "Desarrollador",
            "Guatemala",
            "Estudio de juegos educativos y familiares."
        )
    )

    private val _allProducts =
        MutableStateFlow(createTestCatalog(profiles))

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<StoreUiState> = combine(
        _allProducts,
        _searchQuery
    ) { products, query ->
        val filtered = if (query.isBlank()) {
            products
        } else {
            products.filter {
                it.name.contains(
                    query,
                    ignoreCase = true
                )
            }
        }

        StoreUiState(
            products = filtered,
            profiles = profiles,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StoreUiState(
            products = _allProducts.value,
            profiles = profiles
        )
    )

    fun toggleFavorite(productId: String) {
        _allProducts.update { products ->
            products.map { product ->
                if (product.id == productId) {
                    product.copy(
                        isFavorite = !product.isFavorite
                    )
                } else {
                    product
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

    private fun createTestCatalog(
        availableProfiles: List<DeveloperProfile>
    ): List<GameProduct> {
        val themes = listOf(
            "Crónicas", "Horizonte", "Leyendas",
            "Reinos", "Circuito", "Guardianes",
            "Ecos", "Expedición", "Arena", "Misterios"
        )
        val worlds = listOf(
            "de Aether", "del Jaguar", "Neón",
            "del Norte", "Solar", "Abisal",
            "de Jade", "Andina", "Estelar", "Esmeralda"
        )
        val genres = listOf(
            "aventura", "acción", "estrategia",
            "carreras", "rompecabezas", "simulación",
            "rol", "deportes", "supervivencia",
            "plataformas"
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
                imageUrl =
                    "https://picsum.photos/seed/" +
                            "gamestore-$number/600/400",
                isAvailable = number % 7 != 0
            )
        }
    }
}