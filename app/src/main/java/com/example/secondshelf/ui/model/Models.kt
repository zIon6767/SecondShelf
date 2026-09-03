package com.example.secondshelf.ui.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val imageUrl: String? = null
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

/**
 * Formats a UTC ISO-8601 timestamp string from Supabase (e.g. "2026-09-04T04:15:30.123456+00:00")
 * into local time format "HH:mm" using the device's system default time zone.
 */
fun formatMessageTime(createdAt: String): String {
    if (createdAt.isBlank()) return ""
    return runCatching {
        val cleanTime = createdAt.replace("Z", "+0000").replace(Regex("([+-]\\d{2}):(\\d{2})$"), "$1$2")
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(cleanTime) ?: return createdAt

        val outputFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getDefault()
        }
        outputFormat.format(date)
    }.getOrElse {
        if (createdAt.contains(":")) createdAt.substringAfter("T").take(5) else createdAt
    }
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
