package com.example.gamestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestore.model.BillingType
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct
import com.example.gamestore.model.OrderReceipt
import com.example.gamestore.model.PaymentMethod
import com.example.gamestore.ui.state.CheckoutField
import com.example.gamestore.ui.state.CheckoutUiState
import com.example.gamestore.ui.state.StoreUiState
import com.example.gamestore.validation.CheckoutValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class StoreViewModel : ViewModel() {

    private val _checkoutState = MutableStateFlow(CheckoutUiState())
    val checkoutState: StateFlow<CheckoutUiState> = _checkoutState

    fun startCheckout(productId: String) {
        val selectedProductId = _allProducts.value
            .find { product -> product.id == productId }
            ?.id

        _checkoutState.value = CheckoutUiState(
            productId = selectedProductId,
        )
    }

    fun onCheckoutFieldChange(field: CheckoutField, value: String) {
        _checkoutState.update { currentState ->
            val updatedState = when (field) {
                CheckoutField.NAME -> currentState.copy(name = value)
                CheckoutField.PHONE -> currentState.copy(phone = value)
                CheckoutField.NIT -> currentState.copy(nit = value)
                CheckoutField.BUSINESS_NAME -> currentState.copy(businessName = value)
            }

            validateCheckoutState(updatedState)
        }
    }

    fun onCheckoutFieldTouched(field: CheckoutField) {
        _checkoutState.update { currentState ->
            validateCheckoutState(
                currentState.copy(
                    touchedFields = currentState.touchedFields + field,
                ),
            )
        }
    }

    fun onBillingTypeChange(type: BillingType) {
        _checkoutState.update { currentState ->
            val updatedState = if (type == BillingType.CF) {
                currentState.copy(
                    billingType = type,
                    nit = "",
                    businessName = "",
                    touchedFields = currentState.touchedFields -
                        CheckoutField.NIT - CheckoutField.BUSINESS_NAME,
                )
            } else {
                currentState.copy(billingType = type)
            }

            validateCheckoutState(updatedState)
        }
    }

    fun onPaymentMethodChange(method: PaymentMethod) {
        _checkoutState.update { currentState ->
            validateCheckoutState(
                currentState.copy(paymentMethod = method),
            )
        }
    }

    fun submitOrder() {
        val currentState = _checkoutState.value
        val requiredFields = buildSet {
            add(CheckoutField.NAME)
            add(CheckoutField.PHONE)

            if (currentState.billingType == BillingType.NIT) {
                add(CheckoutField.NIT)
                add(CheckoutField.BUSINESS_NAME)
            }
        }

        val validatedState = validateCheckoutState(
            currentState.copy(
                touchedFields = currentState.touchedFields + requiredFields,
            ),
        )
        val selectedProduct = validatedState.productId?.let { productId ->
            _allProducts.value.find { product -> product.id == productId }
        }

        if (!validatedState.isFormValid || selectedProduct == null) {
            _checkoutState.value = validatedState
            return
        }

        val receipt = OrderReceipt(
            id = "order-${System.currentTimeMillis()}",
            productId = selectedProduct.id,
            productName = selectedProduct.name,
            total = selectedProduct.price,
            customerName = validatedState.name.trim(),
            phone = validatedState.phone,
            billingType = validatedState.billingType,
            nit = if (validatedState.billingType == BillingType.NIT) {
                validatedState.nit.trim()
            } else {
                null
            },
            businessName = if (validatedState.billingType == BillingType.NIT) {
                validatedState.businessName.trim()
            } else {
                null
            },
            paymentMethod = validatedState.paymentMethod,
        )

        _checkoutState.value = validatedState.copy(receipt = receipt)
    }

    private fun validateCheckoutState(state: CheckoutUiState): CheckoutUiState {
        val nameValidation = CheckoutValidators.name(state.name)
        val phoneValidation = CheckoutValidators.phone(state.phone)
        val nitValidation = if (state.billingType == BillingType.NIT) {
            CheckoutValidators.nit(state.nit)
        } else {
            null
        }
        val businessNameValidation = if (state.billingType == BillingType.NIT) {
            CheckoutValidators.businessName(state.businessName)
        } else {
            null
        }

        val isFormValid = nameValidation == null &&
            phoneValidation == null &&
            nitValidation == null &&
            businessNameValidation == null

        return state.copy(
            nameError = nameValidation.takeIf {
                CheckoutField.NAME in state.touchedFields
            },
            phoneError = phoneValidation.takeIf {
                CheckoutField.PHONE in state.touchedFields
            },
            nitError = nitValidation.takeIf {
                CheckoutField.NIT in state.touchedFields
            },
            businessNameError = businessNameValidation.takeIf {
                CheckoutField.BUSINESS_NAME in state.touchedFields
            },
            isFormValid = isFormValid,
        )
    }

    // ----------------------------
    // Catálogo y búsqueda
    // ----------------------------

    private val profiles = listOf(
        DeveloperProfile("dev01", "Nebula Forge", "Estudio independiente", "Guatemala", "Especialistas en aventuras y mundos de fantasía."),
        DeveloperProfile("dev02", "Pixel Jaguar", "Desarrollador", "México", "Crea juegos de acción inspirados en Latinoamérica."),
        DeveloperProfile("dev03", "Aurora Byte", "Desarrollador", "Canadá", "Produce experiencias de estrategia y ciencia ficción."),
        DeveloperProfile("dev04", "Sakura Circuit", "Desarrollador", "Japón", "Estudio enfocado en carreras y juegos competitivos."),
        DeveloperProfile("dev05", "Andes Interactive", "Desarrollador", "Chile", "Desarrolla experiencias cooperativas y de exploración."),
        DeveloperProfile("dev06", "Emerald Owl Games", "Desarrollador", "Irlanda", "Diseña rompecabezas y aventuras narrativas."),
        DeveloperProfile("dev07", "Solaris Works", "Productor", "España", "Publica juegos de deportes y simulación."),
        DeveloperProfile("dev08", "Crimson Kraken", "Estudio independiente", "Australia", "Crea juegos de supervivencia y acción."),
        DeveloperProfile("dev09", "Nordic Lantern", "Desarrollador", "Suecia", "Especialistas en estrategia y construcción."),
        DeveloperProfile("dev10", "Quetzal Labs", "Desarrollador", "Guatemala", "Estudio de juegos educativos y familiares.")
    )

    private val _allProducts = MutableStateFlow(createTestCatalog(profiles))
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<StoreUiState> = combine(
        _allProducts,
        _searchQuery
    ) { products, query ->
        val filtered = if (query.isBlank()) {
            products
        } else {
            products.filter {
                it.name.contains(query, ignoreCase = true)
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
        _allProducts.update { currentProducts ->
            currentProducts.map { product ->
                if (product.id == productId) {
                    product.copy(isFavorite = !product.isFavorite)
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
        val themes = listOf("Crónicas","Horizonte","Leyendas","Reinos","Circuito","Guardianes","Ecos","Expedición","Arena","Misterios")
        val worlds = listOf("de Aether","del Jaguar","Neón","del Norte","Solar","Abisal","de Jade","Andina","Estelar","Esmeralda")
        val genres = listOf("aventura","acción","estrategia","carreras","rompecabezas","simulación","rol","deportes","supervivencia","plataformas")

        return List(500) { index ->
            val number = index + 1
            val profile = availableProfiles[index % availableProfiles.size]
            val genre = genres[index % genres.size]

            GameProduct(
                id = "game-%03d".format(number),
                name = "${themes[index % themes.size]} ${worlds[(index / themes.size) % worlds.size]} #$number",
                description = "Videojuego de $genre desarrollado por ${profile.name}.",
                price = 9.99 + ((index * 7) % 60),
                developerId = profile.id,
                imageUrl = "https://picsum.photos/seed/gamestore-$number/600/400",
                isAvailable = number % 7 != 0
            )
        }
    }
}
