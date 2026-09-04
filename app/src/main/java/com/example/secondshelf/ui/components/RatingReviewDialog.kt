package com.example.secondshelf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

val GoldStar = Color(0xFFFFB800)

@Composable
fun RatingReviewDialog(
    targetUserName: String,
    listingTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String, tags: List<String>) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    val quickTags = listOf(
        "⚡ Fast Response",
        "🤝 Punctual Meetup",
        "📦 Item as Described",
        "😊 Friendly",
        "💬 Great Communication"
    )

    val ratingLabel = when (selectedRating) {
        1 -> "Poor"
        2 -> "Fair"
        3 -> "Good"
        4 -> "Very Good"
        5 -> "Excellent"
        else -> ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rate & Review",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.secondary)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "How was your campus deal with $targetUserName?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (listingTitle.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Item: $listingTitle",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Interactive 5-Star Row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { selectedRating = i },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= selectedRating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$i Stars",
                                tint = if (i <= selectedRating) GoldStar else Color(0xFFD1D5DB),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Text(
                    text = ratingLabel,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldStar,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Quick Tags
                Text(
                    text = "What went well?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(8.dp))
                FlowTagRow(
                    tags = quickTags,
                    selectedTags = selectedTags,
                    onToggleTag = { tag ->
                        selectedTags = if (selectedTags.contains(tag)) selectedTags - tag else selectedTags + tag
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Written Feedback Field
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Share details of your experience (optional)...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )

                Spacer(Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        onSubmit(selectedRating, comment, selectedTags.toList())
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Submit Review", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun FlowTagRow(
    tags: List<String>,
    selectedTags: Set<String>,
    onToggleTag: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val row1 = tags.take(3)
        val row2 = tags.drop(3)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row1.forEach { tag ->
                TagChip(tag = tag, isSelected = selectedTags.contains(tag), onClick = { onToggleTag(tag) })
            }
        }
        if (row2.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row2.forEach { tag ->
                    TagChip(tag = tag, isSelected = selectedTags.contains(tag), onClick = { onToggleTag(tag) })
                }
            }
        }
    }
}

@Composable
private fun TagChip(
    tag: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE5E7EB)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ManualCreateReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (reviewerName: String, listingTitle: String, rating: Int, comment: String, tags: List<String>) -> Unit
) {
    var reviewerName by remember { mutableStateOf("") }
    var listingTitle by remember { mutableStateOf("") }
    var selectedRating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    val quickTags = listOf(
        "⚡ Fast Response",
        "🤝 Punctual Meetup",
        "📦 Item as Described",
        "😊 Friendly",
        "💬 Great Communication"
    )

    val ratingLabel = when (selectedRating) {
        1 -> "Poor"
        2 -> "Fair"
        3 -> "Good"
        4 -> "Very Good"
        5 -> "Excellent"
        else -> ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Student Review",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.secondary)
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Add a feedback review to display on this profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(Modifier.height(14.dp))

                // Reviewer Name input
                OutlinedTextField(
                    value = reviewerName,
                    onValueChange = { reviewerName = it },
                    label = { Text("Reviewer Name") },
                    placeholder = { Text("e.g. Sarah Lee / Marcus Wong") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                // Listing Title input
                OutlinedTextField(
                    value = listingTitle,
                    onValueChange = { listingTitle = it },
                    label = { Text("Item Traded (Optional)") },
                    placeholder = { Text("e.g. Calculus 9th Edition") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Interactive 5-Star Row
                Text(
                    text = "Rating: $ratingLabel ($selectedRating / 5)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldStar
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { selectedRating = i },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= selectedRating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$i Stars",
                                tint = if (i <= selectedRating) GoldStar else Color(0xFFD1D5DB),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Quick Tags
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(6.dp))
                FlowTagRow(
                    tags = quickTags,
                    selectedTags = selectedTags,
                    onToggleTag = { tag ->
                        selectedTags = if (selectedTags.contains(tag)) selectedTags - tag else selectedTags + tag
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Written Feedback Field
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Review Feedback") },
                    placeholder = { Text("Write feedback comments...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 4
                )

                Spacer(Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        val finalAuthor = reviewerName.ifBlank { "Student Peer" }
                        val finalItem = listingTitle.ifBlank { "Campus Deal" }
                        val finalComment = comment.ifBlank { "Great trade experience!" }
                        onSubmit(finalAuthor, finalItem, selectedRating, finalComment, selectedTags.toList())
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Add Review to Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

