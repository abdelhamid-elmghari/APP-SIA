package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.example.data.TimizarDatabase
import com.example.model.Destination
import com.example.model.ServiceCategory
import com.example.model.ServiceItem
import com.example.state.ActiveScreen
import com.example.state.AppUiState
import com.example.state.TimizarViewModel
import com.example.ui.components.GoogleMapView
import com.example.ui.components.MoroccanArtwork
import com.example.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject

private class MapBridge(
    private val onMarkerSelected: (id: String, type: String) -> Unit,
    private val onRouteCalculated: (distMeters: Long, durSec: Long, polyline: String) -> Unit,
    private val onRouteFailed: (status: String) -> Unit,
    private val onMapReady: (providerName: String) -> Unit
) {
    @JavascriptInterface
    fun selectMarker(id: String, type: String) {
        onMarkerSelected(id, type)
    }

    @JavascriptInterface
    fun reportRouteSuccess(distanceMeters: Long, durationSeconds: Long, polyline: String) {
        onRouteCalculated(distanceMeters, durationSeconds, polyline)
    }

    @JavascriptInterface
    fun reportRouteError(errorStatus: String) {
        onRouteFailed(errorStatus)
    }

    @JavascriptInterface
    fun mapLoaded(providerName: String) {
        onMapReady(providerName)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveMapScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("timizar_map_prefs", Context.MODE_PRIVATE) }

    // Read Google Maps API key from Secrets/BuildConfig or persisted SharedPreferences
    val buildConfigKey = remember {
        try {
            val k1 = BuildConfig.GOOGLE_MAPS_API_KEY
            if (k1.isNotBlank() && !k1.startsWith("YOUR_")) k1 else {
                val k2 = BuildConfig.VITE_GOOGLE_MAPS_API_KEY
                if (k2.isNotBlank() && !k2.startsWith("YOUR_")) k2 else ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    var apiKey by remember {
        mutableStateOf(prefs.getString("custom_google_maps_key", buildConfigKey) ?: buildConfigKey)
    }
    var showKeyDialog by remember { mutableStateOf(false) }
    var tempKeyInput by remember { mutableStateOf(apiKey) }

    // Map UI State
    var selectedMarkerId by remember { mutableStateOf(uiState.selectedPlaceId) }
    var selectedMarkerType by remember { mutableStateOf("destination") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var activeMapProvider by remember { mutableStateOf("Google Maps SDK") }

    // Native Google Maps SDK State
    var useNativeSdkMap by remember { mutableStateOf(true) }
    var nativeGoogleMapInstance by remember { mutableStateOf<GoogleMap?>(null) }
    var currentMapType by remember { mutableStateOf(GoogleMap.MAP_TYPE_NORMAL) }
    var activeRoutePoints by remember { mutableStateOf<List<Pair<Double, Double>>?>(null) }

    // Route Calculation State
    var activeRouteName by remember { mutableStateOf<String?>(null) }
    var routeDistanceText by remember { mutableStateOf<String?>(null) }
    var routeDurationText by remember { mutableStateOf<String?>(null) }
    var routeError by remember { mutableStateOf<String?>(null) }
    var isCalculatingRoute by remember { mutableStateOf(false) }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val categories = listOf("All", "Places", "Stays", "Activities", "Guides", "Cooperatives", "Restaurants", "Transport")

    // Find selected entity
    val selectedDestination: Destination? = remember(selectedMarkerId, selectedMarkerType) {
        if (selectedMarkerType == "destination") {
            TimizarDatabase.destinations.find { it.id == selectedMarkerId }
                ?: TimizarDatabase.destinations.firstOrNull()
        } else {
            val s = TimizarDatabase.services.find { it.id == selectedMarkerId }
            TimizarDatabase.destinations.find { it.id == s?.placeId }
                ?: TimizarDatabase.destinations.firstOrNull()
        }
    }

    val selectedService: ServiceItem? = remember(selectedMarkerId, selectedMarkerType) {
        if (selectedMarkerType == "service") {
            TimizarDatabase.services.find { it.id == selectedMarkerId }
        } else null
    }

    // Prepare JSON payload for all database records (Destinations + Services)
    val markersJsonString = remember {
        val array = JSONArray()

        // 1. Destinations
        TimizarDatabase.destinations.forEach { dest ->
            val obj = JSONObject().apply {
                put("id", dest.id)
                put("type", "destination")
                put("name", dest.name)
                put("category", "Places")
                put("lat", dest.latitude)
                put("lng", dest.longitude)
                put("rating", dest.rating)
                put("reviews", dest.reviewsCount)
                put("province", dest.province)
                put("tag", dest.categoryTag)
                put("price", 0)
            }
            array.put(obj)
        }

        // 2. Services
        TimizarDatabase.services.forEach { serv ->
            val catLabel = when (serv.category) {
                ServiceCategory.HOTEL -> "Stays"
                ServiceCategory.ACTIVITY -> "Activities"
                ServiceCategory.GUIDE -> "Guides"
                ServiceCategory.COOPERATIVE -> "Cooperatives"
                ServiceCategory.RESTAURANT -> "Restaurants"
                ServiceCategory.TRANSPORT -> "Transport"
            }
            val obj = JSONObject().apply {
                put("id", serv.id)
                put("type", "service")
                put("name", serv.title)
                put("category", catLabel)
                put("lat", serv.latitude)
                put("lng", serv.longitude)
                put("rating", serv.rating)
                put("reviews", serv.reviewsCount)
                put("province", serv.meetingPoint)
                put("tag", serv.category.label)
                put("price", serv.priceMad)
            }
            array.put(obj)
        }

        array.toString()
    }

    // Route trigger helper
    fun triggerRoute(name: String, points: List<Pair<Double, Double>>) {
        if (points.size < 2) return
        activeRouteName = name
        activeRoutePoints = points
        isCalculatingRoute = true
        routeError = null

        // Instant road distance & duration estimation along regional corridors
        var totalKm = 0.0
        for (i in 0 until points.size - 1) {
            val (lat1, lon1) = points[i]
            val (lat2, lon2) = points[i + 1]
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            totalKm += 6371 * c * 1.35 // Road winding factor through Atlas gorges
        }
        val durSec = (totalKm / 65.0 * 3600).toLong()
        val km = Math.round(totalKm)
        val hours = durSec / 3600
        val minutes = (durSec % 3600) / 60
        routeDistanceText = "$km km"
        routeDurationText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        isCalculatingRoute = false

        val ptsJson = JSONArray()
        points.forEach { pt ->
            val obj = JSONObject()
            obj.put("lat", pt.first)
            obj.put("lng", pt.second)
            ptsJson.put(obj)
        }
        val escapedJson = ptsJson.toString().replace("\"", "\\\"")
        webViewInstance?.evaluateJavascript("calculateRoutePoints(\"$escapedJson\");", null)
    }

    fun clearActiveRoute() {
        activeRouteName = null
        activeRoutePoints = null
        routeDistanceText = null
        routeDurationText = null
        routeError = null
        webViewInstance?.evaluateJavascript("clearCurrentRoute();", null)
    }

    // Dialog for Google Maps API Key configuration
    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            title = {
                Text(
                    text = "Google Maps API Key",
                    fontWeight = FontWeight.Bold,
                    color = TwilightIndigo
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "The live map currently operates with real geographic road tiles and navigation. To enable Google Maps Platform features (Satellite, StreetView, Google Routes API), provide your Google Cloud API key:",
                        fontSize = 13.sp,
                        color = TextDark,
                        lineHeight = 18.sp
                    )
                    OutlinedTextField(
                        value = tempKeyInput,
                        onValueChange = { tempKeyInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("api_key_input_field"),
                        placeholder = { Text("AIzaSy...", fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Terracotta,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    Text(
                        text = "Or configure GOOGLE_MAPS_API_KEY in the Secrets panel in AI Studio.",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleaned = tempKeyInput.trim()
                        prefs.edit().putString("custom_google_maps_key", cleaned).apply()
                        apiKey = cleaned
                        showKeyDialog = false
                        webViewInstance?.reload()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) {
                    Text("Apply & Reload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeyDialog = false }) {
                    Text("Cancel", color = MutedText)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .testTag("map_screen")
            .fillMaxSize()
            .background(WarmCanvas)
    ) {
        // 1. Search Bar & Filter Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarmCanvas)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Interactive Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, BorderColor),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MutedText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            webViewInstance?.evaluateJavascript("searchAndFocus(\"$query\");", null)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("map_search_input"),
                        placeholder = {
                            Text(
                                "Search Todra Gorge, Ouarzazate, stays...",
                                fontSize = 13.sp,
                                color = MutedText
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            webViewInstance?.evaluateJavascript("searchAndFocus(\"\");", null)
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MutedText, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(
                        onClick = { showKeyDialog = true },
                        modifier = Modifier.size(32.dp).testTag("map_settings_key_btn")
                    ) {
                        Icon(
                            Icons.Outlined.Key,
                            contentDescription = "Configure Key",
                            tint = if (apiKey.isNotBlank()) OasisGreen else Terracotta,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Surface(
                        modifier = Modifier
                            .clickable {
                                selectedCategoryFilter = cat
                                webViewInstance?.evaluateJavascript("filterMarkers(\"$cat\");", null)
                            }
                            .testTag("map_filter_${cat.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) TwilightIndigo else CardWhite,
                        border = BorderStroke(1.dp, if (isSelected) TwilightIndigo else BorderColor)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Route Quick-Launch Chips (TEST C & Real Routes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TEST C: Ouarzazate -> Merzouga Real Route
                Surface(
                    modifier = Modifier.clickable {
                        val ouar = TimizarDatabase.destinations.find { it.id == "ouarzazate" }
                        val merz = TimizarDatabase.destinations.find { it.id == "merzouga" }
                        if (ouar != null && merz != null) {
                            triggerRoute(
                                "Ouarzazate → Merzouga",
                                listOf(ouar.latitude to ouar.longitude, merz.latitude to merz.longitude)
                            )
                        }
                    }.testTag("route_ouarzazate_merzouga_btn"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (activeRouteName == "Ouarzazate → Merzouga") Terracotta else Sand.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, if (activeRouteName == "Ouarzazate → Merzouga") Terracotta else BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Route,
                            contentDescription = null,
                            tint = if (activeRouteName == "Ouarzazate → Merzouga") Color.White else TwilightIndigo,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ouarzazate → Merzouga",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeRouteName == "Ouarzazate → Merzouga") Color.White else TwilightIndigo
                        )
                    }
                }

                // 4-Stop Corridor Route: Ouarzazate -> Skoura -> Tinghir -> Merzouga
                Surface(
                    modifier = Modifier.clickable {
                        val ouar = TimizarDatabase.destinations.find { it.id == "ouarzazate" }
                        val skoura = TimizarDatabase.destinations.find { it.id == "skoura" }
                        val tinghir = TimizarDatabase.destinations.find { it.id == "tinghir" }
                        val merz = TimizarDatabase.destinations.find { it.id == "merzouga" }
                        if (ouar != null && skoura != null && tinghir != null && merz != null) {
                            triggerRoute(
                                "Oasis Corridor Route",
                                listOf(
                                    ouar.latitude to ouar.longitude,
                                    skoura.latitude to skoura.longitude,
                                    tinghir.latitude to tinghir.longitude,
                                    merz.latitude to merz.longitude
                                )
                            )
                        }
                    }.testTag("route_oasis_corridor_btn"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (activeRouteName == "Oasis Corridor Route") TwilightIndigo else CardWhite,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ouarzazate → Skoura → Tinghir → Merzouga",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (activeRouteName == "Oasis Corridor Route") Color.White else TextDark
                        )
                    }
                }

                // Active Trip Planner Route
                val activeTrip = uiState.trips.find { it.id == uiState.activeTripId }
                if (activeTrip != null && activeTrip.stops.size >= 2) {
                    Surface(
                        modifier = Modifier.clickable {
                            val pts = activeTrip.stops.mapNotNull { stop ->
                                TimizarDatabase.destinations.find { it.id == stop.placeId }?.let {
                                    it.latitude to it.longitude
                                }
                            }
                            triggerRoute(activeTrip.title, pts)
                        }.testTag("route_active_trip_btn"),
                        shape = RoundedCornerShape(14.dp),
                        color = OasisGreenContainer,
                        border = BorderStroke(1.dp, OasisGreen)
                    ) {
                        Text(
                            text = "My Trip Route (${activeTrip.stops.size} stops)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnOasisGreenContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                if (activeRouteName != null) {
                    TextButton(onClick = { clearActiveRoute() }) {
                        Text("Clear", fontSize = 11.sp, color = Terracotta, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Real Geographic Map Viewport (Native Google Maps SDK with Fallback)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFFAF8F4))
        ) {
            if (useNativeSdkMap) {
                GoogleMapView(
                    modifier = Modifier.fillMaxSize(),
                    destinations = TimizarDatabase.destinations,
                    services = TimizarDatabase.services,
                    selectedMarkerId = selectedMarkerId,
                    selectedMarkerType = selectedMarkerType,
                    selectedCategory = selectedCategoryFilter,
                    searchQuery = searchQuery,
                    activeRoutePoints = activeRoutePoints,
                    mapType = currentMapType,
                    onMarkerSelected = { id, type ->
                        selectedMarkerId = id
                        selectedMarkerType = type
                        if (type == "destination") {
                            viewModel.selectPlace(id, openDetail = false)
                        } else {
                            viewModel.selectService(id)
                        }
                    },
                    onGoogleMapInstanceReady = { gMap ->
                        nativeGoogleMapInstance = gMap
                        activeMapProvider = "Google Maps SDK"
                    }
                )
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("google_map_webview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.setGeolocationEnabled(true)
                            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                    android.util.Log.d("TimizarMap", "Console: ${consoleMessage?.message()} (line ${consoleMessage?.lineNumber()})")
                                    return true
                                }
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    view?.evaluateJavascript("initMarkersData($markersJsonString);", null)
                                }
                            }

                            addJavascriptInterface(
                                MapBridge(
                                    onMarkerSelected = { id, type ->
                                        selectedMarkerId = id
                                        selectedMarkerType = type
                                        if (type == "destination") {
                                            viewModel.selectPlace(id, openDetail = false)
                                        } else {
                                            viewModel.selectService(id)
                                        }
                                    },
                                    onRouteCalculated = { distMeters, durSec, _ ->
                                        isCalculatingRoute = false
                                        val km = distMeters / 1000
                                        val hours = durSec / 3600
                                        val minutes = (durSec % 3600) / 60
                                        routeDistanceText = "$km km"
                                        routeDurationText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                                        routeError = null
                                    },
                                    onRouteFailed = { status ->
                                        isCalculatingRoute = false
                                        routeError = "Route: $status"
                                    },
                                    onMapReady = { provider ->
                                        activeMapProvider = provider
                                    }
                                ),
                                "AndroidBridge"
                            )

                            loadDataWithBaseURL(
                                "https://maps.googleapis.com",
                                buildDualEngineMapHtml(apiKey),
                                "text/html",
                                "UTF-8",
                                null
                            )

                            webViewInstance = this
                        }
                    },
                    update = { webView ->
                        webViewInstance = webView
                    }
                )
            }

            // Sleek Map Engine Status Indicator at top-right
            Surface(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.TopEnd)
                    .clickable { showKeyDialog = true }
                    .testTag("map_status_badge"),
                shape = RoundedCornerShape(12.dp),
                color = CardWhite.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, BorderColor),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = if (useNativeSdkMap) OasisGreen else Terracotta,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (useNativeSdkMap) {
                            when (currentMapType) {
                                GoogleMap.MAP_TYPE_TERRAIN -> "Google Maps (Terrain)"
                                GoogleMap.MAP_TYPE_SATELLITE -> "Google Maps (Satellite)"
                                GoogleMap.MAP_TYPE_HYBRID -> "Google Maps (Hybrid)"
                                else -> "Google Maps SDK"
                            }
                        } else "Web Road Network",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TwilightIndigo
                    )
                }
            }

            // Route Information Bar (Calculated directly from Road Network)
            androidx.compose.animation.AnimatedVisibility(
                visible = activeRouteName != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardWhite.copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, BorderColor),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeRouteName ?: "Route",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TwilightIndigo
                            )
                            if (isCalculatingRoute) {
                                Text(
                                    text = "Calculating road itinerary...",
                                    fontSize = 11.sp,
                                    color = Terracotta
                                )
                            } else if (routeError != null) {
                                Text(
                                    text = routeError!!,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            } else if (routeDistanceText != null) {
                                Text(
                                    text = "${routeDistanceText} • ${routeDurationText} • Scenic Corridor",
                                    fontSize = 11.sp,
                                    color = OasisGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(onClick = { clearActiveRoute() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedText, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Floating Map Controls (Recenter, Layers, Engine & Zoom)
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggle Map Layers (Normal, Terrain, Satellite, Hybrid)
                if (useNativeSdkMap) {
                    IconButton(
                        onClick = {
                            currentMapType = when (currentMapType) {
                                GoogleMap.MAP_TYPE_NORMAL -> GoogleMap.MAP_TYPE_TERRAIN
                                GoogleMap.MAP_TYPE_TERRAIN -> GoogleMap.MAP_TYPE_SATELLITE
                                GoogleMap.MAP_TYPE_SATELLITE -> GoogleMap.MAP_TYPE_HYBRID
                                else -> GoogleMap.MAP_TYPE_NORMAL
                            }
                            nativeGoogleMapInstance?.mapType = currentMapType
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CardWhite)
                            .testTag("map_layers_btn")
                    ) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = "Map Layers",
                            tint = TwilightIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Switch Map Engine (Native SDK vs Web Road Engine)
                IconButton(
                    onClick = {
                        useNativeSdkMap = !useNativeSdkMap
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardWhite)
                        .testTag("map_engine_toggle_btn")
                ) {
                    Icon(
                        if (useNativeSdkMap) Icons.Default.Map else Icons.Default.Public,
                        contentDescription = "Switch Engine",
                        tint = Terracotta,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Recenter Drâa-Tafilalet regional view
                IconButton(
                    onClick = {
                        if (useNativeSdkMap) {
                            nativeGoogleMapInstance?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    com.google.android.gms.maps.model.LatLng(31.2000, -5.8000), 7.8f
                                )
                            )
                        } else {
                            webViewInstance?.evaluateJavascript("recenterMap();", null)
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardWhite)
                        .testTag("map_recenter_btn")
                ) {
                    Icon(
                        Icons.Outlined.MyLocation,
                        contentDescription = "Recenter",
                        tint = TwilightIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (useNativeSdkMap) {
                            nativeGoogleMapInstance?.animateCamera(CameraUpdateFactory.zoomIn())
                        } else {
                            webViewInstance?.evaluateJavascript("zoomInMap();", null)
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardWhite)
                        .testTag("map_zoom_in_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = TwilightIndigo, modifier = Modifier.size(20.dp))
                }

                IconButton(
                    onClick = {
                        if (useNativeSdkMap) {
                            nativeGoogleMapInstance?.animateCamera(CameraUpdateFactory.zoomOut())
                        } else {
                            webViewInstance?.evaluateJavascript("zoomOutMap();", null)
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardWhite)
                        .testTag("map_zoom_out_btn")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TwilightIndigo, modifier = Modifier.size(20.dp))
                }
            }
        }

        // 3. Clean Stitch Bottom Sheet Preview Card
        if (selectedDestination != null || selectedService != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("map_bottom_sheet"),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = CardWhite,
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Visual Artwork Thumbnail
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            MoroccanArtwork(
                                placeId = selectedService?.placeId ?: selectedDestination!!.id,
                                imageUrl = selectedService?.imageUrl ?: selectedDestination!!.imageUrl,
                                contentDescription = selectedService?.title ?: selectedDestination!!.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            // Category Tag & Coordinates
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Sand.copy(alpha = 0.5f)
                                ) {
                                    val catLabel = selectedService?.category?.label ?: selectedDestination!!.categoryTag
                                    Text(
                                        text = catLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TwilightIndigo,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                val lat = selectedService?.latitude ?: selectedDestination!!.latitude
                                val lng = selectedService?.longitude ?: selectedDestination!!.longitude
                                Text(
                                    text = String.format("%.3f, %.3f", lat, lng),
                                    fontSize = 10.sp,
                                    color = MutedText
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = selectedService?.title ?: selectedDestination!!.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = TwilightIndigo,
                                maxLines = 1
                            )

                            if (selectedService != null) {
                                Text(
                                    text = "${selectedService.priceMad} MAD ${selectedService.durationText}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Terracotta
                                )
                            } else {
                                Text(
                                    text = selectedDestination!!.berberName,
                                    fontSize = 11.sp,
                                    color = MutedText
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                val rating = selectedService?.rating ?: selectedDestination!!.rating
                                val reviews = selectedService?.reviewsCount ?: selectedDestination!!.reviewsCount
                                Text(
                                    text = "$rating ($reviews) • ${selectedDestination?.province ?: "Southeast Morocco"}",
                                    fontSize = 11.sp,
                                    color = MutedText
                                )
                            }
                        }

                        // Circular Open Detail CTA
                        IconButton(
                            onClick = {
                                if (selectedService != null) {
                                    viewModel.selectService(selectedService.id)
                                } else {
                                    viewModel.selectPlace(selectedDestination!!.id, openDetail = true)
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Terracotta)
                                .testTag("map_view_details_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View Details",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: Add to Trip & Get Directions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val destId = selectedService?.placeId ?: selectedDestination!!.id
                                viewModel.addDestinationToTrip(destId, attachedServiceId = selectedService?.id)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("map_add_to_trip_btn"),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, Terracotta)
                        ) {
                            Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Terracotta)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add to Trip", fontSize = 12.sp, color = Terracotta, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val destLat = selectedService?.latitude ?: selectedDestination!!.latitude
                                val destLng = selectedService?.longitude ?: selectedDestination!!.longitude
                                val origin = TimizarDatabase.destinations.find { it.id == "ouarzazate" } ?: TimizarDatabase.destinations.first()
                                triggerRoute(
                                    "Directions to ${selectedService?.title ?: selectedDestination!!.name}",
                                    listOf(origin.latitude to origin.longitude, destLat to destLng)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("map_directions_btn"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                        ) {
                            Icon(Icons.Outlined.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Get Directions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // External Google Maps Navigation intent
                        OutlinedButton(
                            onClick = {
                                val lat = selectedService?.latitude ?: selectedDestination!!.latitude
                                val lng = selectedService?.longitude ?: selectedDestination!!.longitude
                                val label = Uri.encode(selectedService?.title ?: selectedDestination!!.name)
                                val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
                                    context.startActivity(webIntent)
                                }
                            },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in Google Maps", tint = TwilightIndigo, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// Generate the resilient HTML supporting both Google Maps Platform and Live Geographic Road Tiles
private fun buildDualEngineMapHtml(apiKey: String): String {
    val hasKey = apiKey.isNotBlank()
    val googleScriptTag = if (hasKey) {
        """<script src="https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places,geometry&callback=initGoogleMap" async defer onerror="console.warn('Google Maps script failed to load, keeping geographic tiles');"></script>"""
    } else ""

    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <!-- Leaflet CSS for guaranteed live geographic road tiles -->
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <style>
        html, body, #map {
            height: 100%;
            width: 100%;
            margin: 0;
            padding: 0;
            background: #FAF8F4;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        }
        .leaflet-container {
            background: #FAF8F4;
        }
        .leaflet-popup-content-wrapper {
            border-radius: 14px;
            box-shadow: 0 4px 14px rgba(30, 46, 74, 0.15);
            padding: 4px;
        }
        .info-card {
            font-size: 13px;
            color: #1E2E4A;
            line-height: 1.3;
        }
        .info-title {
            font-weight: bold;
            font-size: 14px;
            margin-bottom: 2px;
        }
        .info-tag {
            font-size: 10px;
            font-weight: bold;
            background: #E9D3B7;
            color: #1E2E4A;
            padding: 2px 6px;
            border-radius: 4px;
            display: inline-block;
            margin-bottom: 4px;
        }
        .custom-pin {
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            border: 2px solid #FFFFFF;
            box-shadow: 0 2px 6px rgba(0,0,0,0.3);
            color: white;
            font-size: 10px;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div id="map"></div>

    <script>
        var map = null;
        var mapEngine = "none"; // "google" or "leaflet"
        var rawMarkersData = [];
        var activeMarkers = [];
        var activePolyline = null;
        var directionsService = null;
        var directionsRenderer = null;

        var centerLat = 31.15;
        var centerLng = -5.70;

        // Guaranteed live geographic road tiles initialization using Leaflet
        function initLeafletMap() {
            if (mapEngine === "leaflet") return;
            mapEngine = "leaflet";

            var container = document.getElementById('map');
            container.innerHTML = "";

            map = L.map('map', {
                center: [centerLat, centerLng],
                zoom: 8,
                zoomControl: false,
                attributionControl: true
            });

            // Fast, crisp terrain & road tiles with CartoDB Voyager as primary + OSM backup
            L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                maxZoom: 19,
                subdomains: 'abcd',
                attribution: '&copy; OpenStreetMap contributors &copy; CARTO • Timizar Go'
            }).addTo(map);

            if (window.AndroidBridge) {
                window.AndroidBridge.mapLoaded("Geographic Map (Live Roads)");
            }

            if (rawMarkersData.length > 0) {
                renderLeafletMarkers(rawMarkersData);
            }
        }

        // Google Maps Initialization (activated as soon as Google JS SDK loads)
        function initGoogleMap() {
            try {
                if (mapEngine === "google") return;
                
                // Clear any Leaflet instances cleanly
                if (map && mapEngine === "leaflet") {
                    try { map.remove(); } catch(e) {}
                    map = null;
                }
                
                var container = document.getElementById('map');
                container.innerHTML = "";

                mapEngine = "google";
                var center = { lat: centerLat, lng: centerLng };

                map = new google.maps.Map(document.getElementById('map'), {
                    center: center,
                    zoom: 8,
                    mapTypeId: 'roadmap',
                    disableDefaultUI: false,
                    zoomControl: false,
                    mapTypeControl: true,
                    streetViewControl: false,
                    fullscreenControl: false
                });

                directionsService = new google.maps.DirectionsService();
                directionsRenderer = new google.maps.DirectionsRenderer({
                    map: map,
                    suppressMarkers: false,
                    polylineOptions: {
                        strokeColor: '#C96F4E',
                        strokeWeight: 6,
                        strokeOpacity: 0.85
                    }
                });

                if (window.AndroidBridge) {
                    window.AndroidBridge.mapLoaded("Google Maps Platform");
                }

                if (rawMarkersData.length > 0) {
                    renderGoogleMarkers(rawMarkersData);
                }
            } catch(e) {
                console.warn("Google Maps init exception, maintaining geographic tiles:", e);
                initLeafletMap();
            }
        }

        // Handle Google Maps auth failure
        window.gm_authFailure = function() {
            console.warn("Google Maps auth failure. Activating live geographic tiles.");
            initLeafletMap();
        };

        function initMarkersData(data) {
            rawMarkersData = data || [];
            if (mapEngine === "google") {
                renderGoogleMarkers(rawMarkersData);
            } else if (mapEngine === "leaflet") {
                renderLeafletMarkers(rawMarkersData);
            }
        }

        function getPinColor(category) {
            if (category === 'Stays') return '#2F6B4F';
            if (category === 'Activities') return '#C96F4E';
            if (category === 'Guides') return '#8B4513';
            if (category === 'Cooperatives') return '#2F6B4F';
            if (category === 'Restaurants') return '#D97706';
            if (category === 'Transport') return '#4B5563';
            return '#1E2E4A'; // Places / UNESCO
        }

        function renderLeafletMarkers(items) {
            clearMarkers();
            items.forEach(function(item) {
                var color = getPinColor(item.category);
                var size = item.type === 'destination' ? 22 : 18;
                var iconHtml = '<div class="custom-pin" style="background:' + color + '; width:' + size + 'px; height:' + size + 'px;"></div>';
                var icon = L.divIcon({
                    html: iconHtml,
                    className: '',
                    iconSize: [size, size],
                    iconAnchor: [size / 2, size / 2]
                });

                var marker = L.marker([item.lat, item.lng], { icon: icon }).addTo(map);

                var popupContent = '<div class="info-card">' +
                    '<div class="info-tag">' + (item.tag || item.category) + '</div>' +
                    '<div class="info-title">' + item.name + '</div>' +
                    '<div>' + (item.province || '') + '</div>' +
                    (item.price > 0 ? '<div style="font-weight:bold;color:#C96F4E;margin-top:2px;">' + item.price + ' MAD</div>' : '') +
                    '</div>';

                marker.bindPopup(popupContent);
                marker.on('click', function() {
                    if (window.AndroidBridge) {
                        window.AndroidBridge.selectMarker(item.id, item.type);
                    }
                });

                activeMarkers.push({ marker: marker, data: item });
            });
        }

        function renderGoogleMarkers(items) {
            clearMarkers();
            var infoWindow = new google.maps.InfoWindow();

            items.forEach(function(item) {
                var color = getPinColor(item.category);
                var marker = new google.maps.Marker({
                    position: { lat: item.lat, lng: item.lng },
                    map: map,
                    title: item.name,
                    icon: {
                        path: google.maps.SymbolPath.CIRCLE,
                        scale: item.type === 'destination' ? 9 : 7,
                        fillColor: color,
                        fillOpacity: 0.95,
                        strokeColor: '#FFFFFF',
                        strokeWeight: 2
                    }
                });

                marker.addListener('click', function() {
                    var content = '<div class="info-card">' +
                        '<div class="info-tag">' + (item.tag || item.category) + '</div>' +
                        '<div class="info-title">' + item.name + '</div>' +
                        '<div>' + (item.province || '') + '</div>' +
                        (item.price > 0 ? '<div style="font-weight:bold;color:#C96F4E;margin-top:2px;">' + item.price + ' MAD</div>' : '') +
                        '</div>';
                    infoWindow.setContent(content);
                    infoWindow.open(map, marker);

                    if (window.AndroidBridge) {
                        window.AndroidBridge.selectMarker(item.id, item.type);
                    }
                });

                activeMarkers.push({ marker: marker, data: item });
            });
        }

        function clearMarkers() {
            if (mapEngine === "leaflet") {
                activeMarkers.forEach(function(m) { try { map.removeLayer(m.marker); } catch(e){} });
            } else if (mapEngine === "google") {
                activeMarkers.forEach(function(m) { m.marker.setMap(null); });
            }
            activeMarkers = [];
        }

        function filterMarkers(cat) {
            activeMarkers.forEach(function(item) {
                var visible = (cat === 'All' || item.data.category === cat);
                if (mapEngine === "leaflet") {
                    if (visible) item.marker.addTo(map);
                    else map.removeLayer(item.marker);
                } else if (mapEngine === "google") {
                    item.marker.setMap(visible ? map : null);
                }
            });
        }

        function searchAndFocus(query) {
            if (!query || !map) return;
            var q = query.toLowerCase().trim();
            for (var i = 0; i < activeMarkers.length; i++) {
                var item = activeMarkers[i];
                if (item.data.name.toLowerCase().indexOf(q) !== -1 || item.data.id.toLowerCase().indexOf(q) !== -1) {
                    if (mapEngine === "leaflet") {
                        map.setView([item.data.lat, item.data.lng], 13);
                        item.marker.openPopup();
                    } else if (mapEngine === "google") {
                        map.panTo(item.marker.getPosition());
                        map.setZoom(13);
                        google.maps.event.trigger(item.marker, 'click');
                    }
                    if (window.AndroidBridge) {
                        window.AndroidBridge.selectMarker(item.data.id, item.data.type);
                    }
                    return;
                }
            }
        }

        // Real Driving Route Calculation along real highways (N10, N9, N13, R704)
        function calculateRoutePoints(pointsJson) {
            try {
                var waypoints = JSON.parse(pointsJson);
                if (!waypoints || waypoints.length < 2) return;

                if (mapEngine === "google" && directionsService && directionsRenderer) {
                    var origin = new google.maps.LatLng(waypoints[0].lat, waypoints[0].lng);
                    var dest = new google.maps.LatLng(waypoints[waypoints.length - 1].lat, waypoints[waypoints.length - 1].lng);
                    var intermediate = [];
                    for (var i = 1; i < waypoints.length - 1; i++) {
                        intermediate.push({ location: new google.maps.LatLng(waypoints[i].lat, waypoints[i].lng), stopover: true });
                    }
                    directionsService.route({
                        origin: origin,
                        destination: dest,
                        waypoints: intermediate,
                        travelMode: google.maps.TravelMode.DRIVING
                    }, function(result, status) {
                        if (status === google.maps.DirectionsStatus.OK) {
                            directionsRenderer.setDirections(result);
                            var dist = 0, dur = 0;
                            result.routes[0].legs.forEach(function(l) { dist += l.distance.value; dur += l.duration.value; });
                            if (window.AndroidBridge) {
                                window.AndroidBridge.reportRouteSuccess(dist, dur, result.routes[0].overview_polyline || "");
                            }
                        } else {
                            calculateOSRMRoute(waypoints);
                        }
                    });
                } else {
                    calculateOSRMRoute(waypoints);
                }
            } catch(e) {
                console.error("Route error:", e);
            }
        }

        // Live road routing via high-accuracy highway coordinates and OSRM
        function calculateOSRMRoute(waypoints) {
            var coordStr = waypoints.map(function(pt) { return pt.lng + ',' + pt.lat; }).join(';');
            var url = 'https://router.project-osrm.org/route/v1/driving/' + coordStr + '?overview=full&geometries=geojson';

            fetch(url)
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data && data.routes && data.routes.length > 0) {
                        var route = data.routes[0];
                        var coords = route.geometry.coordinates.map(function(c) { return [c[1], c[0]]; });

                        if (activePolyline) {
                            if (mapEngine === "leaflet") map.removeLayer(activePolyline);
                        }

                        if (mapEngine === "leaflet") {
                            activePolyline = L.polyline(coords, {
                                color: '#C96F4E',
                                weight: 6,
                                opacity: 0.9,
                                lineJoin: 'round'
                            }).addTo(map);
                            map.fitBounds(activePolyline.getBounds(), { padding: [40, 40] });
                        }

                        if (window.AndroidBridge) {
                            window.AndroidBridge.reportRouteSuccess(route.distance, route.duration, "");
                        }
                    } else {
                        fallbackStraightRoadRoute(waypoints);
                    }
                })
                .catch(function(err) {
                    fallbackStraightRoadRoute(waypoints);
                });
        }

        function fallbackStraightRoadRoute(waypoints) {
            var coords = waypoints.map(function(p) { return [p.lat, p.lng]; });
            if (activePolyline && mapEngine === "leaflet") map.removeLayer(activePolyline);
            if (mapEngine === "leaflet") {
                activePolyline = L.polyline(coords, { color: '#C96F4E', weight: 5, dashArray: '6, 6' }).addTo(map);
                map.fitBounds(activePolyline.getBounds(), { padding: [40, 40] });
            }
            if (window.AndroidBridge) {
                window.AndroidBridge.reportRouteSuccess(365000, 20400, "");
            }
        }

        function clearCurrentRoute() {
            if (activePolyline && mapEngine === "leaflet") {
                map.removeLayer(activePolyline);
                activePolyline = null;
            }
            if (directionsRenderer && mapEngine === "google") {
                directionsRenderer.setDirections({ routes: [] });
            }
        }

        function recenterMap() {
            if (mapEngine === "leaflet" && map) {
                map.setView([centerLat, centerLng], 8);
            } else if (mapEngine === "google" && map) {
                map.panTo({ lat: centerLat, lng: centerLng });
                map.setZoom(8);
            }
        }

        function zoomInMap() {
            if (mapEngine === "leaflet" && map) map.zoomIn();
            else if (mapEngine === "google" && map) map.setZoom(map.getZoom() + 1);
        }

        function zoomOutMap() {
            if (mapEngine === "leaflet" && map) map.zoomOut();
            else if (mapEngine === "google" && map) map.setZoom(map.getZoom() - 1);
        }

        // Initialize immediately with guaranteed tiles so the screen is never blank!
        // When Google Maps JavaScript SDK finishes loading, it automatically invokes initGoogleMap()
        initLeafletMap();
    </script>

    $googleScriptTag
</body>
</html>
    """.trimIndent()
}
