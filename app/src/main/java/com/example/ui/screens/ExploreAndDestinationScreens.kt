package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.TimizarDatabase
import com.example.model.ServiceCategory
import com.example.state.ActiveScreen
import com.example.state.AppUiState
import com.example.state.TimizarViewModel
import com.example.ui.components.DestinationHeroCard
import com.example.ui.components.MoroccanArtwork
import com.example.ui.components.ServiceListItemCard
import com.example.ui.components.SiaVideoPlayerCard
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("All (20)") }
    var exploreSearchQuery by remember { mutableStateOf("") }
    val categories = listOf("All (20)", "UNESCO & Kasbahs", "Sahara Dunes", "Oases & Valleys", "Gorges & Hikes")

    val filteredDestinations = TimizarDatabase.destinations.filter { dest ->
        val matchesCategory = when (selectedCategoryFilter) {
            "UNESCO & Kasbahs" -> dest.categoryTag == "UNESCO" || dest.categoryTag == "Kasbah"
            "Sahara Dunes" -> dest.categoryTag == "Desert"
            "Oases & Valleys" -> dest.categoryTag == "Oasis" || dest.id.contains("valley") || dest.id == "kelaat_mgouna"
            "Gorges & Hikes" -> dest.categoryTag == "Nature" || dest.id.contains("gorge") || dest.id.contains("tinghir")
            else -> true
        }
        val matchesSearch = exploreSearchQuery.isBlank() ||
                dest.name.contains(exploreSearchQuery, ignoreCase = true) ||
                dest.province.contains(exploreSearchQuery, ignoreCase = true) ||
                dest.description.contains(exploreSearchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .testTag("explore_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Top Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TwilightIndigo,
                        fontSize = 24.sp
                    )
                )
                Text(
                    text = "Drâa-Tafilalet (${filteredDestinations.size} Destinations)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Terracotta,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Input Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MutedText, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = exploreSearchQuery,
                            onValueChange = { exploreSearchQuery = it },
                            placeholder = { Text("Search 20 kasbahs, dunes, gorges...", fontSize = 13.sp, color = MutedText) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("explore_search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Filter Pills (Exact Stitch Explore View)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        Surface(
                            modifier = Modifier.clickable { selectedCategoryFilter = cat },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) TwilightIndigo else CardWhite,
                            border = BorderStroke(1.dp, if (isSelected) TwilightIndigo else BorderColor)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextDark,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Destinations List
        items(filteredDestinations) { dest ->
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                DestinationHeroCard(
                    destination = dest,
                    isSaved = uiState.savedPlaceIds.contains(dest.id),
                    onCardClick = { viewModel.selectPlace(dest.id) },
                    onToggleSave = { viewModel.toggleSavePlace(dest.id) }
                )
            }
        }
    }
}

