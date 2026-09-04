package com.example.secondshelf.ui.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Category(val label: String) {
    ALL("All"),
    BOOKS("Books"),
    ELECTRONICS("Electronics"),
    STATIONERY("Stationery"),
    CLOTHING("Clothing"),
    LAB_EQUIPMENT("Lab Equipment"),
    FURNITURE("Furniture"),
    OTHER("Other")
}

enum class Condition(val label: String) {
    LIKE_NEW("Like New"),
    GOOD("Good"),
    FAIR("Fair")
}

enum class SortOption(val label: String) {
    NEWEST("Newest"),
    PRICE_ASC("Price: Low to High"),
    PRICE_DESC("Price: High to Low"),
    RELEVANCE("Relevance")
}

enum class NotificationType {
    SAVED, MESSAGE, INTEREST, DEAL, LISTING_POSTED
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val university: String,
    val campus: String,
    val avatarInitial: String = if (name.isNotBlank()) name.first().uppercaseChar().toString() else "U",
    val avatarUrl: String = "",
    val lastNameChangeTimestamp: Long = 0L,
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val bio: String = "TAR UMT Student"
)

data class Review(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarInitial: String = if (authorName.isNotBlank()) authorName.first().uppercaseChar().toString() else "U",
    val targetUserId: String,
    val listingTitle: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val tags: List<String> = emptyList(),
    val timeAgo: String = "Just now"
)

data class Listing(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val category: Category,
    val condition: Condition,
    val sellerId: String,
    val location: String,
    val createdAt: String,
    val cardColor: Color = Color(0xFFE8F0FE),
    val imageUrl: String? = null,
    val sellerName: String = "",
    val sellerCampus: String = "",
    val sellerAvatarUrl: String = "",
    val isOwnedByCurrentUser: Boolean = false
)

data class AppNotification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val time: String,
    val read: Boolean = false
)

data class Message(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val listingId: String,
    val text: String,
    val timestamp: String,
    val read: Boolean = false
)

data class Conversation(
    val id: String,
    val listingId: String,
    val participantId: String,
    val lastMessage: String,
    val lastTimestamp: String,
    val unreadCount: Int = 0
)

data class ListingFilters(
    val category: Category = Category.ALL,
    val condition: Condition? = null,
    val sort: SortOption = SortOption.NEWEST,
    val query: String = ""
)

fun formatPrice(price: Int) = "RM $price"

fun formatListingDate(value: String): String {
    val date = value.take(10)
    val parts = date.split("-")
    if (parts.size != 3) return value

    val month = when (parts[1]) {
        "01" -> "Jan"
        "02" -> "Feb"
        "03" -> "Mar"
        "04" -> "Apr"
        "05" -> "May"
        "06" -> "Jun"
        "07" -> "Jul"
        "08" -> "Aug"
        "09" -> "Sep"
        "10" -> "Oct"
        "11" -> "Nov"
        "12" -> "Dec"
        else -> return value
    }
    val day = parts[2].trimStart('0').ifBlank { "0" }
    return "$day $month ${parts[0]}"
}

// ── Supabase Realtime Chat Models ──────────────────────────────────────────────

/**
 * Mirrors the `messages` table row in Supabase.
 * Field names use @SerialName to match the DB's snake_case columns.
 */
@Serializable
data class ChatMessage(
    val id: String = "",
    @SerialName("sender_id")   val senderId: String = "",
    @SerialName("receiver_id") val receiverId: String = "",
    @SerialName("listing_id")  val listingId: String = "",
    val content: String = "",
    @SerialName("is_read")     val isRead: Boolean = false,
    @SerialName("created_at")  val createdAt: String = ""
)

/**
 * Payload tracked via the Supabase Presence API.
 * Each connected user broadcasts this struct on the shared channel.
 */
@Serializable
data class PresenceState(
    @SerialName("user_id")   val userId: String,
    @SerialName("online_at") val onlineAt: String = ""
)
