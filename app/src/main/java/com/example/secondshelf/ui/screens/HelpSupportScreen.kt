package com.example.secondshelf.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

data class FaqItem(val question: String, val answer: String)

private val defaultFaqs = listOf(
    FaqItem(
        question = "How does campus pickup and trade work?",
        answer = "Second Shelf connects students within the same campus. Once you chat with the seller and agree on a deal, you can arrange to meet at a designated public campus spot like the Library lobby, Canteen, or Student Center."
    ),
    FaqItem(
        question = "How do I list an item for sale?",
        answer = "Tap the standout '+' or 'Sell' button on the bottom navigation bar. Add clear photos, title, price, category, condition, and a brief description. Your listing will immediately appear in the marketplace!"
    ),
    FaqItem(
        question = "Is Second Shelf free to use?",
        answer = "Yes! Second Shelf is 100% free for TAR UMT university students with zero listing fees or commission charges."
    ),
    FaqItem(
        question = "How are payments handled?",
        answer = "Buyers and sellers typically settle payments in-person during the item inspection using DuitNow QR, Touch 'n Go eWallet, or cash."
    ),
    FaqItem(
        question = "What items are prohibited?",
        answer = "Prohibited items include counterfeit goods, copyrighted digital test banks, weapons, dangerous materials, and non-student related commercial stock."
    )
)

@Composable
fun HelpSupportScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    var expandedFaqIndex by remember { mutableIntStateOf(-1) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    var contactSubject by remember { mutableStateOf("") }
    var contactMessage by remember { mutableStateOf("") }
    var contactSubmitted by remember { mutableStateOf(false) }

    var reportType by remember { mutableStateOf("Bug / Technical Issue") }
    var reportDetails by remember { mutableStateOf("") }
    var reportSubmitted by remember { mutableStateOf(false) }

    // Contact Support Dialog
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("Contact Support", fontWeight = FontWeight.Bold) },
            text = {
                if (contactSubmitted) {
                    Text("Thank you! Your support ticket has been sent to our campus student support team. We'll reply to your university email within 24 hours.", color = Color(0xFF34C759))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = contactSubject,
                            onValueChange = { contactSubject = it },
                            label = { Text("Subject") },
                            placeholder = { Text("e.g., Question about my listing") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = contactMessage,
                            onValueChange = { contactMessage = it },
                            label = { Text("Describe your issue") },
                            placeholder = { Text("Provide details...") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                if (contactSubmitted) {
                    TextButton(onClick = {
                        showContactDialog = false
                        contactSubmitted = false
                        contactSubject = ""
                        contactMessage = ""
                    }) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (contactMessage.isNotBlank()) {
                                contactSubmitted = true
                            }
                        },
                        enabled = contactMessage.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Send Message")
                    }
                }
            },
            dismissButton = {
                if (!contactSubmitted) {
                    TextButton(onClick = { showContactDialog = false }) { Text("Cancel") }
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Report a Problem Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report a Problem", fontWeight = FontWeight.Bold) },
            text = {
                if (reportSubmitted) {
                    Text("Report received. Our moderation team will investigate this promptly. Thank you for keeping our campus marketplace safe!", color = Color(0xFF34C759))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Bug / Glitch", "Inappropriate Item", "Scam / Abuse").forEach { type ->
                                val selected = reportType == type
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { reportType = type }
                                ) {
                                    Text(
                                        text = type,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = reportDetails,
                            onValueChange = { reportDetails = it },
                            label = { Text("Details") },
                            placeholder = { Text("What happened?") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                if (reportSubmitted) {
                    TextButton(onClick = {
                        showReportDialog = false
                        reportSubmitted = false
                        reportDetails = ""
                    }) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (reportDetails.isNotBlank()) {
                                reportSubmitted = true
                            }
                        },
                        enabled = reportDetails.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Report")
                    }
                }
            },
            dismissButton = {
                if (!reportSubmitted) {
                    TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Terms of Service Modal
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 300.dp)) {
                    Text(
                        "1. Acceptance of Terms\nBy accessing Second Shelf, you agree to comply with our campus student marketplace policies.\n\n" +
                        "2. Student Conduct\nAll users are expected to treat peers with mutual respect. Misrepresentation of item conditions is prohibited.\n\n" +
                        "3. Trade Safety\nSecond Shelf is a facilitation platform. Ensure all face-to-face exchanges occur in well-lit public campus locations.\n\n" +
                        "4. Account Responsibility\nYou are responsible for safeguarding your TAR UMT student authentication credentials."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) { Text("Close", fontWeight = FontWeight.Bold) }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Privacy Policy Modal
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 300.dp)) {
                    Text(
                        "1. Information We Collect\nWe collect basic profile details (Name, Campus Email, University) to verify verified student credentials.\n\n" +
                        "2. Listing Information\nListings and campus communications are visible to authenticated student peers on campus.\n\n" +
                        "3. Data Protection\nWe do not sell or monetize student data to external third-party advertisers."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close", fontWeight = FontWeight.Bold) }
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
            Text("Help & Support", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Section 1: Quick Support Actions ──
            Column {
                SectionTitle("Get in Touch")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SupportActionRow(
                            icon = Icons.Outlined.Chat,
                            title = "Contact Support / Inquiries",
                            subtitle = "Message our student support assistance team",
                            iconColor = MaterialTheme.colorScheme.primary,
                            onClick = { showContactDialog = true }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(start = 56.dp))
                        SupportActionRow(
                            icon = Icons.Outlined.BugReport,
                            title = "Report a Problem",
                            subtitle = "Report a bug or inappropriate marketplace listing",
                            iconColor = Color(0xFFFF9500),
                            onClick = { showReportDialog = true }
                        )
                    }
                }
            }

            // ── Section 2: FAQ / Help Center ──
            Column {
                SectionTitle("Frequently Asked Questions")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        defaultFaqs.forEachIndexed { index, faq ->
                            val isExpanded = expandedFaqIndex == index
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedFaqIndex = if (isExpanded) -1 else index
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = faq.question,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Text(
                                        text = faq.answer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                            if (index < defaultFaqs.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }

            // ── Section 3: Legal & Safety Guidelines ──
            Column {
                SectionTitle("Legal & Campus Guidelines")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SupportActionRow(
                            icon = Icons.Outlined.Description,
                            title = "Terms of Service",
                            subtitle = "Read our student marketplace agreement",
                            iconColor = Color(0xFF5856D6),
                            onClick = { showTermsDialog = true }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(start = 56.dp))
                        SupportActionRow(
                            icon = Icons.Outlined.PrivacyTip,
                            title = "Privacy Policy",
                            subtitle = "Learn how your campus data is protected",
                            iconColor = Color(0xFF34C759),
                            onClick = { showPrivacyDialog = true }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SupportActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
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
                fontWeight = FontWeight.SemiBold
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
