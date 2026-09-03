package com.example.secondshelf.data.marketplace

import com.example.secondshelf.supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseMarketplaceRepository(
    private val client: SupabaseClient = supabase
) : MarketplaceRepository {

    override suspend fun getListings(
        query: MarketplaceQuery
    ): Result<List<ListingRecord>> = runCatching {
        val records = client.from(LISTINGS_TABLE).select {
            filter {
                eq("status", ACTIVE_STATUS)
                query.category?.let { eq("category", it) }
                query.condition?.let { eq("condition", it) }
                query.minimumPriceCents?.let { gte("price_cents", it) }
                query.maximumPriceCents?.let { lte("price_cents", it) }
                query.uploadedAfter?.let { gte("created_at", it) }
            }
        }.decodeList<ListingRecord>()

        val keyword = query.keyword.trim()
        val filtered = if (keyword.isBlank()) {
            records
        } else {
            records.filter { listing ->
                listing.title.contains(keyword, ignoreCase = true) ||
                    listing.description.contains(keyword, ignoreCase = true) ||
                    listing.location.contains(keyword, ignoreCase = true)
            }
        }

        when (query.sort) {
            MarketplaceSort.NEWEST ->
                filtered.sortedByDescending(ListingRecord::createdAt)
            MarketplaceSort.PRICE_LOW_TO_HIGH ->
                filtered.sortedBy(ListingRecord::priceCents)
            MarketplaceSort.PRICE_HIGH_TO_LOW ->
                filtered.sortedByDescending(ListingRecord::priceCents)
        }
    }

    override suspend fun getListing(
        listingId: String
    ): Result<ListingRecord> = runCatching {
        require(listingId.isNotBlank()) { "Listing ID is required" }

        client.from(LISTINGS_TABLE).select {
            filter {
                eq("id", listingId)
            }
        }.decodeSingle<ListingRecord>()
    }

    override suspend fun createListing(
        input: CreateListingRecord
    ): Result<ListingRecord> = runCatching {
        validateCreateInput(input)

        client.from(LISTINGS_TABLE).insert(input) {
            select()
        }.decodeSingle<ListingRecord>()
    }

    override suspend fun updateListing(
        listingId: String,
        input: UpdateListingRecord
    ): Result<ListingRecord> = runCatching {
        require(listingId.isNotBlank()) { "Listing ID is required" }
        validateUpdateInput(input)

        client.from(LISTINGS_TABLE).update(input) {
            select()
            filter {
                eq("id", listingId)
            }
        }.decodeSingle<ListingRecord>()
    }

    override suspend fun deleteListing(
        listingId: String
    ): Result<Unit> = runCatching {
        require(listingId.isNotBlank()) { "Listing ID is required" }

        client.from(LISTINGS_TABLE).delete {
            filter {
                eq("id", listingId)
            }
        }
        Unit
    }

    override suspend fun getFavoriteListingIds(
        userId: String
    ): Result<Set<String>> = runCatching {
        require(userId.isNotBlank()) { "User ID is required" }

        client.from(FAVORITES_TABLE).select {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<FavoriteRecord>()
            .map(FavoriteRecord::listingId)
            .toSet()
    }

    override suspend fun addFavorite(
        userId: String,
        listingId: String
    ): Result<Unit> = runCatching {
        validateFavoriteIds(userId, listingId)

        client.from(FAVORITES_TABLE).insert(
            FavoriteInsertRecord(userId = userId, listingId = listingId)
        )
        Unit
    }

    override suspend fun removeFavorite(
        userId: String,
        listingId: String
    ): Result<Unit> = runCatching {
        validateFavoriteIds(userId, listingId)

        client.from(FAVORITES_TABLE).delete {
            filter {
                eq("user_id", userId)
                eq("listing_id", listingId)
            }
        }
        Unit
    }

    suspend fun getSellerProfiles(): Result<Map<String, SellerProfileRecord>> = runCatching {
        client.from(PROFILES_TABLE)
            .select()
            .decodeList<SellerProfileRecord>()
            .associateBy(SellerProfileRecord::id)
    }

    suspend fun getListingImageUrls(): Result<Map<String, String>> = runCatching {
        client.from(LISTING_IMAGES_TABLE)
            .select()
            .decodeList<ListingImageRecord>()
            .sortedBy(ListingImageRecord::displayOrder)
            .associate { image ->
                image.listingId to client.storage
                    .from(LISTING_IMAGES_BUCKET)
                    .publicUrl(image.storagePath)
            }
    }

    suspend fun uploadListingImage(
        listingId: String,
        userId: String,
        bytes: ByteArray,
        extension: String
    ): Result<String> = runCatching {
        require(listingId.isNotBlank()) { "Listing ID is required" }
        require(userId.isNotBlank()) { "You must be signed in" }
        require(bytes.isNotEmpty()) { "The selected image is empty" }
        require(bytes.size <= MAX_IMAGE_BYTES) { "Image must not exceed 5 MB" }

        val safeExtension = extension.lowercase().takeIf {
            it == "jpg" || it == "jpeg" || it == "png" || it == "webp"
        } ?: "jpg"
        val storagePath = "$userId/$listingId/${UUID.randomUUID()}.$safeExtension"
        val bucket = client.storage.from(LISTING_IMAGES_BUCKET)

        bucket.upload(storagePath, bytes)
        client.from(LISTING_IMAGES_TABLE).insert(
            ListingImageInsertRecord(
                listingId = listingId,
                storagePath = storagePath
            )
        )

        bucket.publicUrl(storagePath)
    }

    private fun validateCreateInput(input: CreateListingRecord) {
        validateListingFields(
            sellerId = input.sellerId,
            title = input.title,
            description = input.description,
            priceCents = input.priceCents,
            location = input.location
        )
    }

    private fun validateUpdateInput(input: UpdateListingRecord) {
        validateListingFields(
            sellerId = "existing-seller",
            title = input.title,
            description = input.description,
            priceCents = input.priceCents,
            location = input.location
        )
    }

    private fun validateListingFields(
        sellerId: String,
        title: String,
        description: String,
        priceCents: Int,
        location: String
    ) {
        require(sellerId.isNotBlank()) { "You must be signed in" }
        require(title.trim().length in 3..100) {
            "Title must contain 3 to 100 characters"
        }
        require(description.trim().length in 10..2000) {
            "Description must contain 10 to 2000 characters"
        }
        require(priceCents in 1..MAX_PRICE_CENTS) {
            "Price must be between RM 0.01 and RM 1,000,000"
        }
        require(location.trim().length in 2..120) {
            "Location must contain 2 to 120 characters"
        }
    }

    private fun validateFavoriteIds(userId: String, listingId: String) {
        require(userId.isNotBlank()) { "User ID is required" }
        require(listingId.isNotBlank()) { "Listing ID is required" }
    }

    private companion object {
        const val LISTINGS_TABLE = "listings"
        const val FAVORITES_TABLE = "favorites"
        const val PROFILES_TABLE = "profiles"
        const val LISTING_IMAGES_TABLE = "listing_images"
        const val LISTING_IMAGES_BUCKET = "listing-images"
        const val ACTIVE_STATUS = "ACTIVE"
        const val MAX_PRICE_CENTS = 100_000_000
        const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
    }
}

@Serializable
private data class FavoriteInsertRecord(
    @SerialName("user_id")
    val userId: String,
    @SerialName("listing_id")
    val listingId: String
)
