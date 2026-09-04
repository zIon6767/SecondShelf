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
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
    userId: String = "",
    userName: String = "User",
    userEmail: String = "",
    userCampus: String = "",
    userUniversity: String = "",
    userAvatarUrl: String = "",
    targetTab: Int? = null,
    onTabConsumed: (() -> Unit)? = null,
    onNavigateToNotifications: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToListing: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateListing: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToConversation: (participantId: String, listingId: String) -> Unit = { _, _ -> },
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToScanner: () -> Unit //qr code testing remove later
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(targetTab) {
        if (targetTab != null) {
            selectedTab = targetTab
            onTabConsumed?.invoke()
        }
    }

    LaunchedEffect(userId, userName, userEmail, userCampus, userUniversity, userAvatarUrl) {
        viewModel.syncAuthUser(
            id = userId,
            name = userName,
            email = userEmail,
            campus = userCampus,
            university = userUniversity,
            avatarUrl = userAvatarUrl
        )
    }

    LaunchedEffect(viewModel.toastMessage) {
        viewModel.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissToast()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val message = data.visuals.message
                val icon = when {
                    "saved" in message.lowercase() -> Icons.Default.Favorite
                    "removed" in message.lowercase() -> Icons.Default.DeleteOutline
                    "message" in message.lowercase() -> Icons.Default.Send
                    else -> Icons.Default.CheckCircle
                }

                Surface(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = message,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        data.visuals.actionLabel?.let { action ->
                            TextButton(onClick = { data.performAction() }) {
                                Text(action, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
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
                    onNavigateToCreateListing = onNavigateToCreateListing,
                    onNavigateToMarketplace = onNavigateToMarketplace,
                    onNavigateToProfile = { selectedTab = 3 },
                    onNavigateToScanner = onNavigateToScanner //qr code testing remove later
                )
                1 -> SavedTab(viewModel, onNavigateToListing, onNavigateToCreateListing)
                2 -> MessagesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { selectedTab = 0 },
                    onConversationClick = { pId, lId -> onNavigateToConversation(pId, lId) }
                )
                3 -> ProfileTab(
                    viewModel,
                    onNavigateToSettings,
                    onNavigateToCreateListing,
                    onNavigateToListing,
                    onNavigateToSaved,
                    onNavigateToMessages = { selectedTab = 2 },
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
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onNavigateToProfile)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
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
    onNavigateToCreateListing: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToProfile: () -> Unit,
    //qr code testing remove later
    onNavigateToScanner: () -> Unit
    //---------------------------------------
) {
    val user = viewModel.currentUser
    val displayName = user.name.ifBlank { if (userName.isNotBlank() && userName != "User") userName else "Student" }
    val category = viewModel.homeCategory
    val filtered = viewModel.filterListings(
        ListingFilters(category = category)
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
            onNavigateToNotifications = onNavigateToNotifications,
            onNavigateToProfile = onNavigateToProfile
        )

        // ── Scrollable Body Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(14.dp))

            // ── Marketplace Search Entry ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onNavigateToMarketplace),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Search textbooks, calculators, tech...",
                        color = Color(0xFF8E8E93),
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Search and filter marketplace",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // ── QR Handoff Scanner Action ──
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = { onNavigateToScanner() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Scan Seller's QR Code", fontWeight = FontWeight.SemiBold)
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
                    text = if (category == Category.ALL) "Recent Listings" else "${category.label} Listings",
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
                    title = "No listings found",
                    subtitle = "Try selecting another category"
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
        // ── Premium header ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Saved Items",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "${saved.size} item${if (saved.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f), thickness = 1.dp)

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
        // ── Premium header ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Messages",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (conversations.isNotEmpty()) {
                    Text(
                        "${conversations.size} chat${if (conversations.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f), thickness = 1.dp)

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
    var isReviewsExpanded by remember { mutableStateOf(true) }
    var isListingsExpanded by remember { mutableStateOf(true) }
    var isFaqSectionExpanded by remember { mutableStateOf(false) }
    var expandedFaqIndex by remember { mutableIntStateOf(-1) }

    val profileFaqList = remember {
        listOf(
            "Who can register and trade on Second Shelf?" to "Second Shelf is exclusively available to verified students and staff of TAR UMT. You must use an official @student.tarc.edu.my email address to register.",
            "Are there any fees or commissions for listing and selling?" to "No. Second Shelf is 100% free for all TAR UMT students. We take 0% commission and there are no listing or transaction fees.",
            "How does payment and item handover work?" to "Transactions are completed in person on campus. Buyers inspect the item and pay the seller directly using DuitNow QR, instant bank transfer, or cash upon meetup.",
            "Where are the best spots for meetups on campus?" to "We recommend high-traffic, well-lit campus areas during daytime hours, such as the Library Lobby, Cafeteria concourse, or Student Activity Centre.",
            "How do student ratings and reviews work?" to "After completing a deal, either party can tap '★ Rate Deal' in chat to leave a 1 to 5 star rating, quick feedback tags, and written comments that display on the profile.",
            "Can I edit or delete my listings after posting?" to "Yes! Go to Settings -> My Listings (or scroll to My Listings on your profile), tap your listing, and select Edit Listing or Delete.",
            "What items are prohibited on Second Shelf?" to "Prohibited items include illegal substances, academic dishonesty materials (exam leakages, paid assignment services), weapons, and pirated copyrighted materials.",
            "What should I do if a buyer or seller does not show up?" to "Message them in chat to confirm or reschedule. If the user continually ghosts or engages in dishonest conduct, you can report their profile.",
            "How do I update my name, email, or campus branch?" to "Tap the Settings gear icon at the top right of your Profile page and select 'Edit Profile' to modify your personal details and campus info.",
            "How do I report a suspicious listing or user?" to "On any listing or chat screen, tap the More/Report button to submit a report directly to campus moderators. For urgent safety matters, contact TAR UMT Campus Security."
        )
    }

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
                    if (user.avatarUrl.isNotBlank()) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(user.avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile photo",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(user.avatarInitial, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
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
                if (user.bio.isNotBlank()) {
                    Text(
                        user.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // User Rating Badge
                val userReviews = viewModel.getReviewsForUser(user.id)
                val avgRating = viewModel.getAverageRatingForUser(user.id)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (userReviews.isEmpty()) Color(0xFFF4F7FB) else Color(0xFFFFF8E1),
                    border = BorderStroke(1.dp, if (userReviews.isEmpty()) Color(0xFFE5E7EB) else Color(0xFFFFD54F)),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        if (userReviews.isEmpty()) {
                            Text(
                                text = "New Seller",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", avgRating),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFB78103)
                            )
                            Text(
                                text = " (${userReviews.size} review${if (userReviews.size == 1) "" else "s"})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

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

        // Student Reviews Section (Expandable & Manually Creatable)
        val allUserReviews = viewModel.getReviewsForUser(user.id)
        val reviewsRotation by animateFloatAsState(targetValue = if (isReviewsExpanded) 180f else 0f, label = "reviewsChevron")
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isReviewsExpanded = !isReviewsExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Reviews (${allUserReviews.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (allUserReviews.isEmpty()) "No reviews yet" else if (isReviewsExpanded) "Tap to collapse" else "Tap to view feedback",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (allUserReviews.isNotEmpty()) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", viewModel.getAverageRatingForUser(user.id)),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isReviewsExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(reviewsRotation)
                        )
                    }
                }

                AnimatedVisibility(visible = isReviewsExpanded) {
                    Column(
                        modifier = Modifier.padding(top = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (allUserReviews.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.RateReview,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "No reviews yet",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "Reviews given by others will appear here when you complete deals on Second Shelf.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        } else {
                            allUserReviews.forEach { review ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.background,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(review.authorAvatarInitial, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                                Spacer(Modifier.width(8.dp))
                                                Column {
                                                    Text(review.authorName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                    Text(review.timeAgo, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                repeat(review.rating) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                        if (review.listingTitle.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "Deal: ${review.listingTitle}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        if (review.comment.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(review.comment, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (review.tags.isNotEmpty()) {
                                            Spacer(Modifier.height(6.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                review.tags.forEach { tag ->
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                    ) {
                                                        Text(tag, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // My Listings Section (Expandable)
        val listingsRotation by animateFloatAsState(targetValue = if (isListingsExpanded) 180f else 0f, label = "listingsChevron")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isListingsExpanded = !isListingsExpanded }
                    .padding(vertical = 4.dp)
            ) {
                Text("My Listings (${myListings.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isListingsExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(listingsRotation)
                )
            }
            Button(onClick = onNavigateToCreateListing, shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("List Item")
            }
        }

        Spacer(Modifier.height(12.dp))

        AnimatedVisibility(visible = isListingsExpanded) {
            Column {
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
            }
        }

        Spacer(Modifier.height(24.dp))

        // Frequently Asked Questions (FAQ) Section
        val faqSectionRotation by animateFloatAsState(
            targetValue = if (isFaqSectionExpanded) 180f else 0f,
            label = "faqSectionChevron"
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isFaqSectionExpanded = !isFaqSectionExpanded }
                        .padding(bottom = if (isFaqSectionExpanded) 14.dp else 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Frequently Asked Questions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = { isFaqSectionExpanded = !isFaqSectionExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isFaqSectionExpanded) "Collapse FAQ" else "Expand FAQ",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(faqSectionRotation)
                        )
                    }
                }

                AnimatedVisibility(visible = isFaqSectionExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        profileFaqList.forEachIndexed { index, faq ->
                            val isFaqExpanded = expandedFaqIndex == index
                            val faqRotation by animateFloatAsState(targetValue = if (isFaqExpanded) 180f else 0f, label = "faqChevron$index")

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isFaqExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f) else MaterialTheme.colorScheme.background,
                                border = BorderStroke(0.5.dp, if (isFaqExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color(0xFFE5E7EB)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        expandedFaqIndex = if (isFaqExpanded) -1 else index
                                    }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = faq.first,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isFaqExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = if (isFaqExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .rotate(faqRotation)
                                        )
                                    }

                                    AnimatedVisibility(visible = isFaqExpanded) {
                                        Column(modifier = Modifier.padding(top = 10.dp)) {
                                            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = faq.second,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    com.example.secondshelf.ui.theme.SecondShelfTheme {
        HomeScreen(
            viewModel = MarketplaceViewModel(),
            userId = "user-1",
            userName = "Sarah Lee",
            userEmail = "sarahlee@student.tarc.edu.my",
            userCampus = "KL Main Campus",
            userUniversity = "TAR UMT",
            userAvatarUrl = "",
            onNavigateToNotifications = {},
            onNavigateToMarketplace = {},
            onNavigateToListing = {},
            onNavigateToSettings = {},
            onNavigateToCreateListing = {},
            onNavigateToSaved = {},
            onNavigateToMessages = {},
            onNavigateToEditProfile = {},
            onLogout = {},
            //qr code testinfg remove llater
            onNavigateToScanner = {}
        )
    }
}

