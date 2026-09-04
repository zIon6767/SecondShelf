package com.example.secondshelf.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.R
import com.example.secondshelf.ui.AuthUiState
import com.example.secondshelf.ui.AuthViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun isValidLoginEmail(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return emailRegex.matches(email.trim())
}

/** Only @student.tarc.edu.my emails are allowed to access the app. */
private fun isTarcStudentEmail(email: String): Boolean =
    email.trim().lowercase().endsWith("@student.tarc.edu.my")

// ─────────────────────────────────────────────────────────────────────────────
// LoginScreen
// Allows TAR UMT students to sign in using their @student.tarc.edu.my email
// or Google account. All real data comes from Supabase — no dummy data used.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    viewModel: AuthViewModel? = null,           // null only in Preview mode
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // ── State: input fields and validation errors ────────────────────────────
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError      by remember { mutableStateOf("") }
    var passwordError   by remember { mutableStateOf("") }

    // ── Observe login result from ViewModel ──────────────────────────────────
    val uiState      = viewModel?.uiState?.collectAsState()?.value ?: AuthUiState.Idle
    val isLoading    = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message ?: ""

    val context = LocalContext.current

    // ── Navigate to home when login succeeds ─────────────────────────────────
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            viewModel?.resetState()
            onNavigateToHome()
        }
    }

    // ── Forgot Password Dialog State ─────────────────────────────────────────
    var showForgotDialog   by remember { mutableStateOf(false) }
    var resetEmail         by remember { mutableStateOf("") }
    var resetEmailError    by remember { mutableStateOf("") }
    var resetResultMessage by remember { mutableStateOf("") }
    var resetIsSuccess     by remember { mutableStateOf(false) }
    var isSendingReset     by remember { mutableStateOf(false) }

    // ── Forgot Password Dialog ───────────────────────────────────────────────
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotDialog = false
                resetEmail = ""
                resetEmailError = ""
                resetResultMessage = ""
            },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter your @student.tarc.edu.my email and we will send you a password reset link.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = {
                            resetEmail = it
                            resetEmailError = ""
                            resetResultMessage = ""
                        },
                        label = { Text("Student Email") },
                        placeholder = { Text("name@student.tarc.edu.my") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                        },
                        isError = resetEmailError.isNotEmpty(),
                        supportingText = if (resetEmailError.isNotEmpty()) {
                            { Text(resetEmailError, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSendingReset
                    )
                    // Result feedback after attempting to send reset link
                    if (resetResultMessage.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (resetIsSuccess) Color(0xFFE8F5E9)
                                    else MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = resetResultMessage,
                                color = if (resetIsSuccess) Color(0xFF2E7D32)
                                        else MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            resetEmail.isBlank() ->
                                resetEmailError = "Please enter your email address."
                            !isTarcStudentEmail(resetEmail) ->
                                resetEmailError = "Only @student.tarc.edu.my email is accepted."
                            else -> {
                                isSendingReset = true
                                viewModel?.sendPasswordReset(resetEmail) { success, message ->
                                    isSendingReset = false
                                    resetIsSuccess = success
                                    resetResultMessage = message
                                }
                            }
                        }
                    },
                    enabled = !isSendingReset,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSendingReset) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sending...")
                    } else {
                        Text("Send Reset Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotDialog = false
                    resetEmail = ""
                    resetEmailError = ""
                    resetResultMessage = ""
                }) { Text("Close") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Screen Layout ────────────────────────────────────────────────────────
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
            verticalArrangement = Arrangement.Center
        ) {

            // Header text
            Text(
                text = "Welcome back!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Sign in to your SecondShelf account.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
            )

            // Error message card (only shown when login fails)
            AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Email input field — restricted to @student.tarc.edu.my
            Text(
                text = "Student Email",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = ""
                    if (uiState is AuthUiState.Error) viewModel?.resetState()
                },
                placeholder = { Text("name@student.tarc.edu.my") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = if (emailError.isNotEmpty()) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.primary
                    )
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

            Spacer(modifier = Modifier.height(16.dp))

            // Password input field
            Text(
                text = "Password",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = ""
                    if (uiState is AuthUiState.Error) viewModel?.resetState()
                },
                placeholder = { Text("Enter your password") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (passwordError.isNotEmpty()) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                          else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                isError = passwordError.isNotEmpty(),
                supportingText = if (passwordError.isNotEmpty()) {
                    { Text(passwordError, color = MaterialTheme.colorScheme.error) }
                } else null,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )

            // Forgot password link — opens the reset dialog
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { showForgotDialog = true }, enabled = !isLoading) {
                    Text(
                        "Forgot Password?",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Login button — validates domain before calling Supabase
            Button(
                onClick = {
                    var valid = true
                    if (email.isBlank() || !isValidLoginEmail(email)) {
                        emailError = "Please enter a valid email address."
                        valid = false
                    } else if (!isTarcStudentEmail(email)) {
                        emailError = "Email not valid."
                        valid = false
                    }
                    if (password.length < 6) {
                        passwordError = "Password must be at least 6 characters."
                        valid = false
                    }
                    if (valid) viewModel?.signIn(email.trim(), password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "or continue with" divider
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                Text(
                    "  or continue with  ",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google sign-in button — only @student.tarc.edu.my Google accounts are accepted
            OutlinedButton(
                onClick = { viewModel?.signInWithGoogle(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                enabled = !isLoading
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_favicon_2025),
                    contentDescription = "google logo",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Link to Register screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account? ",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Sign Up",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable(enabled = !isLoading) { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview — shows the LoginScreen in Android Studio Design panel
// ─────────────────────────────────────────────────────────────────────────────
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    com.example.secondshelf.ui.theme.SecondShelfTheme {
        LoginScreen(
            onNavigateToHome = {},
            onNavigateToRegister = {}
        )
    }
}
