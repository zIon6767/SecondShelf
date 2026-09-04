package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.secondshelf.ui.components.*
import com.example.secondshelf.ui.model.*
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onListingClick: (String) -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }
    val filters = viewModel.marketplaceFilters
    val results = viewModel.filterListings(filters)

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(12.dp))
                Text("Marketplace", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            FilterChip(
                selected = showFilters,
                onClick = { showFilters = !showFilters },
                label = { Text("Filters") },
                leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = filters.query,
                onValueChange = { viewModel.patchMarketplaceFilters { copy(query = it) } },
                placeholder = { Text("Search marketplace…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (filters.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.patchMarketplaceFilters { copy(query = "") } }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            if (showFilters) {
                MarketplaceFiltersPanel(
                    filters = filters,
                    onCategoryChange = { viewModel.patchMarketplaceFilters { copy(category = it) } },
                    onConditionChange = { viewModel.patchMarketplaceFilters { copy(condition = it) } },
                    onSortChange = { viewModel.patchMarketplaceFilters { copy(sort = it) } },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (filters.category == Category.ALL) "All Listings" else "${filters.category.label} Listings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("${results.size} items", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }

            if (results.isEmpty()) {
                EmptyState("No listings found", "Try adjusting your filters or search query")
            } else {
                ListingGrid(
                    listings = results,
                    isFavorite = viewModel::isFavorite,
                    onFavoriteClick = viewModel::toggleFavorite,
                    onListingClick = onListingClick
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MarketplaceFiltersPanel(
    filters: ListingFilters,
    onCategoryChange: (Category) -> Unit,
    onConditionChange: (Condition?) -> Unit,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            CategorySelector(
                selected = filters.category,
                onSelect = onCategoryChange,
                modifier = Modifier.offset(x = (-20).dp)
            )

            Text("Condition", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filters.condition == null,
                    onClick = { onConditionChange(null) },
                    label = { Text("All") }
                )
                Condition.entries.forEach { cond ->
                    FilterChip(
                        selected = filters.condition == cond,
                        onClick = { onConditionChange(cond) },
                        label = { Text(cond.label) }
                    )
                }
            }

            Text("Sort by", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortOption.entries.forEach { sort ->
                    FilterChip(
                        selected = filters.sort == sort,
                        onClick = { onSortChange(sort) },
                        label = { Text(sort.label, maxLines = 1) }
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun MarketplaceScreenPreview() {
    com.example.secondshelf.ui.theme.SecondShelfTheme {
        MarketplaceScreen(
            viewModel = MarketplaceViewModel(android.app.Application()),
            onNavigateBack = {},
            onListingClick = {}
        )
    }
}

