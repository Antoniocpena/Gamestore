package com.example.gamestore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.gamestore.model.DeveloperProfile
import com.example.gamestore.model.GameProduct
import com.example.gamestore.navigation.StoreNavKey
import com.example.gamestore.ui.screens.CatalogScreen
import com.example.gamestore.ui.screens.CheckoutScreen
import com.example.gamestore.ui.screens.ConfirmationScreen
import com.example.gamestore.ui.screens.DetailScreen
import com.example.gamestore.ui.screens.ProfileScreen
import com.example.gamestore.ui.theme.GamestoreTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            GamestoreTheme {
                val appContainer = (application as GamestoreApplication).container

                val backStack = rememberNavBackStack(StoreNavKey.Catalog)

                val storeViewModel: StoreViewModel = viewModel(
                    factory = StoreViewModelFactory(
                        favoritesRepository = appContainer.favoritesRepository,
                        orderRepository = appContainer.orderRepository,
                        preferencesRepository = appContainer.preferencesRepository,
                    ),
                )

                val uiState by storeViewModel.uiState.collectAsStateWithLifecycle()

                val checkoutState by storeViewModel.checkoutState.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    NavDisplay(
                        backStack = backStack,
                        modifier = Modifier.padding(innerPadding),
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        },
                    ) { key ->
                        NavEntry(key) {
                            when (key) {
                                is StoreNavKey.Catalog -> {
                                    CatalogScreen(
                                        products = uiState.products,
                                        searchQuery = uiState.searchQuery,
                                        isDarkTheme = uiState.isDarkTheme ?: false,
                                        onDarkThemeChange = storeViewModel::setDarkTheme,
                                        onProductSelected = { productId ->
                                            backStack.add(
                                                StoreNavKey.Detail(productId),
                                            )
                                        },
                                        onToggleFavorite = storeViewModel::toggleFavorite,
                                        onQueryChange = storeViewModel::onQueryChange,
                                        onClearQuery = { storeViewModel.clearQuery() },
                                        onScrollTop = {},
                                    )
                                }

                                is StoreNavKey.Detail -> {
                                    val product: GameProduct? =
                                        uiState.catalog.find {
                                            it.id == key.productId
                                        }

                                    product?.let {
                                        DetailScreen(
                                            product = it,
                                            onBack = {
                                                backStack.removeLastOrNull()
                                            },
                                            onToggleFavorite = storeViewModel::toggleFavorite,
                                            onOpenProfile = { developerId ->
                                                backStack.add(
                                                    StoreNavKey.Profile(developerId),
                                                )
                                            },
                                            onAddToOrder = { productId ->
                                                storeViewModel.startCheckout(productId)
                                                backStack.add(StoreNavKey.Checkout)
                                            },
                                        )
                                    }
                                }

                                is StoreNavKey.Profile -> {
                                    val profile: DeveloperProfile? =
                                        uiState.profiles.find {
                                            it.id == key.developerId
                                        }

                                    profile?.let {
                                        ProfileScreen(
                                            profile = it,
                                            onBack = {
                                                backStack.removeLastOrNull()
                                            },
                                        )
                                    }
                                }

                                is StoreNavKey.Checkout -> {
                                    CheckoutScreen(
                                        state = checkoutState,
                                        onFieldChange = storeViewModel::onCheckoutFieldChange,
                                        onFieldTouched = storeViewModel::onCheckoutFieldTouched,
                                        onBillingTypeChange = storeViewModel::onBillingTypeChange,
                                        onPaymentMethodChange = storeViewModel::onPaymentMethodChange,
                                        onQuantityChange = storeViewModel::changeQuantity,
                                        onSubmit = storeViewModel::submitOrder,
                                        onBack = {
                                            backStack.removeLastOrNull()
                                        },
                                    )
                                }

                                is StoreNavKey.Confirmation -> {
                                    checkoutState.receipt?.let { receipt ->
                                        ConfirmationScreen(
                                            receipt = receipt,
                                            onDone = {
                                                while (backStack.size > 1) {
                                                    backStack.removeLastOrNull()
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    BackHandler(
                        enabled = backStack.size > 1,
                    ) {
                        backStack.removeLastOrNull()
                    }
                }
            }
        }
    }
}
