package com.example.secondshelf.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondshelf.data.ChatRepository
import com.example.secondshelf.data.marketplace.CreateListingRecord
import com.example.secondshelf.data.marketplace.ListingRecord
import com.example.secondshelf.data.marketplace.SellerProfileRecord
import com.example.secondshelf.data.marketplace.SupabaseMarketplaceRepository
import com.example.secondshelf.data.marketplace.UpdateListingRecord
import com.example.secondshelf.supabase
import com.example.secondshelf.ui.model.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONArray
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import java.util.UUID

class MarketplaceViewModel(
    private val repository: SupabaseMarketplaceRepository = SupabaseMarketplaceRepository(),
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    init {
        fetchReviews()
    }

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
        campus: String = "",
        avatarUrl: String = ""
    ) {
        val previousId = currentUser.id
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
        if (cleanId != previousId && isSupabaseUserId(cleanId)) {
            refreshMarketplace()
            loadConversationsFromDb(cleanId)
        }
    }

    var listings by mutableStateOf<List<Listing>>(emptyList())
        private set

    var favorites by mutableStateOf(setOf<String>())
        private set

    var isMarketplaceLoading by mutableStateOf(false)
        private set

    var isListingSaving by mutableStateOf(false)
        private set

    var marketplaceError by mutableStateOf<String?>(null)
        private set

    var reviews by mutableStateOf<List<Review>>(emptyList())
        private set

    var notifications by mutableStateOf(initialNotifications)
        private set

    var messages by mutableStateOf(initialMessages)
        private set

    var conversations by mutableStateOf(initialConversations)
        private set

    /**
     * Fetches the user's conversation history from Supabase and populates the
     * [conversations] list shown on the Messages screen. Safe to call multiple
     * times; always replaces the current list with the freshest data.
     */
    fun loadConversationsFromDb(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val loaded = runCatching { chatRepository.loadConversations(userId) }
                .getOrDefault(emptyList())
            if (loaded.isNotEmpty()) {
                conversations = loaded
            }
        }
    }

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

    fun refreshMarketplace() {
        viewModelScope.launch {
            isMarketplaceLoading = true
            marketplaceError = null

            val imageUrls = repository.getListingImageUrls()
                .getOrElse { emptyMap() }
            val sellerProfiles = repository.getSellerProfiles()
                .getOrElse { emptyMap() }

            repository.getListings()
                .onSuccess { records ->
                    listings = records.map { record ->
                        toUiListing(
                            record = record,
                            imageUrl = imageUrls[record.id],
                            sellerProfile = sellerProfiles[record.sellerId]
                        )
                    }
                }
                .onFailure { error ->
                    marketplaceError = error.userMessage()
                    showToast("Could not load marketplace: ${error.userMessage()}")
                }

            if (isSupabaseUserId(currentUser.id)) {
                repository.getFavoriteListingIds(currentUser.id)
                    .onSuccess { favorites = it }
                    .onFailure { error ->
                        marketplaceError = error.userMessage()
                    }
            } else {
                favorites = emptySet()
            }

            isMarketplaceLoading = false
        }
    }

    private fun toUiListing(
        record: ListingRecord,
        imageUrl: String? = null,
        sellerProfile: SellerProfileRecord? = null
    ): Listing {
        val isOwnedByCurrentUser = record.sellerId == currentUser.id
        val resolvedSellerName = sellerProfile?.name
            ?.takeIf { it.isNotBlank() && it != "Student" && it != "User" }
            ?: currentUser.name.takeIf { isOwnedByCurrentUser && it.isNotBlank() }
            ?: "Unknown Seller"
        val resolvedSellerCampus = sellerProfile?.campus
            ?.takeIf { it.isNotBlank() }
            ?: currentUser.campus.takeIf { isOwnedByCurrentUser }
            .orEmpty()
        val resolvedSellerAvatar = sellerProfile?.avatarUrl
            ?.takeIf { it.isNotBlank() }
            ?: currentUser.avatarUrl.takeIf { isOwnedByCurrentUser }
            .orEmpty()

        return Listing(
        id = record.id,
        title = record.title,
        description = record.description,
        price = record.priceCents / 100,
        category = runCatching { Category.valueOf(record.category) }.getOrDefault(Category.OTHER),
        condition = runCatching { Condition.valueOf(record.condition) }.getOrDefault(Condition.GOOD),
        sellerId = record.sellerId,
        location = record.location,
        createdAt = record.createdAt,
        imageUrl = imageUrl,
        sellerName = resolvedSellerName,
        sellerCampus = resolvedSellerCampus,
        sellerAvatarUrl = resolvedSellerAvatar,
        isOwnedByCurrentUser = isOwnedByCurrentUser
        )
    }

    private fun isSupabaseUserId(id: String): Boolean =
        runCatching { UUID.fromString(id) }.isSuccess

    private fun authenticatedUserId(): String? =
        supabase.auth.currentUserOrNull()?.id?.takeIf(::isSupabaseUserId)

    private fun Throwable.userMessage(): String =
        message?.substringBefore("\n")?.take(160) ?: "Unknown Supabase error"

    fun getListing(id: String): Listing? = listings.find { it.id == id }

    fun isFavorite(listingId: String) = favorites.contains(listingId)

    fun toggleFavorite(listingId: String) {
        if (!isSupabaseUserId(currentUser.id)) {
            showToast("Please sign in before saving an item")
            return
        }

        val wasFavorite = favorites.contains(listingId)
        favorites = if (wasFavorite) favorites - listingId else favorites + listingId

        viewModelScope.launch {
            val result = if (wasFavorite) {
                repository.removeFavorite(currentUser.id, listingId)
            } else {
                repository.addFavorite(currentUser.id, listingId)
            }

            result.onSuccess {
                showToast(if (wasFavorite) "Removed from saved items" else "Item saved")
            }.onFailure { error ->
                favorites = if (wasFavorite) favorites + listingId else favorites - listingId
                showToast("Could not update saved item: ${error.userMessage()}")
            }
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

        filters.minPrice?.let { minimum ->
            result = result.filter { it.price >= minimum }
        }
        filters.maxPrice?.let { maximum ->
            result = result.filter { it.price <= maximum }
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
            SortOption.OLDEST -> result.sortedBy { it.createdAt }
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
        location: String,
        imageBytes: ByteArray? = null,
        imageExtension: String = "jpg",
        onComplete: (String?) -> Unit
    ) {
        val validationError = validateListingInput(title, description, price, category, location)
        if (validationError != null) {
            onComplete(validationError)
            return
        }
        viewModelScope.launch {
            isListingSaving = true

            // Session restoration is asynchronous in supabase-kt. Wait until Auth has
            // finished loading the stored Google session before checking the user.
            supabase.auth.awaitInitialization()
            val sellerId = authenticatedUserId()
            if (sellerId == null) {
                isListingSaving = false
                onComplete("Your session has expired. Please sign in again.")
                return@launch
            }
            if (currentUser.id != sellerId) {
                currentUser = currentUser.copy(id = sellerId)
            }

            repository.createListing(
                CreateListingRecord(
                    sellerId = sellerId,
                    title = title.trim(),
                    description = description.trim(),
                    priceCents = price * 100,
                    category = category.name,
                    condition = condition.name,
                    location = location.trim()
                )
            ).onSuccess { record ->
                var imageUrl: String? = null
                if (imageBytes != null) {
                    repository.uploadListingImage(
                        listingId = record.id,
                        userId = sellerId,
                        bytes = imageBytes,
                        extension = imageExtension
                    ).onSuccess { uploadedUrl ->
                        imageUrl = uploadedUrl
                    }.onFailure { uploadError ->
                        marketplaceError = uploadError.userMessage()
                        showToast("Listing posted, but image upload failed: ${uploadError.userMessage()}")
                    }
                }

                val listing = toUiListing(record, imageUrl)
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
                onComplete(null)
            }.onFailure { error ->
                onComplete("Could not create listing: ${error.userMessage()}")
            }
            isListingSaving = false
        }
    }

    fun updateListing(
        id: String,
        title: String,
        description: String,
        price: Int,
        category: Category,
        condition: Condition,
        location: String,
        onComplete: (String?) -> Unit
    ) {
        val validationError = validateListingInput(title, description, price, category, location)
        if (validationError != null) {
            onComplete(validationError)
            return
        }

        viewModelScope.launch {
            isListingSaving = true
            repository.updateListing(
                id,
                UpdateListingRecord(
                    title = title.trim(),
                    description = description.trim(),
                    priceCents = price * 100,
                    category = category.name,
                    condition = condition.name,
                    location = location.trim()
                )
            ).onSuccess { record ->
                val existingListing = listings.firstOrNull { it.id == id }
                listings = listings.map { listing ->
                    if (listing.id == id) {
                        toUiListing(record, imageUrl = existingListing?.imageUrl)
                    } else {
                        listing
                    }
                }
                showToast("Listing updated")
                onComplete(null)
            }.onFailure { error ->
                onComplete("Could not update listing: ${error.userMessage()}")
            }
            isListingSaving = false
        }
    }

    private fun validateListingInput(
        title: String,
        description: String,
        price: Int,
        category: Category,
        location: String
    ): String? {
        if (title.trim().length !in 3..100) return "Item name must contain 3 to 100 characters"
        if (description.trim().length !in 10..2000) return "Description must contain 10 to 2000 characters"
        if (price <= 0 || price > 1_000_000) return "Price must be between RM 1 and RM 1,000,000"
        if (location.trim().length !in 2..120) return "Location must contain 2 to 120 characters"
        if (category == Category.ALL) return "Please select a category"
        return null
    }

    fun deleteListing(id: String) {
        val removed = getListing(id) ?: return
        listings = listings.filter { it.id != id }
        favorites = favorites - id

        viewModelScope.launch {
            repository.deleteListing(id)
                .onSuccess { showToast("Listing deleted") }
                .onFailure { error ->
                    listings = listOf(removed) + listings
                    showToast("Could not delete listing: ${error.userMessage()}")
                }
        }
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
        tags: List<String> = emptyList(),
        authorName: String? = null,
        authorAvatarInitial: String? = null
    ) {
        val resolvedAuthorName = if (!authorName.isNullOrBlank()) authorName.trim() else if (currentUser.name.isNotBlank()) currentUser.name else "Student"
        val resolvedInitial = if (!authorAvatarInitial.isNullOrBlank()) authorAvatarInitial.trim().uppercase()
            else if (!authorName.isNullOrBlank()) authorName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "S"
            else currentUser.avatarInitial

        val newReview = Review(
            id = "rev-${UUID.randomUUID().toString().take(8)}",
            authorId = currentUser.id.ifBlank { "user-${UUID.randomUUID().toString().take(6)}" },
            authorName = resolvedAuthorName,
            authorAvatarInitial = resolvedInitial,
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

    fun deleteReview(reviewId: String) {
        reviews = reviews.filter { it.id != reviewId }
        showToast("Review removed")
    }

    fun createHandoffToken(
        listingId: String,
        buyerId: String,
        sellerId: String,
        onTokenReady: (String) -> Unit
    ) {
        viewModelScope.launch {
            val transactionRepo = com.example.secondshelf.data.TransactionRepository(supabase)
            val token = transactionRepo.createHandoff(listingId, buyerId, sellerId) ?: listingId
            onTokenReady(token)
        }
    }

    fun clearAllUserData() {
        listings = emptyList()
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
