package com.example.model

enum class ServiceCategory(val label: String, val iconName: String) {
    HOTEL("Stays", "hotel"),
    GUIDE("Guides", "person_pin"),
    ACTIVITY("Activities", "explore"),
    COOPERATIVE("Cooperatives", "store"),
    RESTAURANT("Restaurants", "restaurant"),
    TRANSPORT("Transport", "directions_car")
}

enum class BookingStatus(val label: String) {
    DRAFT("Draft"),
    PENDING("Pending Review"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    REJECTED("Declined"),
    CANCELLED("Cancelled")
}

enum class PaymentStatus(val label: String) {
    UNPAID("Unpaid"),
    PENDING("Processing"),
    PAID("Paid (Verified Sandbox)"),
    FAILED("Failed"),
    REFUNDED("Refunded")
}

enum class RoadCondition(val label: String, val level: String) {
    CLEAR("Open & Smooth", "Safe"),
    CAUTION("Pass Windy / Light Gravel", "Caution"),
    ALERT("High Pass Snow / Flood Alert", "Hazard"),
    UNKNOWN("No Recent Report", "Unknown")
}

data class Destination(
    val id: String,
    val name: String,
    val berberName: String,
    val province: String,
    val categoryTag: String, // "UNESCO", "Nature", "Desert", "Kasbah", "Oasis"
    val description: String,
    val longDescription: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Int,
    val rating: Double = 4.8,
    val reviewsCount: Int = 320,
    val distanceText: String = "2h from Ouarzazate",
    val openingHours: String = "Open daily 8:00 - 18:00",
    val highlights: List<String>,
    val imageUrl: String,
    val bestTimeToVisit: String,
    val culturalTip: String,
    val passportEligible: Boolean = true,
    val galleryImages: List<String> = emptyList(),
    val videoUrl: String = "",
    val videoTitle: String = "",
    val videoDuration: String = "01:45",
    val hasVerifiedVideo: Boolean = true,
    val mediaAttribution: String = "Wikimedia Commons / Drâa-Tafilalet Heritage Documentation"
)

data class RecommendationItem(
    val destination: Destination,
    val matchScore: Int,
    val explanationReason: String,
    val matchedTag: String
)

data class Provider(
    val id: String,
    val name: String,
    val role: String,
    val phone: String,
    val location: String,
    val rating: Double,
    val reviewsCount: Int,
    val verified: Boolean = true,
    val avatarUrl: String,
    val bio: String
)

data class ServiceItem(
    val id: String,
    val providerId: String,
    val placeId: String,
    val title: String,
    val category: ServiceCategory,
    val priceMad: Int,
    val durationText: String,
    val rating: Double,
    val reviewsCount: Int,
    val description: String,
    val features: List<String>,
    val imageUrl: String,
    val availableDates: List<String>,
    val maxGuests: Int = 10,
    val meetingPoint: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val videoUrl: String = "",
    val videoTitle: String = "",
    val videoDuration: String = "01:20",
    val hasVerifiedVideo: Boolean = true,
    val galleryImages: List<String> = emptyList()
)

data class TransportOffer(
    val id: String,
    val providerId: String,
    val originPlaceId: String,
    val destinationPlaceId: String,
    val vehicleType: String,
    val priceMadPerSeat: Int,
    val departureTimes: List<String>,
    val estimatedDurationHours: Double,
    val imageUrl: String,
    val airConditioned: Boolean = true,
    val luggageCapacity: String = "2 large bags"
)

data class TripStop(
    val stopId: String,
    val placeId: String,
    val dayNumber: Int,
    val orderInDay: Int,
    val notes: String = "",
    val attachedServiceId: String? = null
)

data class TripItinerary(
    val id: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val numberOfTravelers: Int,
    val stops: List<TripStop>,
    val estimatedBudgetMad: Int,
    val notes: String,
    val isDownloadedOffline: Boolean = false,
    val lastSyncTimestamp: String = "Just now"
)

data class BookingRecord(
    val id: String,
    val userId: String,
    val serviceId: String,
    val serviceTitle: String,
    val placeId: String,
    val category: ServiceCategory,
    val providerId: String,
    val providerName: String,
    val date: String,
    val guestsCount: Int,
    val totalAmountMad: Int,
    val status: BookingStatus,
    val paymentStatus: PaymentStatus,
    val specialRequests: String = "",
    val createdAt: String
)

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val placeId: String,
    val title: String,
    val content: String,
    val practicalTip: String,
    val imageUrl: String = "",
    val likesCount: Int,
    val commentsCount: Int = 14,
    val timestamp: String,
    val isVerifiedLocalTip: Boolean = false,
    val tags: List<String>
)

data class PassportStamp(
    val id: String,
    val placeId: String,
    val placeName: String,
    val verifiedDate: String,
    val badgeIcon: String,
    val regionalTrophyTitle: String,
    val tokenHash: String
)

data class RoadAlert(
    val id: String,
    val routeCode: String,
    val roadName: String,
    val condition: RoadCondition,
    val reportedAt: String,
    val sourceDescription: String,
    val warningMessage: String
)

data class LocalProduct(
    val id: String,
    val title: String,
    val category: String, // "Tamegroute pottery", "Medjool dates", "Rose Valley products", "Traditional Amazigh weaving", "Handmade baskets", "Traditional textiles", "Regional foods", "Souvenirs"
    val origin: String,
    val province: String,
    val artisanId: String = "artisan_local",
    val artisanName: String,
    val sellerBio: String = "Verified artisan cooperative dedicated to Drâa-Tafilalet heritage preservation.",
    val sellerPhone: String = "+212 661 23 45 67",
    val sellerRating: Double = 4.9,
    val description: String,
    val priceMAD: Int,
    val stock: Int,
    val image: String,
    val galleryImages: List<String> = emptyList(),
    val videoUrl: String = "",
    val model3dUrl: String = "",
    val has3dModel: Boolean = false,
    val isAuthentic: Boolean = true,
    val tags: List<String> = emptyList(),
    val placeId: String = "zagora",
    val pickupMeetingPoint: String = "Cooperative Storefront",
    val reviewsCount: Int = 34,
    val favoriteCount: Int = 19
) {
    val priceMad: Int get() = priceMAD
    val stockCount: Int get() = stock
    val imageUrl: String get() = image
}

data class SellerProfile(
    val id: String = "seller_tamegroute",
    val name: String = "Master Artisan Brahim",
    val shopName: String = "Coopérative Potiers de Tamegroute",
    val bio: String = "Traditional green enamel ceramics workshop passing down ancestral 16th-century glaze techniques in Zagora.",
    val city: String = "Tamegroute",
    val province: String = "Zagora Province",
    val phone: String = "+212 524 84 10 20",
    val rating: Double = 4.95,
    val salesCount: Int = 284,
    val avatarUrl: String = "https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/Zagora_maroc.jpg/1280px-Zagora_maroc.jpg",
    val bannerUrl: String = "https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/Zagora_maroc.jpg/1280px-Zagora_maroc.jpg",
    val verifiedCooperativeNumber: String = "COP-DRAA-2024-88",
    val isVerified: Boolean = true,
    val email: String = "contact@tamegroute-crafts.ma"
)

data class ProductInquiry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val productId: String,
    val productTitle: String,
    val customerName: String,
    val customerContact: String,
    val message: String,
    val quantity: Int = 1,
    val inquiryDate: String,
    val status: String = "Pending", // "Pending", "Confirmed", "Answered"
    val replyNote: String = ""
)

