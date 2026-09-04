package com.example.secondshelf.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.content.Context
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.outlined.Security
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.secondshelf.ui.model.formatPrice
import com.example.secondshelf.ui.model.getSellerName
import com.example.secondshelf.ui.model.getUserById
import com.example.secondshelf.ui.viewmodel.ChatViewModel
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

private fun formatChatTime(raw: String): String {
    if (raw.isBlank()) return ""
    return runCatching {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val zonedDateTime = java.time.OffsetDateTime.parse(raw).atZoneSameInstant(java.time.ZoneId.systemDefault())
                return zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            } catch (_: Exception) {
                try {
                    val ldt = java.time.LocalDateTime.parse(raw.take(19))
                    return ldt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                } catch (_: Exception) {}
            }
        }
        val clean = raw.replace("T", " ")
        if (clean.contains(" ")) {
            clean.substringAfter(" ").take(5)
        } else {
            clean.take(5)
        }
    }.getOrDefault(raw.take(5))
}

fun resolveParticipantName(participantId: String, listingId: String, viewModel: MarketplaceViewModel): String {
    // 1. Direct listing match
    val listing = viewModel.getListing(listingId)
    if (listing != null) {
        if (listing.sellerId == participantId && listing.sellerName.isNotBlank() && listing.sellerName != "Unknown Seller" && listing.sellerName != "Student") {
            return listing.sellerName
        }
    }
    // 2. Any listing by this seller
    val listingBySeller = viewModel.listings.find { it.sellerId == participantId && it.sellerName.isNotBlank() && it.sellerName != "Unknown Seller" && it.sellerName != "Student" }
    if (listingBySeller != null) {
        return listingBySeller.sellerName
    }
    // 3. Current user
    if (participantId == viewModel.currentUser.id && viewModel.currentUser.name.isNotBlank() && viewModel.currentUser.name != "Student") {
        return viewModel.currentUser.name
    }
    // 4. Mock / registered users
    val mock = getUserById(participantId)
    if (mock != null && mock.name.isNotBlank() && mock.name != "Student") {
        return mock.name
    }
    val fallback = getSellerName(participantId)
    return if (fallback.isNotBlank() && fallback != "Unknown Seller" && fallback != "Student") fallback else "Seller"
}

