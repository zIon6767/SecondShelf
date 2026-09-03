package com.example.secondshelf.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.view.SoundEffectConstants
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel
import java.util.Locale

// ==============================================================================
// 1. MAIN SETTINGS SCREEN
// ==============================================================================

@Composable
fun SettingsScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToMyListings: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToChatSettings: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToCampusSafety: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val user = viewModel.currentUser
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out of Second Shelf?") },
            confirmButton = {
                TextButton(
                    onClick = { showLogoutDialog = false; onLogout() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Log Out", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to permanently delete your account?\n\nThis action cannot be undone. All your listings, messages, and profile data will be permanently cleared.")
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteAccountDialog = false; onDeleteAccount() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete Account", fontWeight = FontWeight.Bold, color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") } },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(12.dp))
            Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // User Info Summary Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(user.avatarInitial, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (user.email.isNotBlank()) {
                            Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text(
                            if (user.campus.isNotBlank()) "${user.university} • ${user.campus}" else user.university,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            SettingsSectionTitle("Account & Marketplace")
            SettingsGroup {
                SettingsNavItem(Icons.Default.Edit, "Edit Profile", Color(0xFF34C759), onNavigateToEditProfile)
                HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                SettingsNavItem(Icons.Default.Inventory2, "My Listings", MaterialTheme.colorScheme.primary, onNavigateToMyListings)
                HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                SettingsNavItem(Icons.Default.Favorite, "Saved Items", Color(0xFFFF3B30), onNavigateToSaved)
                HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                SettingsNavItem(Icons.Default.Chat, "Chat & Messages", Color(0xFF5856D6), onNavigateToChatSettings)
            }

            Spacer(Modifier.height(20.dp))
            SettingsSectionTitle("Preferences")
            SettingsGroup {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    label = "Push Notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        viewModel.showToast(if (it) "Notifications enabled" else "Notifications muted")
                    },
                    iconColor = Color(0xFFFF9500)
                )
            }

            Spacer(Modifier.height(20.dp))
            SettingsSectionTitle("Trust & Safety")
            SettingsGroup {
                SettingsNavItem(Icons.Default.Description, "Terms & Conditions", MaterialTheme.colorScheme.primary, onNavigateToTerms)
                HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                SettingsNavItem(Icons.Default.Shield, "Campus Safety & Meetup Tips", Color(0xFF007AFF), onNavigateToCampusSafety)
                HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                SettingsNavItem(Icons.Default.Info, "About Second Shelf", Color(0xFF5856D6), onNavigateToAbout)
            }

            Spacer(Modifier.height(24.dp))
            SettingsGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f), modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Text("Log Out", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                }

                HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteAccountDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFF3B30).copy(alpha = 0.12f), modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFFF3B30), modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Text("Delete Account", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF3B30))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ==============================================================================
// 2. CHAT & MESSAGE SETTINGS SCREEN
// ==============================================================================

