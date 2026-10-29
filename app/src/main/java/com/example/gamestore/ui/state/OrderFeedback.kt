package com.example.gamestore.ui.state

data class OrderFeedback(
    val message: String,
    val isError: Boolean = false,
)
