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

private val jsonSerializer = Json { ignoreUnknownKeys = true }

/**
 * Repository responsible for all Supabase interactions related to the in-app chat.
 */
class ChatRepository {

    private var channel: RealtimeChannel? = null

    // ── Read ──────────────────────────────────────────────────────────────────

    suspend fun loadMessages(
        myId: String,
        participantId: String,
        listingId: String
    ): List<ChatMessage> = runCatching {
        supabase.postgrest.from("messages")
            .select {
                filter {
                    eq("listing_id", listingId)
                    isIn("sender_id", listOf(myId, participantId))
                    isIn("receiver_id", listOf(myId, participantId))
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<ChatMessage>()
    }.getOrElse { e ->
        e.printStackTrace()
        emptyList()
    }

    suspend fun loadConversations(myId: String): List<Conversation> = runCatching {
        val messages = supabase.postgrest.from("messages")
            .select {
                filter {
                    // 筛选 sender_id 或 receiver_id 是当前用户的所有消息
                    or {
                        eq("sender_id", myId)
                        eq("receiver_id", myId)
                    }
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<ChatMessage>()

        messages.groupBy {
            val otherId = if (it.senderId == myId) it.receiverId else it.senderId
            "${it.listingId}_$otherId"
        }.map { (key, msgs) ->
            val latest = msgs.first()
            val otherId = if (latest.senderId == myId) latest.receiverId else latest.senderId
            val unreadCount = msgs.count { it.senderId == otherId && it.receiverId == myId && !it.isRead }

            Conversation(
                id = "${latest.listingId}_$otherId",
                participantId = otherId,
                listingId = latest.listingId,
                lastMessage = latest.content,
                lastTimestamp = latest.createdAt ?: "",
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

    suspend fun enterChat(
        scope: CoroutineScope,
        channelName: String,
        myUserId: String,
        listingId: String,
        onNewMessage: suspend (ChatMessage) -> Unit,
        onMessageUpdated: suspend (ChatMessage) -> Unit,
        onPresenceChange: (onlineUserIds: Set<String>) -> Unit
    ) = runCatching {
        leaveChat()

        val ch = supabase.realtime.channel(channelName)
        channel = ch

        // 1. New messages (INSERT)
        ch.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter("listing_id", FilterOperator.EQ, listingId)
        }.onEach { action ->
            runCatching {
                val msg = jsonSerializer.decodeFromJsonElement(ChatMessage.serializer(), action.record)
                onNewMessage(msg)
            }
        }.launchIn(scope)

        // 2. Read-receipt updates (UPDATE)
        ch.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "messages"
            filter("listing_id", FilterOperator.EQ, listingId)
        }.onEach { action ->
            runCatching {
                val msg = jsonSerializer.decodeFromJsonElement(ChatMessage.serializer(), action.record)
                onMessageUpdated(msg)
            }
        }.launchIn(scope)

        // 3. Presence tracking via presenceDataFlow
        ch.presenceDataFlow<PresenceState>()
            .onEach { presenceList ->
                val onlineIds = presenceList.map { it.userId }.toSet()
                onPresenceChange(onlineIds)
            }.launchIn(scope)

        // Asynchronously subscribe without throwing fatal crash on socket error
        runCatching {
            ch.subscribe(blockUntilSubscribed = false)
            ch.track(PresenceState(userId = myUserId))
        }
    }.getOrElse { e ->
        e.printStackTrace()
    }

    suspend fun leaveChat() {
        channel?.let { ch ->
            runCatching { ch.unsubscribe() }
            runCatching { supabase.realtime.removeChannel(ch) }
        }
        channel = null
    }
}
