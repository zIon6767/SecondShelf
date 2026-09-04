package com.example.secondshelf.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.model.InAppNotificationData
import kotlinx.coroutines.delay

@Composable
fun InAppNotificationBanner(
    notification: InAppNotificationData?,
    onDismiss: () -> Unit,
    onClick: (participantId: String, listingId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(notification?.id) {
        if (notification != null) {
            delay(5000L)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (notification != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        val pId = notification.participantId ?: ""
                        val lId = notification.listingId ?: ""
                        onDismiss()
                        if (pId.isNotBlank() && lId.isNotBlank()) {
                            onClick(pId, lId)
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar / Chat icon with online-style indicator dot
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = notification.avatarInitial.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color(0xFF34C759), CircleShape)
                                .padding(2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Text details
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = notification.senderName.ifBlank { "New message" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Chat",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Just now",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Quick action row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val pId = notification.participantId ?: ""
                                    val lId = notification.listingId ?: ""
                                    onDismiss()
                                    if (pId.isNotBlank() && lId.isNotBlank()) {
                                        onClick(pId, lId)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "View conversation",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
