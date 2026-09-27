package com.example.gamestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestore.model.BillingType
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct
<<<<<<< Updated upstream
import com.example.gamestore.model.OrderReceipt
import com.example.gamestore.model.PaymentMethod
import com.example.gamestore.ui.state.CheckoutField
import com.example.gamestore.ui.state.CheckoutUiState
import com.example.gamestore.ui.state.StoreUiState
import java.util.UUID
=======
import com.example.gamestore.model.BillingType
import com.example.gamestore.model.PaymentMethod
import com.example.gamestore.ui.state.StoreUiState
import com.example.gamestore.ui.state.CheckoutUiState
>>>>>>> Stashed changes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class StoreViewModel : ViewModel() {
    private val _checkoutState = MutableStateFlow(CheckoutUiState())

    val checkoutState: StateFlow<CheckoutUiState> =
        _checkoutState.asStateFlow()

    fun onCheckoutFieldChange(
        field: CheckoutField,
        value: String
    ) {
        _checkoutState.update { state ->
            val updatedState = when (field) {
                CheckoutField.NAME ->
                    state.copy(name = value)

                CheckoutField.PHONE ->
                    state.copy(phone = value)

                CheckoutField.NIT ->
                    state.copy(nit = value)

                CheckoutField.BUSINESS_NAME ->
                    state.copy(businessName = value)
            }

            updatedState.touch(field)
        }
    }

    fun onCheckoutFieldTouched(field: CheckoutField) {
        _checkoutState.update { state ->
            state.touch(field)
        }
    }

    fun onBillingTypeChange(type: BillingType) {
        _checkoutState.update { state ->
            state.withBillingType(type)
        }
    }

    fun onPaymentMethodChange(method: PaymentMethod) {
        _checkoutState.update { state ->
            state.copy(paymentMethod = method)
        }
    }

    fun startCheckout(productId: String) {
        _checkoutState.update { state ->
            state.copy(
                productId = productId,
                receipt = null
            )
        }
    }

    fun submitOrder() {
        val state = _checkoutState.value

        if (state.receipt != null) return

        if (!state.isFormValid) {
            _checkoutState.update { current ->
                CheckoutField.entries.fold(current) { next, field ->
                    next.touch(field)
                }
            }
            return
        }

        val product = _allProducts.value.find {
            it.id == state.productId
        } ?: return

        if (!product.isAvailable) return

        val receipt = OrderReceipt(
            id = UUID.randomUUID().toString(),
            productId = product.id,
            productName = product.name,
            total = product.price,
            customerName = state.name.trim(),
            phone = state.phone,
            billingType = state.billingType,
            nit = state.nit.takeIf {
                state.billingType == BillingType.NIT
            },
            businessName = state.businessName.trim().takeIf {
                state.billingType == BillingType.NIT
            },
            paymentMethod = state.paymentMethod
        )

        _checkoutState.value = state.copy(receipt = receipt)
    }

    private val profiles = listOf(
        DeveloperProfile(
            id = "dev01",
            name = "Nebula Forge",
            role = "Estudio independiente",
            location = "Guatemala",
            description = "Especialistas en aventuras y mundos de fantasía."
        ),
        DeveloperProfile(
            id = "dev02",
            name = "Pixel Jaguar",
            role = "Desarrollador",
            location = "México",
            description = "Crea juegos de acción inspirados en Latinoamérica."
        ),
        DeveloperProfile(
            id = "dev03",
            name = "Aurora Byte",
            role = "Desarrollador",
            location = "Canadá",
            description = "Produce experiencias de estrategia y ciencia ficción."
        ),
        DeveloperProfile(
            id = "dev04",
            name = "Sakura Circuit",
            role = "Desarrollador",
            location = "Japón",
            description = "Estudio enfocado en carreras y juegos competitivos."
        ),
        DeveloperProfile(
            id = "dev05",
            name = "Andes Interactive",
            role = "Desarrollador",
            location = "Chile",
            description = "Desarrolla experiencias cooperativas y de exploración."
        ),
        DeveloperProfile(
            id = "dev06",
            name = "Emerald Owl Games",
            role = "Desarrollador",
            location = "Irlanda",
            description = "Diseña rompecabezas y aventuras narrativas."
        ),
        DeveloperProfile(
            id = "dev07",
            name = "Solaris Works",
            role = "Productor",
            location = "España",
            description = "Publica juegos de deportes y simulación."
        ),
        DeveloperProfile(
            id = "dev08",
            name = "Crimson Kraken",
            role = "Estudio independiente",
            location = "Australia",
            description = "Crea juegos de supervivencia y acción."
        ),
        DeveloperProfile(
            id = "dev09",
            name = "Nordic Lantern",
            role = "Desarrollador",
            location = "Suecia",
            description = "Especialistas en estrategia y construcción."
        ),
        DeveloperProfile(
            id = "dev10",
            name = "Quetzal Labs",
            role = "Desarrollador",
            location = "Guatemala",
            description = "Estudio de juegos educativos y familiares."
        )
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
        val themes = listOf(
            "Crónicas",
            "Horizonte",
            "Leyendas",
            "Reinos",
            "Circuito",
            "Guardianes",
            "Ecos",
            "Expedición",
            "Arena",
            "Misterios"
        )

        val worlds = listOf(
            "de Aether",
            "del Jaguar",
            "Neón",
            "del Norte",
            "Solar",
            "Abisal",
            "de Jade",
            "Andina",
            "Estelar",
            "Esmeralda"
        )

        val genres = listOf(
            "aventura",
            "acción",
            "estrategia",
            "carreras",
            "rompecabezas",
            "simulación",
            "rol",
            "deportes",
            "supervivencia",
            "plataformas"
        )

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
<<<<<<< Updated upstream
}
=======

    private val _checkoutUiState = MutableStateFlow(CheckoutUiState())
    val checkoutUiState: StateFlow<CheckoutUiState> = _checkoutUiState

    private fun validateName(name: String): String? {
        val letters = name.filter { it.isLetter() }
        return if (letters.length < 3 || name.any { it.isDigit() }) {
            "Debe tener al menos 3 letras y sin números"
        } else null
    }

    private fun validatePhone(phone: String): String? {
        return if (phone.length != 8 || phone.any { !it.isDigit() }) {
            "Debe tener exactamente 8 dígitos"
        } else null
    }

    private fun validateNit(nit: String): String? {
        return if (nit.length < 5 || nit.any { !it.isDigit() }) {
            "Ingrese al menos 5 dígitos"
        } else null
    }

    private fun validateRazonSocial(razon: String): String? {
        return if (razon.trim().length < 3) {
            "Debe tener al menos 3 caracteres"
        } else null
    }

    fun onNameChange(newName: String) {
        _checkoutUiState.update { it.copy(fullName = newName, isTouched = it.isTouched + ("name" to true)) }
        validateForm()
    }

    fun onPhoneChange(newPhone: String) {
        _checkoutUiState.update { it.copy(phone = newPhone, isTouched = it.isTouched + ("phone" to true)) }
        validateForm()
    }

    fun onBillingTypeChange(type: BillingType) {
        _checkoutUiState.update {
            if (type == BillingType.CF) {
                it.copy(
                    billingType = type,
                    nit = "",
                    razonSocial = "",
                    errors = it.errors + ("nit" to null) + ("razon" to null),
                    isTouched = it.isTouched + ("nit" to false) + ("razon" to false)
                )
            } else {
                it.copy(billingType = type)
            }
        }
        validateForm()
    }

    fun onNitChange(newNit: String) {
        _checkoutUiState.update { it.copy(nit = newNit, isTouched = it.isTouched + ("nit" to true)) }
        validateForm()
    }

    fun onRazonSocialChange(newRazon: String) {
        _checkoutUiState.update { it.copy(razonSocial = newRazon, isTouched = it.isTouched + ("razon" to true)) }
        validateForm()
    }

    fun onPaymentMethodChange(method: PaymentMethod) {
        _checkoutUiState.update { it.copy(paymentMethod = method) }
        validateForm()
    }

    private fun validateForm() {
        val state = _checkoutUiState.value
        val errors = mutableMapOf<String, String?>()

        errors["name"] = validateName(state.fullName)
        errors["phone"] = validatePhone(state.phone)

        if (state.billingType == BillingType.NIT) {
            errors["nit"] = validateNit(state.nit)
            errors["razon"] = validateRazonSocial(state.razonSocial)
        } else {
            errors["nit"] = null
            errors["razon"] = null
        }

        val isValid = errors.values.all { it == null }

        _checkoutUiState.update {
            it.copy(errors = errors, isFormValid = isValid)
        }
    }
}
>>>>>>> Stashed changes
