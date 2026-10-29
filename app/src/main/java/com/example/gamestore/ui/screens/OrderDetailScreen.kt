package com.example.gamestore.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamestore.ui.state.OrdersUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    state: OrdersUiState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del pedido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            when (state) {
                OrdersUiState.Loading -> CircularProgressIndicator()
                is OrdersUiState.Detail -> {
                    Text("Pedido #${state.order.id}")
                    state.order.lines.forEach { line ->
                        Text("${line.productName} · ${line.quantity} × $${line.unitPrice}")
                    }
                    Text("Total: $${state.order.total}")
                    Text("Fecha: ${state.order.date}")
                }
                OrdersUiState.NotFound -> Text("Este pedido ya no existe en el servidor.")
                is OrdersUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                else -> {}
            }
        }
    }
}
