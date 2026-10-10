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
import com.example.data.RegionalPhotoManifest
import com.example.model.LocalProduct
import com.example.model.ProductInquiry
import com.example.state.ActiveScreen
import com.example.state.AppUiState
import com.example.state.TimizarViewModel
import com.example.ui.components.MoroccanArtwork
import com.example.ui.theme.*

/**
 * Seller & Cooperative Hub: Allows artisans and cooperatives to manage their shop profile,
 * add/edit products, manage stock quantities, delete products, and respond to customer inquiries.
 */
@Composable
fun SellerDashboardScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Products", "Customer Inquiries", "Shop Profile")
    val seller = uiState.currentSellerProfile

    var showEditProfileDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .testTag("seller_dashboard_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Seller Shop Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TwilightIndigo)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Terracotta
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verified Cooperative Partner", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Switch to Traveler mode button
                        OutlinedButton(
                            onClick = { viewModel.switchToSellerMode(false) },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, AmberGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("switch_to_buyer_btn")
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Traveler View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardWhite)
                        ) {
                            MoroccanArtwork(
                                placeId = "zagora",
                                imageUrl = seller.avatarUrl,
                                contentDescription = seller.shopName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = seller.shopName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${seller.name} • ${seller.city}, ${seller.province}",
                                fontSize = 12.sp,
                                color = AmberGold
                            )
                            Text(
                                text = "Reg: ${seller.verifiedCooperativeNumber}",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = seller.bio,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill(title = "Listed Items", value = "${uiState.marketplaceProducts.size}")
                        StatPill(title = "Inquiries", value = "${uiState.sellerInquiries.size}")
                        StatPill(title = "Artisan Rating", value = "★ ${seller.rating}")
                        StatPill(title = "Phone", value = seller.phone.take(12))
                    }
                }
            }
        }

        // 2. Navigation Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardWhite,
                contentColor = Terracotta,
                modifier = Modifier.padding(horizontal = 20.dp).clip(RoundedCornerShape(16.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }

        // 3. Tab Contents
        when (selectedTab) {
            0 -> {
                // TAB 0: My Products
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Catalog (${uiState.marketplaceProducts.size} Items)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TwilightIndigo
                        )

                        Button(
                            onClick = { viewModel.setEditingProduct(null) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                            modifier = Modifier.testTag("add_product_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(uiState.marketplaceProducts) { product ->
                    SellerProductCard(
                        product = product,
                        onEdit = { viewModel.setEditingProduct(product.id) },
                        onDelete = { viewModel.deleteProduct(product.id) },
                        onStockChange = { delta -> viewModel.updateProductStock(product.id, delta) },
                        onInspect3d = { viewModel.openProduct3dViewer(product.id) }
                    )
                }
            }
            1 -> {
                // TAB 1: Customer Inquiries & Reservations
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Traveler Reservations & Inquiries",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TwilightIndigo
                        )
                        Text(
                            text = "Direct pickup reservations and custom commission requests from visitors.",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }
                }

                if (uiState.sellerInquiries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.padding(28.dp), contentAlignment = Alignment.Center) {
                                Text("No pending customer inquiries yet.", color = MutedText, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(uiState.sellerInquiries) { inquiry ->
                        CustomerInquiryCard(
                            inquiry = inquiry,
                            onUpdateStatus = { newStatus, note ->
                                viewModel.answerProductInquiry(inquiry.id, newStatus, note)
                            }
                        )
                    }
                }
            }
            2 -> {
                // TAB 2: Shop Profile & Settings
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
                                Text("Shop Information", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TwilightIndigo)
                                IconButton(onClick = { showEditProfileDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Terracotta)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            ProfileField(label = "Shop Name", value = seller.shopName)
                            ProfileField(label = "Master Artisan", value = seller.name)
                            ProfileField(label = "Origin & City", value = "${seller.city}, ${seller.province}")
                            ProfileField(label = "Official Phone", value = seller.phone)
                            ProfileField(label = "Email Address", value = seller.email)
                            ProfileField(label = "Cooperative License", value = seller.verifiedCooperativeNumber)

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = BorderColor)
                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Artisan Heritage Bio", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TwilightIndigo)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(seller.bio, fontSize = 12.sp, color = TextDark, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editShopName by remember { mutableStateOf(seller.shopName) }
        var editBio by remember { mutableStateOf(seller.bio) }
        var editPhone by remember { mutableStateOf(seller.phone) }
        var editCity by remember { mutableStateOf(seller.city) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Artisan Shop Profile", fontWeight = FontWeight.Bold, color = TwilightIndigo) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editShopName,
                        onValueChange = { editShopName = it },
                        label = { Text("Shop / Cooperative Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Heritage Bio & Techniques") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Contact Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCity,
                        onValueChange = { editCity = it },
                        label = { Text("City / Town") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSellerProfile(editShopName, editBio, editPhone, editCity)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = MutedText)
                }
            }
        )
    }
}

