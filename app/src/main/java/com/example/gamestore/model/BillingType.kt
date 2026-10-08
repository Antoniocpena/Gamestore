package com.example.gamestore.model

import kotlinx.serialization.Serializable

@Serializable
enum class BillingType(val label: String) {
    CF("Consumidor Final (CF)"),
    NIT("Factura con NIT")
}
