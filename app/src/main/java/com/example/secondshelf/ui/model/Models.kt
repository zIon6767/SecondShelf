package com.example.secondshelf.ui.model

import androidx.compose.ui.graphics.Color

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
    val lastNameChangeTimestamp: Long = 0L
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
