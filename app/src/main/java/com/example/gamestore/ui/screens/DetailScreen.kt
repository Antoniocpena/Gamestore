package com.example.gamestore.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamestore.model.GameProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    product: GameProduct,
    onBack: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenProfile: (String) -> Unit
) {
    var showMore by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text("Precio: $${product.price}")
            Text("Descripción: ${product.description}")

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onToggleFavorite(product.id) }) {
                Text(if (product.isFavorite) "Quitar de favoritos" else "Marcar favorito")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onOpenProfile(product.developerId) }) {
                Text("Ver perfil del desarrollador")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showMore = !showMore }) {
                Text(if (showMore) "Ocultar ficha técnica" else "Ver ficha técnica")
            }
            if (showMore) {
                Text("Ficha técnica: información extendida del juego...")
            }
        }
    }
}
