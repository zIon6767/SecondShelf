package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.secondshelf.ui.model.formatMessageTime
import com.example.secondshelf.ui.model.getSellerName
import com.example.secondshelf.ui.model.getUserById
import com.example.secondshelf.ui.viewmodel.ChatViewModel
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Chat List Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MessagesScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onConversationClick: (participantId: String, listingId: String) -> Unit
) {
    val conversations = viewModel.conversations
    val isLoading = viewModel.isLoadingConversations
    val primary = MaterialTheme.colorScheme.primary

    // Read the "Message Previews" setting from SharedPreferences via ViewModel
    val showPreviews = viewModel.showMessagePreviews

    // Refresh from Supabase every time the screen enters composition
    LaunchedEffect(Unit) {
        viewModel.refreshConversations()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── Premium header ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Messages",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (conversations.isNotEmpty()) {
                    Text(
                        "${conversations.size} chat${if (conversations.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f), thickness = 1.dp)

        // ── Loading indicator ─────────────────────────────────────────────────
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = primary
            )
        }

        if (conversations.isEmpty() && !isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No message yet", color = MaterialTheme.colorScheme.secondary)
                }
            }
        } else if (!isLoading || conversations.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(conversations) { conv ->
                    val isUserBlocked = viewModel.isBlocked(conv.participantId)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConversationClick(conv.participantId, conv.listingId) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Avatar: try real name first, fallback to first char of UUID
                            val displayName = getSellerName(conv.participantId)
                            val avatarLetter = if (displayName != "Unknown Seller") {
                                displayName.first().uppercaseChar().toString()
                            } else {
                                conv.participantId.first().uppercaseChar().toString()
                            }
                            Surface(shape = CircleShape, color = if (isUserBlocked) Color(0xFFFF3B30) else MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isUserBlocked) {
                                        Icon(Icons.Outlined.Block, contentDescription = "Blocked", tint = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text(avatarLetter, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (displayName != "Unknown Seller") displayName else conv.participantId.take(8) + "…",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isUserBlocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isUserBlocked) {
                                    // Always hide preview content for blocked users
                                    Text(
                                        "You blocked this account",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFF3B30).copy(alpha = 0.8f),
                                        maxLines = 1
                                    )
                                } else {
                                    Text(
                                        // Respect the "Message Previews" setting
                                        if (showPreviews) conv.lastMessage.ifBlank { "Tap to enter conversation" }
                                        else "• • •",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                // Show formatted timestamp if available
                                if (conv.lastTimestamp.isNotBlank() && !isUserBlocked) {
                                    Text(
                                        formatMessageTime(conv.lastTimestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                if (conv.unreadCount > 0 && !isUserBlocked) {
                                    Spacer(Modifier.height(4.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                conv.unreadCount.coerceAtMost(99).toString(),
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Conversation (Chat Detail) Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ConversationScreen(
    viewModel: MarketplaceViewModel,
    chatViewModel: ChatViewModel,
    participantId: String,
    listingId: String,
    currentUserId: String,
    onNavigateBack: () -> Unit
) {
    val uiState by chatViewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listing = viewModel.getListing(listingId)
    val listState = rememberLazyListState()

    // Read chat settings from ViewModel (backed by SharedPreferences)
    val showOnlineStatus = viewModel.showOnlineStatus
    val showReadReceipts = viewModel.showReadReceipts

    // ── Three-dots menu & block dialog state ────────────────────────────────
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }

    // Sync the block state from MarketplaceViewModel into ChatViewModel state
    // so that re-compositions (e.g. after blocking) reflect immediately.
    val isParticipantBlocked = viewModel.isBlocked(participantId)
    LaunchedEffect(isParticipantBlocked) {
        chatViewModel.setBlocked(isParticipantBlocked)
    }

    // ── Initialise chat on first composition ──────────────────────────────────
    LaunchedEffect(participantId, listingId, currentUserId) {
        if (participantId.isBlank() || listingId.isBlank()) {
            onNavigateBack()
            return@LaunchedEffect
        }
        runCatching {
            viewModel.markConversationRead(participantId, listingId) // keep conversation list in sync
            viewModel.upsertConversation(                           // ensure Messages list shows this chat
                participantId = participantId,
                listingId = listingId
            )
            chatViewModel.enterChat(
                myUserId = currentUserId.ifBlank { "user-current" },
                participantId = participantId,
                listingId = listingId,
                participantName = getSellerName(participantId),
                isParticipantBlocked = viewModel.isBlocked(participantId)
            )
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    // ── Scroll to bottom when messages update ─────────────────────────────────
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // ── Mark read on every resume (foreground return) ─────────────────────────
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        chatViewModel.onResume()
    }

    // ── Block account confirmation dialog ─────────────────────────────────────
    if (showBlockConfirmDialog) {
        val participantName = uiState.participantName.ifBlank { getSellerName(participantId) }
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            icon = {
                Icon(Icons.Outlined.Block, contentDescription = null, tint = Color(0xFFFF3B30))
            },
            title = {
                Text("Block Account?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Blocking $participantName will prevent them from sending you messages " +
                    "and hide this conversation. This action can be undone from " +
                    "Settings → Chat & Messages → Blocked Accounts."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockConfirmDialog = false
                        viewModel.blockUser(participantId)
                        chatViewModel.setBlocked(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
                ) {
                    Text("Block Account", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Rate dialog ───────────────────────────────────────────────────────────
    var showRatingDialog by remember { mutableStateOf(false) }

    if (showRatingDialog) {
        com.example.secondshelf.ui.components.RatingReviewDialog(
            targetUserName = getSellerName(participantId),
            listingTitle = listing?.title ?: "Campus Item",
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating, comment, tags ->
                viewModel.addReview(
                    targetUserId = participantId,
                    listingTitle = listing?.title ?: "Campus Item",
                    rating = rating,
                    comment = comment,
                    tags = tags
                )
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── Premium conversation header ─────────────────────────────────────
        val primary = MaterialTheme.colorScheme.primary
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 14.dp)
        ) {
            // Back + Rate Deal + Three-dots row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ── Back button ───────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // ── Right-side actions ────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // "Rate Deal" button — hidden when user is blocked
                    if (!uiState.isBlocked) {
                        Button(
                            onClick = { showRatingDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primary.copy(alpha = 0.12f),
                                contentColor = primary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("★ Rate Deal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // ── Three-dots ⋮ menu ─────────────────────────────────
                    Box {
                        IconButton(
                            onClick = { showOptionsMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            if (!uiState.isBlocked) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Outlined.Block,
                                                contentDescription = null,
                                                tint = Color(0xFFFF3B30),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                "Block Account",
                                                color = Color(0xFFFF3B30),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        showBlockConfirmDialog = true
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                "Unblock Account",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.unblockUser(participantId)
                                        chatViewModel.setBlocked(false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Participant name + online presence dot
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = uiState.participantName.ifBlank { getSellerName(participantId) },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isBlocked) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(8.dp))
                // ── Online / Offline presence indicator ────────────────────
                // Only show if the "Online Activity Status" setting is enabled
                if (showOnlineStatus && !uiState.isBlocked) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.isParticipantOnline) Color(0xFF22C55E) // green-500
                                else Color(0xFF9CA3AF)                              // gray-400
                            )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (uiState.isParticipantOnline) "Online" else "Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.isParticipantOnline) Color(0xFF16A34A) else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (uiState.isBlocked) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "You have blocked this account",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF3B30)
                )
            } else if (!listing?.title.isNullOrBlank()) {
                Text(
                    listing!!.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f), thickness = 1.dp)

        // ── Message list ──────────────────────────────────────────────────────
        if (uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (uiState.isBlocked) "You blocked this account."
                        else "Say hello! Be the first to send a message.",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(uiState.messages, key = { it.id }) { msg ->
                    val isMe = msg.senderId == currentUserId
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp, topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                ),
                                color = if (isMe) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        msg.content,
                                        color = if (isMe) Color.White
                                                else MaterialTheme.colorScheme.onSurface
                                    )
                                    // Timestamp
                                    val timeDisplay = remember(msg.createdAt) {
                                        formatMessageTime(msg.createdAt)
                                    }
                                    Text(
                                        timeDisplay,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isMe) Color.White.copy(alpha = 0.7f)
                                                else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                        // ── Double Checkmark Read Receipts (for sent messages) ──
                        if (isMe) {
                            val isReadActive = msg.isRead && showReadReceipts
                            val checkmarkColor = if (isReadActive) Color(0xFF007AFF) else Color(0xFF8E8E93)
                            val statusLabel = if (isReadActive) "Read" else "Sent"
                            Spacer(Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = statusLabel,
                                    tint = checkmarkColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = checkmarkColor,
                                    fontWeight = if (isReadActive) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Error snackbar area ───────────────────────────────────────────────
        uiState.error?.let { errMsg ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    errMsg,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // ── Blocked account banner ────────────────────────────────────────────
        if (uiState.isBlocked) {
            Surface(
                color = Color(0xFFFF3B30).copy(alpha = 0.10f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.Block,
                        contentDescription = null,
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "You blocked this account. Messaging is disabled.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF3B30),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ── Input bar ─────────────────────────────────────────────────────────
        Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { if (!uiState.isBlocked) messageText = it },
                    placeholder = {
                        Text(
                            if (uiState.isBlocked) "Messaging disabled" else "Type a message…"
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    enabled = !uiState.isBlocked
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        chatViewModel.sendMessage(messageText)
                        messageText = ""
                    },
                    enabled = messageText.isNotBlank() && !uiState.isSending && !uiState.isBlocked
                ) {
                    if (uiState.isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun MessagesScreenPreview() {
    com.example.secondshelf.ui.theme.SecondShelfTheme {
        MessagesScreen(
            viewModel = MarketplaceViewModel(android.app.Application()),
            onNavigateBack = {},
            onConversationClick = { _, _ -> }
        )
    }
}
