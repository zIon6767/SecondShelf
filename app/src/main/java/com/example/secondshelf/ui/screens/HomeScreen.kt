package com.example.secondshelf.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.components.*
import com.example.secondshelf.ui.model.*
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: MarketplaceViewModel,
    userName: String = "User",
    userEmail: String = "",
    userCampus: String = "",
    userUniversity: String = "",
    onNavigateToNotifications: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToListing: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateListing: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userName, userEmail, userCampus, userUniversity) {
        viewModel.syncAuthUser(
            name = userName,
            email = userEmail,
            campus = userCampus,
            university = userUniversity
        )
    }

    LaunchedEffect(viewModel.toastMessage) {
        viewModel.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SecondShelfBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onAddListingClick = onNavigateToCreateListing
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeTab(
                    viewModel = viewModel,
                    userName = userName,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToListing = onNavigateToListing,
                    onNavigateToCreateListing = onNavigateToCreateListing
                )
                1 -> SavedTab(viewModel, onNavigateToListing, onNavigateToCreateListing)
                2 -> ChatTab(viewModel, onNavigateToMessages)
                3 -> ProfileTab(
                    viewModel,
                    onNavigateToSettings,
                    onNavigateToCreateListing,
                    onNavigateToListing,
                    onNavigateToSaved,
                    onNavigateToMessages,
                    onNavigateToEditProfile,
                    onLogout
                )
            }
        }
    }
}

@Composable
private fun SecondShelfBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddListingClick: () -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = Color(0xFF8E8E93)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp,
        border = BorderStroke(0.5.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── 1. Home ──
            SecondShelfNavItem(
                label = "Home",
                selected = selectedTab == 0,
                icon = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(0) }
            )

            // ── 2. Saved ──
            SecondShelfNavItem(
                label = "Saved",
                selected = selectedTab == 1,
                icon = if (selectedTab == 1) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(1) }
            )

            // ── 3. Standout "Sell / Add Listing" Center Button ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable(onClick = onAddListingClick)
                    .padding(horizontal = 6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Listing",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Sell",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── 4. Chat ──
            SecondShelfNavItem(
                label = "Chat",
                selected = selectedTab == 2,
                icon = if (selectedTab == 2) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(2) }
            )

            // ── 5. Profile ──
            SecondShelfNavItem(
                label = "Profile",
                selected = selectedTab == 3,
                icon = if (selectedTab == 3) Icons.Filled.Person else Icons.Outlined.Person,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(3) }
            )
        }
    }
}

