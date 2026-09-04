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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.theme.PrimaryBlue
import com.example.secondshelf.utils.QrCodeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(
    onNavigateBack: () -> Unit,
    transactionToken: String,
    itemName: String,
    price: String = "RM 40",
    sellerName: String = "Ahmad Faris",
    onHandoffComplete: (() -> Unit)? = null
) {
    var isHandoffCompleted by remember { mutableStateOf(false) }

    // Poll Supabase to detect when the buyer scans and completes the transaction
    LaunchedEffect(transactionToken) {
        if (transactionToken.isNotBlank()) {
            val repository = com.example.secondshelf.data.TransactionRepository(com.example.secondshelf.supabase)
            while (isActive && !isHandoffCompleted) {
                delay(1000)
                val completed = withContext(Dispatchers.IO) {
                    repository.isTransactionCompleted(transactionToken)
                }
                if (completed) {
                    isHandoffCompleted = true
                    onHandoffComplete?.invoke()
                    break
                }
            }
        }
    }

    if (isHandoffCompleted) {
        SuccessScreen(
            onBackToHome = onNavigateBack,
            itemName = itemName,
            finalPrice = price
        )
    } else {
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

                Text(
                    text = "Ready for Pickup",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Show this QR code to the buyer to complete your transaction",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

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
                            modifier = Modifier
                                .size(220.dp)
                                .padding(8.dp)
                        )
                    } else {
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

                TextButton(
                    onClick = { onNavigateBack() },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Close", color = Color.Gray)
                }
            }
        }
    }
}
