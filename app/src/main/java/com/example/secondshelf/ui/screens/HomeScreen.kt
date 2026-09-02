package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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

    LaunchedEffect(viewModel.toastMessage) {
        viewModel.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = navColors()
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeTab(viewModel, userName, onNavigateToNotifications, onNavigateToMarketplace, onNavigateToListing)
                1 -> SearchTab(viewModel, onNavigateToListing)
                2 -> ProfileTab(
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
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
)

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
private fun HomeTab(
    viewModel: MarketplaceViewModel,
    userName: String,
    onNavigateToNotifications: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToListing: (String) -> Unit
) {
    val category = viewModel.homeCategory
    val filtered = viewModel.filterListings(ListingFilters(category = category))
    val unreadCount = viewModel.getUnreadNotificationCount()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Box {
                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
                }
                if (unreadCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF3B30),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                if (unreadCount > 9) "9+" else "$unreadCount",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            HeroBanner(onBrowseClick = onNavigateToMarketplace)

            Text(
                "Categories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
            )
            CategorySelector(
                selected = category,
                onSelect = viewModel::updateHomeCategory
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (category == Category.ALL) "Recent Listings" else "${category.label} Listings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("${filtered.size} items", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }

            Spacer(Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                EmptyState("No listings found", "Try a different category")
            } else {
                ListingGrid(
                    listings = filtered,
                    isFavorite = viewModel::isFavorite,
                    onFavoriteClick = viewModel::toggleFavorite,
                    onListingClick = onNavigateToListing
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTab(viewModel: MarketplaceViewModel, onNavigateToListing: (String) -> Unit) {
    var query by remember { mutableStateOf(viewModel.searchQuery) }
    var selectedFilter by remember { mutableStateOf(Category.ALL) }

    val results = viewModel.filterListings(
        ListingFilters(
            category = selectedFilter,
            query = query,
            sort = if (query.isNotBlank()) SortOption.RELEVANCE else SortOption.NEWEST
        )
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Search", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search items, sellers…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = ""; viewModel.updateSearchQuery("") }) { Icon(Icons.Default.Close, contentDescription = "Clear") } }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            if (query.isEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Popular searches", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    searchSuggestions.take(4).forEach { suggestion ->
                        SuggestionChip(onClick = { query = suggestion; viewModel.updateSearchQuery(suggestion) }, label = { Text(suggestion) })
                    }
                }
            }
        }

        Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            CategorySelector(selected = selectedFilter, onSelect = { selectedFilter = it })

            Text(
                "${results.size} result${if (results.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (results.isEmpty()) {
                EmptyState("No items found", "Try a different keyword or filter")
            } else {
                ListingGrid(
                    listings = results,
                    isFavorite = viewModel::isFavorite,
                    onFavoriteClick = viewModel::toggleFavorite,
                    onListingClick = onNavigateToListing
                )
            }
            Spacer(Modifier.height(16.dp))
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
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
                Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                Text(user.university, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 2.dp))

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
