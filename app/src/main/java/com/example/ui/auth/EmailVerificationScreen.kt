package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirestoreManager
import com.example.ui.components.GradientButton
import com.example.ui.theme.PrimaryGradient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EmailVerificationScreen(
    userEmail: String,
    onEmailVerified: () -> Unit,
    onContinueLater: () -> Unit = onEmailVerified,
    onSignOut: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var isSendingEmail by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorMessage by remember { mutableStateOf(false) }
    var resendCooldownSeconds by remember { mutableStateOf(0) }

    // Automatic polling every 5 seconds to detect external browser verification smoothly
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                try {
                    user.reload().await()
                    if (user.isEmailVerified) {
                        onEmailVerified()
                        break
                    }
                } catch (e: Exception) {
                    // Ignore transient network errors during passive background check
                }
            }
        }
    }

    // Cooldown countdown timer
    LaunchedEffect(resendCooldownSeconds) {
        if (resendCooldownSeconds > 0) {
            delay(1000)
            resendCooldownSeconds -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Email Icon with Glow
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(PrimaryGradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = "Verify Email",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Verify Your Email ✉️",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "We sent a confirmation link to:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = userEmail.ifBlank { "your email address" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF2D55),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Please tap the link in your email to activate your account and proceed to profile setup. Check your spam/junk folder if it hasn't arrived.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Status feedback banner
            AnimatedVisibility(visible = statusMessage != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isErrorMessage) MaterialTheme.colorScheme.errorContainer else Color(0xFF00E676).copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage ?: "",
                        color = if (isErrorMessage) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF00E676),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Button: Check Verification Status
            GradientButton(
                text = "I've Verified My Email ✓",
                onClick = {
                    coroutineScope.launch {
                        isChecking = true
                        statusMessage = null
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            try {
                                user.reload().await()
                                if (user.isEmailVerified) {
                                    isErrorMessage = false
                                    statusMessage = "Email verified! Continuing to profile setup..."
                                    delay(400)
                                    onEmailVerified()
                                } else {
                                    isErrorMessage = true
                                    statusMessage = "Email not verified yet in inbox. Tap below to continue setup while verifying."
                                }
                            } catch (e: Exception) {
                                isErrorMessage = true
                                statusMessage = "Network error: ${e.localizedMessage ?: "Please try again."}"
                            }
                        } else {
                            onSignOut()
                        }
                        isChecking = false
                    }
                },
                isLoading = isChecking
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Continue Setup (Verify Later) Button
            Button(
                onClick = onContinueLater,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Continue to Profile Setup →",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resend Verification Email Button
            OutlinedButton(
                onClick = {
                    if (resendCooldownSeconds == 0) {
                        coroutineScope.launch {
                            isSendingEmail = true
                            statusMessage = null
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                try {
                                    user.sendEmailVerification().await()
                                    isErrorMessage = false
                                    statusMessage = "Verification email resent! Please check your inbox."
                                    resendCooldownSeconds = 45 // 45 seconds rate limit cooldown
                                } catch (e: Exception) {
                                    isErrorMessage = true
                                    statusMessage = "Failed to resend: ${e.localizedMessage ?: "Too many requests. Please wait."}"
                                }
                            }
                            isSendingEmail = false
                        }
                    }
                },
                enabled = resendCooldownSeconds == 0 && !isSendingEmail,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
            ) {
                if (isSendingEmail) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (resendCooldownSeconds > 0) "Resend in ${resendCooldownSeconds}s" else "Resend Verification Email",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Out / Use Another Email
            TextButton(
                onClick = {
                    FirestoreManager.signOut()
                    onSignOut()
                }
            ) {
                Text(
                    text = "Use a different email / Sign Out",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
