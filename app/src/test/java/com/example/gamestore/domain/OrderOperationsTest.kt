package com.example.gamestore.domain

import com.example.gamestore.model.GameProduct
import com.example.gamestore.model.OrderLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderOperationsTest {
    private val availableProduct = product(id = "game-001", stock = 5, priceCents = 999)
    private val soldOutProduct = product(id = "game-002", stock = 0, priceCents = 2_500)
    private val catalog = listOf(availableProduct, soldOutProduct)

    @Test
    fun addToOrder_accumulatesUnitsInOneLine() {
        val firstOrder = accepted(addToOrder(catalog, emptyList(), availableProduct.id, 1))
        val secondOrder = accepted(addToOrder(catalog, firstOrder, availableProduct.id, 2))

        assertEquals(1, secondOrder.size)
        assertEquals(3, secondOrder.single().quantity)
    }

    @Test
    fun addToOrder_consolidatesPreexistingDuplicateLines() {
        val duplicatedOrder = listOf(
            line(availableProduct, quantity = 1),
            line(availableProduct, quantity = 2),
        )

        val updatedOrder = accepted(addToOrder(catalog, duplicatedOrder, availableProduct.id, 1))

        assertEquals(1, updatedOrder.size)
        assertEquals(4, updatedOrder.single().quantity)
    }

    @Test
    fun invalidAdds_areRejected() {
        assertRejected(
            expected = OrderRejection.PRODUCT_NOT_FOUND,
            result = addToOrder(catalog, emptyList(), "missing"),
        )
        assertRejected(
            expected = OrderRejection.PRODUCT_OUT_OF_STOCK,
            result = addToOrder(catalog, emptyList(), soldOutProduct.id),
        )
        assertRejected(
            expected = OrderRejection.INVALID_QUANTITY,
            result = addToOrder(catalog, emptyList(), availableProduct.id, 0),
        )
        assertRejected(
            expected = OrderRejection.INSUFFICIENT_STOCK,
            result = addToOrder(catalog, emptyList(), availableProduct.id, 6),
        )
    }

    @Test
    fun updateQuantity_validatesStockAndKeepsOneLine() {
        val order = accepted(addToOrder(catalog, emptyList(), availableProduct.id, 1))
        val updatedOrder = accepted(updateQuantity(catalog, order, availableProduct.id, 5))

        assertEquals(1, updatedOrder.size)
        assertEquals(5, updatedOrder.single().quantity)
        assertRejected(
            expected = OrderRejection.INSUFFICIENT_STOCK,
            result = updateQuantity(catalog, updatedOrder, availableProduct.id, 6),
        )
        assertRejected(
            expected = OrderRejection.INVALID_QUANTITY,
            result = updateQuantity(catalog, updatedOrder, availableProduct.id, 0),
        )
    }

    @Test
    fun removeFromOrder_rejectsMissingLine() {
        val order = listOf(line(availableProduct, quantity = 1))

        assertTrue(removeFromOrder(order, availableProduct.id) is OrderOperationResult.Accepted)
        assertRejected(
            expected = OrderRejection.LINE_NOT_FOUND,
            result = removeFromOrder(emptyList(), availableProduct.id),
        )
    }

    @Test
    fun subtotalTotalAndFormatting_useOneCentralRule() {
        val order = listOf(
            line(availableProduct, quantity = 2),
            line(soldOutProduct, quantity = 3),
        )

        assertEquals(1_998L, calculateSubtotal(order.first()))
        assertEquals(9_498L, calculateTotal(order))
        assertEquals("\$94.98", formatCurrency(calculateTotal(order)))
    }

    private fun product(id: String, stock: Int, priceCents: Long) = GameProduct(
        id = id,
        name = "Producto $id",
        description = "Producto de prueba",
        priceCents = priceCents,
        developerId = "dev01",
        stock = stock,
        coverColorIndex = 0,
    )

    private fun line(product: GameProduct, quantity: Int) = OrderLine(
        productId = product.id,
        productName = product.name,
        unitPriceCents = product.priceCents,
        quantity = quantity,
    )

    private fun accepted(result: OrderOperationResult): List<OrderLine> {
        assertTrue(result is OrderOperationResult.Accepted)
        return (result as OrderOperationResult.Accepted).order
    }

    private fun assertRejected(expected: OrderRejection, result: OrderOperationResult) {
        assertTrue(result is OrderOperationResult.Rejected)
        assertEquals(expected, (result as OrderOperationResult.Rejected).reason)
    }
}
