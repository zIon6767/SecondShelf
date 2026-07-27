package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class NotificationItem(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val subtitle: String,
    val time: String,
    val isUnread: Boolean = false
)

private val mockNotifications = listOf(
    NotificationItem(
        Icons.Default.TrendingDown, Color(0xFF34C759),
        "Price Drop Alert! 🎉",
        "Engineering Calculus dropped from RM 50 to RM 35",
        "2 min ago", isUnread = true
    ),
    NotificationItem(
        Icons.Default.ShoppingBag, Color(0xFF007AFF),
        "Your listing got a like",
        "Someone liked your Laptop Stand listing",
        "15 min ago", isUnread = true
    ),
    NotificationItem(
        Icons.Default.Chat, Color(0xFF5856D6),
        "New message from Sarah L.",
        "\"Is the calculator still available?\"",
        "1 hour ago", isUnread = true
    ),
    NotificationItem(
        Icons.Default.Verified, Color(0xFFFF9500),
        "Account Verified ✅",
        "Your university email has been verified",
        "3 hours ago"
    ),
    NotificationItem(
        Icons.Default.Star, Color(0xFFFF9500),
        "New Review from James T.",
        "⭐⭐⭐⭐⭐ Great seller, fast response!",
        "Yesterday"
    ),
    NotificationItem(
        Icons.Default.LocalOffer, Color(0xFFFF3B30),
        "Flash Sale – Books & Notes",
        "Limited time: 20% off on textbooks listed today",
        "Yesterday"
    ),
    NotificationItem(
        Icons.Default.Chat, Color(0xFF5856D6),
        "New message from Wei Ming",
        "\"Can we meet at the library tomorrow?\"",
        "2 days ago"
    ),
    NotificationItem(
        Icons.Default.Notifications, Color(0xFF636366),
        "System Update",
        "Second Shelf has been updated to v1.0.1",
        "3 days ago"
    ),
)

@Composable
fun NotificationsScreen(onNavigateBack: () -> Unit) {
    val unreadCount = mockNotifications.count { it.isUnread }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (unreadCount > 0) {
                        Text(
                            "$unreadCount unread",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            TextButton(onClick = {}) {
                Text(
                    "Mark all read",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            mockNotifications.forEach { notif ->
                NotificationRow(notif)
                Divider(color = Color(0xFFE5E7EB).copy(alpha = 0.7f))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NotificationRow(notif: NotificationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (notif.isUnread) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                else Color.Transparent
            )
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Surface(
            shape = CircleShape,
            color = notif.iconColor.copy(alpha = 0.12f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    notif.icon,
                    contentDescription = null,
                    tint = notif.iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                notif.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (notif.isUnread) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                notif.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                notif.time,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 11.sp,
                color = if (notif.isUnread) MaterialTheme.colorScheme.primary else Color(0xFFD1D5DB)
            )
        }

        // Unread dot
        if (notif.isUnread) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}
