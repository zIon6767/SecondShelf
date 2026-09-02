package com.example.secondshelf.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.secondshelf.ui.model.*
import java.util.UUID

class MarketplaceViewModel : ViewModel() {

    private var isProfileManuallyEdited = false

    var currentUser by mutableStateOf(mockUsers.first { it.id == CURRENT_USER_ID })
        private set

    fun syncAuthUser(fullName: String, email: String = "") {
        if (!isProfileManuallyEdited) {
            val updatedName = if (fullName.isNotBlank() && fullName != "User") fullName.trim() else currentUser.name
            val updatedEmail = if (email.isNotBlank() && email != "user@example.com") email.trim() else currentUser.email
            currentUser = currentUser.copy(
                name = updatedName,
                email = updatedEmail,
                avatarInitial = updatedName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
            )
        }
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
        private set

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
    }

    fun updateMarketplaceFilters(filters: ListingFilters) {
        marketplaceFilters = filters
    }

    fun patchMarketplaceFilters(update: ListingFilters.() -> ListingFilters) {
        marketplaceFilters = marketplaceFilters.update()
    }

    fun getListing(id: String) = listings.find { it.id == id }

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

        val query = filters.query.trim().lowercase()
        if (query.isNotEmpty()) {
            result = result.filter { listing ->
                val seller = getSellerName(listing.sellerId).lowercase()
                listing.title.lowercase().contains(query) ||
                    listing.description.lowercase().contains(query) ||
                    listing.category.label.lowercase().contains(query) ||
                    seller.contains(query)
            }
        }

        result = when (filters.sort) {
            SortOption.PRICE_ASC -> result.sortedBy { it.price }
            SortOption.PRICE_DESC -> result.sortedByDescending { it.price }
            SortOption.RELEVANCE -> {
                if (query.isEmpty()) result.sortedByDescending { it.createdAt }
                else result.sortedByDescending { relevanceScore(it, query) }
            }
            SortOption.NEWEST -> result.sortedByDescending { it.createdAt }
        }

        return result
    }

    private fun relevanceScore(listing: Listing, query: String): Int {
        var score = 0
        val title = listing.title.lowercase()
        if (title.contains(query)) score += 10
        if (title.startsWith(query)) score += 5
        if (listing.description.lowercase().contains(query)) score += 3
        return score
    }

    fun getMyListings() = listings.filter { it.sellerId == CURRENT_USER_ID }

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
            sellerId = CURRENT_USER_ID,
            location = location.trim(),
            createdAt = "Today"
        )

        listings = listOf(listing) + listings
        notifications = listOf(
            AppNotification(
                id = "notif-${UUID.randomUUID().toString().take(8)}",
                userId = CURRENT_USER_ID,
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
            senderId = CURRENT_USER_ID,
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
                ((msg.senderId == participantId && msg.receiverId == CURRENT_USER_ID) ||
                    (msg.senderId == CURRENT_USER_ID && msg.receiverId == participantId))
        }
    }

    fun markConversationRead(participantId: String, listingId: String) {
        messages = messages.map { msg ->
            if (msg.senderId == participantId && msg.receiverId == CURRENT_USER_ID && msg.listingId == listingId) {
                msg.copy(read = true)
            } else msg
        }
        conversations = conversations.map { conv ->
            if (conv.participantId == participantId && conv.listingId == listingId) {
                conv.copy(unreadCount = 0)
            } else conv
        }
    }

    fun updateProfile(name: String, email: String, university: String, campus: String): String? {
        if (name.isBlank()) return "Name is required"
        if (email.isBlank()) return "Email is required"
        isProfileManuallyEdited = true
        currentUser = currentUser.copy(
            name = name.trim(),
            email = email.trim(),
            university = university.trim(),
            campus = campus.trim(),
            avatarInitial = name.trim().first().uppercaseChar().toString()
        )
        showToast("Profile updated")
        return null
    }

    fun showToast(message: String) {
        toastMessage = message
    }

    fun dismissToast() {
        toastMessage = null
    }
}
