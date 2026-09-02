package com.example.secondshelf.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.secondshelf.ui.model.*
import java.util.UUID

class MarketplaceViewModel : ViewModel() {

    var currentUser by mutableStateOf(
        User(
            id = "user-current",
            name = "Student",
            email = "",
            university = "TAR UMT",
            campus = "KL Main Campus"
        )
    )
        private set

    fun syncAuthUser(
        id: String = "",
        name: String = "",
        email: String = "",
        university: String = "",
        campus: String = ""
    ) {
        val cleanId = id.ifBlank { currentUser.id }
        val cleanEmail = if (email.isNotBlank() && email != "user@example.com" && email != "null") email else currentUser.email
        val cleanName = if (name.isNotBlank() && name != "User" && name != "null" && name != "Student") {
            name
        } else if (cleanEmail.isNotBlank()) {
            val prefix = cleanEmail.substringBefore("@").replace(".", " ").replace("_", " ")
            prefix.split(" ").filter { it.isNotBlank() }.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        } else if (currentUser.name.isNotBlank() && currentUser.name != "Student") {
            currentUser.name
        } else {
            "Student"
        }
        val cleanUni = if (university.isNotBlank() && university != "null") university else currentUser.university
        val cleanCampus = if (campus.isNotBlank() && campus != "null") campus else currentUser.campus
        val initial = if (cleanName.isNotBlank() && cleanName != "Student") {
            cleanName.first().uppercaseChar().toString()
        } else if (cleanEmail.isNotBlank()) {
            cleanEmail.first().uppercaseChar().toString()
        } else {
            "S"
        }
        currentUser = currentUser.copy(
            id = cleanId,
            name = cleanName,
            email = cleanEmail,
            university = cleanUni,
            campus = cleanCampus,
            avatarInitial = initial
        )
    }

    var listings by mutableStateOf(initialListings)
        private set

    var favorites by mutableStateOf(setOf<String>())
        private set

    var notifications by mutableStateOf(initialNotifications)
        private set

    var messages by mutableStateOf(initialMessages)
        private set

    var conversations by mutableStateOf(initialConversations)
        private set

    var homeCategory by mutableStateOf(Category.ALL)

    var searchQuery by mutableStateOf("")
        private set

    var marketplaceFilters by mutableStateOf(ListingFilters())
        private set

    var toastMessage by mutableStateOf<String?>(null)
        private set

    fun updateHomeCategory(category: Category) {
        homeCategory = category
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        marketplaceFilters = marketplaceFilters.copy(query = query)
    }

    fun updateFilters(filters: ListingFilters) {
        marketplaceFilters = filters
    }

    fun patchMarketplaceFilters(update: ListingFilters.() -> ListingFilters) {
        marketplaceFilters = marketplaceFilters.update()
    }

    fun getListing(id: String): Listing? = listings.find { it.id == id }

    fun isFavorite(listingId: String) = favorites.contains(listingId)

    fun toggleFavorite(listingId: String) {
        val listing = getListing(listingId)
        favorites = if (favorites.contains(listingId)) {
            showToast("Removed from saved items")
            favorites - listingId
        } else {
            showToast("Saved \"${listing?.title ?: "item"}\"")
            favorites + listingId
        }
    }

    fun getUnreadNotificationCount() = notifications.count { !it.read }

    fun markNotificationRead(id: String) {
        notifications = notifications.map { if (it.id == id) it.copy(read = true) else it }
    }

    fun markAllNotificationsRead() {
        notifications = notifications.map { it.copy(read = true) }
    }

    fun clearNotification(id: String) {
        notifications = notifications.filter { it.id != id }
    }

