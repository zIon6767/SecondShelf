package com.example.secondshelf.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondshelf.supabase
import com.example.secondshelf.data.ChatRepository
import com.example.secondshelf.ui.model.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONArray
import java.util.UUID

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val chatRepository = ChatRepository()

    // ── Current authenticated user state ──
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

    // ── Chat-settings SharedPreferences ──
    private val chatPrefs = application.getSharedPreferences("second_shelf_chat_settings", Context.MODE_PRIVATE)

    val showMessagePreviews: Boolean
        get() = runCatching { chatPrefs.getBoolean("msg_previews", true) }.getOrDefault(true)

    val showOnlineStatus: Boolean
        get() = runCatching { chatPrefs.getBoolean("online_status", true) }.getOrDefault(true)

    val showReadReceipts: Boolean
        get() = runCatching { chatPrefs.getBoolean("read_receipts", true) }.getOrDefault(true)

    // ── Blocked user IDs ──
    private fun blockedPrefsKey(userId: String? = null): String {
        // IMPORTANT: We use a safe fallback if currentUser isn't accessible yet
        val uid = userId ?: try { currentUser.id } catch(e: Exception) { "user-current" }
        return "blocked_ids_${uid.ifBlank { "user-current" }}"
    }

    var blockedUserIds by mutableStateOf(setOf<String>())
        private set

    private fun loadBlockedIds(initialUid: String = "user-current") {
        runCatching {
            val key = blockedPrefsKey(initialUid)
            blockedUserIds = chatPrefs.getStringSet(key, emptySet())?.toSet() ?: emptySet()
        }.onFailure {
            blockedUserIds = emptySet()
        }
    }

    fun isBlocked(userId: String): Boolean = userId in blockedUserIds

    fun blockUser(userId: String) {
        runCatching {
            val updated = blockedUserIds + userId
            blockedUserIds = updated
            chatPrefs.edit().putStringSet(blockedPrefsKey(), updated).apply()
            showToast("Account blocked")
        }
    }

    fun unblockUser(userId: String) {
        runCatching {
            val updated = blockedUserIds - userId
            blockedUserIds = updated
            chatPrefs.edit().putStringSet(blockedPrefsKey(), updated).apply()
            showToast("Account unblocked")
        }
    }

    init {
        // Use safe initialisation path
        loadBlockedIds("user-current")
        fetchReviews()
    }

    fun syncAuthUser(
        id: String = "",
        name: String = "",
        email: String = "",
        university: String = "",
        campus: String = "",
        avatarUrl: String = ""
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
        val cleanAvatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else currentUser.avatarUrl
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
            avatarInitial = initial,
            avatarUrl = cleanAvatarUrl
        )
        // Refresh blocked list for the new UID
        loadBlockedIds(cleanId)
    }

    var listings by mutableStateOf(initialListings)
        private set

    var favorites by mutableStateOf(setOf<String>())
        private set

    var reviews by mutableStateOf<List<Review>>(emptyList())
        private set

    var notifications by mutableStateOf(initialNotifications)
        private set

    var messages by mutableStateOf(initialMessages)
        private set

    var conversations by mutableStateOf(initialConversations)
        private set

    var isLoadingConversations by mutableStateOf(false)
        private set

    var homeCategory by mutableStateOf(Category.ALL)

    var searchQuery by mutableStateOf("")
        private set

    var marketplaceFilters by mutableStateOf(ListingFilters())
        private set

    var toastMessage by mutableStateOf<String?>(null)
        private set

    private var _isDarkMode = mutableStateOf(false)
    val isDarkMode: Boolean get() = _isDarkMode.value

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

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
                    categoryWords.any { it.startsWith(token) || token.startsWith(it) } ||
                    titleWords.any { word -> word.startsWith(token) || (token.length >= 4 && word.contains(token)) } ||
                    descWords.any { word -> word.startsWith(token) } ||
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

    fun refreshConversations() {
        val myId = runCatching { currentUser.id }.getOrNull() ?: return
        if (myId.isBlank() || myId == "user-current") return
        viewModelScope.launch {
            isLoadingConversations = true
            runCatching {
                val remote = chatRepository.loadConversations(myId)
                conversations = remote
            }.onFailure { e ->
                e.printStackTrace()
            }
            isLoadingConversations = false
        }
    }

    fun upsertConversation(
        participantId: String,
        listingId: String,
        lastMessage: String = "",
        lastTimestamp: String = ""
    ) {
        val existing = conversations.find { it.participantId == participantId && it.listingId == listingId }
        conversations = if (existing != null) {
            conversations.map { conv ->
                if (conv.participantId == participantId && conv.listingId == listingId) {
                    conv.copy(
                        lastMessage = lastMessage.ifBlank { conv.lastMessage },
                        lastTimestamp = lastTimestamp.ifBlank { conv.lastTimestamp },
                        unreadCount = 0
                    )
                } else conv
            }
        } else {
            listOf(
                Conversation(
                    id = "${listingId}_${participantId}",
                    listingId = listingId,
                    participantId = participantId,
                    lastMessage = lastMessage,
                    lastTimestamp = lastTimestamp,
                    unreadCount = 0
                )
            ) + conversations
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

    fun updateProfile(
        name: String,
        campus: String,
        bio: String = currentUser.bio,
        avatarUrl: String = currentUser.avatarUrl
    ): String? {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return "Name cannot be blank"

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
            campus = campus.trim().ifBlank { "KL Main Campus" },
            bio = bio.trim().ifBlank { "TAR UMT Student" },
            avatarInitial = if (trimmedName.isNotBlank()) trimmedName.first().uppercaseChar().toString() else "U",
            avatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else currentUser.avatarUrl,
            lastNameChangeTimestamp = newLastNameChange
        )
        showToast("Profile updated successfully")
        return null
    }

    fun fetchReviews() {
        viewModelScope.launch {
            runCatching {
                val response = supabase.postgrest.from("reviews").select()
                val body = response.data
                val array = JSONArray(body)
                val loadedReviews = mutableListOf<Review>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val tagsList = mutableListOf<String>()
                    val tagsArr = obj.optJSONArray("tags")
                    if (tagsArr != null) {
                        for (j in 0 until tagsArr.length()) {
                            tagsList.add(tagsArr.getString(j))
                        }
                    }
                    val authorName = obj.optString("author_name", "Student")
                    val initial = obj.optString("author_avatar_initial").ifBlank {
                        authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
                    }
                    loadedReviews.add(
                        Review(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            authorId = obj.optString("author_id", "demo"),
                            authorName = authorName,
                            authorAvatarInitial = initial,
                            targetUserId = obj.optString("target_user_id", "user-current"),
                            listingTitle = obj.optString("listing_title", "Listing"),
                            rating = obj.optInt("rating", 5),
                            comment = obj.optString("comment", ""),
                            tags = tagsList,
                            timeAgo = "Recently"
                        )
                    )
                }
                if (loadedReviews.isNotEmpty()) {
                    reviews = loadedReviews
                }
            }
        }
    }

    fun getReviewsForUser(userId: String): List<Review> {
        val isTargetingCurrent = userId == "user-current" ||
            userId == currentUser.id ||
            (currentUser.email.isNotBlank() && userId == currentUser.email)
        return reviews.filter { review ->
            if (isTargetingCurrent) {
                review.targetUserId == "user-current" ||
                (currentUser.id.isNotBlank() && review.targetUserId == currentUser.id) ||
                (currentUser.email.isNotBlank() && review.targetUserId == currentUser.email)
            } else {
                review.targetUserId == userId
            }
        }
    }

    fun getAverageRatingForUser(userId: String): Double {
        val userReviews = getReviewsForUser(userId)
        if (userReviews.isEmpty()) return 5.0
        return userReviews.map { it.rating }.average()
    }

    fun addReview(
        targetUserId: String,
        listingTitle: String,
        rating: Int,
        comment: String,
        tags: List<String> = emptyList()
    ) {
        val newReview = Review(
            id = "rev-${UUID.randomUUID().toString().take(8)}",
            authorId = currentUser.id,
            authorName = if (currentUser.name.isNotBlank()) currentUser.name else "Student",
            authorAvatarInitial = currentUser.avatarInitial,
            targetUserId = targetUserId,
            listingTitle = listingTitle,
            rating = rating.coerceIn(1, 5),
            comment = comment.trim(),
            tags = tags,
            timeAgo = "Just now"
        )
        reviews = listOf(newReview) + reviews
        showToast("Review submitted successfully! Thank you.")

        viewModelScope.launch {
            runCatching {
                supabase.postgrest.from("reviews").insert(
                    listOf(
                        buildJsonObject {
                            put("author_id", newReview.authorId)
                            put("author_name", newReview.authorName)
                            put("author_avatar_initial", newReview.authorAvatarInitial)
                            put("target_user_id", newReview.targetUserId)
                            put("listing_title", newReview.listingTitle)
                            put("rating", newReview.rating)
                            put("comment", newReview.comment)
                            put("tags", buildJsonArray {
                                tags.forEach { add(it) }
                            })
                        }
                    )
                )
            }
        }
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
