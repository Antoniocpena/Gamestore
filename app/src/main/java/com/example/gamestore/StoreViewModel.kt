package com.example.gamestore

import androidx.lifecycle.ViewModel
import com.example.gamestore.data.TestCatalog
import com.example.gamestore.domain.OrderOperationResult
import com.example.gamestore.domain.addToOrder as addProductToOrder
import com.example.gamestore.domain.removeFromOrder as removeProductFromOrder
import com.example.gamestore.domain.updateQuantity as updateProductQuantity
import com.example.gamestore.ui.state.OrderFeedback
import com.example.gamestore.ui.state.StoreUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StoreViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        StoreUiState(
            catalog = TestCatalog.createProducts(),
            profiles = TestCatalog.profiles,
        ),
    )

    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun toggleFavorite(productId: String) {
        _uiState.update { state ->
            state.copy(
                catalog = state.catalog.map { product ->
                    if (product.id == productId) {
                        product.copy(isFavorite = !product.isFavorite)
                    } else {
                        product
                    }
                },
            )
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
    }

    fun clearQuery() {
        onQueryChange("")
    }

    fun addToOrder(productId: String, quantity: Int = 1): OrderOperationResult =
        applyOrderOperation(successMessage = "Producto agregado al pedido.") { state ->
            addProductToOrder(
                catalog = state.catalog,
                order = state.order,
                productId = productId,
                quantity = quantity,
            )
        }

    fun removeFromOrder(productId: String): OrderOperationResult =
        applyOrderOperation(successMessage = "Producto eliminado del pedido.") { state ->
            removeProductFromOrder(
                order = state.order,
                productId = productId,
            )
        }

    fun updateQuantity(productId: String, quantity: Int): OrderOperationResult =
        applyOrderOperation(successMessage = "Cantidad actualizada.") { state ->
            updateProductQuantity(
                catalog = state.catalog,
                order = state.order,
                productId = productId,
                quantity = quantity,
            )
        }

    fun updateLazyCatalogPosition(firstVisibleItemIndex: Int, scrollOffset: Int) {
        _uiState.update { state ->
            val updatedPosition = state.catalogPosition.copy(
                lazyFirstVisibleItemIndex = firstVisibleItemIndex.coerceAtLeast(0),
                lazyFirstVisibleItemScrollOffset = scrollOffset.coerceAtLeast(0),
            )
            if (updatedPosition == state.catalogPosition) state else state.copy(catalogPosition = updatedPosition)
        }
    }

    fun updateConventionalCatalogPosition(scrollOffset: Int) {
        _uiState.update { state ->
            val updatedPosition = state.catalogPosition.copy(
                conventionalScrollOffset = scrollOffset.coerceAtLeast(0),
            )
            if (updatedPosition == state.catalogPosition) state else state.copy(catalogPosition = updatedPosition)
        }
    }

    fun clearOrderFeedback() {
        _uiState.update { it.copy(orderFeedback = null) }
    }

    private fun applyOrderOperation(
        successMessage: String,
        operation: (StoreUiState) -> OrderOperationResult,
    ): OrderOperationResult {
        while (true) {
            val currentState = _uiState.value
            val result = operation(currentState)
            val updatedState = when (result) {
                is OrderOperationResult.Accepted -> currentState.copy(
                    order = result.order,
                    orderFeedback = OrderFeedback(message = successMessage, isError = false),
                )

                is OrderOperationResult.Rejected -> currentState.copy(
                    orderFeedback = OrderFeedback(message = result.reason.message, isError = true),
                )
            }
            if (_uiState.compareAndSet(currentState, updatedState)) {
                return result
            }
        }
    }
}
