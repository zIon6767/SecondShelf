package com.example.secondshelf.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String? = null, //nullable because Supabase generates this
    @SerialName("listing_id") val listingId: String,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("seller_id") val sellerId: String,
    @SerialName("qr_token") val qrToken: String,
    val status: String = "pending"
)

@Serializable
data class ListingUpdate(
    val status: String
)