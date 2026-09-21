package com.example.gamestore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamestore.domain.formatCurrency
import com.example.gamestore.model.GameProduct
import com.example.gamestore.ui.state.OrderFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    product: GameProduct,
    quantityInOrder: Int,
    formattedOrderSubtotal: String,
    formattedOrderTotal: String,
    orderFeedback: OrderFeedback?,
    onBack: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onAddToOrder: (String) -> Unit,
    onRemoveFromOrder: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onDismissOrderFeedback: () -> Unit,
) {
    var showMore by rememberSaveable { mutableStateOf(value = false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Precio: ${formatCurrency(product.priceCents)}")
            Text(if (product.isAvailable) "Existencias: ${product.stock}" else "Agotado")
            Text("Descripción: ${product.description}")

            Button(onClick = { onToggleFavorite(product.id) }) {
                Text(if (product.isFavorite) "Quitar de favoritos" else "Marcar favorito")
            }

            Button(onClick = { onOpenProfile(product.developerId) }) {
                Text("Ver perfil del desarrollador")
            }

            Button(
                onClick = { onAddToOrder(product.id) },
                enabled = product.isAvailable && quantityInOrder < product.stock,
            ) {
                Text("Agregar al pedido")
            }

            if (quantityInOrder > 0) {
                OrderControls(
                    productId = product.id,
                    quantity = quantityInOrder,
                    stock = product.stock,
                    formattedSubtotal = formattedOrderSubtotal,
                    formattedTotal = formattedOrderTotal,
                    onRemoveFromOrder = onRemoveFromOrder,
                    onUpdateQuantity = onUpdateQuantity,
                )
            }

            orderFeedback?.let { feedback ->
                Text(
                    text = feedback.message,
                    color = if (feedback.isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
                TextButton(onClick = onDismissOrderFeedback) {
                    Text("Cerrar mensaje")
                }
            }

            Button(onClick = { showMore = !showMore }) {
                Text(if (showMore) "Ocultar ficha técnica" else "Ver ficha técnica")
            }
            if (showMore) {
                Text("Ficha técnica: información extendida del juego...")
            }
        }
    }
}

@Composable
private fun OrderControls(
    productId: String,
    quantity: Int,
    stock: Int,
    formattedSubtotal: String,
    formattedTotal: String,
    onRemoveFromOrder: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("En el pedido: $quantity")
            Text("Subtotal: $formattedSubtotal")
            Text("Total del pedido: $formattedTotal")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onUpdateQuantity(productId, quantity - 1) },
                    enabled = quantity > 1,
                ) {
                    Text("−")
                }
                OutlinedButton(
                    onClick = { onUpdateQuantity(productId, quantity + 1) },
                    enabled = quantity < stock,
                ) {
                    Text("+")
                }
                OutlinedButton(onClick = { onRemoveFromOrder(productId) }) {
                    Text("Eliminar")
                }
            }
        }
    }
}
