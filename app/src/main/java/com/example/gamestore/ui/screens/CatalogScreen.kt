package com.example.gamestore.ui.screens

import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.gamestore.model.GameProduct
import java.text.NumberFormat
import java.util.*

private const val CATALOG_LOG_TAG = "CatalogLifecycle"

private enum class CatalogRenderMode {
    LAZY_GRID,
    CONVENTIONAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<GameProduct>,
    searchQuery: String,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onScrollTop: () -> Unit,
) {
    var renderMode by rememberSaveable { mutableStateOf(CatalogRenderMode.LAZY_GRID) }

    DisposableEffect(renderMode) {
        Log.d(CATALOG_LOG_TAG, "MODO ACTIVO: $renderMode; productos=${products.size}")
        onDispose { Log.d(CATALOG_LOG_TAG, "MODO FINALIZADO: $renderMode") }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo (${products.size})") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onScrollTop) {
                Text("↑")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 🔹 Campo de búsqueda mejorado
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                label = { Text("Buscar productos") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearQuery) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true
            )
            
            Text(
                text = "${products.size} resultados",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall
            )

            RenderModeSelector(
                selectedMode = renderMode
            ) {
                renderMode = it
            }

            when (renderMode) {
                CatalogRenderMode.LAZY_GRID -> {
                    LazyCatalogGrid(
                        products = products,
                        onProductSelected = onProductSelected,
                        onToggleFavorite = onToggleFavorite
                    )
                }
                CatalogRenderMode.CONVENTIONAL -> {
                    ConventionalCatalog(
                        products = products,
                        onProductSelected = onProductSelected,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderModeSelector(
    selectedMode: CatalogRenderMode,
    onModeSelected: (CatalogRenderMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedMode == CatalogRenderMode.LAZY_GRID,
            onClick = { onModeSelected(CatalogRenderMode.LAZY_GRID) },
            label = { Text("Lazy grid") },
        )
        FilterChip(
            selected = selectedMode == CatalogRenderMode.CONVENTIONAL,
            onClick = { onModeSelected(CatalogRenderMode.CONVENTIONAL) },
            label = { Text("Convencional") },
        )
    }
}

@Composable
private fun LazyCatalogGrid(
    products: List<GameProduct>,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(product, onProductSelected, onToggleFavorite)
        }
    }
}

@Composable
private fun ConventionalCatalog(
    products: List<GameProduct>,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        products.forEach { product ->
            ProductCard(product, onProductSelected, onToggleFavorite)
        }
    }
}

@Composable
private fun ProductCard(
    product: GameProduct,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    DisposableEffect(product.id) {
        Log.d(CATALOG_LOG_TAG, "ENTRA tarjeta ${product.id}")
        onDispose { Log.d(CATALOG_LOG_TAG, "SALE tarjeta ${product.id}") }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductSelected(product.id) },
    ) {
        Column {
            RemoteProductImage(imageUrl = product.imageUrl, productName = product.name)
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale.US).format(product.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (product.isAvailable) "Disponible" else "Agotado",
                        color = if (product.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge
                    )
                    IconButton(onClick = { onToggleFavorite(product.id) }) {
                        Icon(
                            imageVector = if (product.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (product.isFavorite) {
                                "Quitar ${product.name} de favoritos"
                            } else {
                                "Agregar ${product.name} a favoritos"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteProductImage(imageUrl: String, productName: String) {
    var isLoading by remember { mutableStateOf(value = true) }
    var hasError by remember { mutableStateOf(value = false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        if (isLoading) {
            ImageSkeleton(modifier = Modifier.fillMaxSize())
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(enable = true)
                .build(),
            contentDescription = "Portada de $productName",
            contentScale = ContentScale.Crop,
            onLoading = { isLoading = true; hasError = false },
            onSuccess = { isLoading = false; hasError = false },
            onError = { isLoading = false; hasError = true },
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isLoading || hasError) 0f else 1f)
        )
        if (hasError) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Imagen no disponible",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ImageSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "image-skeleton")
    val opacity by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton-opacity"
    )
    Box(
        modifier = modifier
            .background(Color.LightGray.copy(alpha = opacity))
    )
}
