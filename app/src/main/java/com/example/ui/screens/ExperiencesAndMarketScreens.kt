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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TimizarDatabase
import com.example.model.ServiceCategory
import com.example.model.ServiceItem
import com.example.model.LocalProduct
import com.example.state.ActiveScreen
import com.example.state.AppUiState
import com.example.state.TimizarViewModel
import com.example.ui.components.MoroccanArtwork
import com.example.ui.theme.*

@Composable
fun ExperiencesScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pottery & Crafts", "Amazigh Weaving", "Cooking & Dining", "Desert Excursions", "Oasis Treks", "Rose & Date Harvest")

    val experienceServices = TimizarDatabase.services.filter { 
        it.category == ServiceCategory.GUIDE || 
        it.category == ServiceCategory.ACTIVITY || 
        it.category == ServiceCategory.RESTAURANT ||
        it.category == ServiceCategory.COOPERATIVE 
    }

    val filtered = when (selectedFilter) {
        "Pottery & Crafts" -> experienceServices.filter { it.id.contains("pottery") || it.category == ServiceCategory.COOPERATIVE }
        "Amazigh Weaving" -> experienceServices.filter { it.id.contains("weaving") || it.id.contains("taznakht") }
        "Cooking & Dining" -> experienceServices.filter { it.category == ServiceCategory.RESTAURANT || it.id.contains("cooking") }
        "Desert Excursions" -> experienceServices.filter { it.id.contains("desert") || it.id.contains("camel") || it.id.contains("dune") }
        "Oasis Treks" -> experienceServices.filter { it.id.contains("oasis") || it.id.contains("walk") || it.id.contains("todra") }
        "Rose & Date Harvest" -> experienceServices.filter { it.id.contains("rose") || it.id.contains("date") }
        else -> experienceServices
    }

    LazyColumn(
        modifier = modifier
            .testTag("experiences_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Local Experiences",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TwilightIndigo,
                        fontSize = 24.sp
                    )
                )
                Text(
                    text = "Book authentic Amazigh workshops, canyon treks & desert excursions",
                    fontSize = 12.sp,
                    color = MutedText
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters) { f ->
                        val isSelected = selectedFilter == f
                        Surface(
                            modifier = Modifier.clickable { selectedFilter = f },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Terracotta else CardWhite,
                            border = BorderStroke(1.dp, if (isSelected) Terracotta else BorderColor)
                        ) {
                            Text(
                                text = f,
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

        items(filtered) { service ->
            val place = TimizarDatabase.destinations.find { it.id == service.placeId }
            val provider = TimizarDatabase.providers.find { it.id == service.providerId }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectService(service.id) }
                    .testTag("exp_card_${service.id}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        MoroccanArtwork(
                            placeId = service.placeId,
                            imageUrl = service.imageUrl,
                            contentDescription = service.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Terracotta,
                            modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
                        ) {
                            Text(
                                text = service.category.label,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (service.videoUrl.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = TwilightIndigo.copy(alpha = 0.85f),
                                modifier = Modifier.padding(12.dp).align(Alignment.TopEnd)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Video Preview",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = service.title,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = TwilightIndigo
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${service.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Provider: ${provider?.name ?: "Verified Local Guide"} • ${place?.name ?: "Drâa-Tafilalet"}",
                            fontSize = 12.sp,
                            color = MutedText
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${service.priceMad} MAD ${service.durationText}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Terracotta
                            )
                            Button(
                                onClick = { viewModel.selectService(service.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TwilightIndigo),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Request Availability", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocalMarketScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf("Featured") }

    val categories = listOf(
        "All",
        "Tamegroute pottery",
        "Medjool dates",
        "Rose Valley products",
        "Traditional Amazigh weaving",
        "Handmade baskets",
        "Traditional textiles",
        "Regional foods",
        "Souvenirs"
    )

    val products = uiState.marketplaceProducts

    val filteredProducts = products.filter { prod ->
        val matchesCategory = selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
            prod.title.contains(searchQuery, ignoreCase = true) ||
            prod.artisanName.contains(searchQuery, ignoreCase = true) ||
            prod.origin.contains(searchQuery, ignoreCase = true) ||
            prod.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }.let { list ->
        when (selectedSort) {
            "Price: Low to High" -> list.sortedBy { it.priceMAD }
            "Price: High to Low" -> list.sortedByDescending { it.priceMAD }
            "Highest Rated" -> list.sortedByDescending { it.sellerRating }
            else -> list
        }
    }

    LazyColumn(
        modifier = modifier
            .testTag("local_market_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Premium Market Header with Artisan Cooperative Mode
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SIA Artisan Marketplace",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TwilightIndigo,
                                    fontSize = 24.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Direct from Drâa-Tafilalet master potters, weavers, and oasis cooperatives.",
                            fontSize = 12.sp,
                            color = MutedText,
                            lineHeight = 17.sp
                        )
                    }

                    // Seller Dashboard Switch CTA
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = TwilightIndigo,
                        modifier = Modifier
                            .clickable { viewModel.switchToSellerMode(true) }
                            .testTag("open_seller_hub_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Seller Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
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
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f).testTag("market_search_input"),
                            placeholder = { Text("Search pottery, Medjool dates, rose water, rugs...", fontSize = 13.sp, color = MutedText) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MutedText, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips (Exact 8 Categories from Part 6)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier
                                .clickable { selectedCategory = cat }
                                .testTag("market_chip_${cat.lowercase().replace(" ", "_")}"),
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Terracotta else CardWhite,
                            border = BorderStroke(1.dp, if (isSelected) Terracotta else BorderColor)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Featured Regional Crafts Carousel (with 3D Badges)
        if (selectedCategory == "All" && searchQuery.isBlank()) {
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Featured Regional Crafts",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TwilightIndigo,
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AmberGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "3D Ready",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(products.filter { it.has3dModel }) { prod ->
                            val isFav = uiState.favoriteProductIds.contains(prod.id)
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable { viewModel.selectProduct(prod.id) },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                    ) {
                                        MoroccanArtwork(
                                            placeId = prod.placeId,
                                            imageUrl = prod.imageUrl,
                                            contentDescription = prod.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Terracotta,
                                            modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = prod.category,
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        // 3D badge button
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = TwilightIndigo.copy(alpha = 0.9f),
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .align(Alignment.BottomEnd)
                                                .clickable { viewModel.openProduct3dViewer(prod.id) }
                                                .testTag("featured_3d_btn_${prod.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.ViewInAr, contentDescription = null, tint = AmberGold, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("View 3D", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }

                                        // Favorite button
                                        IconButton(
                                            onClick = { viewModel.toggleProductFavorite(prod.id) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .padding(4.dp)
                                                .align(Alignment.TopEnd)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.85f))
                                        ) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (isFav) Color.Red else TextDark,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = prod.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TwilightIndigo,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${prod.artisanName} • ${prod.origin}",
                                            fontSize = 10.sp,
                                            color = MutedText,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${prod.priceMAD} MAD",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = Terracotta
                                            )
                                            Text(
                                                text = "${prod.stock} in stock",
                                                fontSize = 10.sp,
                                                color = OasisGreen,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Products Grid Header (Showing count & sorting options)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredProducts.size} Authentic Products",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TwilightIndigo
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val sortOptions = listOf("Featured", "Price: Low to High", "Highest Rated")
                    sortOptions.forEach { opt ->
                        val isSortSelected = selectedSort == opt
                        Surface(
                            modifier = Modifier.clickable { selectedSort = opt },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSortSelected) TwilightIndigo else WarmCanvas,
                            border = BorderStroke(1.dp, if (isSortSelected) TwilightIndigo else BorderColor)
                        ) {
                            Text(
                                text = opt,
                                fontSize = 10.sp,
                                fontWeight = if (isSortSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSortSelected) Color.White else MutedText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Product Cards
        items(filteredProducts) { product ->
            val isFav = uiState.favoriteProductIds.contains(product.id)
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectProduct(product.id) }
                        .testTag("product_card_${product.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                        ) {
                            MoroccanArtwork(
                                placeId = product.placeId,
                                imageUrl = product.imageUrl,
                                contentDescription = product.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Category badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = OasisGreen,
                                modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
                            ) {
                                Text(
                                    text = product.category,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Favorite Action
                            IconButton(
                                onClick = { viewModel.toggleProductFavorite(product.id) },
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(36.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .testTag("fav_btn_${product.id}")
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) Color.Red else TextDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Authenticity Badge
                            if (product.isAuthentic) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = TwilightIndigo.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(12.dp).align(Alignment.BottomStart)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = OasisGreen, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "100% Certified Regional Origin",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // 3D badge ONLY if product has 3D model
                            if (product.has3dModel) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AmberGold,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .align(Alignment.BottomEnd)
                                        .clickable { viewModel.openProduct3dViewer(product.id) }
                                        .testTag("card_3d_btn_${product.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.ViewInAr, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("3D Model", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = product.title,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = TwilightIndigo,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${product.priceMAD} MAD",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Terracotta
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "By ${product.artisanName} • Origin: ${product.origin} (${product.province})",
                                fontSize = 12.sp,
                                color = MutedText
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = product.description,
                                fontSize = 12.sp,
                                color = TextDark,
                                maxLines = 2,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (product.stock > 0) OasisGreen else Color.Red)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (product.stock > 0) "Available: ${product.stock} units" else "Sold Out",
                                        fontSize = 11.sp,
                                        color = if (product.stock > 0) OasisGreen else Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (product.has3dModel) {
                                        OutlinedButton(
                                            onClick = { viewModel.openProduct3dViewer(product.id) },
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.dp, AmberGold),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold),
                                            modifier = Modifier.height(38.dp).testTag("list_view_3d_${product.id}")
                                        ) {
                                            Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("3D", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.selectProduct(product.id) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TwilightIndigo),
                                        modifier = Modifier.height(38.dp)
                                    ) {
                                        Text("Details & Reserve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

/**
 * Premium Product Detail Screen (Part 6 Requirement 5)
 * Displays: Image gallery, Product description, Origin, Seller profile, Price, Availability,
 * Favorite action, Contact/reserve workflow, Related products, and Interactive 3D preview when available.
 */
@Composable
fun ProductDetailScreen(
    productId: String,
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    val product = uiState.marketplaceProducts.find { it.id == productId }
        ?: TimizarDatabase.localProducts.find { it.id == productId }
        ?: TimizarDatabase.localProducts.first()

    val isFav = uiState.favoriteProductIds.contains(product.id)
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var showReservationDialog by remember { mutableStateOf(false) }

    val allGalleryImages = listOf(product.imageUrl) + product.galleryImages.filter { it != product.imageUrl }

    LazyColumn(
        modifier = modifier
            .testTag("product_detail_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        // 1. Hero Image Gallery
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                MoroccanArtwork(
                    placeId = product.placeId,
                    imageUrl = allGalleryImages.getOrElse(selectedImageIndex) { product.imageUrl },
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Bar overlay with Back & Favorite actions
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

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.toggleProductFavorite(product.id) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f))
                                .testTag("detail_favorite_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) Color.Red else TextDark
                            )
                        }
                    }
                }

                // Gallery Thumbnail Selector
                if (allGalleryImages.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allGalleryImages.forEachIndexed { idx, _ ->
                            Box(
                                modifier = Modifier
                                    .size(if (idx == selectedImageIndex) 10.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(if (idx == selectedImageIndex) Terracotta else Color.White)
                                    .clickable { selectedImageIndex = idx }
                            )
                        }
                    }
                }
            }
        }

        // 2. Title, Category & Pricing Info
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Terracotta
                    ) {
                        Text(
                            text = product.category,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (product.stock > 0) OasisGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (product.stock > 0) OasisGreen else Color.Red)
                    ) {
                        Text(
                            text = if (product.stock > 0) "In Stock: ${product.stock} units" else "Out of Stock",
                            color = if (product.stock > 0) OasisGreen else Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = product.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TwilightIndigo,
                        fontSize = 24.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Crafted in ${product.origin} (${product.province})",
                    fontSize = 13.sp,
                    color = MutedText
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${product.priceMAD} MAD",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Terracotta
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${product.sellerRating} (${product.reviewsCount} verified reviews)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TwilightIndigo
                        )
                    }
                }
            }
        }

        // 3. Interactive 3D Model Card (ONLY shown when product has valid 3D file!)
        if (product.has3dModel && product.model3dUrl.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TwilightIndigo)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ViewInAr, contentDescription = null, tint = AmberGold, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Interactive 3D Craft Model", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                    Text("GLB Mesh: ${product.model3dUrl}", fontSize = 10.sp, color = AmberGold)
                                }
                            }

                            Surface(shape = RoundedCornerShape(10.dp), color = Terracotta) {
                                Text("360° AR Ready", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Examine authentic glaze textures, hand-turned contours, and traditional details in real-time 3D rotation with dynamic Moroccan sunlight.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.openProduct3dViewer(product.id) },
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("open_3d_viewer_btn"),
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                        ) {
                            Icon(Icons.Default.ViewInAr, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Launch Fullscreen 3D Viewer", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // 4. Verified Artisan & Cooperative Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Verified Seller & Cooperative", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TwilightIndigo)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(WarmCanvas)
                        ) {
                            MoroccanArtwork(
                                placeId = product.placeId,
                                imageUrl = product.imageUrl,
                                contentDescription = product.artisanName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = product.artisanName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TwilightIndigo
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = OasisGreen, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = "Located in ${product.origin} • ${product.sellerPhone}",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = product.sellerBio,
                        fontSize = 12.sp,
                        color = TextDark,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // 5. Authenticity & Heritage Story
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Authenticity & Heritage Story", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TwilightIndigo)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        fontSize = 13.sp,
                        color = TextDark,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = BorderColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Workshop Pickup Meeting Point:", fontSize = 11.sp, color = MutedText)
                            Text(product.pickupMeetingPoint, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TwilightIndigo)
                        }
                    }
                }
            }
        }

        // 6. Action Bar: Contact & Reserve Workflow (No fake payment gateway)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(22.dp),
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
                            Text("Total Price", fontSize = 11.sp, color = MutedText)
                            Text(
                                text = "${product.priceMAD} MAD",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = Terracotta
                            )
                        }

                        Button(
                            onClick = { showReservationDialog = true },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("reserve_craft_action_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reserve for Pickup", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ℹ️ Clearly labeled reservation workflow: No online payment processed now. Inspect and pay the cooperative directly in ${product.origin}.",
                        fontSize = 10.sp,
                        color = MutedText,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // 7. Related Products
        val relatedProducts = uiState.marketplaceProducts
            .filter { it.category == product.category && it.id != product.id }
            .take(3)

        if (relatedProducts.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("Related Crafts from this Category", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TwilightIndigo)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            items(relatedProducts) { rel ->
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectProduct(rel.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp))) {
                                MoroccanArtwork(placeId = rel.placeId, imageUrl = rel.imageUrl, contentDescription = rel.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rel.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TwilightIndigo, maxLines = 1)
                                Text("${rel.artisanName} • ${rel.origin}", fontSize = 11.sp, color = MutedText)
                                Text("${rel.priceMAD} MAD", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Terracotta)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MutedText)
                        }
                    }
                }
            }
        }
    }

    // Reservation / Inquiry Dialog (Part 6 Requirement 7)
    if (showReservationDialog) {
        var customerName by remember { mutableStateOf(uiState.currentUserName) }
        var customerContact by remember { mutableStateOf("+212 661 00 22 33") }
        var reserveQty by remember { mutableIntStateOf(1) }
        var specialNote by remember { mutableStateOf("I will pick up this craft during my tour in ${product.origin}.") }

        AlertDialog(
            onDismissRequest = { showReservationDialog = false },
            title = {
                Text(
                    text = "Reserve: ${product.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TwilightIndigo
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Reserve directly with ${product.artisanName}. The cooperative will set aside your items for workshop pickup in ${product.origin}.",
                        fontSize = 12.sp,
                        color = MutedText,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth().testTag("inquiry_name_input")
                    )

                    OutlinedTextField(
                        value = customerContact,
                        onValueChange = { customerContact = it },
                        label = { Text("Phone or WhatsApp") },
                        modifier = Modifier.fillMaxWidth().testTag("inquiry_contact_input")
                    )

                    // Quantity Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quantity to Reserve:", fontSize = 12.sp, color = TextDark)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (reserveQty > 1) reserveQty-- },
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(WarmCanvas)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "-1", modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = "$reserveQty",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { if (reserveQty < product.stock) reserveQty++ },
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(WarmCanvas)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "+1", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = specialNote,
                        onValueChange = { specialNote = it },
                        label = { Text("Message to Cooperative") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = WarmCanvas,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = TwilightIndigo, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Estimated Total: ${product.priceMAD * reserveQty} MAD (Payable at pickup)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Terracotta
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendProductInquiry(
                            productId = product.id,
                            customerName = customerName,
                            customerContact = customerContact,
                            message = specialNote,
                            quantity = reserveQty
                        )
                        viewModel.reserveProductForPickup(product.id)
                        showReservationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                    modifier = Modifier.testTag("confirm_reservation_submit_btn")
                ) {
                    Text("Confirm Reservation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReservationDialog = false }) {
                    Text("Cancel", color = MutedText)
                }
            }
        )
    }
}
