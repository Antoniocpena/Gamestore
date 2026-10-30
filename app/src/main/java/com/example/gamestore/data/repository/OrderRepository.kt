package com.example.gamestore.data.repository

import com.example.gamestore.model.CreateOrderDto
import com.example.gamestore.model.CreatedOrderDto
import com.example.gamestore.ui.state.OrderDetailDto
import com.example.gamestore.ui.state.OrderSummaryDto

interface OrderRepository {

    suspend fun createOrder(
        order: CreateOrderDto
    ): Result<CreatedOrderDto>

    suspend fun getOrders():
            Result<List<OrderSummaryDto>>

    suspend fun getOrderById(
        id: String
    ): Result<OrderDetailDto>
}