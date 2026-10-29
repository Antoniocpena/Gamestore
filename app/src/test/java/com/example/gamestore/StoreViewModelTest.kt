package com.example.gamestore

import com.example.gamestore.data.OrderLineEntity
import com.example.gamestore.data.TestCatalog
import com.example.gamestore.ui.state.buildStoreUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreViewModelTest {

    @Test
    fun projection_combinesCatalogRoomDataAndPreference() {
        val catalog = TestCatalog.createProducts()
        val favorite = catalog.last()

        val line = OrderLineEntity(
            productId = favorite.id,
            productName = favorite.name,
            quantity = 2,
            unitPrice = favorite.price,
            subtotal = favorite.price * 2,
        )

        val state = buildStoreUiState(
            products = catalog,
            profiles = TestCatalog.profiles,
            query = "Crónicas",
            favoriteIds = listOf(favorite.id),
            orderLines = listOf(line),
            darkTheme = true,
        )

        assertEquals(TestCatalog.CATALOG_SIZE, state.catalog.size)
        assertEquals(50, state.products.size)
        assertTrue(state.catalog.last().isFavorite)
        assertEquals(listOf(line), state.orderLines)
        assertEquals(true, state.isDarkTheme)
        assertEquals("Crónicas", state.searchQuery)
    }

    @Test
    fun projection_keepsFavoritesAvailableOutsideSearchResults() {
        val catalog = TestCatalog.createProducts()
        val favorite = catalog.first()

        val state = buildStoreUiState(
            products = catalog,
            profiles = TestCatalog.profiles,
            query = "sin coincidencias",
            favoriteIds = listOf(favorite.id),
            orderLines = emptyList(),
            darkTheme = false,
        )

        assertTrue(state.products.isEmpty())
        assertTrue(state.catalog.first().isFavorite)
        assertFalse(state.isDarkTheme!!)
    }
}