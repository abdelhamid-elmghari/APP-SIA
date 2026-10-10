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
import com.example.model.ServiceCategory
import com.example.state.ActiveScreen
import com.example.state.AppUiState
import com.example.state.TimizarViewModel
import com.example.ui.components.DestinationHeroCard
import com.example.ui.components.ServiceListItemCard
import com.example.ui.theme.*

@Composable
fun UnifiedSearchScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf(uiState.searchQuery) }

    val filteredDestinations = TimizarDatabase.destinations.filter {
        queryText.isBlank() || it.name.contains(queryText, ignoreCase = true) ||
                it.description.contains(queryText, ignoreCase = true) ||
                it.province.contains(queryText, ignoreCase = true)
    }

    val filteredServices = TimizarDatabase.services.filter { serv ->
        val matchesCategory = uiState.selectedCategory == null || serv.category == uiState.selectedCategory
        val matchesQuery = queryText.isBlank() || serv.title.contains(queryText, ignoreCase = true) ||
                serv.description.contains(queryText, ignoreCase = true) ||
                serv.placeId.contains(queryText, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Column(
        modifier = modifier
            .testTag("search_screen")
            .fillMaxSize()
            .background(WarmCanvas)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Input Field
        OutlinedTextField(
            value = queryText,
            onValueChange = {
                queryText = it
                viewModel.setSearchQuery(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_input"),
            placeholder = { Text("Search places, experiences, stays...", fontSize = 13.sp, color = MutedText) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Terracotta)
            },
            trailingIcon = {
                if (queryText.isNotEmpty()) {
                    IconButton(onClick = {
                        queryText = ""
                        viewModel.setSearchQuery("")
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = MutedText)
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardWhite,
                unfocusedContainerColor = CardWhite,
                focusedBorderColor = Terracotta,
                unfocusedBorderColor = BorderColor
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.clickable { viewModel.selectCategoryFilter(null) },
                    shape = RoundedCornerShape(18.dp),
                    color = if (uiState.selectedCategory == null) TwilightIndigo else CardWhite,
                    border = BorderStroke(1.dp, if (uiState.selectedCategory == null) TwilightIndigo else BorderColor)
                ) {
                    Text(
                        text = "All",
                        fontSize = 12.sp,
                        fontWeight = if (uiState.selectedCategory == null) FontWeight.Bold else FontWeight.Medium,
                        color = if (uiState.selectedCategory == null) Color.White else TextDark,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
            items(ServiceCategory.values()) { cat ->
                val isSelected = uiState.selectedCategory == cat
                Surface(
                    modifier = Modifier.clickable { viewModel.selectCategoryFilter(if (isSelected) null else cat) },
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) TwilightIndigo else CardWhite,
                    border = BorderStroke(1.dp, if (isSelected) TwilightIndigo else BorderColor)
                ) {
                    Text(
                        text = cat.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextDark,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            if (filteredDestinations.isNotEmpty() && uiState.selectedCategory == null) {
                item {
                    Text(
                        text = "Destinations (${filteredDestinations.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TwilightIndigo
                    )
                }
                items(filteredDestinations) { dest ->
                    DestinationHeroCard(
                        destination = dest,
                        isSaved = uiState.savedPlaceIds.contains(dest.id),
                        onCardClick = { viewModel.selectPlace(dest.id) },
                        onToggleSave = { viewModel.toggleSavePlace(dest.id) }
                    )
                }
            }

            if (filteredServices.isNotEmpty()) {
                item {
                    Text(
                        text = "Experiences & Stays (${filteredServices.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TwilightIndigo
                    )
                }
                items(filteredServices) { serv ->
                    val place = TimizarDatabase.destinations.find { it.id == serv.placeId }
                    ServiceListItemCard(
                        service = serv,
                        placeName = place?.name ?: "Drâa-Tafilalet",
                        onBookClick = { viewModel.selectService(serv.id) }
                    )
                }
            }

            if (filteredDestinations.isEmpty() && filteredServices.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Sand.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, tint = Terracotta, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No matching records found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TwilightIndigo
                        )
                        Text(
                            text = "Try searching 'Todra', 'Merzouga', 'Kasbah', or reset category filters.",
                            fontSize = 13.sp,
                            color = MutedText,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                queryText = ""
                                viewModel.setSearchQuery("")
                                viewModel.selectCategoryFilter(null)
                            },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Text("Reset All Filters", fontSize = 12.sp, color = Terracotta, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
