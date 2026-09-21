package com.example.gamestore.data

import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct
import java.util.Locale

object TestCatalog {
    val profiles: List<DeveloperProfile> = listOf(
        DeveloperProfile(id = "dev01", name = "Nebula Forge", role = "Estudio independiente", location = "Guatemala", description = "Especialistas en aventuras y mundos de fantasía."),
        DeveloperProfile(id = "dev02", name = "Pixel Jaguar", role = "Desarrollador", location = "México", description = "Crea juegos de acción inspirados en Latinoamérica."),
        DeveloperProfile(id = "dev03", name = "Aurora Byte", role = "Desarrollador", location = "Canadá", description = "Produce experiencias de estrategia y ciencia ficción."),
        DeveloperProfile(id = "dev04", name = "Sakura Circuit", role = "Desarrollador", location = "Japón", description = "Estudio enfocado en carreras y juegos competitivos."),
        DeveloperProfile(id = "dev05", name = "Andes Interactive", role = "Desarrollador", location = "Chile", description = "Desarrolla experiencias cooperativas y de exploración."),
        DeveloperProfile(id = "dev06", name = "Emerald Owl Games", role = "Desarrollador", location = "Irlanda", description = "Diseña rompecabezas y aventuras narrativas."),
        DeveloperProfile(id = "dev07", name = "Solaris Works", role = "Productor", location = "España", description = "Publica juegos de deportes y simulación."),
        DeveloperProfile(id = "dev08", name = "Crimson Kraken", role = "Estudio independiente", location = "Australia", description = "Crea juegos de supervivencia y acción."),
        DeveloperProfile(id = "dev09", name = "Nordic Lantern", role = "Desarrollador", location = "Suecia", description = "Especialistas en estrategia y construcción."),
        DeveloperProfile(id = "dev10", name = "Quetzal Labs", role = "Desarrollador", location = "Guatemala", description = "Estudio de juegos educativos y familiares."),
    )

    fun createProducts(): List<GameProduct> {
        val themes = listOf("Crónicas", "Horizonte", "Leyendas", "Reinos", "Circuito", "Guardianes", "Ecos", "Expedición", "Arena", "Misterios")
        val worlds = listOf("de Aether", "del Jaguar", "Neón", "del Norte", "Solar", "Abisal", "de Jade", "Andina", "Estelar", "Esmeralda")
        val genres = listOf("aventura", "acción", "estrategia", "carreras", "rompecabezas", "simulación", "rol", "deportes", "supervivencia", "plataformas")

        return List(CATALOG_SIZE) { index ->
            val number = index + 1
            val profile = profiles[index % profiles.size]
            val genre = genres[index % genres.size]

            GameProduct(
                id = String.format(Locale.US, "game-%03d", number),
                name = "${themes[index % themes.size]} ${worlds[(index / themes.size) % worlds.size]} #$number",
                description = "Videojuego de $genre desarrollado por ${profile.name}.",
                priceCents = 999L + ((index * 7) % 60) * 100L,
                developerId = profile.id,
                stock = if (number % 7 == 0) 0 else 1 + ((index * 3) % 25),
                coverColorIndex = index % profiles.size,
            )
        }
    }

    const val CATALOG_SIZE = 500
}
