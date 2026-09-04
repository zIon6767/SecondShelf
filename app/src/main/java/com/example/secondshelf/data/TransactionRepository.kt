package com.example.secondshelf.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import com.example.secondshelf.data.model.Transaction
import com.example.secondshelf.data.model.ListingUpdate
import java.util.UUID

class TransactionRepository(private val supabase: SupabaseClient) {

    /**
     * Called when the Seller accepts an offer.
     * Generates a token, creates the transaction, and reserves the item.
     */
    suspend fun createHandoff(listingId: String, buyerId: String, sellerId: String): String? {
        return try {
            // 1. Generate the secure, single-use token
            val newToken = UUID.randomUUID().toString()

            val newTransaction = Transaction(
                listingId = listingId,
                buyerId = buyerId,
                sellerId = sellerId,
                qrToken = newToken,
                status = "pending"
            )

            // 2. Insert into the transactions table
            supabase.postgrest["transactions"].insert(newTransaction)

            // 3. Update the listing status to 'reserved'
            val update = ListingUpdate(status = "reserved")
            supabase.postgrest["listings"].update(update) {
                filter { eq("id", listingId) }
            }

            // Return the token so it can be passed to the UI for the QR generator
            newToken
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Called when the Seller scans a QR code.
     * Verifies the token, ensures the user is the seller, and completes the sale.
     */
    suspend fun verifyScannedToken(scannedToken: String, currentUserId: String): Boolean {
        return try {
            // 1. Look up the transaction using the scanned token
            val transaction = supabase.postgrest["transactions"].select {
                filter {
                    eq("qr_token", scannedToken)
                    eq("status", "pending")
                }
            }.decodeSingleOrNull<Transaction>()

            // 2. Security Check: Does this transaction exist, and is the current user the actual seller?
            if (transaction != null && transaction.sellerId == currentUserId) {

                // 3. Mark transaction as completed
                supabase.postgrest["transactions"].update(mapOf("status" to "completed")) {
                    filter { eq("id", transaction.id!!) }
                }

                // 4. Mark the listing as sold
                val update = ListingUpdate(status = "sold")
                supabase.postgrest["listings"].update(update) {
                    filter { eq("id", transaction.listingId) }
                }

                true //verification successful
            } else {
                false //token invalid, already used, or wrong user
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}