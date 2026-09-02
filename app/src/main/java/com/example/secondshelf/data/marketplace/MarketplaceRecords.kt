package com.example.secondshelf.data.marketplace

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListingRecord(
    val id: String,
    @SerialName("seller_id")
    val sellerId: String,
    val title: String,
    val description: String,
    @SerialName("price_cents")
    val priceCents: Int,
    val category: String,
    val condition: String,
    val location: String,
    val status: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class CreateListingRecord(
    @SerialName("seller_id")
    val sellerId: String,
    val title: String,
    val description: String,
    @SerialName("price_cents")
    val priceCents: Int,
    val category: String,
    val condition: String,
    val location: String
)

@Serializable
data class UpdateListingRecord(
    val title: String,
    val description: String,
    @SerialName("price_cents")
    val priceCents: Int,
    val category: String,
    val condition: String,
    val location: String
)

@Serializable
data class ListingImageRecord(
    val id: String,
    @SerialName("listing_id")
    val listingId: String,
    @SerialName("storage_path")
    val storagePath: String,
    @SerialName("display_order")
    val displayOrder: Int,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class FavoriteRecord(
    @SerialName("user_id")
    val userId: String,
    @SerialName("listing_id")
    val listingId: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
