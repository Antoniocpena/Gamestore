package com.example.gamestore.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gamestore.domain.formatCurrency
import com.example.gamestore.model.GameProduct
import com.example.gamestore.ui.state.CatalogPosition
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val CATALOG_LOG_TAG = "CatalogLifecycle"

private enum class CatalogRenderMode {
    LAZY_GRID,
    CONVENTIONAL,
}

private val coverPalettes = listOf(
    Color(0xFF355C7D) to Color(0xFF6C5B7B),
    Color(0xFF8E2DE2) to Color(0xFF4A00E0),
    Color(0xFF11998E) to Color(0xFF38EF7D),
    Color(0xFFF7971E) to Color(0xFFFFD200),
    Color(0xFF0F2027) to Color(0xFF2C5364),
    Color(0xFFB24592) to Color(0xFFF15F79),
    Color(0xFF134E5E) to Color(0xFF71B280),
    Color(0xFF42275A) to Color(0xFF734B6D),
    Color(0xFF1D4350) to Color(0xFFA43931),
    Color(0xFF232526) to Color(0xFF414345),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<GameProduct>,
    searchQuery: String,
    catalogPosition: CatalogPosition,
    orderItemCount: Int,
    formattedOrderTotal: String,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onLazyPositionChange: (Int, Int) -> Unit,
    onConventionalPositionChange: (Int) -> Unit,
) {
    var renderMode by rememberSaveable { mutableStateOf(CatalogRenderMode.LAZY_GRID) }
    val lazyGridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = catalogPosition.lazyFirstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = catalogPosition.lazyFirstVisibleItemScrollOffset,
    )
    val conventionalScrollState = rememberScrollState(
        initial = catalogPosition.conventionalScrollOffset,
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(lazyGridState) {
        snapshotFlow {
            lazyGridState.firstVisibleItemIndex to lazyGridState.firstVisibleItemScrollOffset
        }.distinctUntilChanged().collect { (index, offset) ->
            onLazyPositionChange(index, offset)
        }
    }
    LaunchedEffect(conventionalScrollState) {
        snapshotFlow { conventionalScrollState.value }
            .distinctUntilChanged()
            .collect(onConventionalPositionChange)
    }

    DisposableEffect(renderMode) {
        Log.d(CATALOG_LOG_TAG, "MODO ACTIVO: $renderMode; productos=${products.size}")
        onDispose { Log.d(CATALOG_LOG_TAG, "MODO FINALIZADO: $renderMode") }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Catálogo (${products.size})")
                        Text(
                            text = "Pedido: $orderItemCount · $formattedOrderTotal",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        when (renderMode) {
                            CatalogRenderMode.LAZY_GRID -> lazyGridState.animateScrollToItem(0)
                            CatalogRenderMode.CONVENTIONAL -> conventionalScrollState.animateScrollTo(0)
                        }
                    }
                },
            ) {
                Text("↑")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
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
                singleLine = true,
            )

            Text(
                text = "${products.size} resultados",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
            )

            RenderModeSelector(selectedMode = renderMode) { renderMode = it }

            when (renderMode) {
                CatalogRenderMode.LAZY_GRID -> LazyCatalogGrid(
                    products = products,
                    state = lazyGridState,
                    onProductSelected = onProductSelected,
                    onToggleFavorite = onToggleFavorite,
                )

                CatalogRenderMode.CONVENTIONAL -> ConventionalCatalog(
                    products = products,
                    state = conventionalScrollState,
                    onProductSelected = onProductSelected,
                    onToggleFavorite = onToggleFavorite,
                )
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
    state: LazyGridState,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = state,
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
    state: ScrollState,
    onProductSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state)
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
            LocalProductCover(product)
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatCurrency(product.priceCents),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (product.isAvailable) "Existencias: ${product.stock}" else "Agotado",
                        color = if (product.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    IconButton(onClick = { onToggleFavorite(product.id) }) {
                        Icon(
                            imageVector = if (product.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (product.isFavorite) {
                                "Quitar ${product.name} de favoritos"
                            } else {
                                "Agregar ${product.name} a favoritos"
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalProductCover(product: GameProduct) {
    val (startColor, endColor) = coverPalettes[product.coverColorIndex % coverPalettes.size]
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(Brush.linearGradient(listOf(startColor, endColor))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = product.id.removePrefix("game-"),
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
        )
    }
}
