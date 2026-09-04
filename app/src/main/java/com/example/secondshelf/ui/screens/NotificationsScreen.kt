package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.model.AppNotification
import com.example.secondshelf.ui.model.NotificationType
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@Composable
fun NotificationsScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToConversation: (participantId: String, listingId: String) -> Unit = { _, _ -> },
    onNavigateToListing: (listingId: String) -> Unit = {}
) {
    val notifications = viewModel.notifications
    val unreadCount = viewModel.getUnreadNotificationCount()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Notice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (unreadCount > 0) {
                        Text("$unreadCount unread", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { viewModel.simulateIncomingMessage() }) {
                    Text("Test msg", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = viewModel::markAllNotificationsRead) {
                        Text("Mark all read", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Text(
                        "No notices yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Incoming chat messages and campus marketplace alerts will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.simulateIncomingMessage() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Simulate Incoming Message")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                notifications.forEach { notif ->
                    NotificationRow(
                        notif = notif,
                        onClick = {
                            viewModel.markNotificationRead(notif.id)
                            if (notif.type == NotificationType.MESSAGE && !notif.participantId.isNullOrBlank() && !notif.listingId.isNullOrBlank()) {
                                onNavigateToConversation(notif.participantId, notif.listingId)
                            } else if (notif.type == NotificationType.LISTING_POSTED && !notif.listingId.isNullOrBlank()) {
                                onNavigateToListing(notif.listingId)
                            }
                        },
                        onDismiss = { viewModel.clearNotification(notif.id) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notif: AppNotification,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val (icon, color) = notificationIcon(notif.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(if (!notif.read) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    notif.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (!notif.read) FontWeight.Bold else FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (!notif.read) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                notif.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    notif.time,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = if (!notif.read) MaterialTheme.colorScheme.primary else Color(0xFF9E9E9E)
                )
                if (notif.type == NotificationType.MESSAGE && !notif.participantId.isNullOrBlank()) {
                    Text(
                        "Tap to reply •",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFFBDBDBD), modifier = Modifier.size(16.dp))
        }
    }
}

private fun notificationIcon(type: NotificationType): Pair<ImageVector, Color> = when (type) {
    NotificationType.SAVED -> Icons.Default.Favorite to Color(0xFFFF3B30)
    NotificationType.MESSAGE -> Icons.Default.Chat to Color(0xFF5856D6)
    NotificationType.INTEREST -> Icons.Default.TrendingUp to Color(0xFF34C759)
    NotificationType.DEAL -> Icons.Default.LocalOffer to Color(0xFFFF9500)
    NotificationType.LISTING_POSTED -> Icons.Default.CheckCircle to Color(0xFF007AFF)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    com.example.secondshelf.ui.theme.SecondShelfTheme {
        NotificationsScreen(
            viewModel = MarketplaceViewModel(),
            onNavigateBack = {}
        )
    }
}

