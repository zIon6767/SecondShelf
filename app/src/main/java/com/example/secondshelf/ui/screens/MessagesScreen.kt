package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.secondshelf.ui.model.getSellerName
import com.example.secondshelf.ui.model.getUserById
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@Composable
fun MessagesScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onConversationClick: (participantId: String, listingId: String) -> Unit
) {
    val conversations = viewModel.conversations

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(12.dp))
            Text("Messages", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        if (conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No messages yet", color = MaterialTheme.colorScheme.secondary)
                }
            }
        } else {
            LazyColumn {
                items(conversations) { conv ->
                    val listing = viewModel.getListing(conv.listingId)
                    val participant = getUserById(conv.participantId)

                    Surface(
                        onClick = { onConversationClick(conv.participantId, conv.listingId) },
                        color = if (conv.unreadCount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(participant?.avatarInitial ?: "?", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(getSellerName(conv.participantId), fontWeight = FontWeight.SemiBold)
                                Text(
                                    listing?.title ?: "Listing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    conv.lastMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(conv.lastTimestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                if (conv.unreadCount > 0) {
                                    Spacer(Modifier.height(4.dp))
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("${conv.unreadCount}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Divider(color = Color(0xFFE5E7EB).copy(alpha = 0.7f), modifier = Modifier.padding(start = 82.dp))
                }
            }
        }
    }
}

@Composable
fun ConversationScreen(
    participantId: String,
    listingId: String,
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    val listing = viewModel.getListing(listingId)
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(participantId, listingId) {
        viewModel.markConversationRead(participantId, listingId)
    }

    val messages = viewModel.getConversationMessages(participantId, listingId)

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(getSellerName(participantId), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(listing?.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == viewModel.currentUser.id
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
                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                msg.text,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                msg.timestamp,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Surface(shadowElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message…") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        viewModel.sendMessage(listingId, participantId, messageText)
                        messageText = ""
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}
