package com.example.gamestore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.gamestore.model.GameProduct
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.navigation.StoreNavKey
import com.example.gamestore.ui.screens.CatalogScreen
import com.example.gamestore.ui.screens.DetailScreen
import com.example.gamestore.ui.screens.ProfileScreen
import com.example.gamestore.ui.theme.GamestoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GamestoreTheme {
                val backStack = rememberNavBackStack(StoreNavKey.Catalog)
                val storeViewModel: StoreViewModel = viewModel()
                val uiState by storeViewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavDisplay(
                        backStack = backStack,
                        modifier = Modifier.padding(innerPadding),
                        onBack = { backStack.removeLastOrNull() },
                    ) { key ->
                        NavEntry(key) {
                            when (key) {
                                is StoreNavKey.Catalog -> {
                                    CatalogScreen(
                                        products = uiState.visibleProducts,
                                        searchQuery = uiState.searchQuery,
                                        catalogPosition = uiState.catalogPosition,
                                        orderItemCount = uiState.orderItemCount,
                                        formattedOrderTotal = uiState.formattedOrderTotal,
                                        onProductSelected = { productId ->
                                            backStack.add(StoreNavKey.Detail(productId))
                                        },
                                        onToggleFavorite = { productId ->
                                            storeViewModel.toggleFavorite(productId)
                                        },
                                        onQueryChange = { query ->
                                            storeViewModel.onQueryChange(query)
                                        },
                                        onClearQuery = {
                                            storeViewModel.clearQuery()
                                        },
                                        onLazyPositionChange = storeViewModel::updateLazyCatalogPosition,
                                        onConventionalPositionChange = storeViewModel::updateConventionalCatalogPosition,
                                    )
                                }
                                is StoreNavKey.Detail -> {
                                    val product: GameProduct? = uiState.catalog.find { it.id == key.productId }
                                    product?.let {
                                        val orderLine = uiState.orderLine(it.id)
                                        DetailScreen(
                                            product = it,
                                            quantityInOrder = orderLine?.quantity ?: 0,
                                            formattedOrderSubtotal = uiState.formattedSubtotal(it.id),
                                            formattedOrderTotal = uiState.formattedOrderTotal,
                                            orderFeedback = uiState.orderFeedback,
                                            onBack = { backStack.removeLastOrNull() },
                                            onToggleFavorite = { productId ->
                                                storeViewModel.toggleFavorite(productId)
                                            },
                                            onOpenProfile = { developerId ->
                                                backStack.add(StoreNavKey.Profile(developerId))
                                            },
                                            onAddToOrder = { productId -> storeViewModel.addToOrder(productId) },
                                            onRemoveFromOrder = { productId -> storeViewModel.removeFromOrder(productId) },
                                            onUpdateQuantity = { productId, quantity ->
                                                storeViewModel.updateQuantity(productId, quantity)
                                            },
                                            onDismissOrderFeedback = storeViewModel::clearOrderFeedback,
                                        )
                                    }
                                }
                                is StoreNavKey.Profile -> {
                                    val profile: DeveloperProfile? = uiState.profiles.find { it.id == key.developerId }
                                    profile?.let {
                                        ProfileScreen(
                                            profile = it,
                                        ) {
                                            backStack.removeLastOrNull()
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }

                    BackHandler(enabled = backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                }
            }
        }
    }
}
