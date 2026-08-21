package com.example.ui.safety

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.UserProfile
import com.example.ui.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyCenterScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    var showVerificationDialog by remember { mutableStateOf<String?>(null) } // "PHONE", "EMAIL", "SELFIE", "ID"
    var successToast by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Safety Shield Banner
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(Color(0xFF00D2FF), Color(0xFF00E676))),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0C2430),
                                    Color(0xFF07141C)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF00D2FF).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00D2FF), modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Anti-Fake & Verification Hub 🛡️", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Authenticity Score: 95% Verified", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E676), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "We use automated moderation, duplicate-account detection, and selfie liveness verification to ensure 100% genuine cross-cultural connections.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Multi-Tier Verification Badges
        item {
            Text("Verification Status Badges 🟢", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VerificationRowItem("📱 Phone Number Verified", "Confirmed via SMS code", isVerified = true) {
                    showVerificationDialog = "PHONE"
                }
                VerificationRowItem("✉️ Email Address Verified", "Official email address confirmed", isVerified = true) {
                    showVerificationDialog = "EMAIL"
                }
                VerificationRowItem("📸 Photo Liveness Verified", "Real-time pose & selfie liveness test", isVerified = userProfile.isVerified) {
                    showVerificationDialog = "SELFIE"
                }
                VerificationRowItem("🪪 Government ID Verified", "Optional passport or national ID match", isVerified = false) {
                    showVerificationDialog = "ID"
                }
            }
        }

        // Scam & Anti-Fraud Guidelines
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Anti-Scam Protection Rules ⚠️", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.05f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScamTipItem("🚫 Never Send Money / Crypto", "Legitimate matches will never ask for wire transfers, gift cards, crypto investments, or emergency travel cash.")
                    ScamTipItem("💬 Keep Communications on Platform", "Avoid moving immediately to external unmoderated platforms until you have established mutual trust.")
                    ScamTipItem("📹 Video Call Early", "Use our built-in video call before meeting in person to confirm identity and chemistry.")
                    ScamTipItem("🚩 Report Suspicious Behavior", "If someone asks for money or behaves aggressively, tap 'Report' in their profile or chat.")
                }
            }
        }

        // Privacy Controls
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Privacy & Visibility Controls 🔒", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }

        item {
            val privacy = userProfile.privacySettings
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.05f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrivacyToggleRow("Show Online Status", "Let matches see when you are active", privacy.showOnlineStatus) {
                        profileViewModel.updatePrivacySettings(privacy.copy(showOnlineStatus = it))
                    }
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    PrivacyToggleRow("Read Receipts", "Show double checks when messages are read", privacy.readReceipts) {
                        profileViewModel.updatePrivacySettings(privacy.copy(readReceipts = it))
                    }
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    PrivacyToggleRow("Show Distance", "Display approximate miles distance", privacy.showDistance) {
                        profileViewModel.updatePrivacySettings(privacy.copy(showDistance = it))
                    }
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    PrivacyToggleRow("Typing Indicators", "Show when you are typing a response", privacy.typingIndicator) {
                        profileViewModel.updatePrivacySettings(privacy.copy(typingIndicator = it))
                    }
                }
            }
        }

        // Emergency Helplines
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Global Safety & Emergency Support 🆘", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFF2D55).copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF2D55).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("24/7 Human Trust & Safety Support", fontWeight = FontWeight.Bold, color = Color(0xFFFF2D55), style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Our dedicated safety team reviews reports within 15 minutes. For immediate emergencies, always contact your local emergency services (e.g., 911, 112, 15, 110).", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Verification Dialog Modal
    if (showVerificationDialog != null) {
        val type = showVerificationDialog!!
        AlertDialog(
            onDismissRequest = { showVerificationDialog = null },
            title = {
                Text(
                    text = when (type) {
                        "PHONE" -> "Phone Verification 📱"
                        "EMAIL" -> "Email Verification ✉️"
                        "SELFIE" -> "Photo Liveness Verification 📸"
                        else -> "Government ID Verification 🪪"
                    }
                )
            },
            text = {
                Column {
                    Text(
                        text = when (type) {
                            "PHONE" -> "Your phone number is verified and linked to prevent automated spam accounts."
                            "EMAIL" -> "Your email address is confirmed for account recovery and safety alerts."
                            "SELFIE" -> "Complete our 3-second selfie pose challenge to receive your blue verified badge! ✨"
                            else -> "Submit an optional photo of your national ID/passport to get an exclusive Identity Verified badge."
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (type == "SELFIE") {
                            profileViewModel.toggleVerification(true)
                            successToast = "Photo Liveness Verified! Blue badge activated! 🛡️"
                        } else {
                            successToast = "Verification status updated successfully! ✓"
                        }
                        showVerificationDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55))
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerificationDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    successToast?.let { msg ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFF00D2FF),
                contentColor = Color.Black,
                action = {
                    TextButton(onClick = { successToast = null }) {
                        Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                Text(msg, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun VerificationRowItem(title: String, subtitle: String, isVerified: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.05f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isVerified) Color(0xFF00E676).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)
            ) {
                Text(
                    text = if (isVerified) "Verified ✓" else "Verify Now",
                    color = if (isVerified) Color(0xFF00E676) else Color(0xFFFF8C42),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ScamTipItem(title: String, description: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
private fun PrivacyToggleRow(title: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFF2D55)
            )
        )
    }
}
