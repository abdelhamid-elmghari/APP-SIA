package com.example.ui.components

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.Destination
import com.example.model.ServiceCategory
import com.example.model.ServiceItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*

/**
 * Native Google Maps SDK Viewport powered by play-services-maps.
 * Handles lifecycle events, interactive pins, camera animations, and road routing polylines.
 */
@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    destinations: List<Destination>,
    services: List<ServiceItem>,
    selectedMarkerId: String?,
    selectedMarkerType: String,
    selectedCategory: String,
    searchQuery: String,
    activeRoutePoints: List<Pair<Double, Double>>?,
    mapType: Int = GoogleMap.MAP_TYPE_NORMAL,
    onMarkerSelected: (id: String, type: String) -> Unit,
    onGoogleMapInstanceReady: (GoogleMap) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Lifecycle-aware MapView instance
    val mapView = remember {
        MapView(context).apply {
            id = android.view.View.generateViewId()
        }
    }

    var googleMapState by remember { mutableStateOf<GoogleMap?>(null) }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    // Initialize Map and UI settings
    LaunchedEffect(mapView) {
        mapView.getMapAsync { gMap ->
            googleMapState = gMap
            onGoogleMapInstanceReady(gMap)

            gMap.uiSettings.isZoomControlsEnabled = false
            gMap.uiSettings.isCompassEnabled = true
            gMap.uiSettings.isMyLocationButtonEnabled = false
            gMap.uiSettings.isMapToolbarEnabled = true

            // Initial focus: Center of Drâa-Tafilalet Region (Ouarzazate - Tinghir - Merzouga expanse)
            val regionalCenter = LatLng(31.2000, -5.8000)
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(regionalCenter, 7.8f))

            gMap.setOnMarkerClickListener { marker ->
                val tag = marker.tag as? String
                if (tag != null) {
                    val parts = tag.split(":")
                    if (parts.size == 2) {
                        onMarkerSelected(parts[1], parts[0])
                    }
                }
                false
            }
        }
    }

    // Update Map Type
    LaunchedEffect(googleMapState, mapType) {
        googleMapState?.mapType = mapType
    }

    // Update Markers & Polylines when data or filters change
    LaunchedEffect(
        googleMapState,
        destinations,
        services,
        selectedCategory,
        searchQuery,
        activeRoutePoints
    ) {
        val map = googleMapState ?: return@LaunchedEffect

        map.clear()

        // 1. Filter Destinations
        val filteredDestinations = destinations.filter { dest ->
            val matchesCategory = selectedCategory == "All" || selectedCategory == "Places"
            val matchesSearch = searchQuery.isBlank() ||
                    dest.name.contains(searchQuery, ignoreCase = true) ||
                    dest.province.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }

        filteredDestinations.forEach { dest ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(dest.latitude, dest.longitude))
                    .title(dest.name)
                    .snippet("${dest.province} • ★ ${dest.rating}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
            marker?.tag = "destination:${dest.id}"
        }

        // 2. Filter Services
        val filteredServices = services.filter { s ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Stays" -> s.category == ServiceCategory.HOTEL
                "Activities" -> s.category == ServiceCategory.ACTIVITY
                "Guides" -> s.category == ServiceCategory.GUIDE
                "Cooperatives" -> s.category == ServiceCategory.COOPERATIVE
                "Restaurants" -> s.category == ServiceCategory.RESTAURANT
                "Transport" -> s.category == ServiceCategory.TRANSPORT
                else -> false
            }
            val matchesSearch = searchQuery.isBlank() ||
                    s.title.contains(searchQuery, ignoreCase = true) ||
                    s.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }

        filteredServices.forEach { s ->
            val hue = when (s.category) {
                ServiceCategory.HOTEL -> BitmapDescriptorFactory.HUE_AZURE
                ServiceCategory.ACTIVITY -> BitmapDescriptorFactory.HUE_GREEN
                ServiceCategory.GUIDE -> BitmapDescriptorFactory.HUE_VIOLET
                ServiceCategory.COOPERATIVE -> BitmapDescriptorFactory.HUE_ROSE
                ServiceCategory.RESTAURANT -> BitmapDescriptorFactory.HUE_RED
                ServiceCategory.TRANSPORT -> BitmapDescriptorFactory.HUE_YELLOW
            }
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(s.latitude, s.longitude))
                    .title(s.title)
                    .snippet("${s.category.label} • ${s.priceMad} MAD")
                    .icon(BitmapDescriptorFactory.defaultMarker(hue))
            )
            marker?.tag = "service:${s.id}"
        }

        // 3. Render Route Polyline if present
        if (!activeRoutePoints.isNullOrEmpty() && activeRoutePoints.size >= 2) {
            val latLngList = activeRoutePoints.map { LatLng(it.first, it.second) }
            map.addPolyline(
                PolylineOptions()
                    .addAll(latLngList)
                    .color(android.graphics.Color.parseColor("#C25E00"))
                    .width(12f)
                    .geodesic(true)
            )

            // Auto-frame camera to encompass entire route
            try {
                val boundsBuilder = LatLngBounds.Builder()
                latLngList.forEach { boundsBuilder.include(it) }
                val bounds = boundsBuilder.build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
            } catch (e: Exception) {
                android.util.Log.w("GoogleMapView", "Could not animate camera bounds: ${e.message}")
            }
        }
    }

    // Camera animation when user selects a specific item
    LaunchedEffect(googleMapState, selectedMarkerId, selectedMarkerType) {
        val map = googleMapState ?: return@LaunchedEffect
        if (selectedMarkerId == null) return@LaunchedEffect

        val targetLatLng: LatLng? = if (selectedMarkerType == "destination") {
            destinations.find { it.id == selectedMarkerId }?.let { LatLng(it.latitude, it.longitude) }
        } else {
            services.find { it.id == selectedMarkerId }?.let { LatLng(it.latitude, it.longitude) }
        }

        targetLatLng?.let { pos ->
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 11.5f), 700, null)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
            .fillMaxSize()
            .testTag("native_google_map_view")
    )
}
