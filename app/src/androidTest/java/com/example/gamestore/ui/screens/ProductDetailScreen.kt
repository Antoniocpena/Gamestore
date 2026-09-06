package com.example.gamestore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamestore.model.GameProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: GameProduct,
    onToggleFavorite: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTechnicalInfo by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalle")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "$${"%.2f".format(product.price)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = {
                    onToggleFavorite(product.id)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (product.isFavorite) {
                        "★ Quitar de favoritos"
                    } else {
                        "☆ Agregar a favoritos"
                    }
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ficha técnica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(
                        onClick = {
                            showTechnicalInfo = !showTechnicalInfo
                        }
                    ) {
                        Text(
                            if (showTechnicalInfo) {
                                "Ver menos"
                            } else {
                                "Ver más"
                            }
                        )
                    }

                    if (showTechnicalInfo) {
                        Text("Identificador: ${product.id}")
                        Text("Formato: Videojuego digital")
                        Text("Disponibilidad: En existencia")
                    }
                }
            }

            Button(
                onClick = {
                    onOpenProfile(product.developerId)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver desarrollador")
            }
        }
    }
}