    fun filterListings(filters: ListingFilters = marketplaceFilters): List<Listing> {
        var result = listings.toList()

        if (filters.category != Category.ALL) {
            result = result.filter { it.category == filters.category }
        }

        filters.condition?.let { cond ->
            result = result.filter { it.condition == cond }
        }

        val rawQuery = filters.query.trim().lowercase()
        if (rawQuery.isNotEmpty()) {
            val queryTokens = rawQuery.split(Regex("\\s+")).filter { it.isNotEmpty() }

            result = result.filter { listing ->
                val titleWords = listing.title.lowercase().split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
                val descWords = listing.description.lowercase().split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
                val categoryWords = listing.category.label.lowercase().split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
                val sellerWords = getSellerName(listing.sellerId).lowercase().split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }

                // Every token in the query must match at least one relevant attribute
                queryTokens.all { token ->
                    // 1. Category name match (e.g. "book" matches "Books", "cloth" matches "Clothing")
                    categoryWords.any { it.startsWith(token) || token.startsWith(it) } ||
                    // 2. Title word match (e.g. "table" matches "Table", "calculus" matches "Calculus")
                    titleWords.any { word -> word.startsWith(token) || (token.length >= 4 && word.contains(token)) } ||
                    // 3. Description word match (must start with token to avoid "suitable" matching "table")
                    descWords.any { word -> word.startsWith(token) } ||
                    // 4. Seller name match
                    sellerWords.any { it.startsWith(token) }
                }
            }
        }

        result = when (filters.sort) {
            SortOption.PRICE_ASC -> result.sortedBy { it.price }
            SortOption.PRICE_DESC -> result.sortedByDescending { it.price }
            SortOption.RELEVANCE -> {
                if (rawQuery.isEmpty()) result.sortedByDescending { it.createdAt }
                else result.sortedByDescending { relevanceScore(it, rawQuery) }
            }
            SortOption.NEWEST -> result.sortedByDescending { it.createdAt }
        }