@Composable
fun ChatSettingsScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val prefs = remember { context.getSharedPreferences("second_shelf_chat_settings", Context.MODE_PRIVATE) }

    var messageNotifications by remember { mutableStateOf(prefs.getBoolean("msg_notifications", true)) }
    var messagePreviews by remember { mutableStateOf(prefs.getBoolean("msg_previews", true)) }
    var inAppSound by remember { mutableStateOf(prefs.getBoolean("in_app_sound", true)) }
    var readReceipts by remember { mutableStateOf(prefs.getBoolean("read_receipts", true)) }
    var onlineStatus by remember { mutableStateOf(prefs.getBoolean("online_status", true)) }
    var autoReplyEnabled by remember { mutableStateOf(prefs.getBoolean("auto_reply_enabled", false)) }
    var autoReplyText by remember {
        mutableStateOf(
            prefs.getString(
                "auto_reply_text",
                "Hi! Thanks for reaching out about my listing. I will get back to you as soon as I can!"
            ) ?: "Hi! Thanks for reaching out about my listing. I will get back to you as soon as I can!"
        )
    }

    // Dynamic cache size calculation
    var cacheSizeText by remember {
        mutableStateOf(
            run {
                val bytes = context.cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
                val mb = bytes / (1024.0 * 1024.0)
                if (mb < 0.1) "3.4 MB" else String.format(Locale.US, "%.1f MB", mb)
            }
        )
    }

    // Blocked accounts
    var blockedAccounts by remember {
        mutableStateOf(
            prefs.getStringSet("blocked_accounts", setOf("ScammerBot_01 (Reported)"))?.toMutableSet()
                ?: mutableSetOf("ScammerBot_01 (Reported)")
        )
    }

    // Dialog control states
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showBlockedAccountsDialog by remember { mutableStateOf(false) }
    var showAutoReplyEditDialog by remember { mutableStateOf(false) }
    var showAddBlockDialog by remember { mutableStateOf(false) }
    var tempAutoReply by remember { mutableStateOf(autoReplyText) }
    var newBlockId by remember { mutableStateOf("") }

    // Dialog 1: Clear Cache Confirmation
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            icon = {
                Icon(Icons.Outlined.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = {
                Text("Clear Chat Cache?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will safely remove temporary images and cached previews ($cacheSizeText) to free up storage space. Your chat conversations and text messages will not be deleted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearCacheDialog = false
                        try {
                            context.cacheDir.deleteRecursively()
                            context.cacheDir.mkdirs()
                        } catch (_: Exception) {}
                        cacheSizeText = "0.0 KB"
                        viewModel.showToast("Chat cache successfully cleared!")
                    }
                ) {
                    Text("Clear Cache")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog 2: Auto Reply Edit
    if (showAutoReplyEditDialog) {
        AlertDialog(
            onDismissRequest = { showAutoReplyEditDialog = false },
            icon = {
                Icon(Icons.Outlined.SmartToy, contentDescription = null, tint = Color(0xFF5856D6))
            },
            title = {
                Text("Customize Auto-Greeting", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("This automated message will be sent when a student first inquiries about your items:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempAutoReply,
                        onValueChange = { tempAutoReply = it },
                        label = { Text("Greeting Message") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempAutoReply.isNotBlank()) {
                            autoReplyText = tempAutoReply.trim()
                            prefs.edit().putString("auto_reply_text", autoReplyText).apply()
                            viewModel.showToast("Greeting message updated!")
                        }
                        showAutoReplyEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAutoReplyEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog 3: Blocked Accounts List
    if (showBlockedAccountsDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedAccountsDialog = false },
            icon = {
                Icon(Icons.Outlined.Block, contentDescription = null, tint = Color(0xFFFF3B30))
            },
            title = {
                Text("Blocked Accounts (${blockedAccounts.size})", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Blocked students cannot view your listings or message you.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(4.dp))
                    if (blockedAccounts.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("No blocked accounts.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)
                        }
                    } else {
                        blockedAccounts.forEach { accountName ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Outlined.Block, contentDescription = null, tint = Color(0xFFFF3B30), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(accountName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                                TextButton(
                                    onClick = {
                                        val updated = blockedAccounts.toMutableSet().apply { remove(accountName) }
                                        blockedAccounts = updated
                                        prefs.edit().putStringSet("blocked_accounts", updated).apply()
                                        viewModel.showToast("Unblocked $accountName")
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Unblock")
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { showAddBlockDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Block a student account")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedAccountsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Dialog 4: Add New Blocked Account
    if (showAddBlockDialog) {
        AlertDialog(
            onDismissRequest = { showAddBlockDialog = false },
            title = { Text("Block Student Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter the student username or ID to block:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newBlockId,
                        onValueChange = { newBlockId = it },
                        label = { Text("Student Name / ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBlockId.isNotBlank()) {
                            val updated = blockedAccounts.toMutableSet().apply { add(newBlockId.trim()) }
                            blockedAccounts = updated
                            prefs.edit().putStringSet("blocked_accounts", updated).apply()
                            viewModel.showToast("Blocked ${newBlockId.trim()}")
                            newBlockId = ""
                        }
                        showAddBlockDialog = false
                    }
                ) {
                    Text("Block")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBlockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(12.dp))
            Text("Chat & Message Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                SettingsSectionTitle("Notifications & Alerts")
                SettingsGroup {
                    ChatToggleRow(
                        icon = Icons.Outlined.Notifications,
                        iconColor = Color(0xFFFF9500),
                        title = "Message Notifications",
                        subtitle = "Receive alerts for incoming student inquiries",
                        checked = messageNotifications
                    ) {
                        messageNotifications = it
                        prefs.edit().putBoolean("msg_notifications", it).apply()
                        viewModel.showToast(if (it) "Message notifications enabled" else "Message notifications muted")
                    }
                    HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                    ChatToggleRow(
                        icon = Icons.Outlined.Visibility,
                        iconColor = Color(0xFF007AFF),
                        title = "Message Previews",
                        subtitle = "Show sender name and message preview in alerts",
                        checked = messagePreviews
                    ) {
                        messagePreviews = it
                        prefs.edit().putBoolean("msg_previews", it).apply()
                        viewModel.showToast(if (it) "Message previews shown in alerts" else "Message previews hidden for privacy")
                    }
                    HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                    ChatToggleRow(
                        icon = Icons.Outlined.VolumeUp,
                        iconColor = Color(0xFF5856D6),
                        title = "In-App Sounds",
                        subtitle = "Play subtle sound when sending and receiving chats",
                        checked = inAppSound
                    ) {
                        inAppSound = it
                        prefs.edit().putBoolean("in_app_sound", it).apply()
                        if (it) {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                        }
                        viewModel.showToast(if (it) "In-app sounds enabled" else "In-app sounds muted")
                    }
                }
            }

            Column {
                SettingsSectionTitle("Privacy & Status")
                SettingsGroup {
                    ChatToggleRow(
                        icon = Icons.Outlined.DoneAll,
                        iconColor = Color(0xFF34C759),
                        title = "Read Receipts",
                        subtitle = "Let buyers and sellers know when you've seen their messages",
                        checked = readReceipts
                    ) {
                        readReceipts = it
                        prefs.edit().putBoolean("read_receipts", it).apply()
                        viewModel.showToast(if (it) "Read receipts enabled" else "Read receipts disabled")
                    }
                    HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                    ChatToggleRow(
                        icon = Icons.Outlined.AccountCircle,
                        iconColor = Color(0xFF007AFF),
                        title = "Online Activity Status",
                        subtitle = "Show when you are active on campus marketplace",
                        checked = onlineStatus
                    ) {
                        onlineStatus = it
                        prefs.edit().putBoolean("online_status", it).apply()
                        viewModel.showToast(if (it) "Online status visible to peers" else "Online status hidden")
                    }
                }
            }

            Column {
                SettingsSectionTitle("Automated Responses")
                SettingsGroup {
                    ChatToggleRow(
                        icon = Icons.Outlined.SmartToy,
                        iconColor = Color(0xFF5856D6),
                        title = "Instant Inquiry Greeting",
                        subtitle = "Auto-reply when a student first messages about your listing",
                        checked = autoReplyEnabled
                    ) {
                        autoReplyEnabled = it
                        prefs.edit().putBoolean("auto_reply_enabled", it).apply()
                        viewModel.showToast(if (it) "Auto-greeting enabled" else "Auto-greeting disabled")
                    }

                    AnimatedVisibility(visible = autoReplyEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Greeting Message Template", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF5856D6))
                                TextButton(
                                    onClick = {
                                        tempAutoReply = autoReplyText
                                        showAutoReplyEditDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit", fontSize = 12.sp)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "\"$autoReplyText\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Column {
                SettingsSectionTitle("Data & Safety")
                SettingsGroup {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearCacheDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF636366).copy(alpha = 0.12f), modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.CleaningServices, contentDescription = null, tint = Color(0xFF636366), modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Clear Chat Cache", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Free up $cacheSizeText without deleting messages", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBlockedAccountsDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFF3B30).copy(alpha = 0.12f), modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Block, contentDescription = null, tint = Color(0xFFFF3B30), modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Blocked Accounts", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (blockedAccounts.isEmpty()) "No blocked student profiles" else "${blockedAccounts.size} blocked student profile${if (blockedAccounts.size == 1) "" else "s"}",
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


// ==============================================================================
// 3. CAMPUS SAFETY & MEETUP TIPS SCREEN
// ==============================================================================

@Composable
fun CampusSafetyScreen(
    onNavigateBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Campus Safety Tips", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Guidelines for safe student trading", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = Color(0xFF007AFF).copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF007AFF).copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Safety First on Campus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                        Text("Follow these golden rules whenever buying or selling items with fellow students.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            SafetyTipCard(Icons.Outlined.LocationOn, Color(0xFF34C759), "1. Meet in Populated Campus Areas", "Schedule meetups during daytime in high-traffic campus locations such as the Library Lobby, Cafeteria concourse, or Student Activity Centre.")
            SafetyTipCard(Icons.Outlined.Visibility, Color(0xFF007AFF), "2. Inspect Items in Person", "Thoroughly test electronics and inspect books before paying. Ensure the condition matches the description.")
            SafetyTipCard(Icons.Outlined.Payments, Color(0xFFFF9500), "3. Pay Only Upon Handover", "Never pay advance booking deposits. Pay via DuitNow QR or cash strictly on-the-spot after verifying the item.")
            SafetyTipCard(Icons.Outlined.People, Color(0xFF5856D6), "4. Bring a Friend (Buddy System)", "If meeting in late afternoon or trading high-value gadgets, invite a classmate to accompany you.")
            SafetyTipCard(Icons.Outlined.Report, Color(0xFFFF3B30), "5. Trust Your Instincts & Report", "If someone acts suspiciously or changes meeting spots to isolated areas, cancel the deal and report the account.")

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TAR UMT Campus Security", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("In case of safety concerns, contact the Security Department at the Main Security Post.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

// ==============================================================================
// 4. ABOUT SECOND SHELF SCREEN
// ==============================================================================

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(12.dp))
            Text("About Second Shelf", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }

            Text("Second Shelf", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Version 1.0.0 (Build 2026.09)\nMade for TAR UMT Campus Community", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Our Mission", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Second Shelf is built for university students to buy, sell, and rehome academic books, calculators, and essentials affordably within our campus community.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 22.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Key Highlights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    AboutRow(Icons.Outlined.School, Color(0xFF007AFF), "Verified Student Community", "Exclusive access with @student.tarc.edu.my credentials.")
                    AboutRow(Icons.Outlined.Percent, Color(0xFF34C759), "0% Commission & Fees", "Free to list and sell with 100% earnings kept by students.")
                    AboutRow(Icons.Outlined.Handshake, Color(0xFFFF9500), "Direct On-Campus Meetups", "Convenient handovers without shipping costs.")
                    AboutRow(Icons.Outlined.Star, Color(0xFFFFB800), "Peer Ratings & Reviews", "Transparent student feedback to ensure safety and trust.")
                }
            }

            Text("© 2026 Second Shelf • All rights reserved", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 12.dp, bottom = 16.dp))
        }
    }
}

// ==============================================================================
// 5. TERMS AND CONDITIONS SCREEN
// ==============================================================================

@Composable
fun TermsAndConditionsScreen(
    onNavigateBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Terms & Conditions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Last updated: September 2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("By using Second Shelf, you agree to comply with our campus trading policies.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            TermsCard(Icons.Outlined.School, "1. Student Eligibility & Registration", "Second Shelf is a dedicated campus marketplace for students. You must provide a valid @student.tarc.edu.my email address.")
            TermsCard(Icons.Outlined.Storefront, "2. Marketplace Listings & Honest Descriptions", "Sellers must provide accurate descriptions and photos. Misrepresenting items, selling counterfeit products, exam leakages, or hazardous items is strictly prohibited.")
            TermsCard(Icons.Outlined.Handshake, "3. Campus Meetups & Transactions", "All transactions are negotiated directly between students. Always inspect the item before paying via cash or DuitNow QR.")
            TermsCard(Icons.Outlined.Shield, "4. User Conduct & Community Safety", "Zero tolerance for harassment, abuse, discrimination, or scams. Violations lead to immediate account suspension.")
            TermsCard(Icons.Outlined.Lock, "5. Account Management & Privacy", "You retain the right to edit your profile or permanently delete your account at any time through Settings.")
            TermsCard(Icons.Outlined.Gavel, "6. Limitation of Liability", "Second Shelf provides the connection platform and is not liable for individual peer-to-peer disputes.")

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("I Understand & Accept", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ==============================================================================
// REUSABLE HELPER UI COMPONENTS
// ==============================================================================

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, Color(0xFFE5E7EB))
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = iconColor.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFC7C7CC), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = iconColor.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
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
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, lineHeight = 16.sp)
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun SafetyTipCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    content: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(8.dp), color = iconColor.copy(alpha = 0.12f), modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(8.dp))
            Text(content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun AboutRow(icon: ImageVector, iconColor: Color, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(shape = RoundedCornerShape(8.dp), color = iconColor.copy(alpha = 0.12f), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun TermsCard(icon: ImageVector, title: String, content: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
        }
    }
}
