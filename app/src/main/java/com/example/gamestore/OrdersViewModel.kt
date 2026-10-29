package com.example.gamestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestore.data.OrderApi
import com.example.gamestore.data.OrderHttpException
import com.example.gamestore.data.OrderNotFoundException
import com.example.gamestore.ui.state.OrdersUiState
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

class OrdersViewModel(
    private val orderApi: OrderApi = OrderApi()
) : ViewModel() {

    private val _ordersState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val ordersState: StateFlow<OrdersUiState> = _ordersState

    private val _orderDetailState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val orderDetailState: StateFlow<OrdersUiState> = _orderDetailState

    fun loadOrders() {
        viewModelScope.launch {
            _ordersState.value = OrdersUiState.Loading
            try {
                val orders = orderApi.getOrders()
                if (orders.isEmpty()) {
                    _ordersState.value = OrdersUiState.Empty
                } else {
                    _ordersState.value = OrdersUiState.Success(orders)
                }
            } catch (e: OrderHttpException) {
                _ordersState.value = OrdersUiState.Error("El servidor no pudo atender la solicitud.")
            } catch (e: SerializationException) {
                _ordersState.value = OrdersUiState.Error("Respuesta inesperada del servidor.")
            } catch (e: IOException) {
                _ordersState.value = OrdersUiState.Error("Sin conexión. Revisa internet.")
            }
        }
    }

    fun loadOrderDetail(id: String) {
        viewModelScope.launch {
            _orderDetailState.value = OrdersUiState.Loading
            try {
                val order = orderApi.getOrderById(id)
                _orderDetailState.value = OrdersUiState.Detail(order)
            } catch (e: OrderNotFoundException) {
                _orderDetailState.value = OrdersUiState.NotFound
            } catch (e: OrderHttpException) {
                _orderDetailState.value = OrdersUiState.Error("El servidor no pudo atender la solicitud.")
            } catch (e: SerializationException) {
                _orderDetailState.value = OrdersUiState.Error("Respuesta inesperada del servidor.")
            } catch (e: IOException) {
                _orderDetailState.value = OrdersUiState.Error("Sin conexión. Revisa internet.")
            }
        }
    }
}