        return result
    }

    private fun relevanceScore(listing: Listing, query: String): Int {
        var score = 0
        val title = listing.title.lowercase()
        val cat = listing.category.label.lowercase()
        if (title == query) score += 40
        if (title.startsWith(query)) score += 25
        if (cat.contains(query) || query.contains(cat)) score += 20
        if (title.contains(query)) score += 15
        val descWords = listing.description.lowercase().split(Regex("[^a-z0-9]+"))
        if (descWords.any { it.startsWith(query) }) score += 5
        return score
    }

    fun getMyListings() = listings.filter { it.sellerId == currentUser.id }

    fun getSavedListings() = listings.filter { favorites.contains(it.id) }

    fun createListing(
        title: String,
        description: String,
        price: Int,
        category: Category,
        condition: Condition,
        location: String
    ): String? {
        if (title.isBlank()) return "Item name is required"
        if (description.isBlank()) return "Description is required"
        if (price <= 0) return "Price must be greater than 0"
        if (location.isBlank()) return "Location is required"
        if (category == Category.ALL) return "Please select a category"

        val id = "listing-${UUID.randomUUID().toString().take(8)}"
        val listing = Listing(
            id = id,
            title = title.trim(),
            description = description.trim(),
            price = price,
            category = category,
            condition = condition,
            sellerId = currentUser.id,
            location = location.trim(),
            createdAt = "Today"
        )

        listings = listOf(listing) + listings
        notifications = listOf(
            AppNotification(
                id = "notif-${UUID.randomUUID().toString().take(8)}",
                userId = currentUser.id,
                type = NotificationType.LISTING_POSTED,
                title = "Listing Posted",
                message = "\"${listing.title}\" was successfully posted!",
                time = "Just now",
                read = false
            )
        ) + notifications

        showToast("Listing created successfully!")
        return null
    }

    fun updateListing(
        id: String,
        title: String,
        description: String,
        price: Int,
        category: Category,
        condition: Condition,
        location: String
    ): String? {
        if (title.isBlank()) return "Item name is required"
        if (description.isBlank()) return "Description is required"
        if (price <= 0) return "Price must be greater than 0"
        if (location.isBlank()) return "Location is required"
        if (category == Category.ALL) return "Please select a category"

        listings = listings.map { listing ->
            if (listing.id == id) {
                listing.copy(
                    title = title.trim(),
                    description = description.trim(),
                    price = price,
                    category = category,
                    condition = condition,
                    location = location.trim()
                )
            } else listing
        }
        showToast("Listing updated")
        return null
    }

    fun deleteListing(id: String) {
        listings = listings.filter { it.id != id }
        favorites = favorites - id
        showToast("Listing deleted")
    }

    fun sendMessage(listingId: String, receiverId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val msg = Message(
            id = "msg-${UUID.randomUUID().toString().take(8)}",
            senderId = currentUser.id,
            receiverId = receiverId,
            listingId = listingId,
            text = trimmed,
            timestamp = "Just now",
            read = false
        )
        messages = messages + msg

        val existing = conversations.find { it.participantId == receiverId && it.listingId == listingId }
        conversations = if (existing != null) {
            conversations.map { conv ->
                if (conv.id == existing.id) conv.copy(lastMessage = trimmed, lastTimestamp = "Just now")
                else conv
            }
        } else {
            listOf(
                Conversation(
                    id = "conv-${UUID.randomUUID().toString().take(8)}",
                    listingId = listingId,
                    participantId = receiverId,
                    lastMessage = trimmed,
                    lastTimestamp = "Just now"
                )
            ) + conversations
        }
        showToast("Message sent")
    }

    fun getConversationMessages(participantId: String, listingId: String): List<Message> {
        return messages.filter { msg ->
            msg.listingId == listingId &&
                ((msg.senderId == participantId && msg.receiverId == currentUser.id) ||
                    (msg.senderId == currentUser.id && msg.receiverId == participantId))
        }
    }

    fun markConversationRead(participantId: String, listingId: String) {
        messages = messages.map { msg ->
            if (msg.senderId == participantId && msg.receiverId == currentUser.id && msg.listingId == listingId) {
                msg.copy(read = true)
            } else msg
        }
        conversations = conversations.map { conv ->
            if (conv.participantId == participantId && conv.listingId == listingId) {
                conv.copy(unreadCount = 0)
            } else conv
        }
    }

    fun getDaysUntilNextNameChange(): Int {
        val lastChange = currentUser.lastNameChangeTimestamp
        if (lastChange == 0L) return 0
        val fourteenDaysMs = 14L * 24 * 60 * 60 * 1000L
        val elapsed = System.currentTimeMillis() - lastChange
        if (elapsed >= fourteenDaysMs) return 0
        val remainingMs = fourteenDaysMs - elapsed
        return kotlin.math.ceil(remainingMs.toDouble() / (24.0 * 60 * 60 * 1000)).toInt()
    }

    fun canChangeName(): Boolean = getDaysUntilNextNameChange() == 0

    fun updateProfile(name: String, email: String, university: String, campus: String): String? {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return "Name is required"
        if (email.isBlank()) return "Email is required"

        var newLastNameChange = currentUser.lastNameChangeTimestamp
        if (trimmedName != currentUser.name) {
            val daysLeft = getDaysUntilNextNameChange()
            if (daysLeft > 0) {
                return "You can only change your name once every 14 days. Please try again in $daysLeft day${if (daysLeft > 1) "s" else ""}."
            }
            newLastNameChange = System.currentTimeMillis()
        }

        currentUser = currentUser.copy(
            name = trimmedName,
            email = email.trim(),
            university = university.trim(),
            campus = campus.trim(),
            avatarInitial = trimmedName.first().uppercaseChar().toString(),
            lastNameChangeTimestamp = newLastNameChange
        )
        showToast("Profile updated successfully")
        return null
    }

    fun clearAllUserData() {
        listings = initialListings
        favorites = emptySet()
        notifications = emptyList()
        messages = emptyList()
        conversations = emptyList()
        currentUser = User(
            id = "user-current",
            name = "",
            email = "",
            university = "TAR UMT",
            campus = "KL Main Campus"
        )
        homeCategory = Category.ALL
        searchQuery = ""
        toastMessage = null
    }

    fun showToast(message: String) {
        toastMessage = message
    }

    fun dismissToast() {
        toastMessage = null
    }
}
