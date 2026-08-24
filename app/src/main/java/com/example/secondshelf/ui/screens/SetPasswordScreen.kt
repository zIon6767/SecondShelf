package com.example.secondshelf.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.AuthUiState
import com.example.secondshelf.ui.AuthViewModel
import com.example.secondshelf.ui.theme.DangerRed
import com.example.secondshelf.ui.theme.SuccessGreen
import androidx.compose.material.icons.filled.MarkEmailRead

@Composable
fun SetPasswordScreen(
    viewModel: AuthViewModel,
    userName: String  = "",
    userEmail: String = "",
    userPhone: String = "",
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(false) }

    val uiState   by viewModel.uiState.collectAsState()
    val isLoading = uiState is AuthUiState.Loading
    val apiError  = (uiState as? AuthUiState.Error)?.message ?: ""

    // ── Password requirement checks ───────────────────────────────────────────
    val hasMinLength   = password.length >= 8
    val hasUppercase   = password.any { it.isUpperCase() }
    val hasDigit       = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    val allMet         = hasMinLength && hasUppercase && hasDigit && hasSpecialChar

    // Navigate to home on successful registration (session confirmed)
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            viewModel.resetState()
            onNavigateToHome()
        }
    }

    // Show email confirmation screen if Supabase requires it
    if (uiState is AuthUiState.EmailConfirmationRequired) {
        EmailConfirmationScreen(
            email = (uiState as AuthUiState.EmailConfirmationRequired).email,
            onBackToLogin = {
                viewModel.resetState()
                onNavigateBack()
            }
        )
        return
    }

    // Reset API error when user starts typing
    val onPasswordChange: (String) -> Unit = {
        password = it
        if (uiState is AuthUiState.Error) viewModel.resetState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Set Password",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (userName.isNotBlank()) "Almost done, $userName! Create a strong password."
                   else "Create a strong password for your account.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
        )

        // ── API error card ────────────────────────────────────────────────────
        AnimatedVisibility(visible = apiError.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(apiError, color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // ── Password field ────────────────────────────────────────────────────
        Text("New Password", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("Enter a strong password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

        Spacer(modifier = Modifier.height(12.dp))

        // ── Requirements card ─────────────────────────────────────────────────
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Password requirements",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary)
                PasswordRequirement("At least 8 characters",              hasMinLength)
                PasswordRequirement("At least one uppercase letter (A–Z)", hasUppercase)
                PasswordRequirement("At least one digit (0–9)",            hasDigit)
                PasswordRequirement("At least one special character (!@#\$…)", hasSpecialChar)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Confirm password ──────────────────────────────────────────────────
        Text("Confirm Password", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("Re-enter your password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

        Spacer(modifier = Modifier.height(16.dp))

        // ── Terms checkbox ────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = agreeToTerms,
                onCheckedChange = { agreeToTerms = it },
                enabled = !isLoading,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                buildAnnotatedString {
                    append("I agree to the ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                        append("Terms of Service")
                    }
                    append(" and ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                        append("Privacy Policy")
                    }
                    append(" of Second Shelf")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Create account button ─────────────────────────────────────────────
        var localError by remember { mutableStateOf("") }

        AnimatedVisibility(visible = localError.isNotEmpty()) {
            Text(localError, color = DangerRed,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = {
                localError = when {
                    !allMet           -> "Password does not meet all requirements above."
                    password != confirmPassword -> "Passwords do not match."
                    !agreeToTerms     -> "Please agree to the Terms of Service to continue."
                    else              -> ""
                }
                if (localError.isEmpty()) {
                    viewModel.signUp(
                        email    = userEmail,
                        password = password,
                        fullName = userName,
                        phone    = userPhone
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp),
                    color = Color.White, strokeWidth = 2.5.dp)
            } else {
                Text("Create Account →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PasswordRequirement(text: String, met: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (met) SuccessGreen else Color(0xFFD1D5DB),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (met) SuccessGreen else MaterialTheme.colorScheme.secondary
        )
    }
}

// ── Email Confirmation Screen ─────────────────────────────────────────────────

@Composable
fun EmailConfirmationScreen(
    email: String,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = SuccessGreen.copy(alpha = 0.12f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = "Email sent",
                    tint = SuccessGreen,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Check Your Email",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "We sent a confirmation link to:",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Please open your email and click the confirmation link to activate your account. Once confirmed, you can log in.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onBackToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Back to Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
