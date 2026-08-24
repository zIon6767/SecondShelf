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
    onLogout: () -> Unit
) {
    val user = viewModel.currentUser
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
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

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(user.avatarInitial, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        Text(user.university, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            SettingsSectionTitle("Account")
            SettingsGroup {
                SettingsNavItem(Icons.Default.Person, "Edit Profile", MaterialTheme.colorScheme.primary, onNavigateToEditProfile)
                Divider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                SettingsNavItem(Icons.Default.Lock, "Privacy & Security", Color(0xFF5856D6)) {}
            }

            Spacer(Modifier.height(20.dp))
            SettingsSectionTitle("Preferences")
            SettingsGroup {
                SettingsToggleItem(Icons.Default.Notifications, "Notifications", notificationsEnabled, { notificationsEnabled = it }, Color(0xFFFF3B30))
                Divider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                SettingsToggleItem(Icons.Default.DarkMode, "Dark Mode", darkModeEnabled, { darkModeEnabled = it }, Color(0xFF636366))
            }

            Spacer(Modifier.height(20.dp))
            SettingsSectionTitle("Support")
            SettingsGroup {
                SettingsNavItem(Icons.Default.HelpOutline, "Help & Support", Color(0xFF34C759)) {}
                Divider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(start = 56.dp))
                SettingsNavItem(Icons.Default.Info, "About Second Shelf", MaterialTheme.colorScheme.primary) {}
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
            }

            Spacer(Modifier.height(8.dp))
            Text("Second Shelf v1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(32.dp))
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
