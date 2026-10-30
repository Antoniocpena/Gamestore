package com.example.gamestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestore.data.repository.OrderRepository
import com.example.gamestore.ui.state.OrdersUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val repository: OrderRepository,
) : ViewModel() {

    private val _ordersState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val ordersState: StateFlow<OrdersUiState> = _ordersState.asStateFlow()

    private val _orderDetailState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val orderDetailState: StateFlow<OrdersUiState> = _orderDetailState.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            _ordersState.value = OrdersUiState.Loading
            repository.getOrders()
                .onSuccess { orders ->
                    if (orders.isEmpty()) {
                        _ordersState.value = OrdersUiState.Empty
                    } else {
                        _ordersState.value = OrdersUiState.Success(orders)
                    }
                }
                .onFailure { error ->
                    _ordersState.value = OrdersUiState.Error(
                        error.message ?: "No se pudieron cargar los pedidos.",
                    )
                }
        }
    }

    fun loadOrderDetail(id: String) {
        viewModelScope.launch {
            _orderDetailState.value = OrdersUiState.Loading
            repository.getOrderById(id)
                .onSuccess { order ->
                    _orderDetailState.value = OrdersUiState.Detail(order)
                }
                .onFailure { error ->
                    _orderDetailState.value = OrdersUiState.Error(
                        error.message ?: "No se encontró el pedido.",
                    )
                }
        }
    }
}
