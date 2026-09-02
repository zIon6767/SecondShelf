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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.model.AppNotification
import com.example.secondshelf.ui.model.NotificationType
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@Composable
fun NotificationsScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
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
                    Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (unreadCount > 0) {
                        Text("$unreadCount unread", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            TextButton(onClick = viewModel::markAllNotificationsRead) {
                Text("Mark all read", color = MaterialTheme.colorScheme.primary)
            }
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                notifications.forEach { notif ->
                    NotificationRow(
                        notif = notif,
                        onClick = { viewModel.markNotificationRead(notif.id) },
                        onDismiss = { viewModel.clearNotification(notif.id) }
                    )
                    Divider(color = Color(0xFFE5E7EB).copy(alpha = 0.7f))
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
            .background(if (!notif.read) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f) else Color.Transparent)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                notif.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (!notif.read) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(Modifier.height(2.dp))
            Text(notif.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(
                notif.time,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = if (!notif.read) MaterialTheme.colorScheme.primary else Color(0xFFD1D5DB)
            )
        }
        if (!notif.read) {
            Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape).align(Alignment.CenterVertically))
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFFD1D5DB), modifier = Modifier.size(16.dp))
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
