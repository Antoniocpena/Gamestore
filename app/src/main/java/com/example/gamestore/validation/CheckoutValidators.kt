package com.example.gamestore.validation

object CheckoutValidators {

    fun name(value: String): String? {
        val hasEnoughLetters = value.count { it.isLetter() } >= 3
        val hasNoDigits = value.none { it.isDigit() }

        return if (hasEnoughLetters && hasNoDigits) {
            null
        } else {
            "El nombre debe tener al menos 3 letras y ningún dígito."
        }
    }

    fun phone(value: String): String? {
        val isValid =
            value.length == 8 && value.all { it in ('0'..'9') }

        return if (isValid) {
            null
        } else {
            "El teléfono debe contener exactamente 8 dígitos."
        }
    }

    fun nit(value: String): String? {
        val isValid =
            value.length >= 5 && value.all { it in ('0'..'9') }

        return if (isValid) {
            null
        } else {
            "El NIT debe contener al menos 5 dígitos."
        }
    }

    fun businessName(value: String): String? {
        return if (value.trim().length >= 3) {
            null
        } else {
            "La razón social debe tener al menos 3 caracteres."
        }
    }
}
