package com.example.gamestore.ui.screens

import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import java.util.Locale

private const val CATALOG_LOG_TAG = "CatalogLifecycle"

private enum class CatalogRenderMode {
    LAZY_GRID,
    CONVENTIONAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<GameProduct>,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    var renderMode by rememberSaveable {
        mutableStateOf(CatalogRenderMode.LAZY_GRID)
    }

    DisposableEffect(renderMode) {
        Log.d(
            CATALOG_LOG_TAG,
            "MODO ACTIVO: $renderMode; productos=${products.size}"
        )

        onDispose {
            Log.d(
                CATALOG_LOG_TAG,
                "MODO FINALIZADO: $renderMode"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Catálogo (${products.size})")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RenderModeSelector(
                selectedMode = renderMode,
                onModeSelected = {
                    renderMode = it
                }
            )

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
    onModeSelected: (CatalogRenderMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedMode == CatalogRenderMode.LAZY_GRID,
            onClick = {
                onModeSelected(CatalogRenderMode.LAZY_GRID)
            },
            label = {
                Text("Lazy grid")
            }
        )

        FilterChip(
            selected = selectedMode == CatalogRenderMode.CONVENTIONAL,
            onClick = {
                onModeSelected(CatalogRenderMode.CONVENTIONAL)
            },
            label = {
                Text("Convencional")
            }
        )
    }
}

@Composable
private fun LazyCatalogGrid(
    products: List<GameProduct>,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = products,
            key = { product ->
                product.id
            }
        ) { product ->
            ProductCard(
                product = product,
                onProductSelected = onProductSelected,
                onToggleFavorite = onToggleFavorite
            )
        }
    }
}

@Composable
private fun ConventionalCatalog(
    products: List<GameProduct>,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        products.forEach { product ->
            ProductCard(
                product = product,
                onProductSelected = onProductSelected,
                onToggleFavorite = onToggleFavorite
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: GameProduct,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    DisposableEffect(product.id) {
        Log.d(
            CATALOG_LOG_TAG,
            "ENTRA tarjeta ${product.id}"
        )

        onDispose {
            Log.d(
                CATALOG_LOG_TAG,
                "SALE tarjeta ${product.id}"
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onProductSelected(product.id)
            }
    ) {
        Column {
            RemoteProductImage(
                imageUrl = product.imageUrl,
                productName = product.name
            )

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
                    text = NumberFormat
                        .getCurrencyInstance(Locale.US)
                        .format(product.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (product.isAvailable) {
                            "Disponible"
                        } else {
                            "Agotado"
                        },
                        color = if (product.isAvailable) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.labelLarge
                    )

                    IconButton(
                        onClick = {
                            onToggleFavorite(product.id)
                        }
                    ) {
                        Icon(
                            imageVector = if (product.isFavorite) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
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
private fun RemoteProductImage(
    imageUrl: String,
    productName: String
) {
    var isLoading by rememberSaveable(imageUrl) {
        mutableStateOf(true)
    }

    var hasError by rememberSaveable(imageUrl) {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            ImageSkeleton(
                modifier = Modifier.fillMaxSize()
            )
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Portada de $productName",
            contentScale = ContentScale.Crop,
            onLoading = {
                isLoading = true
                hasError = false
            },
            onSuccess = {
                isLoading = false
                hasError = false
            },
            onError = {
                isLoading = false
                hasError = true
            },
            modifier = Modifier
                .fillMaxSize()
                .alpha(
                    if (isLoading || hasError) {
                        0f
                    } else {
                        1f
                    }
                )
        )

        if (hasError) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
private fun ImageSkeleton(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(
        label = "image-skeleton"
    )

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
        modifier = modifier.background(
            MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = opacity
            )
        )
    )
}
