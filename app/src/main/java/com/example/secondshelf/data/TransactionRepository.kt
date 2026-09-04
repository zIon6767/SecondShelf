package com.example.secondshelf.data

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import com.example.secondshelf.data.model.Transaction
import com.example.secondshelf.data.model.ListingUpdate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TransactionCompleteUpdate(
    val status: String = "completed",
    @SerialName("buyer_id") val buyerId: String
)

class TransactionRepository(private val supabase: SupabaseClient) {

    private fun toValidUuid(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return UUID.randomUUID().toString()
        return if (runCatching { UUID.fromString(trimmed) }.isSuccess) {
            trimmed
        } else {
            UUID.nameUUIDFromBytes(trimmed.toByteArray()).toString()
        }
    }

    private suspend fun getAuthenticatedUserId(fallback: String = ""): String {
        runCatching { supabase.auth.awaitInitialization() }
        val authId = supabase.auth.currentUserOrNull()?.id
        return if (!authId.isNullOrBlank() && runCatching { UUID.fromString(authId) }.isSuccess) {
            authId
        } else {
            toValidUuid(fallback)
        }
    }

    /**
     * Called when the seller generates a pickup pass / QR code.
     * Inserts transaction into `transactions` table using the seller's auth.uid().
     */
    suspend fun createHandoff(listingId: String, buyerId: String, sellerId: String): String? {
        if (listingId.isBlank()) return null

        val currentAuthUserId = getAuthenticatedUserId(sellerId)
        val safeListingId = listingId.trim()

        return try {
            // Check if a pending transaction already exists for this listing
            val existing = runCatching {
                supabase.from("transactions").select {
                    filter {
                        eq("listing_id", safeListingId)
                        eq("status", "pending")
                    }
                }.decodeSingleOrNull<Transaction>()
            }.getOrNull()

            val token = existing?.qrToken ?: UUID.randomUUID().toString()

            if (existing == null) {
                // Set buyer_id initially to seller_id (placeholder as specified in RLS policy)
                val newTransaction = Transaction(
                    listingId = safeListingId,
                    buyerId = currentAuthUserId,
                    sellerId = currentAuthUserId,
                    qrToken = token,
                    status = "pending"
                )

                val insertResult = runCatching {
                    supabase.from("transactions").insert(newTransaction)
                }
                if (insertResult.isFailure) {
                    Log.e("TransactionRepo", "Failed inserting transaction", insertResult.exceptionOrNull())
                }

                // Update listing status to 'reserved'
                runCatching {
                    supabase.from("listings").update(ListingUpdate(status = "reserved")) {
                        filter { eq("id", safeListingId) }
                    }
                }
            }

            "SECONDSHELF::$safeListingId::$token"
        } catch (e: Exception) {
            Log.e("TransactionRepo", "createHandoff error", e)
            "SECONDSHELF::$listingId::${UUID.randomUUID()}"
        }
    }

    /**
     * Called when the buyer scans the QR code.
     * Updates `transactions` status to 'completed' and `buyer_id` to `auth.uid()`.
     */
    suspend fun verifyScannedToken(scannedToken: String, currentUserId: String): Boolean {
        val trimmed = scannedToken.trim()
        if (trimmed.isBlank()) return false

        var rawListingId: String? = null
        var extractedToken: String? = null

        if (trimmed.startsWith("SECONDSHELF::")) {
            val parts = trimmed.removePrefix("SECONDSHELF::").split("::")
            if (parts.isNotEmpty() && parts[0].isNotBlank()) rawListingId = parts[0]
            if (parts.size > 1 && parts[1].isNotBlank()) extractedToken = parts[1]
        } else if (trimmed.contains("::")) {
            val parts = trimmed.split("::")
            if (parts.isNotEmpty() && parts[0].isNotBlank()) rawListingId = parts[0]
            if (parts.size > 1 && parts[1].isNotBlank()) extractedToken = parts[1]
        } else {
            extractedToken = trimmed
            rawListingId = trimmed
        }

        if (extractedToken.isNullOrBlank()) return false

        val buyerAuthUserId = getAuthenticatedUserId(currentUserId)

        return try {
            // Update transactions table: status = 'completed', buyer_id = auth.uid()
            val updateResult = runCatching {
                supabase.from("transactions").update(
                    TransactionCompleteUpdate(status = "completed", buyerId = buyerAuthUserId)
                ) {
                    select()
                    filter {
                        eq("qr_token", extractedToken)
                        eq("status", "pending")
                    }
                }.decodeList<Transaction>()
            }.getOrDefault(emptyList())

            Log.d("TransactionRepo", "verifyScannedToken updateResult count=${updateResult.size}")

            val listingIdStr = updateResult.firstOrNull()?.listingId ?: rawListingId ?: trimmed

            // Attempt setting listing status = 'sold'
            runCatching {
                supabase.from("listings").update(ListingUpdate(status = "sold")) {
                    filter { eq("id", listingIdStr) }
                }
            }

            // Clean up buyer's favorites
            runCatching {
                supabase.from("favorites").delete {
                    filter {
                        eq("user_id", buyerAuthUserId)
                        eq("listing_id", listingIdStr)
                    }
                }
            }

            if (updateResult.isNotEmpty()) {
                return true
            }

            // Fallback update if select() projection was restricted by RLS
            val fallbackResult = runCatching {
                supabase.from("transactions").update(
                    TransactionCompleteUpdate(status = "completed", buyerId = buyerAuthUserId)
                ) {
                    filter {
                        eq("qr_token", extractedToken)
                        eq("status", "pending")
                    }
                }
            }

            fallbackResult.isSuccess
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error in verifyScannedToken", e)
            false
        }
    }

