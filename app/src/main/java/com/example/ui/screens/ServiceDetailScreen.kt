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
import com.example.model.BookingStatus
import com.example.model.PaymentStatus
import com.example.state.ActiveScreen
import com.example.state.AppUiState
import com.example.state.TimizarViewModel
import com.example.ui.components.MoroccanArtwork
import com.example.ui.components.SiaVideoPlayerCard
import com.example.ui.theme.*

@Composable
fun ServiceDetailScreen(
    serviceId: String,
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    val service = TimizarDatabase.services.find { it.id == serviceId } ?: TimizarDatabase.services.first()
    val provider = TimizarDatabase.providers.find { it.id == service.providerId }
    val destination = TimizarDatabase.destinations.find { it.id == service.placeId }

    var selectedDate by remember { mutableStateOf(service.availableDates.firstOrNull() ?: "2026-10-12") }
    var guestsCount by remember { mutableStateOf(2) }
    var showBookingConfirmationDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .testTag("service_detail_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Stitch 06. Service Details Header Photo
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                MoroccanArtwork(
                    placeId = service.placeId,
                    imageUrl = service.imageUrl,
                    contentDescription = service.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Back & Favorite
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }

                    IconButton(
                        onClick = { /* Favorite */ },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                    ) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Fav", tint = TextDark)
                    }
                }
            }
        }

        // Title & Rating
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TwilightIndigo,
                        fontSize = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Terracotta, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${destination?.name ?: "Drâa-Tafilalet"} • ${service.category.label}",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${service.rating} (${service.reviewsCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }
        }

        // Authentic Workshop / Experience Video Demonstration
        if (service.videoUrl.isNotBlank()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SiaVideoPlayerCard(
                        videoUrl = service.videoUrl,
                        title = service.videoTitle.ifBlank { "${service.title} Live Demonstration" },
                        posterUrl = service.imageUrl,
                        durationText = service.videoDuration,
                        isVerified = service.hasVerifiedVideo,
                        attribution = "Authentic Artisan Demonstration • Drâa-Tafilalet"
                    )
                }
            }
        }

        // Quick Feature Icons (Camp, Dinner, Camel Ride, Stargazing)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val features = listOf(
                    Pair("Camp", Icons.Default.HolidayVillage),
                    Pair("Dinner", Icons.Default.Restaurant),
                    Pair("Camel Ride", Icons.Default.DirectionsWalk),
                    Pair("Stargazing", Icons.Default.Bedtime)
                )

                features.forEach { (label, icon) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardWhite)
                            .padding(vertical = 10.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = Terracotta, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    }
                }
            }
        }

        // Description Body
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextDark,
                        lineHeight = 20.sp,
                        fontSize = 14.sp
                    )
                )
            }
        }

        // Price & Primary CTA (Exact Stitch: "From €85 per person" & "Book This Experience")
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
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
                            Text("Total Price:", fontSize = 11.sp, color = MutedText)
                            Text(
                                text = "${service.priceMad * guestsCount} MAD",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Terracotta
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Guests: $guestsCount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { if (guestsCount > 1) guestsCount-- },
                                modifier = Modifier.size(30.dp).clip(CircleShape).background(WarmCanvas)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { if (guestsCount < 10) guestsCount++ },
                                modifier = Modifier.size(30.dp).clip(CircleShape).background(WarmCanvas)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showBookingConfirmationDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_booking_request_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Book This Experience", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showBookingConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showBookingConfirmationDialog = false },
            title = { Text("Confirm Reservation Request", fontWeight = FontWeight.Bold) },
            text = {
                Text("Reserve '${service.title}' on $selectedDate for $guestsCount guest(s). The provider will review the request before confirmation.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBookingConfirmationDialog = false
                        viewModel.requestBooking(service.id, selectedDate, guestsCount, "")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
