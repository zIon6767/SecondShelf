package com.example.secondshelf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.model.Category
import com.example.secondshelf.ui.model.Condition
import com.example.secondshelf.ui.model.Listing
import com.example.secondshelf.ui.model.formatPrice
import com.example.secondshelf.ui.model.getSellerName

import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun ListingCard(
    listing: Listing,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFavorite: Boolean = true
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(listing.cardColor),
                contentAlignment = Alignment.Center
            ) {
                if (!listing.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = listing.imageUrl,
                        contentDescription = listing.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = categoryIcon(listing.category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                }
                ConditionBadge(
                    condition = listing.condition,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                )
                if (showFavorite) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from saved" else "Save listing",
                            tint = if (isFavorite) Color(0xFFFF3B30) else Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .height(88.dp),          // fixed height → all cards identical
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    listing.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Column {
                    Text(
                        formatPrice(listing.price),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            getSellerName(listing.sellerId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun categoryIcon(category: Category): ImageVector = when (category) {
    Category.BOOKS -> Icons.Default.MenuBook
    Category.ELECTRONICS -> Icons.Default.Devices
    Category.STATIONERY -> Icons.Default.Edit
    Category.CLOTHING -> Icons.Default.Checkroom
    Category.LAB_EQUIPMENT -> Icons.Default.Science
    Category.FURNITURE -> Icons.Default.Weekend
    Category.OTHER, Category.ALL -> Icons.Default.ShoppingBag
}

@Composable
fun ListingGrid(
    listings: List<Listing>,
    isFavorite: (String) -> Boolean,
    onFavoriteClick: (String) -> Unit,
    onListingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listings.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { item ->
                    ListingCard(
                        listing = item,
                        isFavorite = isFavorite(item.id),
                        onFavoriteClick = { onFavoriteClick(item.id) },
                        onClick = { onListingClick(item.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            tint = Color(0xFFD1D5DB),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD1D5DB))
        action?.let {
            Spacer(modifier = Modifier.height(16.dp))
            it()
        }
    }
}
