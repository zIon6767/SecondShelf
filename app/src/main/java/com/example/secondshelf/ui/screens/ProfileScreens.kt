package com.example.secondshelf.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.components.ListingGrid
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

// ─────────────────────────────────────────────────────────────────────────────
// SavedItemsScreen
// Shows all listings the user has bookmarked/saved.
// Data comes from MarketplaceViewModel — no dummy data used.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SavedItemsScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onListingClick: (String) -> Unit
) {
    // Get the list of saved listings from the ViewModel
    val saved = viewModel.getSavedListings()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopBar(
            title = "Saved Items",
            subtitle = if (saved.isEmpty()) "0 items"
                       else "${saved.size} item${if (saved.size > 1) "s" else ""}",
            onBack = onNavigateBack
        )

        if (saved.isEmpty()) {
            // Empty state — show a friendly message
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No saved items",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else {
            // Show the saved listings in a grid
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ListingGrid(
                    listings = saved,
                    isFavorite = viewModel::isFavorite,
                    onFavoriteClick = viewModel::toggleFavorite,
                    onListingClick = onListingClick
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EditProfileScreen
// Allows the user to update their display name, campus, bio, and profile photo.
// Changes are saved to Supabase via AuthViewModel — no dummy data used.
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: MarketplaceViewModel,
    authViewModel: com.example.secondshelf.ui.AuthViewModel? = null,    // null only in Preview mode
    onNavigateBack: () -> Unit
) {
    // ── Get current user info from the ViewModel ─────────────────────────────
    val user               = viewModel.currentUser
    val daysUntilNameChange = viewModel.getDaysUntilNextNameChange()
    val canChangeName      = daysUntilNameChange == 0   // 14-day cooldown rule

    // ── State: editable fields ───────────────────────────────────────────────
    var name           by remember { mutableStateOf(user.name) }
    var selectedCampus by remember { mutableStateOf(user.campus.ifBlank { "KL Main Campus" }) }
    var bio            by remember { mutableStateOf(user.bio.ifBlank { "TAR UMT Student" }) }
    var error          by remember { mutableStateOf<String?>(null) }
    var isSaving       by remember { mutableStateOf(false) }

    // ── Avatar / photo upload state ──────────────────────────────────────────
    val avatarUrl   = authViewModel?.cachedAvatarUrl?.ifBlank { user.avatarUrl } ?: user.avatarUrl
    val isUploading = authViewModel?.isUploadingAvatar ?: false
    val uploadError = authViewModel?.avatarUploadError

    // ── Campus options ───────────────────────────────────────────────────────
    val campuses = remember {
        listOf(
            "KL Main Campus",
            "Penang Branch Campus",
            "Perak Branch Campus",
            "Johor Branch Campus",
            "Pahang Branch Campus",
            "Sabah Branch Campus"
        )
    }

    // ── Image picker — opens gallery when user taps profile photo ────────────
    val imagePicker = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { authViewModel?.uploadAvatar(it) }
    }

    // ── Screen Layout ────────────────────────────────────────────────────────
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopBar(title = "Edit Profile", onBack = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Profile Photo with camera badge ──────────────────────────────
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                // Circular avatar (loads from Supabase URL, or shows first letter)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(96.dp)
                ) {
                    if (avatarUrl.isNotBlank()) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile photo",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Loading spinner overlay while uploading
                if (isUploading) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape,
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp
                            )
                        }
                    }
                }

                // Camera icon badge (bottom-right corner of avatar)
                if (!isUploading) {
                    Box(
                        modifier = Modifier.size(104.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Change photo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = if (isUploading) "Uploading photo…" else "Change Profile Photo",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            // Upload error message (if photo upload fails)
            uploadError?.let {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Student Email (read-only, cannot be changed) ──────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Student Email (Account Identity)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                // Email pulled from Supabase session — not hardcoded
                                Text(
                                    text = user.email.ifBlank {
                                        authViewModel?.getUserEmail()?.ifBlank { "Not specified" } ?: "Not specified"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Permanent account login ID",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        // Verified badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF34C759).copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Verified",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Your student email is verified and cannot be modified after registration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp
                )
            }

            // ── Display Name field (with 14-day cooldown) ─────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Display Name / Username *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { input ->
                        name = input.filter { it.isLetter() || it.isWhitespace() }
                        error = null
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    },
                    enabled = canChangeName,    // locked for 14 days after last change
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                if (!canChangeName) {
                    Text(
                        text = "Name changed recently. You can change it again in $daysUntilNameChange day${if (daysUntilNameChange > 1) "s" else ""}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE65100),
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "Note: You can only change your display name once every 14 days.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 11.sp
                    )
                }
            }

            // ── University (read-only, always TAR UMT) ────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("University", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TAR UMT",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tunku Abdul Rahman University of Management and Technology",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // ── Campus Dropdown ───────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Campus Branch *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))

                var campusMenuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = campusMenuExpanded,
                    onExpandedChange = { campusMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCampus,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = campusMenuExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = campusMenuExpanded,
                        onDismissRequest = { campusMenuExpanded = false }
                    ) {
                        campuses.forEach { campus ->
                            DropdownMenuItem(
                                text = { Text(campus) },
                                onClick = {
                                    selectedCampus = campus
                                    campusMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ── Bio / About Me ────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bio / About You", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text("${bio.length}/120", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 120) bio = it },
                    placeholder = { Text("e.g. Year 2 CS student | Buying & selling textbooks") },
                    leadingIcon = {
                        Icon(Icons.Default.Info, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Error message banner
            error?.let {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Save button — saves changes to Supabase via AuthViewModel
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Display name cannot be blank."
                        return@Button
                    }
                    isSaving = true
                    val currentAvatar = authViewModel?.cachedAvatarUrl?.ifBlank { user.avatarUrl } ?: user.avatarUrl
                    val result = viewModel.updateProfile(
                        name      = name,
                        campus    = selectedCampus,
                        bio       = bio,
                        avatarUrl = currentAvatar
                    )
                    if (result == null) {
                        if (authViewModel != null) {
                            authViewModel.updateUserProfile(
                                fullName = name.trim(),
                                campus   = selectedCampus
                            ) {
                                isSaving = false
                                onNavigateBack()
                            }
                        } else {
                            isSaving = false
                            onNavigateBack()
                        }
                    } else {
                        isSaving = false
                        error = result
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Saving Changes…")
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TopBar
// A reusable top navigation bar with a back button, title, and optional subtitle.
// Used by EditProfileScreen, SavedItemsScreen, and other sub-screens.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 16.dp)
    ) {
        // Back button
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Page title and optional subtitle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f),
        thickness = 1.dp
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews — show screens in Android Studio Design panel
// ─────────────────────────────────────────────────────────────────────────────
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    com.example.secondshelf.ui.theme.SecondShelfTheme {
        EditProfileScreen(
            viewModel = MarketplaceViewModel(),
            onNavigateBack = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SavedItemsScreenPreview() {
    com.example.secondshelf.ui.theme.SecondShelfTheme {
        SavedItemsScreen(
            viewModel = MarketplaceViewModel(),
            onNavigateBack = {},
            onListingClick = {}
        )
    }
}
