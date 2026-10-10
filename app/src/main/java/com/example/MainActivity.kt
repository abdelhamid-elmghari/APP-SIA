package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.state.ActiveScreen
import com.example.state.TimizarViewModel
import com.example.ui.components.TimizarBottomNavigation
import com.example.ui.screens.*
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TimizarTheme
import com.example.ui.theme.WarmCanvas

class MainActivity : ComponentActivity() {

    private val viewModel: TimizarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            com.google.android.gms.maps.MapsInitializer.initialize(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Google Maps SDK initialization error", e)
        }
        enableEdgeToEdge()
        setContent {
            TimizarTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.toastMessage) {
                    uiState.toastMessage?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                BackHandler(enabled = uiState.navigationStack.size > 1) {
                    viewModel.navigateBack()
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(WarmCanvas),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    floatingActionButton = {
                        val showFab = uiState.currentScreen !in listOf(
                            ActiveScreen.AI_ASSISTANT,
                            ActiveScreen.AUTH,
                            ActiveScreen.ONBOARDING,
                            ActiveScreen.PRODUCT_3D_VIEWER
                        )
                        if (showFab) {
                            ExtendedFloatingActionButton(
                                onClick = { viewModel.navigateTo(ActiveScreen.AI_ASSISTANT) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "ASK SIA",
                                        tint = Color.White
                                    )
                                },
                                text = {
                                    Text(
                                        text = "ASK SIA",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                containerColor = Terracotta,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(24.dp),
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .testTag("floating_ask_sia_launcher")
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    bottomBar = {
                        // Keep bottom nav visible across primary explorer tabs
                        if (uiState.currentScreen in listOf(
                                ActiveScreen.HOME,
                                ActiveScreen.EXPLORE,
                                ActiveScreen.EXPERIENCES,
                                ActiveScreen.LOCAL_MARKET,
                                ActiveScreen.TRIP_PLANNER,
                                ActiveScreen.COMMUNITY
                            )
                        ) {
                            TimizarBottomNavigation(
                                currentScreen = uiState.currentScreen,
                                onTabSelected = { screen -> viewModel.navigateTo(screen) }
                            )
                        }
                    }
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)

                    when (uiState.currentScreen) {
                        ActiveScreen.HOME -> HomeScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.EXPLORE -> ExploreScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.EXPERIENCES -> ExperiencesScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.LOCAL_MARKET -> LocalMarketScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.PRODUCT_DETAIL -> ProductDetailScreen(
                            productId = uiState.selectedProductId,
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                        ActiveScreen.SELLER_DASHBOARD -> SellerDashboardScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                        ActiveScreen.ADD_EDIT_PRODUCT -> AddEditProductScreen(
                            productId = uiState.editingProductId,
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                        ActiveScreen.PRODUCT_3D_VIEWER -> {
                            val prod = uiState.active3dViewerProduct
                            if (prod != null) {
                                Product3dViewerScreen(
                                    product = prod,
                                    viewModel = viewModel,
                                    modifier = screenModifier
                                )
                            } else {
                                LocalMarketScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                            }
                        }
                        ActiveScreen.DESTINATION_DETAIL -> DestinationDetailScreen(
                            destinationId = uiState.selectedPlaceId,
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                        ActiveScreen.SERVICE_DETAIL -> ServiceDetailScreen(
                            serviceId = uiState.selectedServiceId,
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                        ActiveScreen.SEARCH -> UnifiedSearchScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.MAP -> InteractiveMapScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.TRIP_PLANNER -> TripPlannerScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.TRANSPORT_HUB -> TransportationHubScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.BOOKINGS, ActiveScreen.PARTNER_DASHBOARD -> BookingManagementScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                        ActiveScreen.COMMUNITY -> CommunityScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.PASSPORT -> DigitalPassportScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.OFFLINE_CENTER -> OfflineCenterScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.ROAD_INFO -> RoadInformationScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.AI_ASSISTANT -> AiAssistantScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.ONBOARDING -> WelcomeOnboardingScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        ActiveScreen.AUTH -> AuthScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                        else -> HomeScreen(uiState = uiState, viewModel = viewModel, modifier = screenModifier)
                    }
                }
            }
        }
    }
}
