package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.components.ListingGrid
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@Composable
fun SavedItemsScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onListingClick: (String) -> Unit
) {
    val saved = viewModel.getSavedListings()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopBar(title = "Saved Items", onBack = onNavigateBack)

        if (saved.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No saved items", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }
        } else {
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

@Composable
fun EditProfileScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    val user = viewModel.currentUser
    val daysUntilNameChange = viewModel.getDaysUntilNextNameChange()
    val canChangeName = daysUntilNameChange == 0

    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var university by remember { mutableStateOf(user.university) }
    var campus by remember { mutableStateOf(user.campus) }
    var error by remember { mutableStateOf<String?>(null) }

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
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(name.firstOrNull()?.uppercaseChar()?.toString() ?: "?", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Name field with 14-day cooldown indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Name *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    enabled = canChangeName,
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
                        text = "Note: You can only change your name once every 14 days.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp
                    )
                }
            }

            ProfileField("Email *", email) { email = it; error = null }
            ProfileField("University *", university) { university = it; error = null }
            ProfileField("Campus *", campus) { campus = it; error = null }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    val result = viewModel.updateProfile(name, email, university, campus)
                    if (result == null) onNavigateBack()
                    else error = result
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Profile", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
}
