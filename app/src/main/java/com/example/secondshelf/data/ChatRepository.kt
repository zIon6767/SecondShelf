package com.example.secondshelf.data

import com.example.secondshelf.supabase
import com.example.secondshelf.ui.model.ChatMessage
import com.example.secondshelf.ui.model.Conversation
import com.example.secondshelf.ui.model.PresenceState
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.presenceDataFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val jsonSerializer = Json { 
    ignoreUnknownKeys = true 
    coerceInputValues = true
    isLenient = true
    explicitNulls = false
}

/**
 * Repository responsible for all Supabase interactions related to the in-app chat.
 */
class ChatRepository {

    private var channel: RealtimeChannel? = null

    suspend fun fetchUserName(userId: String): String? = runCatching {
        supabase.postgrest.rpc("get_marketplace_seller_profiles")
            .decodeList<com.example.secondshelf.data.marketplace.SellerProfileRecord>()
            .find { it.id == userId }
            ?.name
            ?.takeIf { it.isNotBlank() && it != "Student" && it != "User" }
    }.getOrNull()


    // ── Read ──────────────────────────────────────────────────────────────────

    suspend fun loadMessages(
        myId: String,
        participantId: String,
        listingId: String
    ): List<ChatMessage> = runCatching {
        supabase.postgrest.from("messages")
            .select {
                filter {
                    and {
                        eq("listing_id", listingId)
                        or {
                            and {
                                eq("sender_id", myId)
                                eq("receiver_id", participantId)
                            }
                            and {
                                eq("sender_id", participantId)
                                eq("receiver_id", myId)
                            }
                        }
                    }
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<ChatMessage>()
    }.getOrDefault(emptyList())

    /**
     * Loads all conversation threads for [myId] by fetching every message
     * where the user is sender or receiver, then grouping by (listingId + opponent).
     * Returns the most-recent conversations first.
     */
    suspend fun loadConversations(myId: String): List<Conversation> = runCatching {
        val messages = supabase.postgrest.from("messages")
            .select {
                filter {
                    or {
                        eq("sender_id", myId)
                        eq("receiver_id", myId)
                    }
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<ChatMessage>()

        messages
            .groupBy { msg ->
                val otherId = if (msg.senderId == myId) msg.receiverId else msg.senderId
                "${msg.listingId}_$otherId"
            }
            .map { (_, msgs) ->
                val latest = msgs.first()
                val otherId = if (latest.senderId == myId) latest.receiverId else latest.senderId
                val unreadCount = msgs.count {
                    it.senderId == otherId && it.receiverId == myId && !it.isRead
                }
                Conversation(
                    id = "${latest.listingId}_$otherId",
                    participantId = otherId,
                    listingId = latest.listingId,
                    lastMessage = latest.content,
                    lastTimestamp = latest.createdAt,
                    unreadCount = unreadCount
                )
            }
    }.getOrDefault(emptyList())

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun sendMessage(
        senderId: String,
        receiverId: String,
        listingId: String,
        content: String
    ): Result<Unit> = runCatching {
        supabase.postgrest.from("messages").insert(
            buildJsonObject {
                put("sender_id", senderId)
                put("receiver_id", receiverId)
                put("listing_id", listingId)
                put("content", content)
                put("is_read", false)
            }
        )
    }

    suspend fun markMessagesRead(
        myId: String,
        senderId: String,
        listingId: String
    ): Result<Unit> = runCatching {
        supabase.postgrest.from("messages")
            .update({ set("is_read", true) }) {
                filter {
                    eq("sender_id", senderId)
                    eq("receiver_id", myId)
                    eq("listing_id", listingId)
                    eq("is_read", false)
                }
            }
    }

    // ── Realtime ──────────────────────────────────────────────────────────────

    suspend fun enterChat(
        scope: CoroutineScope,
        channelName: String,
        myUserId: String,
        listingId: String,
        onNewMessage: suspend (ChatMessage) -> Unit,
        onMessageUpdated: suspend (ChatMessage) -> Unit,
        onPresenceChange: (onlineUserIds: Set<String>) -> Unit
    ) {
        leaveChat()

        // Remove any existing channel with the same name to guarantee clean listener attachment
        runCatching {
            val existing = supabase.realtime.channel(channelName)
            existing.unsubscribe()
            supabase.realtime.removeChannel(existing)
        }

        // 1. Create fresh channel
        val ch = supabase.realtime.channel(channelName)
        channel = ch

        // 2. Attach all Realtime event listeners BEFORE calling subscribe/join
        ch.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter("listing_id", FilterOperator.EQ, listingId)
        }.onEach { action ->
            runCatching {
                val msg = jsonSerializer.decodeFromJsonElement(ChatMessage.serializer(), action.record)
                onNewMessage(msg)
            }.onFailure { e ->
                android.util.Log.e("ChatRepository", "Error parsing incoming INSERT message", e)
            }
        }.launchIn(scope)

        ch.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "messages"
            filter("listing_id", FilterOperator.EQ, listingId)
        }.onEach { action ->
            runCatching {
                val msg = jsonSerializer.decodeFromJsonElement(ChatMessage.serializer(), action.record)
                onMessageUpdated(msg)
            }.onFailure { e ->
                android.util.Log.e("ChatRepository", "Error parsing incoming UPDATE message", e)
            }
        }.launchIn(scope)

        ch.presenceDataFlow<PresenceState>()
            .onEach { presenceList ->
                runCatching {
                    val onlineIds = presenceList.map { it.userId }.toSet()
                    onPresenceChange(onlineIds)
                }
            }.launchIn(scope)

        // 3. Strictly subscribe to the channel AFTER all flows have been declared and attached
        runCatching {
            ch.subscribe(blockUntilSubscribed = true)
            ch.track(PresenceState(userId = myUserId))
        }.onFailure { e ->
            android.util.Log.e("ChatRepository", "Error subscribing to realtime channel", e)
        }
    }

    suspend fun leaveChat() {
        channel?.let { ch ->
            runCatching { ch.unsubscribe() }
            runCatching { supabase.realtime.removeChannel(ch) }
        }
        channel = null
    }
}
