package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.*

private data class HomeCategoryItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val action: () -> Unit
)

@Composable
fun HomeScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    var selectedDestCategory by remember { mutableStateOf("For You ✨") }
    val destCategories = listOf("For You ✨", "All", "UNESCO & Kasbahs", "Sahara Dunes", "Gorges & Oases", "Rose & Valleys")
    var showPersonalizationDialog by remember { mutableStateOf(false) }

    val personalizedRecs = remember(
        uiState.savedPlaceIds,
        uiState.userInterests,
        uiState.derivedChatKeywords,
        uiState.likedCommunityPostIds,
        uiState.personalizationEnabled
    ) {
        viewModel.getPersonalizedRecommendations()
    }

    val filteredDestinations = when (selectedDestCategory) {
        "For You ✨" -> personalizedRecs.map { it.destination }
        "UNESCO & Kasbahs" -> TimizarDatabase.destinations.filter { it.categoryTag == "UNESCO" || it.categoryTag == "Kasbah" }
        "Sahara Dunes" -> TimizarDatabase.destinations.filter { it.categoryTag == "Desert" }
        "Gorges & Oases" -> TimizarDatabase.destinations.filter { it.categoryTag == "Oasis" || it.categoryTag == "Nature" && (it.id.contains("gorge") || it.id.contains("tinghir")) }
        "Rose & Valleys" -> TimizarDatabase.destinations.filter { it.id == "kelaat_mgouna" || it.id == "dades_valley" || it.id == "draa_valley" || it.id == "ziz_valley" }
        else -> TimizarDatabase.destinations
    }

    LazyColumn(
        modifier = modifier
            .testTag("home_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Clean App Header (TimizarGo Brand + Bell icon)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Terracotta),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SIA",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TwilightIndigo,
                                fontSize = 23.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.navigateTo(ActiveScreen.BOOKINGS) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CardWhite)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Alerts",
                        tint = TwilightIndigo
                    )
                }
            }
        }

        // 2. Stitch Universal Search Bar
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(ActiveScreen.SEARCH) }
                        .testTag("home_search_bar"),
                    shape = RoundedCornerShape(24.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, BorderColor),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MutedText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search places, experiences, stays...",
                            color = MutedText,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = "Voice",
                            tint = MutedText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2.5 Cinematic Editorial Hero Banner
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                MoroccanArtwork(
                    placeId = "merzouga",
                    imageUrl = com.example.data.RegionalPhotoManifest.MERZOUGA_DUNES_HERO,
                    contentDescription = "Drâa-Tafilalet Desert Hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                startY = 50f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "Discover the Soul of Drâa-Tafilalet",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Traverse ancient kasbahs, golden Erg Chebbi dunes, and emerald palm oases of Southeast Morocco.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.navigateTo(ActiveScreen.EXPLORE) },
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp).testTag("start_exploring_cta")
                    ) {
                        Text("Start Exploring", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // 3. Category Carousel (Stitch icon circles with warm badges)
        item {
            Column {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val categories = listOf(
                        HomeCategoryItem("Stays", Icons.Default.Hotel, OasisGreen, {
                            viewModel.selectCategoryFilter(ServiceCategory.HOTEL)
                            viewModel.navigateTo(ActiveScreen.SEARCH)
                        }),
                        HomeCategoryItem("Activities", Icons.Default.Explore, TwilightIndigo, {
                            viewModel.selectCategoryFilter(ServiceCategory.ACTIVITY)
                            viewModel.navigateTo(ActiveScreen.SEARCH)
                        }),
                        HomeCategoryItem("Guides", Icons.Default.PersonPin, TerracottaDark, {
                            viewModel.selectCategoryFilter(ServiceCategory.GUIDE)
                            viewModel.navigateTo(ActiveScreen.SEARCH)
                        }),
                        HomeCategoryItem("Cooperatives", Icons.Default.Store, OasisGreen, {
                            viewModel.selectCategoryFilter(ServiceCategory.COOPERATIVE)
                            viewModel.navigateTo(ActiveScreen.SEARCH)
                        }),
                        HomeCategoryItem("Dining", Icons.Default.Restaurant, AmberGold, {
                            viewModel.selectCategoryFilter(ServiceCategory.RESTAURANT)
                            viewModel.navigateTo(ActiveScreen.SEARCH)
                        }),
                        HomeCategoryItem("Transport", Icons.Default.DirectionsCar, Sand, {
                            viewModel.navigateTo(ActiveScreen.TRANSPORT_HUB)
                        })
                    )

                    items(categories) { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { item.action() }
                                .testTag("cat_button_${item.label.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(item.color),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (item.color == Sand) TwilightIndigo else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Travel Telemetry Chips (Live Road Info, Offline Center, Passport)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier.clickable { viewModel.navigateTo(ActiveScreen.ROAD_INFO) },
                            shape = RoundedCornerShape(14.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(8.dp).clip(CircleShape).background(OasisGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "N9 Tichka Pass: Open",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TwilightIndigo
                                )
                            }
                        }
                    }
                    item {
                        Surface(
                            modifier = Modifier.clickable { viewModel.navigateTo(ActiveScreen.OFFLINE_CENTER) },
                            shape = RoundedCornerShape(14.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Terracotta, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Offline Maps Cached",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TwilightIndigo
                                )
                            }
                        }
                    }
                    item {
                        Surface(
                            modifier = Modifier.clickable { viewModel.navigateTo(ActiveScreen.PASSPORT) },
                            shape = RoundedCornerShape(14.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = AmberGold, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Passport: ${uiState.passportStamps.size} Stamps",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TwilightIndigo
                                )
                            }
                        }
                    }
                    item {
                        Surface(
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(ActiveScreen.COMMUNITY) }
                                .testTag("home_community_chip"),
                            shape = RoundedCornerShape(14.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, tint = TwilightIndigo, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Community (${uiState.communityPosts.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TwilightIndigo
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3.5 Watch Drâa-Tafilalet in 4K Video Carousel
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = Terracotta, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Watch in 4K Video",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TwilightIndigo,
                                fontSize = 18.sp
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Terracotta.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Verified Field Media",
                            color = Terracotta,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val videoDestinations = TimizarDatabase.destinations.filter { it.videoUrl.isNotBlank() }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(videoDestinations.take(5)) { vDest ->
                        Card(
                            modifier = Modifier
                                .width(230.dp)
                                .clickable {
                                    viewModel.selectPlace(vDest.id)
                                }
                                .testTag("home_video_card_${vDest.id}"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = TwilightIndigo),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(125.dp)
                                ) {
                                    MoroccanArtwork(
                                        placeId = vDest.id,
                                        imageUrl = vDest.imageUrl,
                                        contentDescription = vDest.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.35f))
                                    )
                                    // Play Icon Badge
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Terracotta.copy(alpha = 0.9f))
                                            .align(Alignment.Center),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    // Duration tag
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Black.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd)
                                    ) {
                                        Text(
                                            text = vDest.videoDuration,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = vDest.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = vDest.videoTitle.ifBlank { "Cinematic Regional Experience" },
                                        fontSize = 11.sp,
                                        color = Sand,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Featured Discover Header & Filter Pills
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Featured Discover",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TwilightIndigo,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.personalizationEnabled) Terracotta.copy(alpha = 0.15f) else CardWhite,
                            border = BorderStroke(1.dp, if (uiState.personalizationEnabled) Terracotta else BorderColor),
                            modifier = Modifier
                                .clickable { showPersonalizationDialog = true }
                                .testTag("tune_personalization_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = "Personalization Settings",
                                    tint = if (uiState.personalizationEnabled) Terracotta else MutedText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.personalizationEnabled) "For You ✨" else "Standard",
                                    color = if (uiState.personalizationEnabled) Terracotta else MutedText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = { viewModel.navigateTo(ActiveScreen.EXPLORE) },
                        modifier = Modifier.testTag("see_all_destinations_btn")
                    ) {
                        Text("See all (${TimizarDatabase.destinations.size})", color = Terracotta, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Destination Category Filter Pills
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(destCategories) { cat ->
                        val isSel = selectedDestCategory == cat
                        Surface(
                            modifier = Modifier.clickable { selectedDestCategory = cat },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) TwilightIndigo else CardWhite,
                            border = BorderStroke(1.dp, if (isSel) TwilightIndigo else BorderColor)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else TextDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Featured Destination Hero (from filtered list)
        item {
            val featuredDest = filteredDestinations.firstOrNull() ?: TimizarDatabase.destinations.first()
            val heroRec = personalizedRecs.find { it.destination.id == featuredDest.id }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (selectedDestCategory == "For You ✨" && heroRec != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Terracotta.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Terracotta.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Terracotta, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = heroRec.explanationReason,
                                color = Terracotta,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                DestinationHeroCard(
                    destination = featuredDest,
                    isSaved = uiState.savedPlaceIds.contains(featuredDest.id),
                    onCardClick = { viewModel.selectPlace(featuredDest.id) },
                    onToggleSave = { viewModel.toggleSavePlace(featuredDest.id) }
                )
            }
        }

        // 6. Secondary Destination Showcase Cards (Horizontal Cards from filtered list)
        item {
            val secondaryDestinations = filteredDestinations.drop(1).take(5)
            if (secondaryDestinations.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(secondaryDestinations) { dest ->
                        val itemRec = personalizedRecs.find { it.destination.id == dest.id }
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { viewModel.selectPlace(dest.id) }
                                .testTag("sub_dest_card_${dest.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                ) {
                                    MoroccanArtwork(
                                        placeId = dest.id,
                                        imageUrl = dest.imageUrl,
                                        contentDescription = dest.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Black.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = dest.categoryTag,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = dest.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TwilightIndigo
                                    )
                                    Text(
                                        text = dest.province,
                                        fontSize = 11.sp,
                                        color = MutedText
                                    )
                                    if (itemRec != null && selectedDestCategory == "For You ✨") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = itemRec.explanationReason,
                                            fontSize = 10.sp,
                                            color = Terracotta,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Compact AI Assistant Entry Card (Direct from Stitch design)
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(ActiveScreen.AI_ASSISTANT) }
                        .testTag("home_ai_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = TwilightIndigo),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Terracotta),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ASK SIA Travel Companion",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Get personalized itineraries & local knowledge",
                                fontSize = 12.sp,
                                color = Sand
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // 8. Nearby & Verified Local Services Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Services",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TwilightIndigo,
                        fontSize = 18.sp
                    )
                )
                TextButton(
                    onClick = { viewModel.navigateTo(ActiveScreen.SEARCH) },
                    modifier = Modifier.testTag("see_all_services_btn")
                ) {
                    Text("See all", color = Terracotta, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // List of Nearby Services
        items(TimizarDatabase.services.take(3)) { serv ->
            val place = TimizarDatabase.destinations.find { it.id == serv.placeId }
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                ServiceListItemCard(
                    service = serv,
                    placeName = place?.name ?: "Drâa-Tafilalet",
                    onBookClick = { viewModel.selectService(serv.id) }
                )
            }
        }
    }

    if (showPersonalizationDialog) {
        PersonalizationSettingsDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showPersonalizationDialog = false }
        )
    }
}

@Composable
private fun PersonalizationSettingsDialog(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    onDismiss: () -> Unit
) {
    val availableInterests = listOf("Kasbah", "Desert", "Oasis", "Nature", "Pottery", "UNESCO", "Rose")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = Terracotta, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Personalization & Privacy", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TwilightIndigo)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Control how SIA personalizes your recommendations. Preferences stay completely private on your device.",
                    fontSize = 12.sp,
                    color = MutedText
                )

                // Toggle 1: Dynamic personalization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic Personalization", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
                        Text("Rank destinations by your interests and saves", fontSize = 11.sp, color = MutedText)
                    }
                    Switch(
                        checked = uiState.personalizationEnabled,
                        onCheckedChange = { viewModel.togglePersonalization(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Terracotta)
                    )
                }

                // Toggle 2: ASK SIA Chat Preferences
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Learn from ASK SIA Chat", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
                        Text("Use queried travel topics to recommend places", fontSize = 11.sp, color = MutedText)
                    }
                    Switch(
                        checked = uiState.userConsentForChatPreferences,
                        onCheckedChange = { viewModel.toggleChatPreferenceConsent(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Terracotta)
                    )
                }

                Divider(color = BorderColor)

                // Explicit Interests
                Text("Your Travel Interests", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableInterests.forEach { interest ->
                        val isSelected = uiState.userInterests.contains(interest)
                        Surface(
                            modifier = Modifier.clickable { viewModel.toggleUserInterest(interest) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Terracotta else WarmCanvas,
                            border = BorderStroke(1.dp, if (isSelected) Terracotta else BorderColor)
                        ) {
                            Text(
                                text = interest,
                                color = if (isSelected) Color.White else TextDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Clear history button
                OutlinedButton(
                    onClick = { viewModel.clearPersonalizationHistory() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Terracotta),
                    border = BorderStroke(1.dp, Terracotta),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Recommendation History", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = CardWhite
    )
}