@Composable
private fun SecondShelfNavItem(
    label: String,
    selected: Boolean,
    icon: ImageVector,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val color = if (selected) activeColor else inactiveColor

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
private fun CleanTopBar(
    displayName: String,
    unreadCount: Int,
    onNavigateToNotifications: () -> Unit
) {
    val shortName = displayName.split(" ").firstOrNull() ?: displayName

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = BorderStroke(0.5.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = shortName.firstOrNull()?.uppercaseChar()?.toString() ?: "S",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = "${greeting()}, $shortName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF34C759), CircleShape)
                        )
                        Text(
                            text = "TAR UMT Marketplace",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF4F7FB), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 1.dp, y = 1.dp)
                            .size(9.dp)
                            .background(Color(0xFFFF3B30), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTab(
    viewModel: MarketplaceViewModel,
    userName: String,
    onNavigateToNotifications: () -> Unit,
    onNavigateToListing: (String) -> Unit,
    onNavigateToCreateListing: () -> Unit
) {
    val user = viewModel.currentUser
    val displayName = user.name.ifBlank { if (userName.isNotBlank() && userName != "User") userName else "Student" }
    var searchQuery by remember { mutableStateOf("") }
    val category = viewModel.homeCategory
    val filtered = viewModel.filterListings(
        ListingFilters(
            category = category,
            query = searchQuery
        )
    )
    val unreadCount = viewModel.getUnreadNotificationCount()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Pinned Clean Formal Top Bar ──
        CleanTopBar(
            displayName = displayName,
            unreadCount = unreadCount,
            onNavigateToNotifications = onNavigateToNotifications
        )

        // ── Scrollable Body Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(14.dp))

            // ── Live In-Place Search Bar ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = if (searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary else Color(0xFF8E8E93),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search textbooks, calculators, tech...",
                                color = Color(0xFF8E8E93),
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = Color(0xFF8E8E93),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Category Selector ──
            Text(
                "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
            CategorySelector(
                selected = category,
                onSelect = viewModel::updateHomeCategory
            )

            Spacer(Modifier.height(18.dp))

            // ── Listings Section Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        searchQuery.isNotBlank() -> "Results for \"$searchQuery\""
                        category == Category.ALL -> "Recent Listings"
                        else -> "${category.label} Listings"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${filtered.size} item${if (filtered.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Listings Grid ──
            if (filtered.isEmpty()) {
                EmptyState(
                    title = if (searchQuery.isNotBlank()) "No items matching \"$searchQuery\"" else "No listings found",
                    subtitle = if (searchQuery.isNotBlank()) "Try checking the spelling or searching another keyword" else "Try selecting another category"
                )
            } else {
                ListingGrid(
                    listings = filtered,
                    isFavorite = viewModel::isFavorite,
                    onFavoriteClick = viewModel::toggleFavorite,
                    onListingClick = onNavigateToListing
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SavedTab(
    viewModel: MarketplaceViewModel,
    onNavigateToListing: (String) -> Unit,
    onNavigateToCreateListing: () -> Unit
) {
    val saved = viewModel.getSavedListings()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            border = BorderStroke(0.5.dp, Color(0xFFE5E7EB))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Saved Items",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${saved.size} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (saved.isEmpty()) {
            EmptyState("No saved items yet", "Tap the heart icon on any listing to save it")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 12.dp)
            ) {
                ListingGrid(
                    listings = saved,
                    isFavorite = viewModel::isFavorite,
                    onFavoriteClick = viewModel::toggleFavorite,
                    onListingClick = onNavigateToListing
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ChatTab(
    viewModel: MarketplaceViewModel,
    onNavigateToMessages: () -> Unit
) {
    val conversations = viewModel.conversations

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            border = BorderStroke(0.5.dp, Color(0xFFE5E7EB))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Messages",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${conversations.size} conversations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (conversations.isEmpty()) {
            EmptyState("No messages yet", "Contact sellers on any listing to start chatting")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(conversations) { conv ->
                    val otherUser = getUserById(conv.participantId)
                    val listing = viewModel.getListing(conv.listingId)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToMessages() }
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(0.5.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = otherUser?.avatarInitial ?: "U",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = otherUser?.name ?: "Student",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = conv.lastTimestamp,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                if (listing != null) {
                                    Text(
                                        text = "Item: ${listing.title}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = conv.lastMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (conv.unreadCount > 0) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(10.dp)
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTab(
    viewModel: MarketplaceViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateListing: () -> Unit,
    onNavigateToListing: (String) -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val user = viewModel.currentUser
    val myListings = viewModel.getMyListings()
    val savedCount = viewModel.getSavedListings().size
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out of Second Shelf?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(user.avatarInitial, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (user.email.isNotBlank()) {
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
                Text(
                    if (user.campus.isNotBlank()) "${user.university} • ${user.campus}" else user.university,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ProfileStat("${myListings.size}", "Listed Items", onClick = onNavigateToCreateListing)
                    VerticalDivider()
                    ProfileStat("$savedCount", "Saved", onClick = onNavigateToSaved)
                    VerticalDivider()
                    ProfileStat("${viewModel.conversations.size}", "Messages", onClick = onNavigateToMessages)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        ProfileMenuSection {
            ProfileMenuItem(Icons.Default.Inventory2, "My Listings", MaterialTheme.colorScheme.primary, onClick = onNavigateToCreateListing)
            ProfileMenuItem(Icons.Default.Favorite, "Saved Items", Color(0xFFFF3B30), onNavigateToSaved)
            ProfileMenuItem(Icons.Default.Chat, "Messages", Color(0xFF5856D6), onNavigateToMessages)
            ProfileMenuItem(Icons.Default.Edit, "Edit Profile", Color(0xFF34C759), onNavigateToEditProfile)
            ProfileMenuItem(Icons.Default.Settings, "Settings", Color(0xFF636366), onNavigateToSettings)
            ProfileMenuItem(Icons.Default.Logout, "Logout", MaterialTheme.colorScheme.error) { showLogoutDialog = true }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Listings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(onClick = onNavigateToCreateListing, shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("List Item")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (myListings.isEmpty()) {
            EmptyState("No listings yet", "Tap List Item to sell something") {
                Button(onClick = onNavigateToCreateListing) { Text("Sell Item") }
            }
        } else {
            ListingGrid(
                listings = myListings,
                isFavorite = viewModel::isFavorite,
                onFavoriteClick = viewModel::toggleFavorite,
                onListingClick = onNavigateToListing
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier.width(0.5.dp).height(40.dp).background(Color(0xFFE5E7EB)))
}

@Composable
private fun ProfileStat(value: String, label: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun ProfileMenuSection(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(content = content)
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, label: String, iconColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(10.dp), color = iconColor.copy(alpha = 0.12f), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(16.dp))
    }
}
