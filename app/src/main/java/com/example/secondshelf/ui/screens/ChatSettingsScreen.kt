package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@Composable
fun ChatSettingsScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    var messageNotifications by remember { mutableStateOf(true) }
    var messagePreviews by remember { mutableStateOf(true) }
    var inAppSound by remember { mutableStateOf(true) }
    var readReceipts by remember { mutableStateOf(true) }
    var onlineStatus by remember { mutableStateOf(true) }
    var autoReplyEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Chat & Message Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        // Settings Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Notifications
            Column {
                ChatSectionTitle("Notifications & Alerts")
                ChatSettingsCard {
                    ChatToggleRow(
                        icon = Icons.Outlined.Notifications,
                        iconColor = Color(0xFFFF9500),
                        title = "Message Notifications",
                        subtitle = "Receive instant alerts for incoming student inquiries",
                        checked = messageNotifications,
                        onCheckedChange = {
                            messageNotifications = it
                            viewModel.showToast(if (it) "Chat notifications enabled" else "Chat notifications muted")
                        }
                    )
                    HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                    ChatToggleRow(
                        icon = Icons.Outlined.Visibility,
                        iconColor = Color(0xFF007AFF),
                        title = "Message Previews",
                        subtitle = "Show sender name and message preview in alerts",
                        checked = messagePreviews,
                        onCheckedChange = { messagePreviews = it }
                    )
                    HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                    ChatToggleRow(
                        icon = Icons.Outlined.VolumeUp,
                        iconColor = Color(0xFF5856D6),
                        title = "In-App Sounds",
                        subtitle = "Play subtle sound when sending and receiving chats",
                        checked = inAppSound,
                        onCheckedChange = { inAppSound = it }
                    )
                }
            }

            // Section 2: Privacy
            Column {
                ChatSectionTitle("Privacy & Status")
                ChatSettingsCard {
                    ChatToggleRow(
                        icon = Icons.Outlined.DoneAll,
                        iconColor = Color(0xFF34C759),
                        title = "Read Receipts",
                        subtitle = "Let buyers and sellers know when you've seen their messages",
                        checked = readReceipts,
                        onCheckedChange = { readReceipts = it }
                    )
                    HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                    ChatToggleRow(
                        icon = Icons.Outlined.AccountCircle,
                        iconColor = Color(0xFF007AFF),
                        title = "Online Activity Status",
                        subtitle = "Show when you are currently active on the campus marketplace",
                        checked = onlineStatus,
                        onCheckedChange = { onlineStatus = it }
                    )
                }
            }

            // Section 3: Smart Messaging
            Column {
                ChatSectionTitle("Automated Responses")
                ChatSettingsCard {
                    ChatToggleRow(
                        icon = Icons.Outlined.SmartToy,
                        iconColor = Color(0xFF5856D6),
                        title = "Instant Inquiry Greeting",
                        subtitle = "Send a quick automated reply when a student first messages about your listing",
                        checked = autoReplyEnabled,
                        onCheckedChange = {
                            autoReplyEnabled = it
                            viewModel.showToast(if (it) "Auto-greeting enabled" else "Auto-greeting disabled")
                        }
                    )
                }
            }

            // Section 4: Chat Management
            Column {
                ChatSectionTitle("Data & Safety")
                ChatSettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.showToast("Temporary chat cache cleared successfully")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF636366).copy(alpha = 0.12f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.CleaningServices,
                                    contentDescription = null,
                                    tint = Color(0xFF636366),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clear Chat Cache",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Free up device storage without losing messages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.showToast("No blocked users found.")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF3B30).copy(alpha = 0.12f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Block,
                                    contentDescription = null,
                                    tint = Color(0xFFFF3B30),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Blocked Accounts",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Manage blocked student profiles",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ChatSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun ChatSettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE5E7EB))
    ) {
        Column(content = content)
    }
}

@Composable
private fun ChatToggleRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = iconColor.copy(alpha = 0.12f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                lineHeight = 16.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
