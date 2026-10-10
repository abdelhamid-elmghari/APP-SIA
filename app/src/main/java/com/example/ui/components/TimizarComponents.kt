package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.*
import com.example.state.ActiveScreen
import com.example.ui.theme.*

/**
 * MoroccanArtwork ensures every destination, card, and hero has an authentic
 * hand-crafted vector illustration fallback when offline or loading, preventing
 * empty white/gray boxes and maintaining the warm Stitch Drâa-Tafilalet aesthetic.
 */
@Composable
fun MoroccanArtwork(
    placeId: String,
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val fallbackDrawable = when (placeId.lowercase()) {
        "merzouga" -> R.drawable.art_merzouga_dunes
        "ouarzazate" -> R.drawable.art_ait_ben_haddou
        "tinghir" -> R.drawable.art_todra_gorge
        "zagora", "errachidia" -> R.drawable.art_draa_valley
        else -> R.drawable.art_moroccan_hospitality
    }

    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = fallbackDrawable),
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )

        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun TimizarBrandHeader(
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Authentic Stitch Brand Mark (Stylized Kasbah & Sun emblem)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Terracotta),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terrain,
                    contentDescription = "SIA Logo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SIA",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TwilightIndigo,
                            fontSize = 21.sp
                        )
                    )
                }
                Text(
                    text = "DRÂA-TAFILALET",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MutedText
                )
            }
        }

        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = TwilightIndigo
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimizarSubPageHeader(
    title: String,
    onBackClick: () -> Unit,
    trailingAction: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TwilightIndigo,
                    fontSize = 18.sp
                )
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TwilightIndigo,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        actions = {
            if (trailingAction != null) {
                trailingAction()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = WarmCanvas
        )
    )
}

@Composable
fun TimizarBottomNavigation(
    currentScreen: ActiveScreen,
    onTabSelected: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_nav_bar"),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(ActiveScreen.HOME, "Home", Icons.Default.Home),
            Triple(ActiveScreen.EXPLORE, "Explore", Icons.Default.Explore),
            Triple(ActiveScreen.EXPERIENCES, "Experiences", Icons.Default.LocalActivity),
            Triple(ActiveScreen.LOCAL_MARKET, "Market", Icons.Default.Storefront),
            Triple(ActiveScreen.TRIP_PLANNER, "Plan", Icons.Default.DateRange)
        )

        items.forEach { (screen, label, icon) ->
            val selected = currentScreen == screen
            NavigationBarItem(
                modifier = Modifier.testTag("nav_tab_${label.lowercase()}"),
                selected = selected,
                onClick = { onTabSelected(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) Terracotta else MutedText,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Terracotta else MutedText
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun DestinationHeroCard(
    destination: Destination,
    isSaved: Boolean,
    onCardClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("destination_card_${destination.id}")
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                MoroccanArtwork(
                    placeId = destination.id,
                    imageUrl = destination.imageUrl,
                    contentDescription = destination.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for bottom text contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                startY = 60f
                            )
                        )
                )

                // Top UNESCO / Category Tag
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Terracotta,
                    modifier = Modifier
                        .padding(14.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "${destination.categoryTag} Heritage",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Heart Save Button
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .align(Alignment.TopEnd)
                        .testTag("bookmark_${destination.id}")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save place",
                        tint = if (isSaved) Terracotta else TextDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bottom Content
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .align(Alignment.BottomStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = destination.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Sand,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = destination.province,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Arrow CTA Circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Terracotta),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Open",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Card Body Info & Rating
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        text = " (${destination.reviewsCount})",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = OasisGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Passport Eligible",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OasisGreen
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceListItemCard(
    service: ServiceItem,
    placeName: String,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("service_card_${service.id}")
            .fillMaxWidth()
            .clickable { onBookClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service Thumbnail Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                MoroccanArtwork(
                    placeId = service.placeId,
                    imageUrl = service.imageUrl,
                    contentDescription = service.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TwilightIndigo,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${service.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${service.category.label} • $placeName",
                    fontSize = 12.sp,
                    color = MutedText
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${service.priceMad} MAD",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Terracotta
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = service.durationText,
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Sand.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = "Details",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TwilightIndigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
