package com.example.gamestore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val storeViewModel: StoreViewModel = viewModel()
            val uiState by storeViewModel.uiState.collectAsStateWithLifecycle()
            val checkoutState by storeViewModel.checkoutState.collectAsStateWithLifecycle()

            val darkTheme = uiState.isDarkTheme

            if (darkTheme != null) {
                GamestoreTheme(darkTheme = darkTheme) {
                    val backStack =
                        rememberNavBackStack(StoreNavKey.Catalog)

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                    ) { innerPadding ->
                        NavDisplay(
                            backStack = backStack,
                            modifier = Modifier.padding(innerPadding),
                            onBack = {
                                if (!checkoutState.isSubmitting) {
                                    if (
                                        backStack.lastOrNull() ==
                                        StoreNavKey.Confirmation
                                    ) {
                                        while (backStack.size > 1) {
                                            backStack.removeLastOrNull()
                                        }
                                    } else {
                                        backStack.removeLastOrNull()
                                    }
                                }
                            },
                        ) { key ->
                            NavEntry(key) {
                                when (key) {
                                    is StoreNavKey.Catalog -> {
                                        CatalogScreen(
                                            products =
                                                uiState.products,
                                            searchQuery =
                                                uiState.searchQuery,
                                            isDarkTheme = darkTheme,
                                            onDarkThemeChange = storeViewModel::setDarkTheme,
                                            onProductSelected = {
                                                    productId ->
                                                backStack.add(
                                                    StoreNavKey.Detail(
                                                        productId,
                                                    )
                                                )
                                            },
                                            onToggleFavorite = {
                                                    productId ->
                                                storeViewModel
                                                    .toggleFavorite(
                                                        productId,
                                                    )
                                            },
                                            onQueryChange = { query ->
                                                storeViewModel
                                                    .onQueryChange(
                                                        query,
                                                    )
                                            },
                                            onClearQuery = {
                                                storeViewModel
                                                    .clearQuery()
                                            },
                                        ) {
                                            /* scroll arriba */
                                        }
                                    }

                                    is StoreNavKey.Detail -> {
                                        val product: GameProduct? =
                                            uiState.products.find {
                                                it.id == key.productId
                                            }

                                        product?.let {
                                            DetailScreen(
                                                product = it,
                                                onBack = {
                                                    backStack
                                                        .removeLastOrNull()
                                                },
                                                onToggleFavorite = {
                                                        productId ->
                                                    storeViewModel
                                                        .toggleFavorite(
                                                            productId,
                                                        )
                                                },
                                                onOpenProfile = {
                                                        developerId ->
                                                    backStack.add(
                                                        StoreNavKey.Profile(
                                                            developerId,
                                                        )
                                                    )
                                                },
                                            ) { productId ->
                                                storeViewModel
                                                    .startCheckout(
                                                        productId,
                                                    )
                                                backStack.add(
                                                    StoreNavKey.Checkout,
                                                )
                                            }
                                        }
                                    }

                                    is StoreNavKey.Profile -> {
                                        val profile:
                                                DeveloperProfile? =
                                            uiState.profiles.find {
                                                it.id ==
                                                        key.developerId
                                            }

                                        profile?.let {
                                            ProfileScreen(
                                                profile = it,
                                            ) {
                                                backStack
                                                    .removeLastOrNull()
                                            }
                                        }
                                    }

                                    is StoreNavKey.Checkout -> {
                                        LaunchedEffect(
                                            checkoutState.receipt?.id,
                                        ) {
                                            if (
                                                (checkoutState.receipt != null) &&
                                                (backStack.lastOrNull() == StoreNavKey.Checkout)
                                            ) {
                                                backStack.add(
                                                    StoreNavKey
                                                        .Confirmation,
                                                )
                                            }
                                        }

                                        CheckoutScreen(
                                            state = checkoutState,
                                            onFieldChange =
                                                storeViewModel::
                                                onCheckoutFieldChange,
                                            onFieldTouched =
                                                storeViewModel::
                                                onCheckoutFieldTouched,
                                            onBillingTypeChange =
                                                storeViewModel::
                                                onBillingTypeChange,
                                            onPaymentMethodChange =
                                                storeViewModel::
                                                onPaymentMethodChange,
                                            onQuantityChange =
                                                storeViewModel::
                                                changeQuantity,
                                            onSubmit =
                                                storeViewModel::
                                                submitOrder,
                                            onBack = {
                                                if (
                                                    !checkoutState
                                                        .isSubmitting
                                                ) {
                                                    backStack
                                                        .removeLastOrNull()
                                                }
                                            },
                                        )
                                    }

                                    is StoreNavKey.Confirmation -> {
                                        checkoutState.receipt?.let {
                                                receipt ->
                                            ConfirmationScreen(
                                                receipt = receipt,
                                            ) {
                                                while (
                                                    backStack.size > 1
                                                ) {
                                                    backStack
                                                        .removeLastOrNull()
                                                }
                                            }
                                        }
                                    }

                                    else -> {}
                                }
                            }
                        }

                        BackHandler(
                            enabled = backStack.size > 1,
                        ) {
                            if (!checkoutState.isSubmitting) {
                                if (
                                    backStack.lastOrNull() ==
                                    StoreNavKey.Confirmation
                                ) {
                                    while (backStack.size > 1) {
                                        backStack.removeLastOrNull()
                                    }
                                } else {
                                    backStack.removeLastOrNull()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
