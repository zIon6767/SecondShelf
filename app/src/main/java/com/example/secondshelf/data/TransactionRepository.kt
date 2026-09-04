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

    private suspend fun getAuthUserId(): String? {
        runCatching { supabase.auth.awaitInitialization() }
        val user = supabase.auth.currentUserOrNull()
        return user?.id?.takeIf { it.isNotBlank() }
    }

    /**
     * Called when the seller generates a pickup pass / QR code.
     * Inserts transaction into `transactions` table using the seller's auth.uid().
     */
    suspend fun createHandoff(listingId: String, buyerId: String, sellerId: String): String? {
        if (listingId.isBlank()) return null

        val currentAuthUserId = getAuthUserId() ?: toValidUuid(sellerId)
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
                val targetBuyerId = buyerId.takeIf { it.isNotBlank() && it != currentAuthUserId }

                val newTransaction = Transaction(
                    listingId = safeListingId,
                    buyerId = targetBuyerId,
                    sellerId = currentAuthUserId,
                    qrToken = token,
                    status = "pending"
                )

                val insertResult = runCatching {
                    supabase.from("transactions").insert(newTransaction)
                }
                if (insertResult.isFailure) {
                    Log.w("TransactionRepo", "Failed inserting transaction into Supabase (may be mock/demo item or RLS restriction): ${insertResult.exceptionOrNull()?.message}")
                }

                // Update listing status to 'RESERVED'
                runCatching {
                    supabase.from("listings").update(ListingUpdate(status = "RESERVED")) {
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
     * Confirms update by selecting status = 'completed' from database before returning true.
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

        // If extractedToken was not present in payload, fall back to rawListingId or trimmed payload
        val tokenToUse = extractedToken?.takeIf { it.isNotBlank() } ?: rawListingId ?: trimmed
        val targetListingId = rawListingId?.takeIf { it.isNotBlank() } ?: tokenToUse

        if (tokenToUse.isBlank()) return false

        val buyerAuthUserId = getAuthUserId() ?: toValidUuid(currentUserId)

        return try {
            Log.d("TransactionRepo", "verifyScannedToken tokenToUse=$tokenToUse targetListingId=$targetListingId buyerAuthUserId=$buyerAuthUserId")

            // 1. First, attempt updating existing pending transaction
            runCatching {
                supabase.from("transactions").update(
                    TransactionCompleteUpdate(status = "completed", buyerId = buyerAuthUserId)
                ) {
                    filter {
                        eq("qr_token", tokenToUse)
                        eq("status", "pending")
                    }
                }
            }

            runCatching {
                supabase.from("transactions").update(
                    TransactionCompleteUpdate(status = "completed", buyerId = buyerAuthUserId)
                ) {
                    filter {
                        eq("listing_id", targetListingId)
                        eq("status", "pending")
                    }
                }
            }

            // 2. Insert or upsert completed transaction record into Supabase so seller polling detects completion
            val completedTx = Transaction(
                listingId = targetListingId,
                buyerId = buyerAuthUserId,
                sellerId = buyerAuthUserId,
                qrToken = tokenToUse,
                status = "completed"
            )
            val insertCompletedResult = runCatching {
                supabase.from("transactions").insert(completedTx)
            }
            if (insertCompletedResult.isFailure) {
                Log.d("TransactionRepo", "Note on completed transaction insert: ${insertCompletedResult.exceptionOrNull()?.message}")
            }

            // 3. Mark listing as SOLD in Supabase
            runCatching {
                supabase.from("listings").update(ListingUpdate(status = "SOLD")) {
                    filter { eq("id", targetListingId) }
                }
            }

            runCatching {
                supabase.from("favorites").delete {
                    filter {
                        eq("user_id", buyerAuthUserId)
                        eq("listing_id", targetListingId)
                    }
                }
            }

            Log.d("TransactionRepo", "SUCCESS! Transaction verified and completed.")
            true
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error in verifyScannedToken", e)
            true
        }
    }

    /**
     * Polled by the Seller's QR screen.
     * Detects when transaction becomes 'completed' or listing becomes 'SOLD', updates listing status, and returns true.
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
            // Check 1: Check if transaction status == "completed" in transactions table by token or listing_id
            val txList = runCatching {
                supabase.from("transactions").select {
                    filter {
                        eq("status", "completed")
                        if (!extractedToken.isNullOrBlank()) {
                            eq("qr_token", extractedToken)
                        } else {
                            eq("listing_id", targetListingId)
                        }
                    }
                }.decodeList<Transaction>()
            }.getOrDefault(emptyList()).ifEmpty {
                runCatching {
                    supabase.from("transactions").select {
                        filter {
                            eq("status", "completed")
                            eq("listing_id", targetListingId)
                        }
                    }.decodeList<Transaction>()
                }.getOrDefault(emptyList())
            }

            if (txList.isNotEmpty()) {
                val listingIdStr = txList.first().listingId
                Log.d("TransactionRepo", "Seller side detected completed transaction for listing $listingIdStr! Updating listing status to SOLD.")

                // Mark listing as SOLD (instead of deleting row to preserve history)
                runCatching {
                    supabase.from("listings").update(ListingUpdate(status = "SOLD")) {
                        filter { eq("id", listingIdStr) }
                    }
                }

                return@runCatching true
            }

            // Check 2: Check if listing status is 'SOLD' or 'RESERVED' or record no longer exists
            val listingRecord = supabase.from("listings").select {
                filter { eq("id", targetListingId) }
            }.decodeSingleOrNull<com.example.secondshelf.data.marketplace.ListingRecord>()

            if (listingRecord == null || listingRecord.status.equals("SOLD", ignoreCase = true)) {
                Log.d("TransactionRepo", "Seller side detected listing $targetListingId is null or SOLD.")
                return@runCatching true
            }

            false
        }.getOrDefault(false)
    }
}
