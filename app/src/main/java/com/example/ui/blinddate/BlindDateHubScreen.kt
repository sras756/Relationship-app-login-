package com.example.ui.blinddate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.firebase.FirestoreBlindDateManager
import com.example.data.model.UserProfile
import com.example.ui.wallet.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlindDateHubScreen(
    userProfile: UserProfile,
    blindDateViewModel: BlindDateViewModel,
    walletViewModel: WalletViewModel,
    allCandidates: List<UserProfile>,
    onBack: () -> Unit,
    onNavigateToChat: (UserProfile) -> Unit,
    onNavigateToProfileSetup: () -> Unit
) {
    val screenState by blindDateViewModel.screenState.collectAsState()
    val preferences by blindDateViewModel.preferences.collectAsState()
    val activeSession by blindDateViewModel.activeSession.collectAsState()
    val matchedPartner by blindDateViewModel.matchedPartner.collectAsState()
    val messages by blindDateViewModel.messages.collectAsState()
    val remainingSeconds by blindDateViewModel.remainingSeconds.collectAsState()
    val currentIcebreaker by blindDateViewModel.currentIcebreaker.collectAsState()
    val showRevealDialog by blindDateViewModel.showRevealDialog.collectAsState()
    val showHowItWorks by blindDateViewModel.showHowItWorks.collectAsState()
    val showSafetyInfo by blindDateViewModel.showSafetyInfo.collectAsState()
    val showPreferencesSheet by blindDateViewModel.showPreferencesSheet.collectAsState()
    val searchErrorMessage by blindDateViewModel.searchErrorMessage.collectAsState()
    val historyList by blindDateViewModel.history.collectAsState()
    val snackbarMessage by blindDateViewModel.snackbarMessage.collectAsState()

    val pointsBalance by walletViewModel.pointsBalance.collectAsState()
    val isMasterMember by walletViewModel.isMasterMember.collectAsState()
    val eligibility = remember(userProfile) { FirestoreBlindDateManager.checkEligibility(userProfile) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            blindDateViewModel.clearSnackbar()
        }
    }

    when (screenState) {
        BlindDateScreenState.MATCHMAKING -> {
            BlindDateMatchingScreen(
                errorMessage = searchErrorMessage,
                onCancel = { blindDateViewModel.backToHub() },
                onOpenPreferences = {
                    blindDateViewModel.backToHub()
                    blindDateViewModel.setShowPreferencesSheet(true)
                },
                onRetry = {
                    blindDateViewModel.startBlindDate(userProfile, allCandidates, walletViewModel)
                }
            )
        }
        BlindDateScreenState.ACTIVE_DATE -> {
            val session = activeSession
            val partner = matchedPartner
            if (session != null && partner != null) {
                BlindDateChatScreen(
                    session = session,
                    partnerProfile = partner,
                    messages = messages,
                    remainingSeconds = remainingSeconds,
                    currentIcebreaker = currentIcebreaker,
                    showRevealDialog = showRevealDialog,
                    walletViewModel = walletViewModel,
                    onSendMessage = { content, type ->
                        blindDateViewModel.sendMessage(content, type, "", 0, walletViewModel)
                    },
                    onNextIcebreaker = { blindDateViewModel.nextIcebreaker() },
                    onRevealChoice = { consent ->
                        blindDateViewModel.submitRevealConsent(consent, walletViewModel)
                    },
                    onEndSessionEarly = { reason ->
                        blindDateViewModel.endDateEarly(reason)
                    },
                    onBlockOrReport = { reason, isBlock ->
                        blindDateViewModel.blockOrReportPartner(reason, isBlock)
                    }
                )
            }
        }
        BlindDateScreenState.MUTUAL_CELEBRATION -> {
            val session = activeSession
            val partner = matchedPartner
            if (session != null && partner != null) {
                BlindDateCelebrationScreen(
                    session = session,
                    partnerProfile = partner,
                    onContinueToChat = {
                        blindDateViewModel.backToHub()
                        onNavigateToChat(partner)
                    },
                    onBackToHub = { blindDateViewModel.backToHub() }
                )
            }
        }
        BlindDateScreenState.HISTORY -> {
            BlindDateHistoryScreen(
                historyItems = historyList,
                onBack = { blindDateViewModel.backToHub() }
            )
        }
        BlindDateScreenState.HUB -> {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "BLIND DATE",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("💘", fontSize = 14.sp)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = { blindDateViewModel.openHistory() }) {
                                Icon(Icons.Default.History, contentDescription = "History", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Hero Banner Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFFFF2D55),
                                                Color(0xFF8A2387),
                                                Color(0xFF2C1E40)
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Black.copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = "🎯 1 Free Date Daily",
                                            color = Color(0xFFFFD700),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "💘 Blind Date",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "“Meet someone new without seeing their profile first.”",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // 2. Eligibility & Verification Banner
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (eligibility.isEligible) Color(0xFF19281E) else Color(0xFF2B1C22)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (eligibility.isEligible) Color(0xFF00E676).copy(alpha = 0.4f) else Color(0xFFFF2D55).copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (eligibility.isEligible) Icons.Default.VerifiedUser else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (eligibility.isEligible) Color(0xFF00E676) else Color(0xFFFF2D55),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (eligibility.isEligible) "Account Verified & Eligible ✅" else "Action Required For Blind Date",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (eligibility.isEligible) "100% Real, Verified Singles Only" else eligibility.reasons.firstOrNull() ?: "Complete profile verification",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                if (!eligibility.isEligible) {
                                    TextButton(onClick = onNavigateToProfileSetup) {
                                        Text("Complete", color = Color(0xFFFF2D55), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 3. Primary CTA: Start Blind Date
                    item {
                        Button(
                            onClick = {
                                if (eligibility.isEligible) {
                                    blindDateViewModel.startBlindDate(userProfile, allCandidates, walletViewModel)
                                } else {
                                    blindDateViewModel.updatePreferences(preferences) // trigger snackbar
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (eligibility.isEligible) Color(0xFFFF2D55) else Color.Gray
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text("💘", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Blind Date",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // 4. Quick Actions Grid
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Row 1: How it works & Safety
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // How Blind Date Works
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { blindDateViewModel.setShowHowItWorks(true) },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("ℹ️", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("How It Works", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text("Step-by-step guide", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }

                                // Safety Information
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { blindDateViewModel.setShowSafetyInfo(true) },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("🛡️", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Safety Info", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text("Privacy & protection", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Row 2: Preferences
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { blindDateViewModel.setShowPreferencesSheet(true) },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⚙️", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Blind Date Preferences", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                            Text(
                                                text = "${preferences.interestedIn.joinToString()} • ${preferences.minAge}-${preferences.maxAge} yrs • ${preferences.selectedCountries.firstOrNull() ?: "All"}",
                                                color = Color(0xFFFF85A1),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // How Blind Date Works Dialog
    if (showHowItWorks) {
        AlertDialog(
            onDismissRequest = { blindDateViewModel.setShowHowItWorks(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ℹ️ How Blind Date Works", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1. 🎭 Anonymous First: You start connected in a temporary private chat without photos or full names.", color = Color.LightGray, fontSize = 13.sp)
                    Text("2. ⏱️ 10-Minute Timer: Enjoy meaningful conversation using interactive icebreakers.", color = Color.LightGray, fontSize = 13.sp)
                    Text("3. 🤝 Mutual Reveal: When time is up, choose whether to reveal your profiles. Profiles are only unlocked if BOTH users agree!", color = Color.LightGray, fontSize = 13.sp)
                    Text("4. 🛡️ 100% Real Users: No bots or AI profiles. Every blind date is with a real verified person.", color = Color(0xFF00E676), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { blindDateViewModel.setShowHowItWorks(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55))
                ) {
                    Text("Got It!")
                }
            }
        )
    }

    // Safety Information Dialog
    if (showSafetyInfo) {
        AlertDialog(
            onDismissRequest = { blindDateViewModel.setShowSafetyInfo(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️ Safety & Privacy", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("• Zero Private Data Exposure: Your phone, email, and exact location are NEVER shared.", color = Color.LightGray, fontSize = 13.sp)
                    Text("• Instant Block & Report: Available from the top corner at all times during the date.", color = Color.LightGray, fontSize = 13.sp)
                    Text("• Anti-Scam Protection: Automated detection protects against financial requests and spam.", color = Color.LightGray, fontSize = 13.sp)
                    Text("• Verified Only: Only verified profiles can participate in Blind Dates.", color = Color(0xFF00D2FF), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { blindDateViewModel.setShowSafetyInfo(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55))
                ) {
                    Text("Understood")
                }
            }
        )
    }

    // Preferences Dialog
    if (showPreferencesSheet) {
        BlindDatePreferencesDialog(
            currentPreferences = preferences,
            onSavePreferences = { newPrefs ->
                blindDateViewModel.updatePreferences(newPrefs)
            },
            onDismiss = { blindDateViewModel.setShowPreferencesSheet(false) }
        )
    }
}
