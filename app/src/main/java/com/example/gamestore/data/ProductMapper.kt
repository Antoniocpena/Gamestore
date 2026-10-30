package com.example.gamestore.data

import com.example.gamestore.model.GameProduct

object ProductMapper {

    fun fromDocument(
        id: String,
        data: Map<String, Any?>,
    ): GameProduct {

        fun text(key: String): String {
            return (data[key] as? String)
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException(
                    "Producto $id: falta $key o no es texto válido."
                )
        }

        fun integer(key: String): Int {
            val value = (data[key] as? Number)?.toDouble()
                ?: throw IllegalArgumentException(
                    "Producto $id: $key debe ser un entero."
                )

            require(
                value.isFinite() &&
                        value >= 0 &&
                        value <= Int.MAX_VALUE &&
                        value % 1.0 == 0.0
            ) {
                "Producto $id: $key debe ser un entero no negativo."
            }

            return value.toInt()
        }

        val price = (data["price"] as? Number)?.toDouble()
            ?: throw IllegalArgumentException(
                "Producto $id: falta price."
            )

        require(price.isFinite() && price >= 0) {
            "Producto $id: precio inválido."
        }

        val available = data["isAvailable"] as? Boolean
            ?: throw IllegalArgumentException(
                "Producto $id: falta isAvailable."
            )

        return GameProduct(
            id = id,
            name = text("name"),
            description = text("description"),
            price = price,
            developerId = text("developerId"),
            imageUrl = text("imageUrl"),
            isAvailable = available,
            stock = integer("stock"),
            coverColorIndex = if (data.containsKey("coverColorIndex")) {
                integer("coverColorIndex")
            } else {
                0
            },
        )
    }
}