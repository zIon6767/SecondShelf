package com.example.secondshelf.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.R
import com.example.secondshelf.ui.AuthUiState
import com.example.secondshelf.ui.AuthViewModel

private fun isValidEmail(email: String): Boolean {
    val trimmed = email.trim().lowercase()
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return emailRegex.matches(trimmed)
}

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToSetPassword: (name: String, email: String, phone: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTerms: () -> Unit = {}
) {
    var fullName   by remember { mutableStateOf("") }
    var email      by remember { mutableStateOf("") }
    var phone      by remember { mutableStateOf("") }
    var nameError  by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }
    var termsError by remember { mutableStateOf("") }

    val uiState   by viewModel.uiState.collectAsState()
    val isLoading = uiState is AuthUiState.Loading
    val apiError  = (uiState as? AuthUiState.Error)?.message ?: ""

    val context = LocalContext.current

    // Handle Google Sign-In success from this screen
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            viewModel.resetState()
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

        //Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Join your campus marketplace today.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
            )
        }

        //Avatar circle
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        //API error card
        AnimatedVisibility(visible = apiError.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(apiError, color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        //Full Name
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Full Name", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { input ->
                    fullName = input.filter { it.isLetter() || it.isWhitespace() }
                    nameError = ""
                },
                placeholder = { Text("Enter your full name") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                },
                isError = nameError.isNotEmpty(),
                supportingText = if (nameError.isNotEmpty()) {
                    { Text(nameError, color = MaterialTheme.colorScheme.error) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        //Email
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Email", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = "" },
                placeholder = { Text("e.g. name@student.tarc.edu.my") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                },
                isError = emailError.isNotEmpty(),
                supportingText = if (emailError.isNotEmpty()) {
                    { Text(emailError, color = MaterialTheme.colorScheme.error) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        //Phone
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Phone Number", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    phone = input.filter { it.isDigit() }.take(11)
                    phoneError = ""
                },
                placeholder = { Text("e.g. 0123456789") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                },
                isError = phoneError.isNotEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )
            if (phoneError.isNotEmpty()) {
                Text(
                    text = phoneError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Terms & Conditions Checkbox
        val primaryColor = MaterialTheme.colorScheme.primary
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = agreeToTerms,
                onCheckedChange = {
                    agreeToTerms = it
                    termsError = ""
                },
                enabled = !isLoading,
                colors = CheckboxDefaults.colors(checkedColor = primaryColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = buildAnnotatedString {
                    append("I agree to the ")
                    withStyle(
                        SpanStyle(
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    ) {
                        append("Terms & Conditions")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable { onNavigateToTerms() }
            )
        }
        if (termsError.isNotEmpty()) {
            Text(
                text = termsError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 12.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        //Continue button
        Button(
            onClick = {
                var valid = true
                if (fullName.trim().isBlank()) {
                    nameError = "Please enter your full name"; valid = false
                }
                if (!isValidEmail(email)) {
                    emailError = "Invalid email address"; valid = false
                }
                if (phone.isNotBlank() && (phone.length < 10 || !phone.startsWith("01"))) {
                    phoneError = "Invalid phone number"
                    valid = false
                }
                if (!agreeToTerms) {
                    termsError = "Please agree to the Terms & Conditions to continue"
                    valid = false
                }
                if (valid) onNavigateToSetPassword(fullName.trim(), email.trim(), phone.trim())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !isLoading
        ) {
            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Divider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            Text("  or sign up with  ",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium)
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Google Sign-Up button
        OutlinedButton(
            onClick = { viewModel.signInWithGoogle(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
            enabled = !isLoading
        ) {
            Image(
                painter = painterResource(id = R.drawable.google_favicon_2025),
                contentDescription = "google logo",
                modifier = Modifier
                    .size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Sign up with Google", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        //Login link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium)
            Text(
                "Sign In",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable(enabled = !isLoading) { onNavigateToLogin() }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
