package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TimizarDatabase
import com.example.state.ActiveScreen
import com.example.state.AppUiState
import com.example.state.TimizarViewModel
import com.example.ui.components.MoroccanArtwork
import com.example.ui.theme.*

@Composable
fun TripPlannerScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrip = uiState.trips.find { it.id == uiState.activeTripId } ?: uiState.trips.first()
    var selectedTab by remember { mutableStateOf("Itinerary") }
    var travelersCount by remember { mutableIntStateOf(2) }
    var currencyMad by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Dialog state
    var selectedCity by remember { mutableStateOf("Ouarzazate") }
    var selectedDays by remember { mutableIntStateOf(5) }
    var selectedStyle by remember { mutableStateOf("Classic") }

    val tabs = listOf("Itinerary", "Map", "Budget")

    // AI Trip Builder Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Terracotta, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SIA AI Trip Generator", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TwilightIndigo)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Customize your journey through the provinces of Drâa-Tafilalet.",
                        fontSize = 12.sp,
                        color = MutedText
                    )

                    Text("Starting Point:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("Ouarzazate", "Errachidia", "Zagora", "Tinghir")) { city ->
                            val sel = selectedCity == city
                            Surface(
                                modifier = Modifier.clickable { selectedCity = city },
                                shape = RoundedCornerShape(12.dp),
                                color = if (sel) TwilightIndigo else Sand.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = city,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sel) Color.White else TextDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Text("Duration:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 5, 7).forEach { days ->
                            val sel = selectedDays == days
                            Surface(
                                modifier = Modifier.clickable { selectedDays = days },
                                shape = RoundedCornerShape(12.dp),
                                color = if (sel) Terracotta else Sand.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = "$days Days",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sel) Color.White else TextDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Text("Travel Style:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Classic", "Sahara", "Culture").forEach { style ->
                            val sel = selectedStyle == style
                            Surface(
                                modifier = Modifier.clickable { selectedStyle = style },
                                shape = RoundedCornerShape(12.dp),
                                color = if (sel) OasisGreen else Sand.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = style,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sel) Color.White else TextDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Sand.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Estimated Budget: ${selectedDays * 750} MAD (~${selectedDays * 70} EUR) for $travelersCount travelers",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TwilightIndigo,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createCustomAiTrip(
                            title = "$selectedDays-Day $selectedStyle ($selectedCity)",
                            startingCity = selectedCity,
                            days = selectedDays,
                            style = selectedStyle,
                            budgetMad = selectedDays * 750
                        )
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) {
                    Text("Generate Plan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = MutedText)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .testTag("trip_planner_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Header with Trip Switcher & Create AI Trip CTA
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                // Trip Switcher Pills
                if (uiState.trips.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        items(uiState.trips) { tr ->
                            val isSel = tr.id == currentTrip.id
                            Surface(
                                modifier = Modifier.clickable { viewModel.selectActiveTrip(tr.id) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSel) TwilightIndigo else CardWhite,
                                border = BorderStroke(1.dp, if (isSel) TwilightIndigo else BorderColor)
                            ) {
                                Text(
                                    text = tr.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else TextDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTrip.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TwilightIndigo,
                                fontSize = 21.sp
                            )
                        )
                        Text(
                            text = "${currentTrip.startDate} - ${currentTrip.endDate} • ${currentTrip.stops.size} Stops • Drâa-Tafilalet",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.height(38.dp).testTag("plan_new_trip_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Trip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { viewModel.toggleOfflineDownload(currentTrip.id) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CardWhite)
                        ) {
                            Icon(
                                imageVector = if (currentTrip.isDownloadedOffline) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                contentDescription = "Offline Cache",
                                tint = if (currentTrip.isDownloadedOffline) OasisGreen else TwilightIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Itinerary, Map, Budget
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedTab = tab }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = tab,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Terracotta else MutedText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Terracotta)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tab Content Switching
        when (selectedTab) {
            "Itinerary" -> {
                // Day-by-Day Timeline Stops
                items(currentTrip.stops) { stop ->
                    val place = TimizarDatabase.destinations.find { it.id == stop.placeId }

                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trip_stop_${stop.stopId}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Place Thumbnail with reliable Moroccan artwork
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    MoroccanArtwork(
                                        placeId = place?.id ?: "tinghir",
                                        imageUrl = place?.imageUrl,
                                        contentDescription = place?.name,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Sand.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = "Day ${stop.dayNumber}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Terracotta,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = place?.province ?: "Southeast Morocco",
                                            fontSize = 11.sp,
                                            color = MutedText
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = place?.name ?: "Stop Destination",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TwilightIndigo
                                    )
                                    Text(
                                        text = stop.notes,
                                        fontSize = 12.sp,
                                        color = MutedText,
                                        maxLines = 1
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.removeStopFromTrip(stop.stopId) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = MutedText, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Add Stop Action Button
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(ActiveScreen.EXPLORE) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("trip_add_stop_btn"),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, Terracotta)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Terracotta, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add stop from destinations", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Terracotta)
                        }
                    }
                }
            }

            "Map" -> {
                // Route Corridor Visualizer
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Route: N10 & N13 Scenic Corridor",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TwilightIndigo
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = OasisGreenContainer
                                    ) {
                                        Text(
                                            text = "365 km • ~5h 45m drive",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnOasisGreenContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Waypoints Connected Sequence
                                currentTrip.stops.forEachIndexed { index, stop ->
                                    val place = TimizarDatabase.destinations.find { it.id == stop.placeId }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (index == 0) TwilightIndigo else if (index == currentTrip.stops.size - 1) Terracotta else OasisGreen,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = place?.name ?: "Waypoint",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = TwilightIndigo
                                            )
                                            Text(
                                                text = "Day ${stop.dayNumber} • ${place?.distanceText ?: "Scenic stop"}",
                                                fontSize = 11.sp,
                                                color = MutedText
                                            )
                                        }
                                    }

                                    if (index < currentTrip.stops.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 13.dp)
                                                .width(2.dp)
                                                .height(20.dp)
                                                .background(BorderColor)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { viewModel.navigateTo(ActiveScreen.MAP) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open on Interactive Regional Map", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            "Budget" -> {
                // Intelligent Drâa-Tafilalet Trip Budget Estimator
                item {
                    val baseStayPerPerson = 1200
                    val baseTransportTotal = 1500
                    val baseActivityPerPerson = 500
                    val baseFoodPerPerson = 350

                    val totalTransport = baseTransportTotal
                    val totalStays = baseStayPerPerson * travelersCount
                    val totalActivities = baseActivityPerPerson * travelersCount
                    val totalFood = baseFoodPerPerson * travelersCount
                    val grandTotalMad = totalTransport + totalStays + totalActivities + totalFood
                    val grandTotalEur = grandTotalMad / 10.8

                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Estimated Trip Cost", fontSize = 12.sp, color = MutedText)
                                        Text(
                                            text = if (currencyMad) "$grandTotalMad MAD" else "≈ ${grandTotalEur.toInt()} €",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 24.sp,
                                            color = Terracotta
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = { currencyMad = !currencyMad },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(if (currencyMad) "Show EUR" else "Show MAD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = BorderColor)
                                Spacer(modifier = Modifier.height(14.dp))

                                // Travelers Count Controller
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Travelers in Group", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TwilightIndigo)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { if (travelersCount > 1) travelersCount-- },
                                            modifier = Modifier.size(32.dp).clip(CircleShape).background(WarmCanvas)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TwilightIndigo, modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = "$travelersCount Persons",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp)
                                        )
                                        IconButton(
                                            onClick = { if (travelersCount < 8) travelersCount++ },
                                            modifier = Modifier.size(32.dp).clip(CircleShape).background(WarmCanvas)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = TwilightIndigo, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Category Breakdown
                                val breakdownItems = listOf(
                                    Triple("Kasbahs & Desert Camps", "$totalStays MAD", OasisGreen),
                                    Triple("Private 4x4 Driver & Fuel", "$totalTransport MAD", Terracotta),
                                    Triple("Guides & Camel Treks", "$totalActivities MAD", TwilightIndigo),
                                    Triple("Berber Cuisine & Tea", "$totalFood MAD", AmberGold)
                                )

                                breakdownItems.forEach { (category, amount, color) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(category, fontSize = 13.sp, color = TextDark)
                                        }
                                        Text(amount, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TwilightIndigo)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransportationHubScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf("Private Driver") }
    var originCity by remember { mutableStateOf("Ouarzazate") }
    var destinationCity by remember { mutableStateOf("Merzouga") }
    var passengerCount by remember { mutableIntStateOf(2) }

    val modes = listOf("Private Driver", "Grand Taxi (Shared)", "4x4 Desert Rental")

    LazyColumn(
        modifier = modifier
            .testTag("transportation_hub_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Stitch 10. Book Transport Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Transportation Hub",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TwilightIndigo,
                        fontSize = 24.sp
                    )
                )
                Text(
                    text = "Licensed mountain drivers & desert 4x4 transfers",
                    fontSize = 12.sp,
                    color = MutedText
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Selector Pills
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(modes) { mode ->
                        val isSelected = selectedMode == mode
                        Surface(
                            modifier = Modifier.clickable { selectedMode = mode },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) OasisGreen else CardWhite,
                            border = BorderStroke(1.dp, if (isSelected) OasisGreen else BorderColor)
                        ) {
                            Text(
                                text = mode,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextDark,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Route Form Card
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TripOrigin, contentDescription = null, tint = TwilightIndigo, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Departure (Origin)", fontSize = 10.sp, color = MutedText)
                                Text(originCity, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BorderColor)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Destination", fontSize = 10.sp, color = MutedText)
                                Text(destinationCity, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sun, 13 Oct 2026", fontSize = 12.sp, color = TextDark)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("$passengerCount Passengers", fontSize = 12.sp, color = TextDark)
                            }
                        }
                    }
                }
            }
        }

        // Vehicle Offer Cards
        items(TimizarDatabase.transportOffers) { offer ->
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            MoroccanArtwork(
                                placeId = "ouarzazate",
                                imageUrl = offer.imageUrl,
                                contentDescription = offer.vehicleType,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = offer.vehicleType,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TwilightIndigo
                            )
                            Text(
                                text = "A/C • Experienced mountain driver",
                                fontSize = 11.sp,
                                color = MutedText
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${offer.priceMadPerSeat * passengerCount} MAD (~${(offer.priceMadPerSeat * passengerCount) / 10}€)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Terracotta
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.requestTransportBooking(offer.id, "2026-10-13", passengerCount)
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                            modifier = Modifier.height(44.dp).testTag("request_transport_${offer.id}")
                        ) {
                            Text("Request", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
