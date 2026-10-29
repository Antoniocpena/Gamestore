package com.example.gamestore.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.gamestore.ui.state.OrdersUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    state: OrdersUiState,
    onRetry: () -> Unit,
    onOrderSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis pedidos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (state) {
                OrdersUiState.Loading -> CircularProgressIndicator()
                OrdersUiState.Empty -> Text("Aún no has enviado pedidos.")
                is OrdersUiState.Error -> Column {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry) { Text("Reintentar") }
                }
                is OrdersUiState.Success -> LazyColumn {
                    items(state.orders) { order ->
                        ListItem(
                            headlineContent = { Text("Pedido #${order.id}") },
                            supportingContent = { Text("Total: $${order.total} · Fecha: ${order.date}") },
                            modifier = Modifier.clickable { onOrderSelected(order.id) }
                        )
                    }
                }
                else -> {}
            }
        }
    }
}
