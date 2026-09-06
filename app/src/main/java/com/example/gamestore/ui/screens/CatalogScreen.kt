package com.example.gamestore.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamestore.model.GameProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<GameProduct>,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Catálogo de Juegos") })
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            products.forEach { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductSelected(product.id) }
                        .padding(8.dp)
                ) {
                    Text("${product.name} - $${product.price}")
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onToggleFavorite(product.id) }) {
                        Icon(
                            imageVector = if (product.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorito"
                        )
                    }
                }
            }
        }
    }
}
