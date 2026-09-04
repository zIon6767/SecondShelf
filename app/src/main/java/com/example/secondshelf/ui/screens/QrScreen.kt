package com.example.secondshelf.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.theme.PrimaryBlue
import com.example.secondshelf.ui.theme.SecondShelfTheme
import com.example.secondshelf.utils.QrCodeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(
    onNavigateBack: () -> Unit,
    //mock data change laterr
    itemName: String = "Engineering Calculus Textbook",
    price: String = "RM 40",
    sellerName: String = "Ahmad Faris"
) {
    //mock transaction token, FETCH FROM BACKEND LATER
    val transactionToken = "SECURE_HANDOFF_TOKEN_XYZ_123"

    //generates the QR bitmap once and remembers it
    val qrBitmap = remember(transactionToken) {
        QrCodeUtils.generateQrBitmap(transactionToken)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pickup Pass", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            //instruction header
            Text(
                text = "Ready for Pickup",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Present this QR code to the seller to complete your transaction",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            //qr code box
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(3.dp, PrimaryBlue, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Transaction QR Code",
                        modifier = Modifier.size(220.dp) // Slightly smaller to leave a margin
                    )
                } else {
                    //fallback in case generation fails
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Code Placeholder",
                            modifier = Modifier.size(120.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Failed to load QR",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            //item summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Order Summary",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = itemName,
                            color = Color.DarkGray,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = price,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Seller: $sellerName",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            //safety /emergency action
            TextButton(
                onClick = { /*handle cancel/report issue*/ },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Report an Issue", color = Color(0xFFE53935))
            }
        }
    }
}

@Preview(showBackground = true, name = "Pickup Pass - Default")
@Composable
fun PickupPassScreenPreview() {
    SecondShelfTheme {
        QrScreen(
            onNavigateBack = {},
            itemName = "Engineering Calculus Textbook", //dummy data just for preview
            price = "RM 40",
            sellerName = "Ahmad Faris"
        )
    }
}