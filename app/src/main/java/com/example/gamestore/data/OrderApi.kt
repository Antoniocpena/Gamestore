package com.example.gamestore.data

import com.example.gamestore.model.CreateOrderDto
import com.example.gamestore.model.CreatedOrderDto
import com.example.gamestore.ui.state.OrderDetailDto
import com.example.gamestore.ui.state.OrderSummaryDto
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class OrderHttpException : IOException()
class OrderNotFoundException : IOException()

class OrderApi(
    private val endpoint: String =
        "https://6ac7d74775a4ce3fe7224a51.mockapi.io/orders",
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun create(order: CreateOrderDto): CreatedOrderDto =
        withContext(Dispatchers.IO) {
            val connection =
                URL(endpoint).openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=utf-8",
                )
                connection.setRequestProperty(
                    "Accept",
                    "application/json",
                )

                connection.outputStream.use { stream ->
                    val body = json.encodeToString(
                        CreateOrderDto.serializer(),
                        order,
                    )
                    stream.write(body.toByteArray(Charsets.UTF_8))
                }

                if (connection.responseCode !in (200..299)) {
                    throw OrderHttpException()
                }

                val response = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                json.decodeFromString(
                    CreatedOrderDto.serializer(),
                    response,
                ).also { created ->
                    if (created.id.isBlank()) {
                        throw SerializationException("Missing order ID")
                    }
                }
            } finally {
                connection.disconnect()
            }
        }

    suspend fun getOrders(): List<OrderSummaryDto> =
        withContext(Dispatchers.IO) {
            val connection =
                URL(endpoint).openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.setRequestProperty(
                    "Accept",
                    "application/json",
                )

                if (connection.responseCode !in (200..299)) {
                    throw OrderHttpException()
                }

                val response = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                json.decodeFromString(
                    ListSerializer(OrderSummaryDto.serializer()),
                    response,
                )
            } finally {
                connection.disconnect()
            }
        }

    suspend fun getOrderById(id: String): OrderDetailDto =
        withContext(Dispatchers.IO) {
            val connection =
                URL("$endpoint/$id").openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.setRequestProperty(
                    "Accept",
                    "application/json",
                )

                if (connection.responseCode == 404) {
                    throw OrderNotFoundException()
                }

                if (connection.responseCode !in (200..299)) {
                    throw OrderHttpException()
                }

                val response = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                json.decodeFromString(
                    OrderDetailDto.serializer(),
                    response,
                )
            } finally {
                connection.disconnect()
            }
        }
}
