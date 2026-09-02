package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@Composable
fun SettingsScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToPrivacySecurity: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val user = viewModel.currentUser
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

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

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to permanently delete your account?\n\nThis action cannot be undone. All your listings, messages, and profile data will be permanently deleted."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── Section 1: Account (Student Item relocated directly under Account label) ──
            SettingsSectionTitle("Account")
            
            // Clickable Student Profile Card that navigates directly to Edit Profile
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToEditProfile)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(user.avatarInitial, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
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
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Account Sub-actions (Privacy & Security)
            SettingsGroup {
                SettingsNavItem(
                    icon = Icons.Default.Lock,
                    label = "Privacy & Security",
                    iconColor = Color(0xFF5856D6),
                    onClick = onNavigateToPrivacySecurity
                )
            }

            Spacer(Modifier.height(20.dp))
            SettingsSectionTitle("Preferences")
            SettingsGroup {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    label = "Notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    iconColor = Color(0xFFFF3B30)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(start = 56.dp))
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    label = "Dark Mode",
                    checked = viewModel.isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) },
                    iconColor = Color(0xFF636366)
                )
            }

            Spacer(Modifier.height(20.dp))
            SettingsSectionTitle("Support")
            SettingsGroup {
                SettingsNavItem(
                    icon = Icons.Default.HelpOutline,
                    label = "Help & Support",
                    iconColor = Color(0xFF34C759),
                    onClick = onNavigateToHelpSupport
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(start = 56.dp))
                SettingsNavItem(
                    icon = Icons.Default.Info,
                    label = "About Second Shelf",
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToHelpSupport
                )
            }

            Spacer(Modifier.height(20.dp))
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(start = 56.dp))

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

            Spacer(Modifier.height(12.dp))
            Text(
                "Second Shelf v1.0.0 (TAR UMT Edition)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, letterSpacing = 1.2.sp, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(content = content)
    }
}

@Composable
private fun SettingsNavItem(icon: ImageVector, label: String, iconColor: Color, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(10.dp), color = iconColor.copy(alpha = 0.12f), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun SettingsToggleItem(icon: ImageVector, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, iconColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(10.dp), color = iconColor.copy(alpha = 0.12f), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary))
    }
}