@Composable
fun MessagesScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit = {},
    onConversationClick: (participantId: String, listingId: String) -> Unit
) {
    val conversations = viewModel.conversations
    val primary = MaterialTheme.colorScheme.primary

    // Refresh conversation list from database on screen entry
    LaunchedEffect(viewModel.currentUser.id) {
        if (viewModel.currentUser.id.isNotBlank()) {
            viewModel.loadConversationsFromDb(viewModel.currentUser.id)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── Premium header ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 16.dp)
        ) {
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

        if (conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No messages yet", color = MaterialTheme.colorScheme.secondary)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(conversations) { conv ->
                    val resolvedName = resolveParticipantName(conv.participantId, conv.listingId, viewModel)
                    val listing = viewModel.getListing(conv.listingId)
                    val localMessages = viewModel.getConversationMessages(conv.participantId, conv.listingId)
                    val lastLocalMsg = localMessages.lastOrNull()
                    
                    // Source the very latest message text and timestamp
                    val latestMessageText = lastLocalMsg?.text?.takeIf { it.isNotBlank() } ?: conv.lastMessage
                    val latestTimestamp = lastLocalMsg?.timestamp?.takeIf { it.isNotBlank() } ?: conv.lastTimestamp
                    val isLastFromMe = lastLocalMsg?.senderId == viewModel.currentUser.id
                    val isLastRead = lastLocalMsg?.read == true

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConversationClick(conv.participantId, conv.listingId) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(resolvedName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = resolvedName,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (latestTimestamp.isNotBlank()) {
                                        Text(
                                            text = formatChatTime(latestTimestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                if (listing != null) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Item: ${listing.title}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                // ── Message Preview (Outside chat bubbles) ────
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f, fill = false),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isLastFromMe) {
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = null,
                                                tint = if (isLastRead) Color(0xFF2196F3) else Color(0xFF9E9E9E),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = latestMessageText.ifBlank { "No messages yet" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (conv.unreadCount > 0) {
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(8.dp)
                                        ) {}
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

@Composable
fun ConversationScreen(
    viewModel: MarketplaceViewModel,
    chatViewModel: ChatViewModel,
    participantId: String,
    listingId: String,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onNavigateToScanner: () -> Unit = {}
) {
    val uiState by chatViewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listing = viewModel.getListing(listingId)
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("second_shelf_chat_settings", Context.MODE_PRIVATE) }
    var blockedAccounts by remember {
        mutableStateOf(
            prefs.getStringSet("blocked_accounts", emptySet())?.toSet() ?: emptySet()
        )
    }
    val resolvedSellerName = resolveParticipantName(participantId, listingId, viewModel)
    val isBlocked = participantId in blockedAccounts || resolvedSellerName in blockedAccounts
    val isSeller = listing != null && (
        listing.sellerId == currentUserId ||
        (viewModel.currentUser.id.isNotBlank() && listing.sellerId == viewModel.currentUser.id) ||
        listing.isOwnedByCurrentUser
    )

    fun blockUser() {
        val updated = blockedAccounts + participantId + resolvedSellerName
        blockedAccounts = updated
        prefs.edit().putStringSet("blocked_accounts", updated).apply()
        viewModel.showToast("Account blocked")
    }

    fun unblockUser() {
        val updated = blockedAccounts - participantId - resolvedSellerName
        blockedAccounts = updated
        prefs.edit().putStringSet("blocked_accounts", updated).apply()
        viewModel.showToast("Account unblocked")
    }

    var showRatingDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var showQrScreen by remember { mutableStateOf(false) }
    var activeQrToken by remember { mutableStateOf<String?>(null) }
    var isDealDeclined by remember { mutableStateOf(false) }

    fun handleAcceptDeal() {
        val buyerId = if (isSeller) participantId else currentUserId
        val sellerId = listing?.sellerId ?: currentUserId
        viewModel.createHandoffToken(listingId, buyerId, sellerId) { token ->
            activeQrToken = token
            showQrScreen = true
        }
    }

    // ── Initialise chat on first composition ──────────────────────────────────
    LaunchedEffect(participantId, listingId) {
        viewModel.markConversationRead(participantId, listingId) // keep conversation list in sync
        chatViewModel.enterChat(
            myUserId = currentUserId,
            participantId = participantId,
            listingId = listingId,
            participantName = resolvedSellerName
        )
    }

    // ── Scroll to bottom when messages update (crash-proof) ───────────────────
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            runCatching {
                listState.scrollToItem(uiState.messages.size - 1)
            }
        }
    }

    // ── Mark read on every resume (foreground return) ─────────────────────────
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        chatViewModel.onResume()
    }

    if (showRatingDialog) {
        com.example.secondshelf.ui.components.RatingReviewDialog(
            targetUserName = resolvedSellerName,
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

    if (showBlockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            title = { Text("Block Account") },
            text = {
                Text("Are you sure you want to block ${uiState.participantName.ifBlank { resolvedSellerName }}? You will no longer be able to message each other.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBlockConfirmDialog = false
                        blockUser()
                    }
                ) {
                    Text("Block", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── QR Code Pickup Pass Dialog for Deal Acceptance ───────────────────────
    if (showQrScreen) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showQrScreen = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                QrScreen(
                    onNavigateBack = { showQrScreen = false },
                    transactionToken = activeQrToken ?: listing?.id ?: listingId,
                    itemName = listing?.title ?: "Campus Item",
                    price = if (listing != null) formatPrice(listing.price) else "RM 40",
                    sellerName = uiState.participantName.ifBlank { resolvedSellerName }
                )
            }
        }
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
            // Back + Deal Actions + Kebab Menu Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSeller) {
                        Button(
                            onClick = { handleAcceptDeal() },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Show Pickup QR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(6.dp))
                    } else {
                        Button(
                            onClick = { onNavigateToScanner() },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Scan QR Code", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                    Button(
                        onClick = { showRatingDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primary.copy(alpha = 0.12f),
                            contentColor = primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("★ Rate", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(4.dp))
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (isSeller) {
                                DropdownMenuItem(
                                    text = { Text("Show Pickup QR Code") },
                                    onClick = {
                                        showMenu = false
                                        handleAcceptDeal()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.QrCode2, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Decline Deal") },
                                    onClick = {
                                        showMenu = false
                                        isDealDeclined = true
                                        viewModel.showToast("Deal offer declined")
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                )
                                HorizontalDivider()
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Scan Seller's QR Code") },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToScanner()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                    }
                                )
                                HorizontalDivider()
                            }
                            if (isBlocked) {
                                DropdownMenuItem(
                                    text = { Text("Unblock Account") },
                                    onClick = {
                                        showMenu = false
                                        unblockUser()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.LockOpen, contentDescription = null)
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Block Account", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showBlockConfirmDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Block,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
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
                    text = uiState.participantName.ifBlank { resolvedSellerName },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(8.dp))
                // ── Online / Offline presence indicator ────────────────────
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

            if (!listing?.title.isNullOrBlank()) {
                Text(
                    listing!!.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f), thickness = 1.dp)

        // ── Interactive Deal Action Banner ──────
        if (listing != null && !isDealDeclined) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = primary.copy(alpha = 0.08f),
                border = BorderStroke(0.5.dp, primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = listing.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatPrice(listing.price),
                            style = MaterialTheme.typography.labelMedium,
                            color = primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Deny Button
                        OutlinedButton(
                            onClick = {
                                isDealDeclined = true
                                viewModel.showToast("Deal offer declined")
                            },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(3.dp))
                            Text("Deny", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.width(6.dp))
                        if (isSeller) {
                            Button(
                                onClick = { handleAcceptDeal() },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("Show Pickup QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { onNavigateToScanner() },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("Scan Seller's QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

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
                        "Say hello! Be the first to send a message.",
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
                itemsIndexed(
                    items = uiState.messages,
                    key = { index, msg ->
                        "${msg.id.ifBlank { "msg" }}_${index}_${msg.senderId}_${msg.createdAt.hashCode()}_${msg.content.hashCode()}"
                    }
                ) { _, msg ->
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
                                        formatChatTime(msg.createdAt)
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
                        // ── Double checkmark delivery & read status (below outgoing message bubble) ──
                        if (isMe) {
                            Spacer(Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth().padding(end = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = if (msg.isRead) "Read (✓✓)" else "Delivered (✓✓)",
                                    tint = if (msg.isRead) Color(0xFF2196F3) else Color(0xFF9E9E9E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = if (msg.isRead) "Read" else "Delivered",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (msg.isRead) Color(0xFF2196F3) else Color(0xFF9E9E9E),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Blocked user banner ───────────────────────────────────────────────
        if (isBlocked) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "You have blocked this account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(onClick = { unblockUser() }) {
                        Text("Unblock", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
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
                    onValueChange = { messageText = it },
                    placeholder = { Text(if (isBlocked) "Account is blocked" else "Type a message…") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    enabled = !isBlocked
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        chatViewModel.sendMessage(messageText)
                        messageText = ""
                    },
                    enabled = messageText.isNotBlank() && !uiState.isSending && !isBlocked
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
        // Preview
    }
}


