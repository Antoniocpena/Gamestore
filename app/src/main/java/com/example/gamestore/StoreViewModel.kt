package com.example.gamestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestore.model.BillingType
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct
import com.example.gamestore.model.PaymentMethod
import com.example.gamestore.ui.state.StoreUiState
import com.example.gamestore.ui.state.CheckoutUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class StoreViewModel : ViewModel() {

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