    /**
     * Polled by the Seller's QR screen.
     * Detects when transaction becomes 'completed', executes seller-side DELETE on listings table
     * (triggering ON DELETE CASCADE), and returns true.
     */
    suspend fun isTransactionCompleted(transactionToken: String): Boolean {
        val trimmed = transactionToken.trim()
        if (trimmed.isBlank()) return false

        var rawListingId: String? = null
        var extractedToken: String? = null

        if (trimmed.startsWith("SECONDSHELF::")) {
            val parts = trimmed.removePrefix("SECONDSHELF::").split("::")
            if (parts.isNotEmpty() && parts[0].isNotBlank()) rawListingId = parts[0]
            if (parts.size > 1 && parts[1].isNotBlank()) extractedToken = parts[1]
        } else if (trimmed.contains("::")) {
            val parts = trimmed.split("::")
            if (parts.isNotEmpty() && parts[0].isNotBlank()) rawListingId = parts[0]
            if (parts.size > 1 && parts[1].isNotBlank()) extractedToken = parts[1]
        } else {
            extractedToken = trimmed
            rawListingId = trimmed
        }

        val targetListingId = rawListingId ?: trimmed

        return runCatching {
            // Check if transaction in transactions table is completed
            val txList = supabase.from("transactions").select {
                filter {
                    if (!extractedToken.isNullOrBlank()) {
                        eq("qr_token", extractedToken)
                    } else {
                        eq("listing_id", targetListingId)
                    }
                    eq("status", "completed")
                }
            }.decodeList<Transaction>()

            if (txList.isNotEmpty()) {
                val listingIdStr = txList.first().listingId
                Log.d("TransactionRepo", "Seller side detected completed transaction for listing $listingIdStr! Deleting listing.")

                // 1. Mark status = 'sold'
                runCatching {
                    supabase.from("listings").update(ListingUpdate(status = "sold")) {
                        filter { eq("id", listingIdStr) }
                    }
                }

                // 2. Execute DELETE on listings table as seller (owner).
                // CASCADE DELETE automatically removes listing from listings, listing_images, favorites, and transactions!
                val deleteResult = runCatching {
                    supabase.from("listings").delete {
                        filter { eq("id", listingIdStr) }
                    }
                }
                if (deleteResult.isFailure) {
                    Log.e("TransactionRepo", "Seller delete listing failed", deleteResult.exceptionOrNull())
                }

                return@runCatching true
            }

            // Check if listing has already been deleted from listings table or status != 'ACTIVE'
            val listingRecord = supabase.from("listings").select {
                filter { eq("id", targetListingId) }
            }.decodeSingleOrNull<com.example.secondshelf.data.marketplace.ListingRecord>()

            if (listingRecord == null || listingRecord.status != "ACTIVE") {
                Log.d("TransactionRepo", "Seller side detected listing $targetListingId is deleted or status=${listingRecord?.status}.")
                return@runCatching true
            }

            false
        }.getOrDefault(false)
    }
}
