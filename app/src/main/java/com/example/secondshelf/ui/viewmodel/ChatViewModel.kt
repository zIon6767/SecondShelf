package com.example.secondshelf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secondshelf.data.ChatRepository
import com.example.secondshelf.ui.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Immutable snapshot of everything [ConversationScreen] needs to render.
 *
 * @property messages          Ordered list of messages for this conversation.
 * @property isParticipantOnline Whether the other party is currently online.
 * @property participantName   Display name of the other party (passed in from nav).
 * @property isSending         True while a send operation is in-flight.
 * @property error             Non-null when a recoverable error should be shown.
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isParticipantOnline: Boolean = false,
    val participantName: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the [ConversationScreen] (Chat Page).
 *
 * Responsibilities:
 * - Load historical messages from Supabase on entry.
 * - Subscribe to Realtime INSERT (new messages) and UPDATE (is_read changes).
 * - Track / observe Presence for online status.
 * - Send messages and expose a loading state.
 * - Mark received messages as read on entry and on resume.
 * - Unsubscribe cleanly when the ViewModel is cleared (memory-leak free).
 */
class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // ── Conversation context (set once when entering the screen) ──────────────
    private var myUserId: String = ""
    private var participantId: String = ""
    private var listingId: String = ""

    // ── Entry point ───────────────────────────────────────────────────────────

    /**
     * Must be called from a [LaunchedEffect] when the Compose screen enters
     * composition.  Safe to call multiple times with the same arguments (no-op
     * if already initialised for the same conversation).
     *
     * @param myUserId      The currently authenticated user's UUID.
     * @param participantId The UUID of the other conversation participant.
     * @param listingId     The listing this conversation is about.
     * @param participantName Display name shown in the header.
     */
    fun enterChat(
        myUserId: String,
        participantId: String,
        listingId: String,
        participantName: String = ""
    ) {
        // Avoid re-initialising for the same conversation
        if (
            this.myUserId == myUserId &&
            this.participantId == participantId &&
            this.listingId == listingId
        ) return

        this.myUserId = myUserId
        this.participantId = participantId
        this.listingId = listingId

        if (participantName.isNotBlank()) {
            _uiState.update { it.copy(participantName = participantName) }
        }

        viewModelScope.launch {
            runCatching {
                // 1. Load history
                val history = repository.loadMessages(myUserId, participantId, listingId)
                _uiState.update { it.copy(messages = history, error = null) }

                // 2. Mark any unread messages from the participant as read
                markRead()

                // 3. Open Realtime channel
                val channelName = buildChannelName(myUserId, participantId, listingId)
                repository.enterChat(
                    scope = viewModelScope,
                    channelName = channelName,
                    myUserId = myUserId,
                    listingId = listingId,
                    onNewMessage = { msg -> handleIncomingMessage(msg) },
                    onMessageUpdated = { msg -> handleMessageUpdated(msg) },
                    onPresenceChange = { onlineIds ->
                        _uiState.update {
                            it.copy(isParticipantOnline = participantId in onlineIds)
                        }
                    }
                )
            }.onFailure { e ->
                e.printStackTrace()
                _uiState.update { it.copy(error = e.message ?: "Failed to connect to chat") }
            }
        }
    }

    // ── ON_RESUME hook ────────────────────────────────────────────────────────

    /**
     * Call this from a [LifecycleEventEffect] for [Lifecycle.Event.ON_RESUME].
     * Ensures that messages received while the app was backgrounded get marked
     * as read when the user returns to the screen.
     */
    fun onResume() {
        if (myUserId.isBlank() || participantId.isBlank() || listingId.isBlank()) return
        viewModelScope.launch {
            runCatching { markRead() }
        }
    }

    // ── Send ──────────────────────────────────────────────────────────────────

    /**
     * Sends [content] as a new message.  The UI is optimistically not updated
     * here; the INSERT Realtime event from Supabase will trigger [handleIncomingMessage]
     * and append it to the list.
     */
    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank() || myUserId.isBlank()) return

        _uiState.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            runCatching {
                repository.sendMessage(
                    senderId = myUserId,
                    receiverId = participantId,
                    listingId = listingId,
                    content = trimmed
                ).onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to send message") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to send message") }
            }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun markRead() = runCatching {
        repository.markMessagesRead(
            myId = myUserId,
            senderId = participantId,
            listingId = listingId
        )
        // Optimistically flip is_read in local state so the list updates instantly
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.senderId == participantId && !msg.isRead) msg.copy(isRead = true)
                    else msg
                }
            )
        }
    }.getOrElse { e ->
        e.printStackTrace()
    }

    /**
     * Handles a Realtime INSERT event.  Appends the message to the list if it
     * belongs to this conversation.  If the new message was sent by the
     * participant (not us), also triggers a read receipt.
     */
    private fun handleIncomingMessage(msg: ChatMessage) {
        // Filter: only accept messages that belong to this conversation
        val belongsHere = msg.listingId == listingId &&
            ((msg.senderId == myUserId && msg.receiverId == participantId) ||
             (msg.senderId == participantId && msg.receiverId == myUserId))
        if (!belongsHere) return

        _uiState.update { state ->
            // Avoid duplicates (Realtime may echo back our own sent message)
            val alreadyPresent = state.messages.any { it.id == msg.id }
            if (alreadyPresent) state
            else state.copy(messages = state.messages + msg)
        }

        // Auto-mark as read when participant sends to us
        if (msg.senderId == participantId) {
            viewModelScope.launch { markRead() }
        }
    }

    /**
     * Handles a Realtime UPDATE event.  Replaces the matching message in the
     * list with the updated version (e.g. is_read flipped to true).
     */
    private fun handleMessageUpdated(updated: ChatMessage) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == updated.id) updated else msg
                }
            )
        }
    }

    /**
     * Produces a deterministic, alphabetically-sorted channel name so both
     * participants always join the same channel regardless of who created it.
     */
    private fun buildChannelName(
        userId1: String,
        userId2: String,
        listingId: String
    ): String {
        val sorted = listOf(userId1, userId2).sorted()
        return "chat:${sorted[0]}:${sorted[1]}:$listingId"
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        // Release Realtime channel to prevent memory/connection leaks
        viewModelScope.launch { repository.leaveChat() }
    }
}
