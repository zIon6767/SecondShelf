package com.example.secondshelf.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NewsBannerItem(
    val id: String,
    val tag: String,
    val title: String,
    val actionText: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val buttonTextColor: Color
)

val defaultNewsBanners = listOf(
    NewsBannerItem(
        id = "deals",
        tag = "Campus Deals",
        title = "Find great items from\nyour fellow students",
        actionText = "Browse Now",
        icon = Icons.Default.ShoppingBag,
        gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
        buttonTextColor = Color(0xFF1D4ED8)
    ),
    NewsBannerItem(
        id = "update",
        tag = "App Update v1.1",
        title = "Instant messaging &\noffer negotiations",
        actionText = "Try Chat",
        icon = Icons.Default.Forum,
        gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
        buttonTextColor = Color(0xFF4F46E5)
    ),
    NewsBannerItem(
        id = "safety",
        tag = "Trust & Safety",
        title = "Trade securely with\nverified students",
        actionText = "Learn More",
        icon = Icons.Default.VerifiedUser,
        gradientColors = listOf(Color(0xFF0D9488), Color(0xFF0F766E)),
        buttonTextColor = Color(0xFF0F766E)
    ),
    NewsBannerItem(
        id = "declutter",
        tag = "Declutter & Earn",
        title = "Turn unused books &\ngear into quick cash",
        actionText = "Post Listing",
        icon = Icons.Default.LocalOffer,
        gradientColors = listOf(Color(0xFFEA580C), Color(0xFFC2410C)),
        buttonTextColor = Color(0xFFC2410C)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroBanner(
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier,
    banners: List<NewsBannerItem> = defaultNewsBanners
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val item = banners[page]
            BannerCard(
                item = item,
                onClick = onBrowseClick
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pagination Dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 22.dp else 7.dp,
                    label = "dotWidth"
                )
                val color = if (isSelected) {
                    banners[index].gradientColors.first()
                } else {
                    Color(0xFFD1D5DB)
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(7.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun BannerCard(
    item: NewsBannerItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        shadowElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(colors = item.gradientColors),
                    RoundedCornerShape(18.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = item.tag,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Surface(
                        onClick = onClick,
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = item.actionText,
                            color = item.buttonTextColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(72.dp)
                )
            }
        }
    }
}

@Composable
fun AppToast(message: String, onDismiss: () -> Unit) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    ) {
        Text(message)
    }
}
