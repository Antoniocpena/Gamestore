package com.example.gamestore

import com.example.gamestore.data.TestCatalog
import com.example.gamestore.domain.OrderOperationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreViewModelTest {
    @Test
    fun uiState_keepsCatalogQueryOrderAndPositionInOneStateFlow() {
        val viewModel = StoreViewModel()

        viewModel.onQueryChange("Crónicas")
        val product = viewModel.uiState.value.catalog.first { it.stock > 1 }
        viewModel.toggleFavorite(product.id)
        viewModel.addToOrder(product.id)
        viewModel.updateLazyCatalogPosition(firstVisibleItemIndex = 24, scrollOffset = 16)
        viewModel.updateConventionalCatalogPosition(scrollOffset = 320)

        val stateAfterConfigurationChange = viewModel.uiState.value
        assertEquals(TestCatalog.CATALOG_SIZE, stateAfterConfigurationChange.catalog.size)
        assertEquals("Crónicas", stateAfterConfigurationChange.searchQuery)
        assertEquals(50, stateAfterConfigurationChange.visibleProducts.size)
        assertEquals(true, stateAfterConfigurationChange.catalog.first { it.id == product.id }.isFavorite)
        assertEquals(1, stateAfterConfigurationChange.order.single().quantity)
        assertEquals(24, stateAfterConfigurationChange.catalogPosition.lazyFirstVisibleItemIndex)
        assertEquals(16, stateAfterConfigurationChange.catalogPosition.lazyFirstVisibleItemScrollOffset)
        assertEquals(320, stateAfterConfigurationChange.catalogPosition.conventionalScrollOffset)
    }

    @Test
    fun rejectedOperation_doesNotMutateOrderAndPublishesFeedback() {
        val viewModel = StoreViewModel()
        val soldOutProduct = viewModel.uiState.value.catalog.first { !it.isAvailable }

        val result = viewModel.addToOrder(soldOutProduct.id)

        assertTrue(result is OrderOperationResult.Rejected)
        assertTrue(viewModel.uiState.value.order.isEmpty())
        assertEquals(true, viewModel.uiState.value.orderFeedback?.isError)
    }
}
