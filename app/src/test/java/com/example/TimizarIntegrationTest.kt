package com.example

import com.example.data.TimizarDatabase
import com.example.model.BookingStatus
import com.example.model.PaymentStatus
import com.example.model.ServiceCategory
import com.example.state.ActiveScreen
import com.example.state.TimizarViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TimizarIntegrationTest {

    private lateinit var viewModel: TimizarViewModel

    @Before
    fun setUp() {
        viewModel = TimizarViewModel()
    }

    @Test
    fun testInitialDatabaseIntegrity() {
        // Assert shared single source of truth holds places, providers, and services
        assertTrue("Destinations must not be empty", TimizarDatabase.destinations.isNotEmpty())
        assertTrue("Services must not be empty", TimizarDatabase.services.isNotEmpty())
        assertTrue("Providers must not be empty", TimizarDatabase.providers.isNotEmpty())

        val todra = TimizarDatabase.destinations.find { it.id == "tinghir" }
        assertNotNull(todra)
        assertEquals("Tinghir & Oasis", todra?.name)

        val guide = TimizarDatabase.services.find { it.category == ServiceCategory.GUIDE }
        assertNotNull(guide)
        assertEquals("ouarzazate", guide?.placeId)
    }

    @Test
    fun testScenario1_BookingLifecycleAndStatePropagation() {
        // Tourist submits booking request
        val service = TimizarDatabase.services.first()
        viewModel.requestBooking(
            serviceId = service.id,
            date = "2026-10-15",
            guestsCount = 2,
            specialRequests = "Berber tea ceremony"
        )

        val latestBooking = viewModel.uiState.value.bookings.first()
        assertEquals(BookingStatus.PENDING, latestBooking.status)
        assertEquals(PaymentStatus.UNPAID, latestBooking.paymentStatus)
        assertEquals(service.id, latestBooking.serviceId)

        // Provider accepts booking in Partner Dashboard
        viewModel.updateBookingStatusByProvider(latestBooking.id, BookingStatus.CONFIRMED)
        val confirmedBooking = viewModel.uiState.value.bookings.find { it.id == latestBooking.id }
        assertEquals(BookingStatus.CONFIRMED, confirmedBooking?.status)

        // Sandbox payment is processed only after confirmation
        viewModel.processPaymentSimulation(latestBooking.id, success = true)
        val paidBooking = viewModel.uiState.value.bookings.find { it.id == latestBooking.id }
        assertEquals(PaymentStatus.PAID, paidBooking?.paymentStatus)
    }

    @Test
    fun testScenario2_CommunityToTripIntegration() {
        // Bookmark and add a destination from community flow into Trip Planner
        val placeId = "zagora"
        viewModel.toggleSavePlace(placeId)
        assertTrue(viewModel.uiState.value.savedPlaceIds.contains(placeId))

        viewModel.addDestinationToTrip(placeId)
        val trip = viewModel.uiState.value.trips.find { it.id == viewModel.uiState.value.activeTripId }
        assertNotNull(trip)
        assertTrue("Trip stops must contain added place", trip!!.stops.any { it.placeId == placeId })
    }

    @Test
    fun testScenario3_DigitalPassportVerification() {
        val testPlace = "merzouga"
        val verified = viewModel.claimPassportStamp(testPlace, "TKZ-QR-TEST-TOKEN")
        assertTrue(verified)

        val stamp = viewModel.uiState.value.passportStamps.find { it.placeId == testPlace }
        assertNotNull(stamp)
        assertEquals("Merzouga Dunes", stamp?.placeName)
    }

    @Test
    fun testScenario4_AiItineraryToTripPlanner() {
        viewModel.sendUserChatMessage("Plan 3-day trip to Merzouga")
        val chat = viewModel.uiState.value.chatMessages.last()
        assertNotNull(chat)

        // Save AI generated trip
        chat.attachedTrip?.let { trip ->
            viewModel.saveAiGeneratedTrip(trip)
            val saved = viewModel.uiState.value.trips.find { it.id == trip.id }
            assertNotNull(saved)
            assertEquals(ActiveScreen.TRIP_PLANNER, viewModel.uiState.value.currentScreen)
        }
    }

    // JOURNEY A: Home -> Explore -> Destination -> Video -> Add to Trip
    @Test
    fun testJourneyA_Home_Explore_Destination_AddToTrip() {
        assertEquals(ActiveScreen.HOME, viewModel.uiState.value.currentScreen)

        viewModel.navigateTo(ActiveScreen.EXPLORE)
        assertEquals(ActiveScreen.EXPLORE, viewModel.uiState.value.currentScreen)

        viewModel.selectPlace("ait_ben_haddou")
        assertEquals(ActiveScreen.DESTINATION_DETAIL, viewModel.uiState.value.currentScreen)
        assertEquals("ait_ben_haddou", viewModel.uiState.value.selectedPlaceId)

        val destination = TimizarDatabase.destinations.find { it.id == "ait_ben_haddou" }
        assertNotNull(destination)
        assertTrue("Verified video stream should be present", destination!!.hasVerifiedVideo)
        assertTrue("Video URL must not be blank", destination.videoUrl.isNotBlank())

        viewModel.addDestinationToTrip("ait_ben_haddou")
        val activeTrip = viewModel.uiState.value.trips.find { it.id == viewModel.uiState.value.activeTripId }
        assertNotNull(activeTrip)
        assertTrue("Active trip must include Ait Benhaddou stop", activeTrip!!.stops.any { it.placeId == "ait_ben_haddou" })
    }

    // JOURNEY B: Community -> Like/Save Post -> Featured Discover Personalization
    @Test
    fun testJourneyB_Community_LikePost_FeaturedDiscoverPersonalization() {
        viewModel.navigateTo(ActiveScreen.COMMUNITY)
        assertEquals(ActiveScreen.COMMUNITY, viewModel.uiState.value.currentScreen)

        val postToLike = "post_1"
        viewModel.toggleLikeCommunityPost(postToLike)
        assertTrue("Post must be recorded in likedCommunityPostIds", viewModel.uiState.value.likedCommunityPostIds.contains(postToLike))

        // Toggle user travel interest
        viewModel.toggleUserInterest("Desert")
        assertTrue(viewModel.uiState.value.userInterests.contains("Desert"))

        // Verify dynamic personalization recommendation generation
        val recommendations = viewModel.getPersonalizedRecommendations()
        assertTrue("Recommendations must not be empty", recommendations.isNotEmpty())
        assertTrue("Top recommendations should have match scores", recommendations.first().matchScore > 0)
        assertTrue("Explanation reason should provide transparency", recommendations.first().explanationReason.isNotBlank())
    }

    // JOURNEY C: ASK SIA -> Travel Preferences -> AI Planner -> Generated Itinerary
    @Test
    fun testJourneyC_AskSia_TravelPreferences_AiPlanner_GeneratedItinerary() {
        viewModel.navigateTo(ActiveScreen.AI_ASSISTANT)
        assertEquals(ActiveScreen.AI_ASSISTANT, viewModel.uiState.value.currentScreen)

        // User asks ASK SIA for journey itinerary
        viewModel.sendUserChatMessage("Plan a three-day journey around Ouarzazate")
        val messages = viewModel.uiState.value.chatMessages
        assertTrue("Chat messages must contain conversation", messages.size >= 2)

        val reply = messages.find { it.sender == "sia_ai" && it.attachedTrip != null }
        assertNotNull("ASK SIA must attach structured itinerary", reply)

        // Save AI generated itinerary
        reply!!.attachedTrip?.let { trip ->
            viewModel.saveAiGeneratedTrip(trip)
            assertEquals(ActiveScreen.TRIP_PLANNER, viewModel.uiState.value.currentScreen)
            val savedTrip = viewModel.uiState.value.trips.find { it.id == trip.id }
            assertNotNull(savedTrip)
        }
    }

    // JOURNEY D: Seller Login -> Upload Product -> Marketplace -> Product Details -> 3D Preview
    @Test
    fun testJourneyD_SellerLogin_UploadProduct_Marketplace_3DPreview() {
        // 1. Switch to Seller Hub
        viewModel.switchToSellerMode(true)
        assertEquals(ActiveScreen.SELLER_DASHBOARD, viewModel.uiState.value.currentScreen)
        assertTrue(viewModel.uiState.value.isSellerMode)

        val initialProductCount = viewModel.uiState.value.marketplaceProducts.size

        // 2. Publish new artisan craft
        viewModel.saveProduct(
            id = null,
            title = "Test Artisanal Green Tagine",
            category = "Tamegroute pottery",
            description = "Hand-thrown underground kiln pottery with green copper glaze.",
            origin = "Tamegroute",
            province = "Zagora Province",
            priceMad = 340,
            stock = 10,
            image = "https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/Zagora_maroc.jpg/1280px-Zagora_maroc.jpg",
            videoUrl = "",
            model3dUrl = "test_tagine.glb"
        )

        assertEquals("Product count should increase by 1", initialProductCount + 1, viewModel.uiState.value.marketplaceProducts.size)
        val addedProduct = viewModel.uiState.value.marketplaceProducts.first()
        assertEquals("Test Artisanal Green Tagine", addedProduct.title)
        assertTrue("Product should be flagged with has3dModel", addedProduct.has3dModel)

        // 3. Navigate to Marketplace
        viewModel.switchToSellerMode(false)
        assertEquals(ActiveScreen.LOCAL_MARKET, viewModel.uiState.value.currentScreen)

        // 4. Open product in 3D viewer
        viewModel.openProduct3dViewer(addedProduct.id)
        assertEquals(ActiveScreen.PRODUCT_3D_VIEWER, viewModel.uiState.value.currentScreen)
        assertNotNull("Active 3D product must be set", viewModel.uiState.value.active3dViewerProduct)
        assertEquals(addedProduct.id, viewModel.uiState.value.active3dViewerProduct?.id)

        // 5. Close 3D viewer
        viewModel.closeProduct3dViewer()
        assertNull("Active 3D product should be cleared", viewModel.uiState.value.active3dViewerProduct)
    }
}
