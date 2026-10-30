package com.example.gamestore.data.repository.impl

import com.example.gamestore.data.OrderApi
import com.example.gamestore.data.repository.OrderRepository
import com.example.gamestore.model.CreateOrderDto

class MockApiOrderRepository(
    private val api: OrderApi
) : OrderRepository {

    override suspend fun createOrder(
        order: CreateOrderDto
    ) =
        runCatching {
            api.create(order)
        }

    override suspend fun getOrders() =
        runCatching {
            api.getOrders()
        }

    override suspend fun getOrderById(
        id: String
    ) =
        runCatching {
            api.getOrderById(id)
        }
}