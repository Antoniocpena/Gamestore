package com.example.gamestore.ui.state

import com.example.gamestore.model.OrderLineDto
import kotlinx.serialization.Serializable

sealed interface OrdersUiState {
    data object Loading : OrdersUiState
    data object Empty : OrdersUiState
    data class Error(val message: String) : OrdersUiState
    data class Success(val orders: List<OrderSummaryDto>) : OrdersUiState
    data class Detail(val order: OrderDetailDto) : OrdersUiState
    data object NotFound : OrdersUiState
}

// DTOs para lista y detalle
@Serializable
data class OrderSummaryDto(
    val id: String,
    val total: Double,
    val date: String = ""
)

@Serializable
data class OrderDetailDto(
    val id: String,
    val lines: List<OrderLineDto> = emptyList(),
    val total: Double = 0.0,
    val date: String = ""
)
