package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.example.state.ActiveScreen
import com.example.state.AppUiState
import com.example.state.TimizarViewModel
import com.example.ui.components.MoroccanArtwork
import com.example.ui.theme.*

@Composable
fun CommunityScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("For You") }
    val categories = listOf("For You", "Local Stories", "Tips", "Photos")

    LazyColumn(
        modifier = modifier
            .testTag("community_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Stitch 11. Community Screen Header & Category Filters
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CardWhite)
                                .testTag("community_back_btn")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TwilightIndigo, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Community",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TwilightIndigo,
                                fontSize = 24.sp
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.navigateTo(ActiveScreen.SEARCH) },
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(CardWhite)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TwilightIndigo, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pill Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.forEach { cat ->
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
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Community Feed Cards with Large Travel Photography (Exact Stitch 11 Card)
        items(uiState.communityPosts) { post ->
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("community_post_${post.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Author Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(post.authorAvatarUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = post.authorName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TwilightIndigo)
                                    Text("Local guide • Tinghir", fontSize = 11.sp, color = MutedText)
                                }
                            }
                            Text(post.timestamp, fontSize = 11.sp, color = MutedText)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = post.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TwilightIndigo
                        )
                        Text(
                            text = post.content,
                            fontSize = 12.sp,
                            color = MutedText,
                            lineHeight = 17.sp
                        )

                        // Hero Photo
                        if (post.imageUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            ) {
                                MoroccanArtwork(
                                    placeId = post.placeId,
                                    imageUrl = post.imageUrl,
                                    contentDescription = post.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Like & Comment Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isLiked = uiState.likedCommunityPostIds.contains(post.id)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.toggleLikeCommunityPost(post.id) }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isLiked) Terracotta else MutedText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${post.likesCount + if (isLiked) 1 else 0}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isLiked) Terracotta else TextDark
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))
                                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${post.commentsCount}", fontSize = 12.sp, color = MutedText)
                            }

                            TextButton(
                                onClick = { viewModel.addDestinationToTrip(post.placeId) },
                                modifier = Modifier.testTag("post_add_to_trip_${post.id}")
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = Terracotta, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Place to Trip", fontSize = 11.sp, color = Terracotta, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalPassportScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .testTag("passport_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Stitch 12. Digital Tourist Passport Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "My Tourist Passport",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TwilightIndigo,
                        fontSize = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sand Passport Card with QR & Moroccan Emblem
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Sand.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, Sand)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Terracotta),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Terrain, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SIA Passport", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TwilightIndigo)
                            }

                            Icon(Icons.Default.QrCode2, contentDescription = "QR", tint = TwilightIndigo, modifier = Modifier.size(36.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "EXPLORE • COLLECT • BELONG",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = Terracotta
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3/8 stamps collected", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TwilightIndigo)
                            Text("Rank: Atlas Pioneer", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OasisGreen)
                        }
                    }
                }
            }
        }

        // Stamps List / Grid
        items(uiState.passportStamps) { stamp ->
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stamp_item_${stamp.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Sand),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = OasisGreen, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(stamp.placeName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TwilightIndigo)
                            Text(stamp.regionalTrophyTitle, fontSize = 11.sp, color = Terracotta, fontWeight = FontWeight.SemiBold)
                            Text("Verified on ${stamp.verifiedDate}", fontSize = 10.sp, color = MutedText)
                        }
                    }
                }
            }
        }
    }
}
