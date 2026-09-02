package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsAndConditionsScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
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
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Terms & Conditions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Last updated: September 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Welcome to Second Shelf. By creating an account, you agree to comply with our campus trading policies.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TermsSection(
                icon = Icons.Outlined.School,
                title = "1. Student Eligibility & Registration",
                content = "Second Shelf is a dedicated campus peer-to-peer marketplace designed for university students. You agree that the information provided during registration (including your name, valid @student.tarc.edu.my email address, and campus) is accurate and true."
            )

            TermsSection(
                icon = Icons.Outlined.Storefront,
                title = "2. Marketplace Listings & Honest Descriptions",
                content = "Sellers must provide accurate descriptions, genuine photos, and truthful condition ratings for all items. Misrepresenting items, selling counterfeit products, academic dishonesty materials (such as live exam answers), or hazardous/illegal substances is strictly prohibited."
            )

            TermsSection(
                icon = Icons.Outlined.Handshake,
                title = "3. Campus Meetups & Transactions",
                content = "All transactions are directly negotiated between students. We strongly recommend meeting in well-lit, public areas on campus (e.g., library lobbies, student cafeterias, hostel main gates). Inspect the item thoroughly before completing payment via cash or DuitNow QR."
            )

            TermsSection(
                icon = Icons.Outlined.Shield,
                title = "4. User Conduct & Community Safety",
                content = "We uphold a zero-tolerance policy against harassment, hate speech, abusive language, discrimination, and spam messaging. Violation of community standards will result in immediate suspension or termination of your account."
            )

            TermsSection(
                icon = Icons.Outlined.Lock,
                title = "5. Account Management & Privacy",
                content = "Your privacy is important to us. Your contact number is only shared when you choose to connect with another student. You retain the right to modify your profile or permanently delete your account and associated listings at any time through Settings."
            )

            TermsSection(
                icon = Icons.Outlined.Gavel,
                title = "6. Limitation of Liability",
                content = "Second Shelf provides the platform to connect student buyers and sellers. Second Shelf is not responsible for direct dispute resolution, loss of property, or personal damages arising from individual peer-to-peer exchanges."
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "I Understand & Accept",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TermsSection(
    icon: ImageVector,
    title: String,
    content: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                lineHeight = 20.sp
            )
        }
    }
}
