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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.AuthUiState
import com.example.secondshelf.ui.AuthViewModel
import com.example.secondshelf.R

// ── Validation helpers ────────────────────────────────────────────────────────
private fun isValidLoginEmail(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return emailRegex.matches(email.trim())
}

@Composable
fun LoginScreen(
    viewModel: AuthViewModel? = null,
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError      by remember { mutableStateOf("") }
    var passwordError   by remember { mutableStateOf("") }

    val uiState      = viewModel?.uiState?.collectAsState()?.value ?: AuthUiState.Idle
    val isLoading    = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message ?: ""

    val context = LocalContext.current

    //Navigate to home when Supabase auth succeeds
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            viewModel?.resetState()
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
            verticalArrangement = Arrangement.Center
        ) {

        //Header
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

        //Error card (shown on auth failure)
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

        //Email field
        Text(
            text = "Email",
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
            placeholder = { Text("Enter your email") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null,
                    tint = if (emailError.isNotEmpty()) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.primary)
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

        //Password field
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
                Icon(Icons.Default.Lock, contentDescription = null,
                    tint = if (passwordError.isNotEmpty()) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.primary)
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

        //Forgot password
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = {}, enabled = !isLoading) {
                Text("Forgot Password?",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        //Login button
        Button(
            onClick = {
                // Client-side validation before hitting Supabase
                var valid = true
                if (email.isBlank() || !isValidLoginEmail(email)) {
                    emailError = "Please enter a valid email address."
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

        //Divider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            Text("  or continue with  ",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium)
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
        }

        Spacer(modifier = Modifier.height(20.dp))

        //Google button
        OutlinedButton(
            onClick = { viewModel?.signInWithGoogle(context) },
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
            Text("Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        //Sign up link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium)
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