@Composable
fun DestinationDetailScreen(
    destinationId: String,
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    val destination = TimizarDatabase.destinations.find { it.id == destinationId } ?: TimizarDatabase.destinations.first()
    val nearbyServices = TimizarDatabase.services.filter { it.placeId == destination.id || it.placeId == "ouarzazate" }
    val isSaved = uiState.savedPlaceIds.contains(destination.id)

    var activeHeroImage by remember(destination.id) { mutableStateOf(destination.imageUrl) }
    val galleryImages = remember(destination) {
        if (destination.galleryImages.isNotEmpty()) destination.galleryImages else listOf(destination.imageUrl)
    }

    var selectedTab by remember { mutableStateOf("Overview") }
    val tabs = listOf("Overview", "Experiences", "Nearby", "Reviews")

    LazyColumn(
        modifier = modifier
            .testTag("destination_detail_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Full Bleed Image Header (Exact Stitch Frame 03)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            ) {
                MoroccanArtwork(
                    placeId = destination.id,
                    imageUrl = activeHeroImage,
                    contentDescription = destination.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Floating Nav Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                            .testTag("detail_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconButton(
                            onClick = { viewModel.toggleSavePlace(destination.id) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f))
                                .testTag("detail_save_btn")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isSaved) Terracotta else TextDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { /* Share */ },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = TextDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Tag on Image Bottom
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TwilightIndigo.copy(alpha = 0.85f),
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        text = "${destination.categoryTag} • Drâa-Tafilalet",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Image Gallery Carousel Selector (when multiple images are available)
        if (galleryImages.size > 1) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Photo Gallery (${galleryImages.size} Photos)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(galleryImages) { imgUrl ->
                            val isSelected = activeHeroImage == imgUrl
                            Surface(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { activeHeroImage = imgUrl },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Terracotta else BorderColor
                                )
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imgUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Title & Location
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TwilightIndigo,
                        fontSize = 24.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = destination.berberName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Terracotta
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${destination.rating}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextDark
                    )
                    Text(
                        text = " (${destination.reviewsCount} reviews) • ${destination.categoryTag} • ${destination.province}",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }
        }

        // Authentic Video Player Section
        if (destination.videoUrl.isNotBlank()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SiaVideoPlayerCard(
                        videoUrl = destination.videoUrl,
                        title = destination.videoTitle.ifBlank { "${destination.name} Cinematic Video" },
                        posterUrl = destination.imageUrl,
                        durationText = destination.videoDuration,
                        isVerified = destination.hasVerifiedVideo,
                        attribution = destination.mediaAttribution
                    )
                }
            }
        }

        // Sub Tabs: Overview, Experiences, Nearby, Reviews
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
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
                                    .width(28.dp)
                                    .height(2.dp)
                                    .background(Terracotta)
                            )
                        }
                    }
                }
            }
        }

        // Key Highlights
        if (destination.highlights.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Key Highlights",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TwilightIndigo
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        destination.highlights.take(3).forEach { hl ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Sand.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = OasisGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = hl,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Description Body
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = destination.longDescription.ifBlank { destination.description },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextDark,
                        lineHeight = 21.sp,
                        fontSize = 14.sp
                    )
                )
            }
        }

        // Cultural Tip Card
        if (destination.culturalTip.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmCanvas),
                    border = BorderStroke(1.dp, AmberGold)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Authentic Local Etiquette",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TwilightIndigo
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = destination.culturalTip,
                                fontSize = 12.sp,
                                color = TextDark,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }

        // Travel Meta Details (Distance by car & Opening hours)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TwilightIndigo, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(destination.distanceText, fontSize = 12.sp, color = TextDark)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = TwilightIndigo, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(destination.openingHours, fontSize = 12.sp, color = TextDark)
                    }
                }
            }
        }

        // Map Preview Mini-Card & Direction CTA
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable {
                        viewModel.selectPlace(destination.id, openDetail = false)
                        viewModel.navigateTo(ActiveScreen.MAP)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Sand.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, Sand)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Location on Google Maps", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TwilightIndigo)
                        Text("${destination.name}, ${destination.province}", fontSize = 12.sp, color = MutedText)
                    }

                    Button(
                        onClick = {
                            viewModel.selectPlace(destination.id, openDetail = false)
                            viewModel.navigateTo(ActiveScreen.MAP)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("detail_get_directions_btn")
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Get Directions", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Primary Action: Add to Trip Planner
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Button(
                    onClick = { viewModel.addDestinationToTrip(destination.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("detail_plan_visit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Plan a Visit", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Related Experiences Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Related Experiences & Workshops",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TwilightIndigo,
                        fontSize = 17.sp
                    )
                )
                TextButton(onClick = { viewModel.navigateTo(ActiveScreen.EXPERIENCES) }) {
                    Text("See all", color = Terracotta, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        items(nearbyServices) { serv ->
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                ServiceListItemCard(
                    service = serv,
                    placeName = destination.name,
                    onBookClick = { viewModel.selectService(serv.id) }
                )
            }
        }
    }
}
