package com.example.secondshelf.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.secondshelf.ui.model.Category
import com.example.secondshelf.ui.model.Condition
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    viewModel: MarketplaceViewModel,
    listingId: String? = null,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val existing = listingId?.let { viewModel.getListing(it) }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var priceText by remember { mutableStateOf(existing?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: Category.BOOKS) }
    var condition by remember { mutableStateOf(existing?.condition ?: Condition.GOOD) }
    var location by remember { mutableStateOf(existing?.location ?: "") }
    var contactPref by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imageExtension by remember { mutableStateOf("jpg") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("Could not read the selected image")
                require(bytes.size <= 5 * 1024 * 1024) { "Image must not exceed 5 MB" }

                val mimeType = context.contentResolver.getType(uri).orEmpty()
                val extension = when (mimeType) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    else -> "jpg"
                }

                selectedImageUri = uri
                imageBytes = bytes
                imageExtension = extension
                error = null
            }.onFailure {
                selectedImageUri = null
                imageBytes = null
                error = it.message ?: "Could not open the selected image"
            }
        }
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
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
            Text(
                if (existing != null) "Edit Listing" else "Sell Item",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FormField("Product Photo *") {
                val previewModel: Any? = selectedImageUri ?: existing?.imageUrl

                if (previewModel != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    ) {
                        AsyncImage(
                            model = previewModel,
                            contentDescription = "Selected product photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(14.dp)
                                )
                        )
                        if (selectedImageUri != null) {
                            IconButton(
                                onClick = {
                                    selectedImageUri = null
                                    imageBytes = null
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        RoundedCornerShape(50)
                                    )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove photo")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (previewModel == null) "Choose Product Photo" else "Change Product Photo")
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "JPEG, PNG or WebP • Maximum 5 MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            FormField("Item Name *") {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    placeholder = { Text("e.g. Engineering Calculus Textbook") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            FormField("Description *") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; error = null },
                    placeholder = { Text("Describe your item…") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5
                )
            }

            FormField("Price (RM) *") {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() }; error = null },
                    placeholder = { Text("e.g. 40") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            FormField("Category *") {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = category.label,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        Category.entries.filter { it != Category.ALL }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.label) },
                                onClick = { category = cat; expanded = false }
                            )
                        }
                    }
                }
            }

            FormField("Condition *") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Condition.entries.forEach { cond ->
                        FilterChip(
                            selected = condition == cond,
                            onClick = { condition = cond },
                            label = { Text(cond.label) }
                        )
                    }
                }
            }

            FormField("Location / Campus *") {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it; error = null },
                    placeholder = { Text("e.g. KL Main Campus") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            FormField("Contact Preference") {
                OutlinedTextField(
                    value = contactPref,
                    onValueChange = { contactPref = it },
                    placeholder = { Text("e.g. In-app messaging") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(
            onClick = {
                if (existing == null && imageBytes == null) {
                    error = "Please choose a product photo"
                } else {
                    val price = priceText.toIntOrNull() ?: 0
                    val handleResult: (String?) -> Unit = { result ->
                        if (result == null) onSuccess() else error = result
                    }
                    if (existing != null) {
                        viewModel.updateListing(
                            existing.id, title, description, price, category, condition, location,
                            onComplete = handleResult
                        )
                    } else {
                        viewModel.createListing(
                            title = title,
                            description = description,
                            price = price,
                            category = category,
                            condition = condition,
                            location = location,
                            imageBytes = imageBytes,
                            imageExtension = imageExtension,
                            onComplete = handleResult
                        )
                    }
                }
            },
            enabled = !viewModel.isListingSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (viewModel.isListingSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Saving…")
            } else {
                Text(
                    if (existing != null) "Save Changes" else "Post Listing",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FormField(label: String, content: @Composable () -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        content()
    }
}
