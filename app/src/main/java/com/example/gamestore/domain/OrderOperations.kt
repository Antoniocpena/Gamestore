package com.example.gamestore.domain

import com.example.gamestore.model.GameProduct
import com.example.gamestore.model.OrderLine
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

enum class OrderRejection(val message: String) {
    PRODUCT_NOT_FOUND("El producto no existe en el catálogo."),
    PRODUCT_OUT_OF_STOCK("El producto no tiene existencias."),
    INVALID_QUANTITY("La cantidad debe ser mayor que cero."),
    INSUFFICIENT_STOCK("La cantidad solicitada supera las existencias."),
    LINE_NOT_FOUND("El producto no está en el pedido."),
}

sealed interface OrderOperationResult {
    data class Accepted(val order: List<OrderLine>) : OrderOperationResult

    data class Rejected(val reason: OrderRejection) : OrderOperationResult
}

fun addToOrder(
    catalog: List<GameProduct>,
    order: List<OrderLine>,
    productId: String,
    quantity: Int = 1,
): OrderOperationResult {
    if (quantity <= 0) {
        return OrderOperationResult.Rejected(OrderRejection.INVALID_QUANTITY)
    }

    val product = catalog.find { it.id == productId }
        ?: return OrderOperationResult.Rejected(OrderRejection.PRODUCT_NOT_FOUND)
    if (!product.isAvailable) {
        return OrderOperationResult.Rejected(OrderRejection.PRODUCT_OUT_OF_STOCK)
    }

    val currentQuantity = order
        .asSequence()
        .filter { it.productId == productId }
        .sumOf { it.quantity.toLong() }
    val requestedQuantity = currentQuantity + quantity
    if (requestedQuantity > product.stock) {
        return OrderOperationResult.Rejected(OrderRejection.INSUFFICIENT_STOCK)
    }

    val updatedLine = OrderLine(
        productId = product.id,
        productName = product.name,
        unitPriceCents = product.priceCents,
        quantity = requestedQuantity.toInt(),
    )
    return OrderOperationResult.Accepted(order.replaceProductLine(updatedLine))
}

fun removeFromOrder(
    order: List<OrderLine>,
    productId: String,
): OrderOperationResult {
    if (order.none { it.productId == productId }) {
        return OrderOperationResult.Rejected(OrderRejection.LINE_NOT_FOUND)
    }
    return OrderOperationResult.Accepted(order.filterNot { it.productId == productId })
}

fun updateQuantity(
    catalog: List<GameProduct>,
    order: List<OrderLine>,
    productId: String,
    quantity: Int,
): OrderOperationResult {
    if (quantity <= 0) {
        return OrderOperationResult.Rejected(OrderRejection.INVALID_QUANTITY)
    }
    val product = catalog.find { it.id == productId }
        ?: return OrderOperationResult.Rejected(OrderRejection.PRODUCT_NOT_FOUND)
    if (order.none { it.productId == productId }) {
        return OrderOperationResult.Rejected(OrderRejection.LINE_NOT_FOUND)
    }
    if (!product.isAvailable) {
        return OrderOperationResult.Rejected(OrderRejection.PRODUCT_OUT_OF_STOCK)
    }
    if (quantity > product.stock) {
        return OrderOperationResult.Rejected(OrderRejection.INSUFFICIENT_STOCK)
    }

    val updatedLine = OrderLine(
        productId = product.id,
        productName = product.name,
        unitPriceCents = product.priceCents,
        quantity = quantity,
    )
    return OrderOperationResult.Accepted(order.replaceProductLine(updatedLine))
}

fun calculateSubtotal(line: OrderLine): Long =
    Math.multiplyExact(line.unitPriceCents, line.quantity.toLong())

fun calculateTotal(order: List<OrderLine>): Long =
    order.fold(0L) { total, line -> Math.addExact(total, calculateSubtotal(line)) }

fun formatCurrency(cents: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance("USD")
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return formatter.format(BigDecimal.valueOf(cents, 2))
}

private fun List<OrderLine>.replaceProductLine(replacement: OrderLine): List<OrderLine> {
    val firstIndex = indexOfFirst { it.productId == replacement.productId }
    if (firstIndex == -1) {
        return this + replacement
    }

    val withoutProduct = filterNot { it.productId == replacement.productId }.toMutableList()
    withoutProduct.add(firstIndex.coerceAtMost(withoutProduct.size), replacement)
    return withoutProduct
}
