package com.example.secondshelf.data

import android.util.Log
import com.example.secondshelf.data.model.Transaction
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.UUID

class TransactionRepository(private val supabase: SupabaseClient) {

    private data class ParsedQrToken(
        val listingId: String?,
        val token: String
    )

    private fun parseQrToken(value: String): ParsedQrToken? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return null

        if (trimmed.startsWith("SECONDSHELF::")) {
            val parts = trimmed.removePrefix("SECONDSHELF::").split("::")
            val listingId = parts.getOrNull(0)?.takeIf { it.isNotBlank() }
            val token = parts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return null
            return ParsedQrToken(listingId, token)
        }

        if (trimmed.contains("::")) {
            val parts = trimmed.split("::")
            val listingId = parts.getOrNull(0)?.takeIf { it.isNotBlank() }
            val token = parts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return null
            return ParsedQrToken(listingId, token)
        }

        return ParsedQrToken(null, trimmed)
    }

    private suspend fun authenticatedUserId(): String? {
        runCatching { supabase.auth.awaitInitialization() }
        return supabase.auth.currentUserOrNull()?.id
            ?.takeIf { runCatching { UUID.fromString(it) }.isSuccess }
    }

    /**
     * Creates one pending handoff for the authenticated seller and the selected buyer.
     * A QR value is returned only after the transaction is confirmed in Supabase.
     */
    suspend fun createHandoff(listingId: String, buyerId: String, sellerId: String): String? {
        val sellerAuthId = authenticatedUserId() ?: return null
        val safeListingId = listingId.trim()
        val safeBuyerId = buyerId.trim()
        val safeSellerId = sellerId.trim()

        if (
            safeListingId.isBlank() ||
            safeBuyerId.isBlank() ||
            safeSellerId.isBlank() ||
            sellerAuthId != safeSellerId ||
            safeBuyerId == sellerAuthId ||
            runCatching { UUID.fromString(safeListingId) }.isFailure ||
            runCatching { UUID.fromString(safeBuyerId) }.isFailure
        ) {
            Log.e("TransactionRepo", "Invalid seller, buyer, or listing for handoff")
            return null
        }

        return runCatching {
            val existing = supabase.from("transactions").select {
                filter {
                    eq("listing_id", safeListingId)
                    eq("seller_id", sellerAuthId)
                    eq("buyer_id", safeBuyerId)
                    eq("status", "pending")
                }
            }.decodeList<Transaction>().firstOrNull()

            val transaction = existing ?: run {
                val token = UUID.randomUUID().toString()
                val newTransaction = Transaction(
                    listingId = safeListingId,
                    buyerId = safeBuyerId,
                    sellerId = sellerAuthId,
                    qrToken = token,
                    status = "pending"
                )

                supabase.from("transactions").insert(newTransaction)

                supabase.from("transactions").select {
                    filter {
                        eq("qr_token", token)
                        eq("listing_id", safeListingId)
                        eq("seller_id", sellerAuthId)
                        eq("buyer_id", safeBuyerId)
                        eq("status", "pending")
                    }
                }.decodeList<Transaction>().firstOrNull()
                    ?: error("Supabase did not create the pending transaction")
            }

            "SECONDSHELF::${transaction.listingId}::${transaction.qrToken}"
        }.onFailure {
            Log.e("TransactionRepo", "Unable to create handoff", it)
        }.getOrNull()
    }

    /**
     * Completes a pending handoff only when a real row is returned for the
     * authenticated, pre-assigned buyer. No affected row means failure.
     */
    suspend fun verifyScannedToken(scannedToken: String, currentUserId: String): Boolean {
        val parsed = parseQrToken(scannedToken) ?: return false
        val buyerAuthId = authenticatedUserId() ?: return false

        if (currentUserId.isNotBlank() && currentUserId != buyerAuthId) {
            Log.e("TransactionRepo", "Scanner user does not match authenticated user")
            return false
        }

        return runCatching {
            val updated = supabase.from("transactions").update(
                {
                    set("status", "completed")
                }
            ) {
                select()
                filter {
                    eq("qr_token", parsed.token)
                    eq("buyer_id", buyerAuthId)
                    eq("status", "pending")
                }
            }.decodeList<Transaction>()

            val completed = updated.singleOrNull() ?: return@runCatching false
            val listingMatches = parsed.listingId == null || parsed.listingId == completed.listingId
            listingMatches && completed.buyerId == buyerAuthId && completed.status == "completed"
        }.onFailure {
            Log.e("TransactionRepo", "Unable to verify scanned QR token", it)
        }.getOrDefault(false)
    }

    /**
     * Seller-side polling. Completion is acknowledged only from a completed
     * transaction owned by the authenticated seller. The listing must then be
     * successfully deleted (or already absent) before the seller sees Success.
     */
    suspend fun isTransactionCompleted(transactionToken: String): Boolean {
        val parsed = parseQrToken(transactionToken) ?: return false
        val sellerAuthId = authenticatedUserId() ?: return false

        return runCatching {
            val completed = supabase.from("transactions").select {
                filter {
                    eq("qr_token", parsed.token)
                    eq("seller_id", sellerAuthId)
                    eq("status", "completed")
                }
            }.decodeList<Transaction>().singleOrNull() ?: return@runCatching false

            if (parsed.listingId != null && parsed.listingId != completed.listingId) {
                return@runCatching false
            }

            val deleteResult = runCatching {
                supabase.from("listings").delete {
                    filter {
                        eq("id", completed.listingId)
                        eq("seller_id", sellerAuthId)
                    }
                }
            }

            if (deleteResult.isFailure) {
                Log.e("TransactionRepo", "Seller could not delete completed listing", deleteResult.exceptionOrNull())
                return@runCatching false
            }

            val remainingListing = supabase.from("listings").select {
                filter { eq("id", completed.listingId) }
            }.decodeList<com.example.secondshelf.data.marketplace.ListingRecord>()

            remainingListing.isEmpty()
        }.onFailure {
            Log.e("TransactionRepo", "Unable to poll completed transaction", it)
        }.getOrDefault(false)
    }
}