@Composable
private fun StatPill(title: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color.White)
            Text(text = title, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MutedText)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}

@Composable
private fun SellerProductCard(
    product: LocalProduct,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStockChange: (Int) -> Unit,
    onInspect3d: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    MoroccanArtwork(
                        placeId = product.placeId,
                        imageUrl = product.imageUrl,
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Terracotta
                        ) {
                            Text(
                                text = product.category,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (product.has3dModel) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AmberGold.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, AmberGold),
                                modifier = Modifier.clickable { onInspect3d() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ViewInAr, contentDescription = null, tint = AmberGold, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("3D Active", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TwilightIndigo,
                        maxLines = 1
                    )
                    Text(
                        text = "${product.priceMAD} MAD • Origin: ${product.origin}",
                        fontSize = 11.sp,
                        color = Terracotta,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderColor)
            Spacer(modifier = Modifier.height(10.dp))

            // Stock Stepper & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Stock: ", fontSize = 11.sp, color = MutedText)
                    IconButton(
                        onClick = { onStockChange(-1) },
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(WarmCanvas)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "-1", tint = TextDark, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = "${product.stock}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TwilightIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { onStockChange(1) },
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(WarmCanvas)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "+1", tint = TextDark, modifier = Modifier.size(14.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (product.has3dModel) {
                        IconButton(
                            onClick = onInspect3d,
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(AmberGold.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.ViewInAr, contentDescription = "3D", tint = AmberGold, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(WarmCanvas)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TwilightIndigo, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color.Red.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerInquiryCard(
    inquiry: ProductInquiry,
    onUpdateStatus: (String, String) -> Unit
) {
    var showReplyDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (inquiry.status) {
                        "Confirmed" -> OasisGreen.copy(alpha = 0.15f)
                        "Answered" -> TwilightIndigo.copy(alpha = 0.15f)
                        else -> AmberGold.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = inquiry.status.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = when (inquiry.status) {
                            "Confirmed" -> OasisGreen
                            "Answered" -> TwilightIndigo
                            else -> Terracotta
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(inquiry.inquiryDate, fontSize = 11.sp, color = MutedText)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Item: ${inquiry.productTitle} (Qty: ${inquiry.quantity})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TwilightIndigo
            )
            Text(
                text = "From: ${inquiry.customerName} • ${inquiry.customerContact}",
                fontSize = 11.sp,
                color = Terracotta,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = WarmCanvas,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${inquiry.message}\"",
                    fontSize = 12.sp,
                    color = TextDark,
                    modifier = Modifier.padding(10.dp),
                    lineHeight = 17.sp
                )
            }

            if (inquiry.replyNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your Reply: ${inquiry.replyNote}",
                    fontSize = 11.sp,
                    color = OasisGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showReplyDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Terracotta),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Terracotta),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reply / Confirm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showReplyDialog) {
        var replyText by remember { mutableStateOf(inquiry.replyNote) }
        var selectedStatus by remember { mutableStateOf(inquiry.status) }

        AlertDialog(
            onDismissRequest = { showReplyDialog = false },
            title = { Text("Respond to ${inquiry.customerName}", fontWeight = FontWeight.Bold, color = TwilightIndigo) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Inquiry: \"${inquiry.message}\"", fontSize = 12.sp, color = MutedText)
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Response Note / Meeting Instructions") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedStatus == "Confirmed",
                            onClick = { selectedStatus = "Confirmed" },
                            label = { Text("Confirm Reservation") }
                        )
                        FilterChip(
                            selected = selectedStatus == "Answered",
                            onClick = { selectedStatus = "Answered" },
                            label = { Text("Answer Question") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStatus(selectedStatus, replyText)
                        showReplyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) {
                    Text("Send Response")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplyDialog = false }) {
                    Text("Cancel", color = MutedText)
                }
            }
        )
    }
}

/**
 * Add / Edit Product Form (Part 6 Requirement 4)
 * Validates fields: Product Name, Category, Description, Origin city, Price in MAD, Quantity, Images, Video, 3D model.
 */
@Composable
fun AddEditProductScreen(
    productId: String?,
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    val existing = if (productId != null) uiState.marketplaceProducts.find { it.id == productId } else null

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(existing?.category ?: "Tamegroute pottery") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var originCity by remember { mutableStateOf(existing?.origin ?: "Tamegroute") }
    var province by remember { mutableStateOf(existing?.province ?: "Zagora Province") }
    var priceText by remember { mutableStateOf(existing?.priceMAD?.toString() ?: "250") }
    var stockText by remember { mutableStateOf(existing?.stock?.toString() ?: "10") }
    var imageUrl by remember { mutableStateOf(existing?.image ?: RegionalPhotoManifest.TAMEGROUTE_POTTERY) }
    var videoUrl by remember { mutableStateOf(existing?.videoUrl ?: "") }
    var model3dUrl by remember { mutableStateOf(existing?.model3dUrl ?: "") }

    val categories = listOf(
        "Tamegroute pottery",
        "Medjool dates",
        "Rose Valley products",
        "Traditional Amazigh weaving",
        "Handmade baskets",
        "Traditional textiles",
        "Regional foods",
        "Souvenirs"
    )

    // Validation
    val isTitleValid = title.trim().length >= 3
    val isDescValid = description.trim().length >= 10
    val isPriceValid = (priceText.toIntOrNull() ?: 0) > 0
    val isStockValid = (stockText.toIntOrNull() ?: 0) >= 0
    val isFormComplete = isTitleValid && isDescValid && isPriceValid && isStockValid

    val completionProgress = listOf(isTitleValid, isDescValid, isPriceValid, isStockValid).count { it } / 4f

    LazyColumn(
        modifier = modifier
            .testTag("add_edit_product_screen")
            .fillMaxSize()
            .background(WarmCanvas),
        contentPadding = PaddingValues(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(CardWhite)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (existing != null) "Edit Artisan Craft" else "Publish New Craft",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = TwilightIndigo
                    )
                    Text(
                        text = "Authentic Drâa-Tafilalet artisan cooperative catalog",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }
            }

            // Progress indicator
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                LinearProgressIndicator(
                    progress = { completionProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (isFormComplete) OasisGreen else Terracotta,
                    trackColor = BorderColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isFormComplete) "✓ Form is complete and ready to publish" else "Please complete all required fields",
                    fontSize = 11.sp,
                    color = if (isFormComplete) OasisGreen else MutedText,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Product Name *") },
                        placeholder = { Text("e.g. Ancestral Green Enamel Vase") },
                        modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                        isError = title.isNotBlank() && !isTitleValid,
                        supportingText = { if (title.isNotBlank() && !isTitleValid) Text("Minimum 3 characters required") }
                    )

                    // 2. Category selection
                    Column {
                        Text("Authentic Category *", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TwilightIndigo)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    modifier = Modifier.clickable { selectedCategory = cat },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) Terracotta else WarmCanvas,
                                    border = BorderStroke(1.dp, if (isSelected) Terracotta else BorderColor)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Heritage Description & Craft Technique *") },
                        placeholder = { Text("Describe materials, kilns, weaving loom methods...") },
                        modifier = Modifier.fillMaxWidth().height(110.dp).testTag("product_desc_input"),
                        maxLines = 4,
                        isError = description.isNotBlank() && !isDescValid,
                        supportingText = { if (description.isNotBlank() && !isDescValid) Text("Minimum 10 characters required") }
                    )

                    // 4. Origin & Province
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = originCity,
                            onValueChange = { originCity = it },
                            label = { Text("Origin City") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = province,
                            onValueChange = { province = it },
                            label = { Text("Province") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 5. Price & Stock
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Price (MAD) *") },
                            modifier = Modifier.weight(1f).testTag("product_price_input")
                        )
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            label = { Text("Available Stock *") },
                            modifier = Modifier.weight(1f).testTag("product_stock_input")
                        )
                    }

                    // 6. Media Options
                    Text("Media Assets & 3D Model", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TwilightIndigo)

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Product Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        label = { Text("Optional Artisan Video URL (MP4)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = model3dUrl,
                        onValueChange = { model3dUrl = it },
                        label = { Text("Optional 3D Model (.glb / .gltf)") },
                        placeholder = { Text("e.g. tamegroute_vase.glb or leave blank") },
                        modifier = Modifier.fillMaxWidth().testTag("product_3d_input"),
                        supportingText = { Text("Only compatible .glb / .gltf models will unlock the 3D Viewer") }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Publish Button
                    Button(
                        onClick = {
                            viewModel.saveProduct(
                                id = existing?.id,
                                title = title.trim(),
                                category = selectedCategory,
                                description = description.trim(),
                                origin = originCity.trim(),
                                province = province.trim(),
                                priceMad = priceText.toIntOrNull() ?: 200,
                                stock = stockText.toIntOrNull() ?: 5,
                                image = imageUrl,
                                videoUrl = videoUrl.trim(),
                                model3dUrl = model3dUrl.trim()
                            )
                        },
                        enabled = isFormComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_product_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existing != null) "Update Product" else "Publish Craft to SIA Marketplace",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
