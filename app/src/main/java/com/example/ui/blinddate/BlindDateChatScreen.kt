package com.example.ui.blinddate

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.firebase.FirestoreManager
import com.example.data.model.*
import com.example.ui.wallet.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BlindDateChatScreen(
    session: BlindDateSession,
    partnerProfile: UserProfile,
    messages: List<ChatMessage>,
    remainingSeconds: Int,
    currentIcebreaker: String,
    showRevealDialog: Boolean,
    walletViewModel: WalletViewModel,
    onSendMessage: (String, MessageType) -> Unit,
    onNextIcebreaker: () -> Unit,
    onRevealChoice: (Boolean) -> Unit,
    onEndSessionEarly: (String) -> Unit,
    onBlockOrReport: (String, Boolean) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var showSafetyMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }
    var showCallConfirmation by remember { mutableStateOf<String?>(null) } // "Voice" or "Video"
    var showSafeInfoCard by remember { mutableStateOf(true) }

    val currentUid = FirestoreManager.currentUser?.uid ?: "current_user"
    val isMasterMember by walletViewModel.isMasterMember.collectAsState()
    val pointsBalance by walletViewModel.pointsBalance.collectAsState()

    // Format countdown MM:SS
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timerText = String.format("%02d:%02d", minutes, seconds)
    val isTimeLow = remainingSeconds < 120

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Silhouette Avatar & Anonymous Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF2E1C4A),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF2D55)),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFFFF85A1),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Mystery Match 💫",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "Private Blind Date",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFF85A1),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Right: Countdown & Safety Controls Menu
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Real-time Timer Badge
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isTimeLow) Color(0xFFFF2D55).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isTimeLow) Color(0xFFFF2D55) else Color(0xFFFFD700)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = if (isTimeLow) Color(0xFFFF2D55) else Color(0xFFFFD700),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = timerText,
                                        color = if (isTimeLow) Color(0xFFFF2D55) else Color(0xFFFFD700),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Safety Menu
                            Box {
                                IconButton(onClick = { showSafetyMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Safety Controls",
                                        tint = Color.White
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSafetyMenu,
                                    onDismissRequest = { showSafetyMenu = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("🚪 End Blind Date", color = Color.White) },
                                        onClick = {
                                            showSafetyMenu = false
                                            onEndSessionEarly("User ended early")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("⚠️ Report User", color = Color(0xFFFF9800)) },
                                        onClick = {
                                            showSafetyMenu = false
                                            showReportDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🚫 Block User", color = Color(0xFFFF2D55)) },
                                        onClick = {
                                            showSafetyMenu = false
                                            onBlockOrReport("Blocked during blind date", true)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Compatibility Bar & Safe Info Accordion Toggle
                    Surface(
                        color = Color.Black.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Real Calculated Compatibility Badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Compatibility: ${session.compatibilityPercentage}%",
                                    color = Color(0xFF00E676),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            // Safe Info Toggle
                            Row(
                                modifier = Modifier.clickable { showSafeInfoCard = !showSafeInfoCard },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (showSafeInfoCard) "Hide Hints ▲" else "Mystery Hints ▼",
                                    color = Color(0xFF00D2FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Expandable Mystery Profile Hints Card
            AnimatedVisibility(visible = showSafeInfoCard) {
                Surface(
                    color = Color(0xFF1E1430),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("👤 Age Range: ${session.maskedPartnerAge}", fontSize = 11.sp, color = Color.LightGray)
                            Text("📍 Region: ${session.maskedPartnerCountry}", fontSize = 11.sp, color = Color.LightGray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🗣️ Languages: ${session.maskedPartnerLanguages.joinToString(", ")}", fontSize = 11.sp, color = Color.LightGray)
                            Text("🎯 Goal: ${session.maskedPartnerGoal}", fontSize = 11.sp, color = Color(0xFFFF85A1))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✨ Interests: ${session.maskedPartnerInterests.joinToString(" • ")}",
                            fontSize = 11.sp,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }

            // Icebreaker Banner
            Surface(
                color = Color(0xFF291A42),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                inputText = currentIcebreaker
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentIcebreaker,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = onNextIcebreaker,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("🎲 New", color = Color(0xFF00D2FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Chat Messages Stream
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    if (msg.messageType == MessageType.MATCH_EVENT) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = msg.messageContent,
                                color = Color(0xFFFF85A1),
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        val isMe = msg.senderId == currentUid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                ),
                                color = if (isMe) Color(0xFFFF2D55) else Color(0xFF2B213A),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        text = msg.messageContent,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Actions & Input Field Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    // Call & Media Quick Bar (With Points Cost Badges)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Voice Call Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.clickable { showCallConfirmation = "Voice Call" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Voice Call", tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isMasterMember) "Free" else "60 pts", fontSize = 10.sp, color = Color.LightGray)
                                }
                            }

                            // Video Call Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.clickable { showCallConfirmation = "Video Call" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color(0xFF00D2FF), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isMasterMember) "Free" else "60 pts", fontSize = 10.sp, color = Color.LightGray)
                                }
                            }
                        }

                        // Points Balance Pill
                        Text(
                            text = if (isMasterMember) "👑 VIP Active" else "💎 $pointsBalance pts",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Text Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = if (isMasterMember) "Type message (Free VIP)…" else "Type message (15 pts)…",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF2D55),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Button
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    onSendMessage(inputText.trim(), MessageType.TEXT)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    if (inputText.isNotBlank()) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Call Confirmation Dialog
        if (showCallConfirmation != null) {
            val callType = showCallConfirmation ?: "Voice Call"
            AlertDialog(
                onDismissRequest = { showCallConfirmation = null },
                title = { Text("Start $callType?") },
                text = {
                    Text(
                        if (isMasterMember) "As a Master Member, calls are free of charge. Your anonymous identity remains protected."
                        else "Starting this call costs 60 points. Your profile and phone number remain completely anonymous."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCallConfirmation = null
                            if (!isMasterMember) {
                                walletViewModel.spendPoints(60, "Blind Date $callType")
                            }
                            onSendMessage("📞 Attempted $callType with Mystery Match", MessageType.MATCH_EVENT)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Text("Start Call")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCallConfirmation = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Report Dialog
        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                title = { Text("Report Mystery Match", color = Color.White) },
                text = {
                    Column {
                        Text("Select a reason to report this user. Active session will end immediately.", color = Color.LightGray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf("Harassment / Abuse", "Financial Scam / Money Request", "Suspicious Links / Spam", "Inappropriate Behavior").forEach { reason ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (reportReason == reason) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { reportReason = reason }
                            ) {
                                Text(
                                    text = reason,
                                    modifier = Modifier.padding(10.dp),
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showReportDialog = false
                            onBlockOrReport(reportReason.ifBlank { "Inappropriate behavior" }, false)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55))
                    ) {
                        Text("Submit Report")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Mutual Profile Reveal Dialog
        if (showRevealDialog) {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💘", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your Blind Date is Over",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Would you like to reveal your real profiles to each other? Profiles are only shown if BOTH of you agree.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Reveal Profile Button
                        Button(
                            onClick = { onRevealChoice(true) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55))
                        ) {
                            Text("❤️ Reveal My Profile", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Not Interested Button
                        OutlinedButton(
                            onClick = { onRevealChoice(false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("❌ Not Interested", color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}
