package com.example.secondshelf.data.marketplace

data class MarketplaceQuery(
    val keyword: String = "",
    val category: String? = null,
    val condition: String? = null,
    val minimumPriceCents: Int? = null,
    val maximumPriceCents: Int? = null,
    val uploadedAfter: String? = null,
    val sort: MarketplaceSort = MarketplaceSort.NEWEST
)

enum class MarketplaceSort {
    NEWEST,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW
}

interface MarketplaceRepository {
    suspend fun getListings(query: MarketplaceQuery = MarketplaceQuery()): Result<List<ListingRecord>>

    suspend fun getListing(listingId: String): Result<ListingRecord>

    suspend fun createListing(input: CreateListingRecord): Result<ListingRecord>

    suspend fun updateListing(
        listingId: String,
        input: UpdateListingRecord
    ): Result<ListingRecord>

    suspend fun deleteListing(listingId: String): Result<Unit>

    suspend fun getFavoriteListingIds(userId: String): Result<Set<String>>

    suspend fun addFavorite(userId: String, listingId: String): Result<Unit>

    suspend fun removeFavorite(userId: String, listingId: String): Result<Unit>
}
