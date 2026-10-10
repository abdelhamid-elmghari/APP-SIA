package com.example.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TimizarDatabase
import com.example.data.RegionalPhotoManifest
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class ActiveScreen {
    HOME,
    EXPLORE,
    EXPERIENCES,
    LOCAL_MARKET,
    PRODUCT_DETAIL,
    SELLER_DASHBOARD,
    ADD_EDIT_PRODUCT,
    PRODUCT_3D_VIEWER,
    MAP,
    DESTINATION_DETAIL,
    SERVICE_DETAIL,
    SEARCH,
    TRIP_PLANNER,
    TRANSPORT_HUB,
    BOOKINGS,
    PARTNER_DASHBOARD,
    COMMUNITY,
    PASSPORT,
    OFFLINE_CENTER,
    ROAD_INFO,
    AI_ASSISTANT,
    CHECKOUT,
    AUTH,
    ONBOARDING
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "sia_ai"
    val text: String,
    val suggestedActions: List<String> = emptyList(),
    val attachedTrip: TripItinerary? = null,
    val attachedDestinationId: String? = null,
    val attachedProductId: String? = null,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

data class AppUiState(
    // Navigation & Selected references
    val currentScreen: ActiveScreen = ActiveScreen.HOME,
    val selectedPlaceId: String = "tinghir",
    val selectedServiceId: String = "serv_guide_brahim_todra",
    val selectedProductId: String = "prod_tamegroute_vase",
    val selectedBookingId: String? = null,
    val selectedCategory: ServiceCategory? = null,
    val searchQuery: String = "",
    val navigationStack: List<ActiveScreen> = listOf(ActiveScreen.HOME),

    // User session
    val isAuthenticated: Boolean = true,
    val currentUserId: String = "usr_tourist_1",
    val currentUserName: String = "Amine El Fassi",
    val isPartnerMode: Boolean = false,
    val partnerId: String = "prov_brahim",

    // Personalization & Privacy Signals (Part 5)
    val isChatLoading: Boolean = false,
    val personalizationEnabled: Boolean = true,
    val userConsentForChatPreferences: Boolean = true,
    val userInterests: Set<String> = setOf("Kasbah", "Desert", "Oasis"),
    val derivedChatKeywords: Set<String> = emptySet(),
    val likedCommunityPostIds: Set<String> = setOf("post_1"),

    // Persistent Repositories (One Source of Truth)
    val savedPlaceIds: Set<String> = setOf("tinghir", "merzouga"),
    val trips: List<TripItinerary> = TimizarDatabase.curatedTrips,
    val activeTripId: String = "trip_draa_tafilalet_grand",
    val bookings: List<BookingRecord> = listOf(
        BookingRecord(
            id = "bk_101",
            userId = "usr_tourist_1",
            serviceId = "serv_guide_brahim_todra",
            serviceTitle = "Todra Gorge Hidden Trails & Palmeraie Trek",
            placeId = "tinghir",
            category = ServiceCategory.GUIDE,
            providerId = "prov_brahim",
            providerName = "Brahim Ait Mansour",
            date = "2026-10-13",
            guestsCount = 2,
            totalAmountMad = 700,
            status = BookingStatus.CONFIRMED,
            paymentStatus = PaymentStatus.UNPAID,
            specialRequests = "Vegetarian tea preference",
            createdAt = "2026-10-07 14:20"
        ),
        BookingRecord(
            id = "bk_102",
            userId = "usr_tourist_1",
            serviceId = "serv_hotel_dar_kamal",
            serviceTitle = "Kasbah Dar Kamal Adobe Suite",
            placeId = "ouarzazate",
            category = ServiceCategory.HOTEL,
            providerId = "prov_dar_kamal",
            providerName = "Kasbah Dar Kamal Heritage",
            date = "2026-10-12",
            guestsCount = 2,
            totalAmountMad = 1300,
            status = BookingStatus.PENDING,
            paymentStatus = PaymentStatus.UNPAID,
            specialRequests = "Quiet room facing Atlas peak",
            createdAt = "2026-10-08 09:15"
        )
    ),
    val communityPosts: List<CommunityPost> = TimizarDatabase.communityPosts,
    val passportStamps: List<PassportStamp> = TimizarDatabase.initialStamps,
    val isOfflineModeActive: Boolean = false,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            sender = "sia_ai",
            text = "Azul fellawen! Welcome to Drâa-Tafilalet. I am ASK SIA, your intelligent travel companion connected to authentic local guides, kasbahs, artisans, and real road conditions across Southeast Morocco. How may I assist your journey today?",
            suggestedActions = listOf(
                "Find hidden places near Tinghir",
                "Plan a three-day journey around Ouarzazate",
                "Recommend cultural workshops",
                "Discover authentic local products"
            )
        )
    ),
    // Marketplace & Seller Accounts (Part 6)
    val marketplaceProducts: List<LocalProduct> = TimizarDatabase.localProducts,
    val favoriteProductIds: Set<String> = setOf("prod_tamegroute_vase", "prod_taznakht_carpet"),
    val isSellerMode: Boolean = false,
    val currentSellerProfile: SellerProfile = TimizarDatabase.defaultSeller,
    val sellerInquiries: List<ProductInquiry> = listOf(
        ProductInquiry(
            id = "inq_1",
            productId = "prod_tamegroute_vase",
            productTitle = "Ancestral Green Enamel Vase",
            customerName = "Sophie Laurent (France)",
            customerContact = "+33 6 12 34 56 78",
            message = "Hello! Can I reserve 2 vases for pickup next Tuesday at your Tamegroute workshop? Are they safe for floral water?",
            quantity = 2,
            inquiryDate = "2026-10-10 11:30",
            status = "Confirmed",
            replyNote = "Yes! Reserved in wooden crate, certified food and water safe."
        ),
        ProductInquiry(
            id = "inq_2",
            productId = "prod_taznakht_carpet",
            productTitle = "Hand-Knotted Taznakht Wool Carpet",
            customerName = "Karim Alami (Casablanca)",
            customerContact = "karim.alami@example.com",
            message = "Assalamu alaykum. Is international insured DHL shipping available to Casablanca or should we inspect and collect in Taznakht?",
            quantity = 1,
            inquiryDate = "2026-10-10 14:15",
            status = "Pending",
            replyNote = ""
        )
    ),
    val editingProductId: String? = null,
    val active3dViewerProduct: LocalProduct? = null,

    val toastMessage: String? = null
)

class TimizarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // Navigation
    fun navigateTo(screen: ActiveScreen) {
        _uiState.update { current ->
            val newStack = if (current.currentScreen != screen) {
                current.navigationStack + screen
            } else current.navigationStack
            current.copy(currentScreen = screen, navigationStack = newStack)
        }
    }

    fun navigateBack(): Boolean {
        val currentStack = _uiState.value.navigationStack
        if (currentStack.size > 1) {
            val updatedStack = currentStack.dropLast(1)
            val previousScreen = updatedStack.last()
            _uiState.update { it.copy(currentScreen = previousScreen, navigationStack = updatedStack) }
            return true
        }
        return false
    }

    fun selectPlace(placeId: String, openDetail: Boolean = true) {
        _uiState.update { it.copy(selectedPlaceId = placeId) }
        if (openDetail) navigateTo(ActiveScreen.DESTINATION_DETAIL)
    }

    fun selectService(serviceId: String) {
        val service = TimizarDatabase.services.find { it.id == serviceId }
        _uiState.update { current ->
            current.copy(
                selectedServiceId = serviceId,
                selectedPlaceId = service?.placeId ?: current.selectedPlaceId
            )
        }
        navigateTo(ActiveScreen.SERVICE_DETAIL)
    }

    fun selectProduct(productId: String) {
        val product = TimizarDatabase.localProducts.find { it.id == productId }
        _uiState.update { current ->
            current.copy(
                selectedProductId = productId,
                selectedPlaceId = product?.placeId ?: current.selectedPlaceId
            )
        }
        navigateTo(ActiveScreen.PRODUCT_DETAIL)
    }

    fun selectCategoryFilter(category: ServiceCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Toggle Saved / Bookmarked Places
    fun toggleSavePlace(placeId: String) {
        _uiState.update { current ->
            val newSet = if (current.savedPlaceIds.contains(placeId)) {
                current.savedPlaceIds - placeId
            } else {
                current.savedPlaceIds + placeId
            }
            val msg = if (newSet.contains(placeId)) "Place added to Saved Places" else "Place removed from Saved Places"
            current.copy(savedPlaceIds = newSet, toastMessage = msg)
        }
    }

    // Trip Planner Actions
    fun addDestinationToTrip(placeId: String, attachedServiceId: String? = null) {
        _uiState.update { current ->
            val trip = current.trips.find { it.id == current.activeTripId } ?: TimizarDatabase.initialTrip
            val place = TimizarDatabase.destinations.find { it.id == placeId }
            val nextDay = (trip.stops.maxOfOrNull { it.dayNumber } ?: 0) + 1
            val newStop = TripStop(
                stopId = "stop_${UUID.randomUUID().toString().take(6)}",
                placeId = placeId,
                dayNumber = nextDay,
                orderInDay = 1,
                notes = "Explore ${place?.name ?: "stop"}",
                attachedServiceId = attachedServiceId
            )
            val updatedStops = trip.stops + newStop
            val updatedTrip = trip.copy(
                stops = updatedStops,
                estimatedBudgetMad = trip.estimatedBudgetMad + (if (attachedServiceId != null) 300 else 100),
                lastSyncTimestamp = "Updated just now"
            )
            val updatedTrips = current.trips.map { if (it.id == trip.id) updatedTrip else it }
            current.copy(
                trips = updatedTrips,
                toastMessage = "${place?.name ?: "Place"} added to your trip!"
            )
        }
    }

    fun selectActiveTrip(tripId: String) {
        _uiState.update { it.copy(activeTripId = tripId) }
    }

    fun createCustomAiTrip(
        title: String,
        startingCity: String,
        days: Int,
        style: String,
        budgetMad: Int
    ) {
        val stops = when (style.lowercase()) {
            "sahara", "desert" -> listOf(
                TripStop("cs_1", "erfoud", 1, 1, "Arrival in Erfoud, Ziz Valley Palm Gorge", null),
                TripStop("cs_2", "erg_chebbi", 2, 1, "4x4 Desert Crossing & Erg Chebbi Sunset Camel Trek", "serv_hotel_dune_camp"),
                TripStop("cs_3", "merzouga", 3, 1, "Sunrise over the dunes & Gnaoua cultural music in Khamlia", "serv_desert_bivouac_stargazing"),
                TripStop("cs_4", "rissani", 4, 1, "Historical Souk of Rissani & Madfouna tasting", "serv_rest_cooking"),
                TripStop("cs_5", "tinghir", 5, 1, "Todra Gorges scenic canyon walk", "serv_guide_brahim_todra")
            ).take(days)
            "kasbah", "culture" -> listOf(
                TripStop("cs_1", "ouarzazate", 1, 1, "Aït Ben Haddou UNESCO Ksar & Atlas Film Studios", "serv_hotel_dar_kamal"),
                TripStop("cs_2", "skoura", 2, 1, "17th Century Kasbah Amridil & Palmeraie Trail", "serv_oasis_walk_skoura"),
                TripStop("cs_3", "kelaat_mgouna", 3, 1, "Valley of the Roses distillation cooperative", "serv_rose_valley_distillation"),
                TripStop("cs_4", "nkob", 4, 1, "Nkob village of 45 earthen kasbahs", null),
                TripStop("cs_5", "tamegroute", 5, 1, "Ancestral green pottery kilns & ancient library", "serv_coop_pottery_tamegroute")
            ).take(days)
            else -> listOf(
                TripStop("cs_1", "ouarzazate", 1, 1, "Aït Ben Haddou & Kasbah Taourirt", "serv_hotel_dar_kamal"),
                TripStop("cs_2", "todra_gorge", 2, 1, "Todra Gorge & Dadès Valley scenic drive", "serv_guide_brahim_todra"),
                TripStop("cs_3", "erg_chebbi", 3, 1, "Erg Chebbi Sahara dunes & luxury camp", "serv_hotel_dune_camp"),
                TripStop("cs_4", "tamegroute", 4, 1, "Tamegroute pottery & Drâa Valley oases", "serv_coop_pottery_tamegroute"),
                TripStop("cs_5", "kasbah_amridil", 5, 1, "Skoura palm oasis & Kasbah Amridil", "serv_oasis_walk_skoura")
            ).take(days)
        }

        val newTrip = TripItinerary(
            id = "trip_custom_${System.currentTimeMillis()}",
            title = title.ifBlank { "$days-Day $style in Drâa-Tafilalet" },
            startDate = "20 Apr 2026",
            endDate = "${20 + days} Apr 2026",
            numberOfTravelers = 2,
            stops = stops,
            estimatedBudgetMad = budgetMad,
            notes = "AI customized journey optimized for $style traveling starting from $startingCity.",
            isDownloadedOffline = false,
            lastSyncTimestamp = "Generated just now"
        )

        _uiState.update { current ->
            current.copy(
                trips = listOf(newTrip) + current.trips,
                activeTripId = newTrip.id,
                toastMessage = "New SIA AI itinerary generated successfully!"
            )
        }
    }

    fun removeStopFromTrip(stopId: String) {
        _uiState.update { current ->
            val trip = current.trips.find { it.id == current.activeTripId } ?: return@update current
            val updatedStops = trip.stops.filter { it.stopId != stopId }
            val updatedTrip = trip.copy(stops = updatedStops, lastSyncTimestamp = "Updated just now")
            val updatedTrips = current.trips.map { if (it.id == trip.id) updatedTrip else it }
            current.copy(trips = updatedTrips, toastMessage = "Stop removed from itinerary")
        }
    }

    // Booking Lifecycle (Draft -> Pending -> Confirmed -> Completed)
    fun requestBooking(
        serviceId: String,
        date: String,
        guestsCount: Int,
        specialRequests: String = ""
    ) {
        val service = TimizarDatabase.services.find { it.id == serviceId } ?: return
        val provider = TimizarDatabase.providers.find { it.id == service.providerId }
        val newBooking = BookingRecord(
            id = "bk_${UUID.randomUUID().toString().take(8)}",
            userId = _uiState.value.currentUserId,
            serviceId = service.id,
            serviceTitle = service.title,
            placeId = service.placeId,
            category = service.category,
            providerId = service.providerId,
            providerName = provider?.name ?: "Local Provider",
            date = date,
            guestsCount = guestsCount,
            totalAmountMad = service.priceMad * guestsCount,
            status = BookingStatus.PENDING, // Never automatically confirmed without provider approval
            paymentStatus = PaymentStatus.UNPAID,
            specialRequests = specialRequests,
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )

        _uiState.update { current ->
            current.copy(
                bookings = listOf(newBooking) + current.bookings,
                toastMessage = "Booking request submitted to ${provider?.name ?: "provider"}! Status: Pending review."
            )
        }
        navigateTo(ActiveScreen.BOOKINGS)
    }

    fun requestTransportBooking(transportOfferId: String, date: String, seats: Int) {
        val offer = TimizarDatabase.transportOffers.find { it.id == transportOfferId } ?: return
        val origin = TimizarDatabase.destinations.find { it.id == offer.originPlaceId }
        val dest = TimizarDatabase.destinations.find { it.id == offer.destinationPlaceId }
        val provider = TimizarDatabase.providers.find { it.id == offer.providerId }

        val newBooking = BookingRecord(
            id = "bk_trans_${UUID.randomUUID().toString().take(8)}",
            userId = _uiState.value.currentUserId,
            serviceId = offer.id,
            serviceTitle = "${offer.vehicleType}: ${origin?.name} → ${dest?.name}",
            placeId = offer.originPlaceId,
            category = ServiceCategory.TRANSPORT,
            providerId = offer.providerId,
            providerName = provider?.name ?: "Atlas-Sahara Transport",
            date = date,
            guestsCount = seats,
            totalAmountMad = offer.priceMadPerSeat * seats,
            status = BookingStatus.PENDING,
            paymentStatus = PaymentStatus.UNPAID,
            specialRequests = "Departure route coordination",
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )

        _uiState.update { current ->
            current.copy(
                bookings = listOf(newBooking) + current.bookings,
                toastMessage = "Transport request created. Waiting for operator dispatch confirmation."
            )
        }
        navigateTo(ActiveScreen.BOOKINGS)
    }

    // Provider Partner Actions (Accept / Reject)
    fun updateBookingStatusByProvider(bookingId: String, newStatus: BookingStatus) {
        _uiState.update { current ->
            val updated = current.bookings.map { bk ->
                if (bk.id == bookingId) bk.copy(status = newStatus) else bk
            }
            val msg = when (newStatus) {
                BookingStatus.CONFIRMED -> "Booking confirmed by Provider! Tourist notified."
                BookingStatus.REJECTED -> "Booking declined by Provider."
                else -> "Booking status updated to ${newStatus.label}"
            }
            current.copy(bookings = updated, toastMessage = msg)
        }
    }

    // Payment Sandbox / Simulator Adapter
    fun processPaymentSimulation(bookingId: String, success: Boolean = true) {
        _uiState.update { current ->
            val updated = current.bookings.map { bk ->
                if (bk.id == bookingId) {
                    bk.copy(paymentStatus = if (success) PaymentStatus.PAID else PaymentStatus.FAILED)
                } else bk
            }
            val statusMsg = if (success) "Payment Verified via Sandbox Adapter!" else "Payment Failed. Please try another card."
            current.copy(bookings = updated, toastMessage = statusMsg)
        }
    }

    // Community Posts
    fun publishCommunityPost(placeId: String, title: String, content: String, tip: String, tags: List<String>) {
        val newPost = CommunityPost(
            id = "post_${UUID.randomUUID().toString().take(8)}",
            authorName = _uiState.value.currentUserName,
            authorAvatarUrl = "",
            placeId = placeId,
            title = title,
            content = content,
            practicalTip = tip,
            likesCount = 1,
            timestamp = "Just now",
            isVerifiedLocalTip = false,
            tags = tags
        )
        _uiState.update { current ->
            current.copy(
                communityPosts = listOf(newPost) + current.communityPosts,
                toastMessage = "Travel story published to Drâa-Tafilalet community!"
            )
        }
    }

    // Passport Stamp Verification with Token
    fun claimPassportStamp(placeId: String, tokenCode: String): Boolean {
        val place = TimizarDatabase.destinations.find { it.id == placeId } ?: return false
        val alreadyClaimed = _uiState.value.passportStamps.any { it.placeId == placeId }
        if (alreadyClaimed) {
            _uiState.update { it.copy(toastMessage = "You already verified your stamp for ${place.name}!") }
            return true
        }

        val newStamp = PassportStamp(
            id = "stamp_${UUID.randomUUID().toString().take(8)}",
            placeId = placeId,
            placeName = place.name,
            verifiedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            badgeIcon = "stars",
            regionalTrophyTitle = "Verified Explorer of ${place.name}",
            tokenHash = tokenCode.ifEmpty { "TKZ-QR-${System.currentTimeMillis()}" }
        )

        _uiState.update { current ->
            current.copy(
                passportStamps = current.passportStamps + newStamp,
                toastMessage = "Congratulations! Official Stamp for ${place.name} added to your Tourist Passport."
            )
        }
        return true
    }

    // Offline Mode Toggle
    fun toggleOfflineDownload(tripId: String) {
        _uiState.update { current ->
            val updatedTrips = current.trips.map {
                if (it.id == tripId) it.copy(
                    isDownloadedOffline = !it.isDownloadedOffline,
                    lastSyncTimestamp = "Offline cached on ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())}"
                ) else it
            }
            val isNowSaved = updatedTrips.find { it.id == tripId }?.isDownloadedOffline == true
            val msg = if (isNowSaved) "Trip downloaded for offline desert navigation!" else "Trip removed from offline cache"
            current.copy(trips = updatedTrips, toastMessage = msg)
        }
    }

    fun setOfflineSimulated(active: Boolean) {
        _uiState.update { it.copy(isOfflineModeActive = active, toastMessage = if (active) "Simulated Offline Mode enabled" else "Online connectivity restored") }
    }

    fun switchUserRole(partnerMode: Boolean) {
        _uiState.update { it.copy(isPartnerMode = partnerMode) }
    }

    // Personalization & Privacy Controls (Part 5)
    fun togglePersonalization(enabled: Boolean) {
        _uiState.update {
            it.copy(
                personalizationEnabled = enabled,
                toastMessage = if (enabled) "Dynamic personalization enabled" else "Personalization disabled (showing default recommendations)"
            )
        }
    }

    fun toggleChatPreferenceConsent(consent: Boolean) {
        _uiState.update {
            it.copy(
                userConsentForChatPreferences = consent,
                toastMessage = if (consent) "Permission granted: ASK SIA conversation signals enabled" else "Conversation preference learning paused"
            )
        }
    }

    fun clearPersonalizationHistory() {
        _uiState.update {
            it.copy(
                derivedChatKeywords = emptySet(),
                userInterests = setOf("Kasbah", "Desert", "Oasis"),
                toastMessage = "Recommendation history cleared"
            )
        }
    }

    fun toggleUserInterest(interest: String) {
        _uiState.update { current ->
            val updated = if (current.userInterests.contains(interest)) {
                current.userInterests - interest
            } else {
                current.userInterests + interest
            }
            current.copy(userInterests = updated, toastMessage = "Preferences updated: $interest")
        }
    }

    fun toggleLikeCommunityPost(postId: String) {
        _uiState.update { current ->
            val updated = if (current.likedCommunityPostIds.contains(postId)) {
                current.likedCommunityPostIds - postId
            } else {
                current.likedCommunityPostIds + postId
            }
            current.copy(likedCommunityPostIds = updated)
        }
    }

    fun getPersonalizedRecommendations(): List<RecommendationItem> {
        val state = _uiState.value
        val destinations = TimizarDatabase.destinations
        if (!state.personalizationEnabled) {
            return destinations.map {
                RecommendationItem(it, 50, "Featured in Drâa-Tafilalet", it.categoryTag)
            }
        }

        return destinations.map { dest ->
            var score = 20
            val reasons = mutableListOf<String>()

            // 1. Saved destinations signal (+40)
            if (state.savedPlaceIds.contains(dest.id)) {
                score += 40
                reasons.add("Inspired by your saved places")
            }

            // 2. Explicit user interests (+35)
            if (state.userInterests.any { interest ->
                    dest.categoryTag.equals(interest, ignoreCase = true) ||
                    dest.highlights.any { h -> h.contains(interest, ignoreCase = true) }
                }
            ) {
                score += 35
                reasons.add("Matches your '${dest.categoryTag}' interests")
            }

            // 3. ASK SIA conversation keywords (with consent) (+30)
            if (state.userConsentForChatPreferences && state.derivedChatKeywords.isNotEmpty()) {
                val matchedKeyword = state.derivedChatKeywords.firstOrNull { kw ->
                    dest.name.contains(kw, ignoreCase = true) ||
                    dest.province.contains(kw, ignoreCase = true) ||
                    dest.categoryTag.contains(kw, ignoreCase = true) ||
                    dest.description.contains(kw, ignoreCase = true)
                }
                if (matchedKeyword != null) {
                    score += 30
                    reasons.add("Recommended from your ASK SIA query: $matchedKeyword")
                }
            }

            // 4. Liked Community interactions (+25)
            val matchedPost = state.communityPosts.find { state.likedCommunityPostIds.contains(it.id) && it.placeId == dest.id }
            if (matchedPost != null) {
                score += 25
                reasons.add("Inspired by your saved stories: ${matchedPost.title.take(24)}...")
            }

            // 5. Cold-start & curated diversity default reasons
            if (reasons.isEmpty()) {
                val defaultReason = when (dest.categoryTag) {
                    "UNESCO" -> "UNESCO World Heritage cultural highlight"
                    "Desert" -> "Because you enjoy desert & dune landscapes"
                    "Oasis" -> "Lush palm groves & tranquil canyon walks"
                    "Kasbah" -> "Historical earthen clay fortress architecture"
                    else -> "Must-see regional highlight in Drâa-Tafilalet"
                }
                reasons.add(defaultReason)
            }

            RecommendationItem(
                destination = dest,
                matchScore = score,
                explanationReason = reasons.first(),
                matchedTag = dest.categoryTag
            )
        }.sortedByDescending { it.matchScore }
    }

    // AI Assistant Tools execution & conversation
    fun sendUserChatMessage(prompt: String) {
        val userMsg = ChatMessage(sender = "user", text = prompt)
        _uiState.update { it.copy(chatMessages = it.chatMessages + userMsg, isChatLoading = true) }

        // Extract learning signals with consent
        val lowerPrompt = prompt.lowercase()
        val extractedKeywords = mutableSetOf<String>()
        if (lowerPrompt.contains("tinghir") || lowerPrompt.contains("todra")) extractedKeywords.addAll(listOf("Tinghir", "Oasis"))
        if (lowerPrompt.contains("merzouga") || lowerPrompt.contains("desert") || lowerPrompt.contains("dune") || lowerPrompt.contains("chebbi")) extractedKeywords.addAll(listOf("Desert", "Merzouga"))
        if (lowerPrompt.contains("ouarzazate") || lowerPrompt.contains("kasbah") || lowerPrompt.contains("haddou")) extractedKeywords.addAll(listOf("Kasbah", "UNESCO", "Ouarzazate"))
        if (lowerPrompt.contains("workshop") || lowerPrompt.contains("pottery") || lowerPrompt.contains("tamegroute") || lowerPrompt.contains("weaving")) extractedKeywords.addAll(listOf("Pottery", "Craft", "Zagora"))
        if (lowerPrompt.contains("valley") || lowerPrompt.contains("rose") || lowerPrompt.contains("dades")) extractedKeywords.addAll(listOf("Nature", "Rose", "Dades"))

        if (_uiState.value.userConsentForChatPreferences && _uiState.value.personalizationEnabled && extractedKeywords.isNotEmpty()) {
            _uiState.update { it.copy(derivedChatKeywords = it.derivedChatKeywords + extractedKeywords) }
        }

        // Direct navigation action shortcuts from chip taps
        when (prompt.trim()) {
            "View Full Road Center", "Check Road Safety" -> {
                _uiState.update { it.copy(isChatLoading = false) }
                navigateTo(ActiveScreen.ROAD_INFO)
                return
            }
            "Open Map", "Inspect on Interactive Map" -> {
                _uiState.update { it.copy(isChatLoading = false) }
                navigateTo(ActiveScreen.MAP)
                return
            }
            "Open Trip Planner", "View Itinerary" -> {
                _uiState.update { it.copy(isChatLoading = false) }
                navigateTo(ActiveScreen.TRIP_PLANNER)
                return
            }
            "Browse Transport Hub" -> {
                _uiState.update { it.copy(isChatLoading = false) }
                navigateTo(ActiveScreen.TRANSPORT_HUB)
                return
            }
            "Open Guide Profile", "Book with Brahim" -> {
                _uiState.update { it.copy(isChatLoading = false) }
                selectService("serv_guide_brahim_todra")
                return
            }
            "View Cooperative Details" -> {
                _uiState.update { it.copy(isChatLoading = false) }
                selectService("serv_coop_pottery_tamegroute")
                return
            }
            "Show Top Stays" -> {
                _uiState.update { it.copy(isChatLoading = false) }
                selectCategoryFilter(ServiceCategory.HOTEL)
                navigateTo(ActiveScreen.SEARCH)
                return
            }
            "View Destination: Tinghir" -> {
                _uiState.update { it.copy(isChatLoading = false) }
                selectPlace("tinghir")
                return
            }
            "View Destination: Ouarzazate" -> {
                _uiState.update { it.copy(isChatLoading = false) }
                selectPlace("ouarzazate")
                return
            }
            "View Product: Green Vase" -> {
                _uiState.update { it.copy(isChatLoading = false) }
                selectProduct("prod_tamegroute_vase")
                return
            }
        }

        viewModelScope.launch {
            kotlinx.coroutines.delay(300) // Brief natural pause for conversational feel
            val lower = prompt.lowercase()
            val reply: ChatMessage = when {
                // Part 5 specific: Find hidden places near Tinghir
                lower.contains("hidden") && lower.contains("tinghir") || lower.contains("places near tinghir") -> {
                    ChatMessage(
                        sender = "sia_ai",
                        text = "Near Tinghir, explore the secluded palm trails along the Todra Riverbed, the ancient earthen ruins of Ksar Asfalou, and the climbing cliffs of the upper Todra Gorge canyon. Brahim Ait Mansour can guide you off the tourist path.",
                        attachedDestinationId = "tinghir",
                        suggestedActions = listOf("View Destination: Tinghir", "Book with Brahim", "Open Map")
                    )
                }
                // Part 5 specific: Plan a three-day journey around Ouarzazate
                (lower.contains("three-day") || lower.contains("3-day") || lower.contains("three day")) && lower.contains("ouarzazate") -> {
                    val generatedTrip = TripItinerary(
                        id = "trip_ai_ouarzazate_${System.currentTimeMillis()}",
                        title = "3-Day Ouarzazate & Valley of Kasbahs",
                        startDate = "2026-10-18",
                        endDate = "2026-10-21",
                        numberOfTravelers = 2,
                        stops = listOf(
                            TripStop("ai_oz1", "ouarzazate", 1, 1, "Aït Ben Haddou UNESCO Ksar & Atlas Film Studios", "serv_hotel_dar_kamal"),
                            TripStop("ai_oz2", "skoura", 2, 1, "Kasbah Amridil & Skoura Palm Oasis Trail", "serv_oasis_walk_skoura"),
                            TripStop("ai_oz3", "kelaat_mgouna", 3, 1, "Rose Valley distillation cooperative & Dades curves", "serv_rose_valley_distillation")
                        ),
                        estimatedBudgetMad = 1800,
                        notes = "Heritage and cultural circuit focusing on Ouarzazate, Skoura, and the Rose Valley.",
                        isDownloadedOffline = false
                    )
                    ChatMessage(
                        sender = "sia_ai",
                        text = "Here is an authentic 3-day itinerary centered on Ouarzazate: Day 1 at Aït Ben Haddou, Day 2 exploring 17th-century Kasbah Amridil in Skoura, and Day 3 visiting organic rose cooperatives in Kelaat M'Gouna.",
                        attachedTrip = generatedTrip,
                        suggestedActions = listOf("Save Itinerary to Trips", "View Destination: Ouarzazate", "Inspect on Interactive Map")
                    )
                }
                // Part 5 specific: Recommend cultural workshops
                lower.contains("cultural workshop") || lower.contains("workshop") || lower.contains("workshops") || lower.contains("atelier") -> {
                    ChatMessage(
                        sender = "sia_ai",
                        text = "Top cultural workshops in Drâa-Tafilalet:\n• Tamegroute: Ancestral green enamel pottery masterclass at Coopérative Potiers.\n• Taznakht: Traditional Amazigh natural wool carpet weaving.\n• Kelaat M'Gouna: Rose water distillation & botanical extraction.\n• Rissani: Authentic Madfouna Berber pizza culinary workshop.",
                        suggestedActions = listOf("View Cooperative Details", "Recommend cultural workshops", "Discover authentic local products")
                    )
                }
                // Part 5 specific: Discover authentic local products
                lower.contains("local product") || lower.contains("authentic local") || lower.contains("products") || lower.contains("produits") -> {
                    ChatMessage(
                        sender = "sia_ai",
                        text = "Authentic certified local products from our regional cooperatives:\n• Tamegroute Green Glazed Pottery (lead-free, handcrafted)\n• Organic Kelaat M'Gouna Pure Rose Water & Argan elixirs\n• Taznakht handwoven wool rugs with tribal Amazigh motifs\n• Royal Majhoul Dates from the oases of Erfoud.",
                        attachedProductId = "prod_tamegroute_vase",
                        suggestedActions = listOf("View Product: Green Vase", "View Cooperative Details", "Open Trip Planner")
                    )
                }
                // Road & Pass Conditions (Tichka / Todra Pass / R704 / N9 / N10)
                lower.contains("tichka") || lower.contains("road") || lower.contains("condition") ||
                        lower.contains("route") || lower.contains("météo") || lower.contains("pass") ||
                        lower.contains("طريق") || lower.contains("تيشكة") || lower.contains("أحوال") -> {
                    val road = TimizarDatabase.roadAlerts.first()
                    ChatMessage(
                        sender = "sia_ai",
                        text = "Real Highway Telemetry: ${road.roadName} (${road.routeCode}) is currently ${road.condition.label}. ${road.warningMessage} [Verified: ${road.sourceDescription}, ${road.reportedAt}].",
                        suggestedActions = listOf("View Full Road Center", "Open Map", "Browse Transport Hub")
                    )
                }
                // Generic Itinerary Generation (3-day / plan / voyage / programme / برنامج)
                lower.contains("3-day") || lower.contains("plan") || lower.contains("itinerary") ||
                        lower.contains("programme") || lower.contains("circuit") || lower.contains("voyage") ||
                        lower.contains("برنامج") || lower.contains("رحلة") || lower.contains("أيام") -> {
                    val generatedTrip = TripItinerary(
                        id = "trip_ai_custom_${System.currentTimeMillis()}",
                        title = "3-Day Ouarzazate to Merzouga Express",
                        startDate = "2026-10-15",
                        endDate = "2026-10-18",
                        numberOfTravelers = 2,
                        stops = listOf(
                            TripStop("ai_1", "ouarzazate", 1, 1, "Stay at Kasbah Dar Kamal, explore Aït Benhaddou", "serv_hotel_dar_kamal"),
                            TripStop("ai_2", "tinghir", 2, 1, "Todra Gorge canyon trek with guide Brahim Ait Mansour", "serv_guide_brahim_todra"),
                            TripStop("ai_3", "merzouga", 3, 1, "Erg Chebbi Sunset Camel trek & Nomad Camp", "serv_hotel_dune_camp")
                        ),
                        estimatedBudgetMad = 1950,
                        notes = "Customized route organized using authenticated providers and licensed drivers.",
                        isDownloadedOffline = false
                    )
                    ChatMessage(
                        sender = "sia_ai",
                        text = "I synthesized an authentic 3-day Drâa-Tafilalet itinerary (Ouarzazate → Tinghir → Merzouga) for 2 travelers at ~1,950 MAD. It connects verified Kasbah stays, canyon trekking, and dune camel excursions.",
                        attachedTrip = generatedTrip,
                        suggestedActions = listOf("Save Itinerary to Trips", "Inspect on Interactive Map", "Request Accommodation Bookings")
                    )
                }
                // Mountain Guides (Brahim / Todra / guide / مرشد)
                lower.contains("guide") || lower.contains("tinghir") || lower.contains("todra") ||
                        lower.contains("randonnée") || lower.contains("مرشد") || lower.contains("طينغير") -> {
                    val guide = TimizarDatabase.services.find { it.category == ServiceCategory.GUIDE }
                    ChatMessage(
                        sender = "sia_ai",
                        text = "Licensed regional mountain guide Brahim Ait Mansour (${guide?.title}) in Tinghir has a 4.95/5 rating across 124 expeditions. Fixed official rate: 350 MAD per session.",
                        attachedDestinationId = "todra_gorge",
                        suggestedActions = listOf("Open Guide Profile", "Book with Brahim", "Open Map")
                    )
                }
                // Cooperatives & Crafts (Pottery / Weaving / Tamegroute / Taznakht / تعاونية)
                lower.contains("pottery") || lower.contains("zagora") || lower.contains("craft") ||
                        lower.contains("coop") || lower.contains("artisanat") || lower.contains("tamegroute") ||
                        lower.contains("tapis") || lower.contains("تعاونية") || lower.contains("خزف") || lower.contains("زرابي") -> {
                    ChatMessage(
                        sender = "sia_ai",
                        text = "In Zagora/Tamegroute, the ancestral Coopérative Potiers de Tamegroute crafts historical green enamel pottery with live workshops at 180 MAD. 100% fair trade direct to artisans.",
                        attachedProductId = "prod_tamegroute_vase",
                        suggestedActions = listOf("View Cooperative Details", "Open Map", "Browse Transport Hub")
                    )
                }
                // Desert Stays & Erg Chebbi (Merzouga / Camp / Dromadaire / فندق / خيام / رمال)
                lower.contains("merzouga") || lower.contains("desert") || lower.contains("dune") ||
                        lower.contains("camp") || lower.contains("sahara") || lower.contains("désert") ||
                        lower.contains("مرزوغة") || lower.contains("صحراء") || lower.contains("مخيم") -> {
                    ChatMessage(
                        sender = "sia_ai",
                        text = "In Merzouga, Erg Chebbi Luxury Nomad Camp offers traditional camel sunset treks and Berber music around desert campfires. Available from 750 MAD/night.",
                        attachedDestinationId = "merzouga",
                        suggestedActions = listOf("Show Top Stays", "Open Map", "Inspect on Interactive Map")
                    )
                }
                // Berber / Amazigh Greetings (Azul, Tanmirt, Marhba, السلام)
                lower.contains("azul") || lower.contains("marhba") || lower.contains("tanmirt") ||
                        lower.contains("salam") || lower.contains("hello") || lower.contains("bonjour") ||
                        lower.contains("مرحبا") || lower.contains("السلام") -> {
                    ChatMessage(
                        sender = "sia_ai",
                        text = "Azul fellawen! Welcome to Drâa-Tafilalet. I am ASK SIA. I can build custom itineraries, recommend cultural workshops, show verified local products, and check mountain pass road statuses.",
                        suggestedActions = listOf(
                            "Find hidden places near Tinghir",
                            "Plan a three-day journey around Ouarzazate",
                            "Recommend cultural workshops",
                            "Discover authentic local products"
                        )
                    )
                }
                else -> {
                    ChatMessage(
                        sender = "sia_ai",
                        text = "I searched our regional database across Ouarzazate, Tinghir, Merzouga, Zagora, Midelt, and Errachidia. How would you like to explore?",
                        suggestedActions = listOf(
                            "Find hidden places near Tinghir",
                            "Plan a three-day journey around Ouarzazate",
                            "Recommend cultural workshops",
                            "Discover authentic local products"
                        )
                    )
                }
            }
            _uiState.update { it.copy(chatMessages = it.chatMessages + reply, isChatLoading = false) }
        }
    }

    fun saveAiGeneratedTrip(trip: TripItinerary) {
        _uiState.update { current ->
            current.copy(
                trips = listOf(trip) + current.trips,
                activeTripId = trip.id,
                toastMessage = "AI Itinerary '${trip.title}' saved to your Trip Planner!"
            )
        }
        navigateTo(ActiveScreen.TRIP_PLANNER)
    }

    fun reserveProductForPickup(productId: String) {
        val prod = TimizarDatabase.localProducts.find { it.id == productId } ?: return
        val newBooking = BookingRecord(
            id = "bk_prod_${UUID.randomUUID().toString().take(8)}",
            userId = _uiState.value.currentUserId,
            serviceId = prod.id,
            serviceTitle = "Pickup: ${prod.title}",
            placeId = prod.placeId,
            category = ServiceCategory.COOPERATIVE,
            providerId = prod.artisanId,
            providerName = prod.artisanName,
            date = "2026-10-12",
            guestsCount = 1,
            totalAmountMad = prod.priceMad,
            status = BookingStatus.PENDING,
            paymentStatus = PaymentStatus.UNPAID,
            specialRequests = "Reserve for pickup at: ${prod.pickupMeetingPoint}",
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )
        _uiState.update { current ->
            current.copy(
                bookings = listOf(newBooking) + current.bookings,
                toastMessage = "Product '${prod.title}' reserved for pickup! Status: Pending confirmation."
            )
        }
        navigateTo(ActiveScreen.BOOKINGS)
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    // Part 6: Marketplace, Seller Accounts & 3D Viewer Methods
    fun toggleProductFavorite(productId: String) {
        _uiState.update { current ->
            val isFav = current.favoriteProductIds.contains(productId)
            val updated = if (isFav) current.favoriteProductIds - productId else current.favoriteProductIds + productId
            val prod = current.marketplaceProducts.find { it.id == productId }
            current.copy(
                favoriteProductIds = updated,
                toastMessage = if (isFav) "Removed from favorites" else "Added '${prod?.title ?: "item"}' to favorites ❤️"
            )
        }
    }

    fun switchToSellerMode(enabled: Boolean) {
        _uiState.update { it.copy(isSellerMode = enabled) }
        if (enabled) {
            navigateTo(ActiveScreen.SELLER_DASHBOARD)
        } else {
            navigateTo(ActiveScreen.LOCAL_MARKET)
        }
    }

    fun updateSellerProfile(shopName: String, bio: String, phone: String, city: String) {
        _uiState.update { current ->
            val updatedProfile = current.currentSellerProfile.copy(
                shopName = shopName,
                bio = bio,
                phone = phone,
                city = city
            )
            current.copy(
                currentSellerProfile = updatedProfile,
                toastMessage = "Shop profile updated successfully!"
            )
        }
    }

    fun setEditingProduct(productId: String?) {
        _uiState.update { it.copy(editingProductId = productId) }
        navigateTo(ActiveScreen.ADD_EDIT_PRODUCT)
    }

    fun saveProduct(
        id: String?,
        title: String,
        category: String,
        description: String,
        origin: String,
        province: String,
        priceMad: Int,
        stock: Int,
        image: String,
        videoUrl: String = "",
        model3dUrl: String = ""
    ) {
        _uiState.update { current ->
            val seller = current.currentSellerProfile
            val has3d = model3dUrl.isNotBlank() && (model3dUrl.endsWith(".glb") || model3dUrl.endsWith(".gltf"))
            val prodList = current.marketplaceProducts.toMutableList()
            if (id != null) {
                val idx = prodList.indexOfFirst { it.id == id }
                if (idx != -1) {
                    val existing = prodList[idx]
                    val updated = existing.copy(
                        title = title,
                        category = category,
                        description = description,
                        origin = origin,
                        province = province,
                        priceMAD = priceMad,
                        stock = stock,
                        image = if (image.isNotBlank()) image else existing.image,
                        videoUrl = videoUrl,
                        model3dUrl = model3dUrl,
                        has3dModel = has3d
                    )
                    prodList[idx] = updated
                }
            } else {
                val newProd = LocalProduct(
                    id = "prod_artisan_${UUID.randomUUID().toString().take(6)}",
                    title = title,
                    category = category,
                    origin = origin,
                    province = province,
                    artisanId = seller.id,
                    artisanName = seller.shopName,
                    sellerBio = seller.bio,
                    sellerPhone = seller.phone,
                    description = description,
                    priceMAD = priceMad,
                    stock = stock,
                    image = if (image.isNotBlank()) image else RegionalPhotoManifest.TAMEGROUTE_POTTERY,
                    galleryImages = listOf(if (image.isNotBlank()) image else RegionalPhotoManifest.TAMEGROUTE_POTTERY),
                    videoUrl = videoUrl,
                    model3dUrl = model3dUrl,
                    has3dModel = has3d,
                    isAuthentic = true,
                    placeId = "zagora",
                    pickupMeetingPoint = "${seller.shopName} (${origin})"
                )
                prodList.add(0, newProd)
            }
            current.copy(
                marketplaceProducts = prodList,
                editingProductId = null,
                toastMessage = if (id != null) "Product '$title' updated successfully!" else "New artisan product '$title' published to SIA Marketplace!"
            )
        }
        navigateTo(ActiveScreen.SELLER_DASHBOARD)
    }

    fun updateProductStock(productId: String, delta: Int) {
        _uiState.update { current ->
            val updated = current.marketplaceProducts.map { prod ->
                if (prod.id == productId) {
                    val newStock = (prod.stock + delta).coerceAtLeast(0)
                    prod.copy(stock = newStock)
                } else prod
            }
            current.copy(
                marketplaceProducts = updated,
                toastMessage = "Inventory quantity updated."
            )
        }
    }

    fun deleteProduct(productId: String) {
        _uiState.update { current ->
            val updated = current.marketplaceProducts.filterNot { it.id == productId }
            current.copy(
                marketplaceProducts = updated,
                toastMessage = "Product removed from catalog."
            )
        }
    }

    fun sendProductInquiry(
        productId: String,
        customerName: String,
        customerContact: String,
        message: String,
        quantity: Int
    ) {
        val prod = _uiState.value.marketplaceProducts.find { it.id == productId } ?: return
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val inquiry = ProductInquiry(
            id = "inq_${UUID.randomUUID().toString().take(6)}",
            productId = prod.id,
            productTitle = prod.title,
            customerName = customerName,
            customerContact = customerContact,
            message = message,
            quantity = quantity,
            inquiryDate = dateStr,
            status = "Pending"
        )
        _uiState.update { current ->
            current.copy(
                sellerInquiries = listOf(inquiry) + current.sellerInquiries,
                toastMessage = "Inquiry sent to ${prod.artisanName}! They will respond shortly."
            )
        }
    }

    fun answerProductInquiry(inquiryId: String, status: String, replyNote: String) {
        _uiState.update { current ->
            val updated = current.sellerInquiries.map { inq ->
                if (inq.id == inquiryId) {
                    inq.copy(status = status, replyNote = replyNote)
                } else inq
            }
            current.copy(
                sellerInquiries = updated,
                toastMessage = "Inquiry updated to '$status'"
            )
        }
    }

    fun openProduct3dViewer(productId: String) {
        val prod = _uiState.value.marketplaceProducts.find { it.id == productId }
        if (prod != null && prod.has3dModel) {
            _uiState.update { it.copy(active3dViewerProduct = prod) }
            navigateTo(ActiveScreen.PRODUCT_3D_VIEWER)
        } else {
            _uiState.update { it.copy(toastMessage = "3D model not available for this item.") }
        }
    }

    fun closeProduct3dViewer() {
        _uiState.update { it.copy(active3dViewerProduct = null) }
        navigateBack()
    }
}
