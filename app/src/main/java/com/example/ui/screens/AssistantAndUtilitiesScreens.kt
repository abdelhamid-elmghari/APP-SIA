package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.RegionalPhotoManifest
import com.example.data.TimizarDatabase
import com.example.model.RoadCondition
import com.example.state.ActiveScreen
import com.example.state.AppUiState
import com.example.state.ChatMessage
import com.example.state.TimizarViewModel
import com.example.ui.components.MoroccanArtwork
import com.example.ui.theme.*

@Composable
fun AiAssistantScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .testTag("ai_assistant_screen")
            .fillMaxSize()
            .background(WarmCanvas)
    ) {
        // Stitch 07. Timizar AI Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Terracotta),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Terrain, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ASK SIA",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = TwilightIndigo
                    )
                    Text(
                        text = "Drâa-Tafilalet intelligent travel companion",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }
            }

            // Privacy learning status badge (Part 5 requirement 6)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.userConsentForChatPreferences) OasisGreen.copy(alpha = 0.15f) else CardWhite,
                border = BorderStroke(1.dp, if (uiState.userConsentForChatPreferences) OasisGreen else BorderColor),
                modifier = Modifier.clickable { viewModel.toggleChatPreferenceConsent(!uiState.userConsentForChatPreferences) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.userConsentForChatPreferences) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Privacy",
                        tint = if (uiState.userConsentForChatPreferences) OasisGreen else MutedText,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.userConsentForChatPreferences) "Private signals on" else "Signals off",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.userConsentForChatPreferences) OasisGreen else MutedText
                    )
                }
            }
        }

        Divider(color = BorderColor)

        // Chat Message Flow
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            items(uiState.chatMessages) { msg ->
                val isUser = msg.sender == "user"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isUser) Terracotta else CardWhite,
                        border = if (isUser) null else BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.widthIn(max = 310.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = if (isUser) Color.White else TextDark,
                                lineHeight = 19.sp
                            )

                            // 1. Itinerary Result Card
                            if (msg.attachedTrip != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = WarmCanvas),
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        ) {
                                            MoroccanArtwork(
                                                placeId = "merzouga",
                                                imageUrl = RegionalPhotoManifest.MERZOUGA_DUNES_HERO,
                                                contentDescription = "Merzouga Dunes",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = msg.attachedTrip.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TwilightIndigo
                                        )
                                        Text(
                                            text = "Ouarzazate → Merzouga • 3 days",
                                            fontSize = 11.sp,
                                            color = MutedText
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.saveAiGeneratedTrip(msg.attachedTrip) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                                            modifier = Modifier.fillMaxWidth().testTag("save_ai_itinerary_btn")
                                        ) {
                                            Text("Save to My Trips", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // 2. Destination Preview Card (Part 5 requirement 3)
                            if (msg.attachedDestinationId != null) {
                                val dest = TimizarDatabase.destinations.find { it.id == msg.attachedDestinationId }
                                if (dest != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = WarmCanvas),
                                        border = BorderStroke(1.dp, BorderColor)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(85.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            ) {
                                                MoroccanArtwork(
                                                    placeId = dest.id,
                                                    imageUrl = dest.imageUrl,
                                                    contentDescription = dest.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = dest.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TwilightIndigo
                                            )
                                            Text(
                                                text = dest.province,
                                                fontSize = 11.sp,
                                                color = MutedText
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = { viewModel.selectPlace(dest.id) },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = TwilightIndigo),
                                                modifier = Modifier.fillMaxWidth().height(36.dp)
                                            ) {
                                                Text("Explore Destination", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Product Preview Card (Part 5 requirement 3)
                            if (msg.attachedProductId != null) {
                                val prod = TimizarDatabase.localProducts.find { it.id == msg.attachedProductId }
                                if (prod != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = WarmCanvas),
                                        border = BorderStroke(1.dp, BorderColor)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(85.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            ) {
                                                MoroccanArtwork(
                                                    placeId = prod.placeId,
                                                    imageUrl = prod.imageUrl,
                                                    contentDescription = prod.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = prod.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TwilightIndigo
                                            )
                                            Text(
                                                text = "${prod.priceMad} MAD • ${prod.artisanName}",
                                                fontSize = 11.sp,
                                                color = Terracotta,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = { viewModel.selectProduct(prod.id) },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                                                modifier = Modifier.fillMaxWidth().height(36.dp)
                                            ) {
                                                Text("View Product", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Suggested quick action chips
                    if (!isUser && msg.suggestedActions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            msg.suggestedActions.take(3).forEach { action ->
                                Surface(
                                    modifier = Modifier.clickable { viewModel.sendUserChatMessage(action) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = CardWhite,
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Text(
                                        text = action,
                                        fontSize = 11.sp,
                                        color = TextDark,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Loading state indicator
            if (uiState.isChatLoading) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Terracotta
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ASK SIA is searching regional data...",
                                fontSize = 12.sp,
                                color = MutedText
                            )
                        }
                    }
                }
            }
        }

        // Suggested Prompt Quick Buttons Bar (Part 5 requirement 3)
        val defaultPrompts = listOf(
            "Find hidden places near Tinghir",
            "Plan a three-day journey around Ouarzazate",
            "Recommend cultural workshops",
            "Discover authentic local products"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            defaultPrompts.forEach { promptText ->
                Surface(
                    modifier = Modifier.clickable { viewModel.sendUserChatMessage(promptText) },
                    shape = RoundedCornerShape(14.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Text(
                        text = promptText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TwilightIndigo,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Input Field
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardWhite,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f).testTag("ai_input_field"),
                    placeholder = { Text("Ask me anything...", fontSize = 13.sp, color = MutedText) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WarmCanvas,
                        unfocusedContainerColor = WarmCanvas,
                        focusedBorderColor = Terracotta,
                        unfocusedBorderColor = BorderColor
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val prompt = inputText
                            inputText = ""
                            viewModel.sendUserChatMessage(prompt)
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Terracotta)
                        .testTag("ai_send_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun RoadInformationScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .testTag("road_info_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Mountain Pass & Road Center",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TwilightIndigo,
                    fontSize = 22.sp
                )
            )
            Text(
                text = "Transparent reporting for high Atlas passes and valley routes.",
                fontSize = 12.sp,
                color = MutedText
            )
        }

        items(TimizarDatabase.roadAlerts) { alert ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(alert.roadName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TwilightIndigo)
                            Text(alert.routeCode, fontSize = 11.sp, color = Terracotta, fontWeight = FontWeight.SemiBold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (alert.condition) {
                                RoadCondition.CLEAR -> OasisGreenContainer
                                else -> AlertRedContainer
                            }
                        ) {
                            Text(
                                text = alert.condition.level,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (alert.condition) {
                                    RoadCondition.CLEAR -> OnOasisGreenContainer
                                    else -> AlertRed
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(alert.warningMessage, fontSize = 12.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Source: ${alert.sourceDescription} • ${alert.reportedAt}", fontSize = 10.sp, color = MutedText)
                }
            }
        }
    }
}

@Composable
fun OfflineCenterScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .testTag("offline_center_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Stitch 12. Offline Center Header with Download Arrow Icon
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(OasisGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Download maps & content\nto explore offline",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = TwilightIndigo,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "Stay connected, even without internet.\nGet maps, key information and travel tips for your journey.",
                    fontSize = 12.sp,
                    color = MutedText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Region Toggles (Drâa-Tafilalet Region 320 MB, Ouarzazate 90 MB, Tinghir 75 MB, Merzouga 110 MB)
        val regions = listOf(
            Triple("Drâa-Tafilalet Region", "320 MB • Map, places, guides", true),
            Triple("Ouarzazate & Ait Ben Haddou", "90 MB", true),
            Triple("Tinghir & Todra Gorge", "75 MB", false),
            Triple("Merzouga & Desert", "110 MB", false)
        )

        items(regions) { (name, size, isDownloaded) ->
            var checkedState by remember { mutableStateOf(isDownloaded) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TwilightIndigo)
                        Text(size, fontSize = 11.sp, color = MutedText)
                    }

                    Switch(
                        checked = checkedState,
                        onCheckedChange = { checkedState = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OasisGreen
                        )
                    )
                }
            }
        }
    }
}
