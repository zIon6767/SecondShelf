package com.example.secondshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class OnboardingSlide(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(onNavigateToLogin: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }
    
    val slides = listOf<OnboardingSlide>(
        OnboardingSlide(Icons.Default.Star, "Buy affordable campus essentials", "Find cheap textbooks, calculators, and dorm gear right on campus."),
        OnboardingSlide(Icons.Default.Info, "Sell unused items easily", "Declutter your room and make some extra cash."),
        OnboardingSlide(Icons.Default.CheckCircle, "Trade safely with verified students", "Exclusive to your university. No strangers, just peers.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        // Skip Button
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            TextButton(onClick = onNavigateToLogin) {
                Text("Skip", color = MaterialTheme.colorScheme.secondary)
            }
        }

        // Content
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = slides[currentStep].icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp).padding(bottom = 32.dp)
            )
            Text(
                text = slides[currentStep].title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = slides[currentStep].description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Bottom Nav / Pagination
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                slides.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(if (index == currentStep) 24.dp else 8.dp)
                            .background(
                                color = if (index == currentStep) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = CircleShape
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (currentStep < slides.size - 1) {
                        currentStep++
                    } else {
                        onNavigateToLogin()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape
            ) {
                Text(if (currentStep == slides.size - 1) "Get Started" else "Next")
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    com.example.secondshelf.ui.theme.SecondShelfTheme {
        OnboardingScreen(onNavigateToLogin = {})
    }
}

