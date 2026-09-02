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
fun CampusSafetyScreen(
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
                    text = "Campus Safety Tips",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Guidelines for safe student trading",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero banner
            Surface(
                color = Color(0xFF007AFF).copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF007AFF).copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = Color(0xFF007AFF),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Safety First on Campus",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF007AFF)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Follow these golden rules whenever buying or selling items with fellow students.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            SafetyTipCard(
                icon = Icons.Outlined.LocationOn,
                iconColor = Color(0xFF34C759),
                title = "1. Meet in Populated Campus Areas",
                content = "Always schedule meetups during daytime in well-lit, high-traffic campus locations such as the Library Lobby, Cafeteria concourse, or Student Activity Centre."
            )

            SafetyTipCard(
                icon = Icons.Outlined.Visibility,
                iconColor = Color(0xFF007AFF),
                title = "2. Inspect Items in Person",
                content = "Thoroughly test electronics, power on devices, and inspect book pages before handing over money. Ensure item condition matches the listing description."
            )

            SafetyTipCard(
                icon = Icons.Outlined.Payments,
                iconColor = Color(0xFFFF9500),
                title = "3. Pay Only Upon Handover",
                content = "Never pay advance deposits or reservation fees online. Complete payments via DuitNow QR or cash strictly on-the-spot after verifying the item."
            )

            SafetyTipCard(
                icon = Icons.Outlined.People,
                iconColor = Color(0xFF5856D6),
                title = "4. Bring a Friend (Buddy System)",
                content = "If meeting in late afternoon or handling high-value gadgets, invite a classmate or friend to accompany you during the meetup."
            )

            SafetyTipCard(
                icon = Icons.Outlined.Report,
                iconColor = Color(0xFFFF3B30),
                title = "5. Trust Your Instincts & Report",
                content = "If a buyer or seller acts suspiciously, changes meeting locations to isolated areas, or refuses inspection, cancel the deal and report the account."
            )

            // Campus security card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TAR UMT Campus Security",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "In case of emergency or safety concerns on campus, contact the Security Department immediately at the Main Security Post.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SafetyTipCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    content: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(10.dp))
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
