package com.example.gamestore.ui.state

import com.example.gamestore.domain.calculateSubtotal
import com.example.gamestore.domain.calculateTotal
import com.example.gamestore.domain.formatCurrency
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct
import com.example.gamestore.model.OrderLine

data class CatalogPosition(
    val lazyFirstVisibleItemIndex: Int = 0,
    val lazyFirstVisibleItemScrollOffset: Int = 0,
    val conventionalScrollOffset: Int = 0,
)

data class OrderFeedback(
    val message: String,
    val isError: Boolean,
)

data class StoreUiState(
    val catalog: List<GameProduct> = emptyList(),
    val profiles: List<DeveloperProfile> = emptyList(),
    val searchQuery: String = "",
    val order: List<OrderLine> = emptyList(),
    val catalogPosition: CatalogPosition = CatalogPosition(),
    val orderFeedback: OrderFeedback? = null,
) {
    val visibleProducts: List<GameProduct>
        get() = if (searchQuery.isBlank()) {
            catalog
        } else {
            catalog.filter { product ->
                product.name.contains(searchQuery, ignoreCase = true) ||
                    product.description.contains(searchQuery, ignoreCase = true)
            }
        }

    val orderItemCount: Int
        get() = order.sumOf { it.quantity }

    val orderTotalCents: Long
        get() = calculateTotal(order)

    val formattedOrderTotal: String
        get() = formatCurrency(orderTotalCents)

    fun orderLine(productId: String): OrderLine? =
        order.find { it.productId == productId }

    fun formattedSubtotal(productId: String): String =
        orderLine(productId)?.let { formatCurrency(calculateSubtotal(it)) } ?: formatCurrency(0)
}
