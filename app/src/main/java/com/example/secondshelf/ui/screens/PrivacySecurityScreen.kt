package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@Composable
fun PrivacySecurityScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(true) }
    var campusOnlyVisibility by remember { mutableStateOf(true) }
    var showActiveStatus by remember { mutableStateOf(true) }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showDataDownloadDialog by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordSuccess by remember { mutableStateOf(false) }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (passwordSuccess) {
                        Text("Password updated successfully!", color = Color(0xFF34C759), fontWeight = FontWeight.Medium)
                    } else {
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it; passwordError = null },
                            label = { Text("Current Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it; passwordError = null },
                            label = { Text("New Password (min 6 chars)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; passwordError = null },
                            label = { Text("Confirm New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        passwordError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (passwordSuccess) {
                    TextButton(onClick = {
                        showPasswordDialog = false
                        passwordSuccess = false
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                    }) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (currentPassword.isBlank() || newPassword.isBlank()) {
                                passwordError = "Please fill in all fields"
                            } else if (newPassword.length < 6) {
                                passwordError = "Password must be at least 6 characters"
                            } else if (newPassword != confirmPassword) {
                                passwordError = "Passwords do not match"
                            } else {
                                passwordSuccess = true
                                passwordError = null
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Update Password")
                    }
                }
            },
            dismissButton = {
                if (!passwordSuccess) {
                    TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") }
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDataDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDataDownloadDialog = false },
            title = { Text("Download Your Data", fontWeight = FontWeight.Bold) },
            text = {
                Text("We will prepare a ZIP archive containing your profile, listings, and trade history, and send a download link to your registered campus email.")
            },
            confirmButton = {
                Button(
                    onClick = { showDataDownloadDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Request Archive")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDataDownloadDialog = false }) { Text("Close") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to permanently delete your account?\n\nThis action cannot be undone. All your listings, messages, and profile data will be permanently removed."
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
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
            Text("Privacy & Security", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Section 1: Security & Authentication ──
            Column {
                SectionHeader("Security & Login")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SecurityActionItem(
                            icon = Icons.Outlined.Lock,
                            title = "Change Password",
                            subtitle = "Regularly update your login credentials",
                            iconColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                passwordSuccess = false
                                passwordError = null
                                showPasswordDialog = true
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(start = 56.dp))
                        SecurityToggleItem(
                            icon = Icons.Outlined.VpnKey,
                            title = "Two-Factor Authentication (2FA)",
                            subtitle = "Verify sign in with email OTP",
                            checked = twoFactorEnabled,
                            onCheckedChange = { twoFactorEnabled = it },
                            iconColor = Color(0xFF5856D6)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(start = 56.dp))
                        SecurityToggleItem(
                            icon = Icons.Outlined.Fingerprint,
                            title = "Biometric Login / Face ID",
                            subtitle = "Sign in quickly using your device sensor",
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it },
                            iconColor = Color(0xFF34C759)
                        )
                    }
                }
            }

            // ── Section 2: Data Privacy Controls ──
            Column {
                SectionHeader("Data & Privacy")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SecurityToggleItem(
                            icon = Icons.Outlined.School,
                            title = "Campus-Only Visibility",
                            subtitle = "Only verified students from your campus can view items",
                            checked = campusOnlyVisibility,
                            onCheckedChange = { campusOnlyVisibility = it },
                            iconColor = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(start = 56.dp))
                        SecurityToggleItem(
                            icon = Icons.Outlined.Visibility,
                            title = "Online Activity Status",
                            subtitle = "Show when you are active on the marketplace",
                            checked = showActiveStatus,
                            onCheckedChange = { showActiveStatus = it },
                            iconColor = Color(0xFFFF9500)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(start = 56.dp))
                        SecurityActionItem(
                            icon = Icons.Outlined.Download,
                            title = "Download My Data",
                            subtitle = "Export personal listings and chat archive",
                            iconColor = Color(0xFF636366),
                            onClick = { showDataDownloadDialog = true }
                        )
                    }
                }
            }

            // ── Section 3: Danger Zone ──
            Column {
                SectionHeader("Danger Zone")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SecurityActionItem(
                        icon = Icons.Outlined.DeleteForever,
                        title = "Delete Account",
                        subtitle = "Permanently remove your account and all data",
                        iconColor = MaterialTheme.colorScheme.error,
                        isDestructive = true,
                        onClick = { showDeleteAccountDialog = true }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SecurityActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    isDestructive: Boolean = false,
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
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFD1D5DB),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SecurityToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = iconColor.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
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
