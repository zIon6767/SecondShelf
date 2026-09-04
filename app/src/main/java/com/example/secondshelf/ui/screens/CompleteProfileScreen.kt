package com.example.secondshelf.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.AuthUiState
import com.example.secondshelf.ui.AuthViewModel
import com.example.secondshelf.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    authViewModel: AuthViewModel,
    marketplaceViewModel: MarketplaceViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    // Intercept system back button / gesture when onNavigateBack is provided
    BackHandler(enabled = onNavigateBack != null) {
        onNavigateBack?.invoke()
    }

    val initialName = authViewModel.getUserFullName().let {
        if (it == "User" || it == "Student" || it == "null") "" else it
    }
    val email = authViewModel.getUserEmail()

    var fullName by remember { mutableStateOf(initialName) }
    var selectedCampus by remember { mutableStateOf("KL Main Campus") }
    var phone by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val uiState by authViewModel.uiState.collectAsState()
    val isLoading = uiState is AuthUiState.Loading

    val campuses = listOf(
        "KL Main Campus",
        "Penang Branch Campus",
        "Perak Branch Campus",
        "Johor Branch Campus",
        "Pahang Branch Campus",
        "Sabah Branch Campus"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        // Back button positioned at the top-left
        if (onNavigateBack != null) {
            Surface(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 20.dp, top = 16.dp)
                    .size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Welcome to Second Shelf!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Please complete your student profile to start trading on campus.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Error Banner
            AnimatedVisibility(visible = errorText != null) {
                errorText?.let { err ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(err, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Google Email (read-only indicator)
            if (email.isNotBlank() && email != "user@example.com") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF4F7FB),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Signed in as", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Text(email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.Outlined.Check, contentDescription = "Verified", tint = Color(0xFF34C759), modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Full Name / Username Field
            Text("Username / Display Name *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; errorText = null },
                placeholder = { Text("e.g. Alex Tan") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Campus Selector
            Text("Campus / Branch *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))

            var campusMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = campusMenuExpanded,
                onExpandedChange = { campusMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCampus,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = campusMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = campusMenuExpanded,
                    onDismissRequest = { campusMenuExpanded = false }
                ) {
                    campuses.forEach { campus ->
                        DropdownMenuItem(
                            text = { Text(campus) },
                            onClick = {
                                selectedCampus = campus
                                campusMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Phone Field
            Text("Phone / WhatsApp (Optional)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            val isPhoneInvalid = phone.isNotBlank() && (phone.length < 10 || !phone.startsWith("01"))
            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    phone = input.filter { it.isDigit() }.take(11)
                    errorText = null
                },
                placeholder = { Text("e.g. 0123456789") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                isError = isPhoneInvalid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            if (isPhoneInvalid) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Invalid phone number",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            // Submit Button
            Button(
                onClick = {
                    if (fullName.trim().isBlank()) {
                        errorText = "Please enter your username / display name."
                        return@Button
                    }
                    if (isPhoneInvalid) {
                        errorText = "Invalid phone number."
                        return@Button
                    }
                    authViewModel.updateUserProfile(
                        fullName = fullName.trim(),
                        campus = selectedCampus
                    ) {
                        marketplaceViewModel.syncAuthUser(
                            id = authViewModel.getUserId(),
                            name = fullName.trim(),
                            email = email,
                            campus = selectedCampus
                        )
                        onNavigateToHome()
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        "Continue to Second Shelf",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
