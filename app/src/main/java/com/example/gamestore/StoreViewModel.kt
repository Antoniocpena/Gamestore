package com.example.gamestore

import androidx.lifecycle.ViewModel
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct
import com.example.gamestore.ui.state.StoreUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StoreViewModel : ViewModel() {

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

    private val products = createTestCatalog(profiles)

    private val _uiState = MutableStateFlow(
        StoreUiState(
            products = products,
            profiles = profiles
        )
    )

    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun toggleFavorite(productId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                products = currentState.products.map { product ->
                    if (product.id == productId) {
                        product.copy(
                            isFavorite = !product.isFavorite
                        )
                    } else {
                        product
                    }
                }
            )
        }
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

            val profile = availableProfiles[
                index % availableProfiles.size
            ]

            val genre = genres[
                index % genres.size
            ]

            GameProduct(
                id = "game-%03d".format(number),
                name = "${themes[index % themes.size]} " +
                        "${worlds[(index / themes.size) % worlds.size]} #$number",
                description = "Videojuego de $genre desarrollado por ${profile.name}.",
                price = 9.99 + ((index * 7) % 60),
                developerId = profile.id,
                imageUrl = "https://picsum.photos/seed/gamestore-$number/600/400",
                isAvailable = number % 7 != 0
            )
        }
    }
}