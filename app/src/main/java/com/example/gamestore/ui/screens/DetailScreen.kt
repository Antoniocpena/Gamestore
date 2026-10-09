package com.example.gamestore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamestore.model.GameProduct
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    product: GameProduct,
    onBack: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onAddToOrder: (String) -> Unit,
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
            Text("Precio: ${NumberFormat.getCurrencyInstance(Locale.US).format(product.price)}")
            Text(if (product.isAvailable) "Disponible" else "Agotado")
            Text("Descripción: ${product.description}")

            Button(onClick = { onToggleFavorite(product.id) }) {
                Text(if (product.isFavorite) "Quitar de favoritos" else "Marcar favorito")
            }

            Button(onClick = { onOpenProfile(product.developerId) }) {
                Text("Ver perfil del desarrollador")
            }

            Button(
                onClick = { onAddToOrder(product.id) },
                enabled = product.isAvailable,
            ) {
                Text("Agregar al pedido")
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