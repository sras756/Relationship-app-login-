package com.example.ui.match

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ConnectionRequest
import com.example.data.model.UserProfile
import com.example.ui.theme.PrimaryGradient
import com.example.ui.wallet.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesAndRequestsScreen(
    walletViewModel: WalletViewModel,
    onStartConversation: (UserProfile) -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val isMasterMember by walletViewModel.isMasterMember.collectAsState()
    val boostRemainingSeconds by walletViewModel.boostRemainingSeconds.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Requests & Likes, 1: Mutual Matches
    var actionToast by remember { mutableStateOf<String?>(null) }

    val mutualMatches = remember {
        listOf(
            UserProfile(
                uid = "cand_1",
                displayName = "Sophia Chen",
                age = 25,
                gender = "Female",
                country = "USA",
                nationality = "United States 🇺🇸",
                city = "San Francisco, CA",
                bio = "Coffee lover and weekend hiker ☕🌲",
                primaryPhotoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80",
                isVerified = true,
                isMasterMember = true
            ),
            UserProfile(
                uid = "cand_2",
                displayName = "Elena Rostova",
                age = 24,
                gender = "Female",
                country = "Russia",
                nationality = "Russia 🇷🇺",
                city = "Moscow / NY",
                bio = "Classical pianist and modern art lover ✨",
                primaryPhotoUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=600&q=80",
                isVerified = true
            ),
            UserProfile(
                uid = "cand_4",
                displayName = "Maya Patel",
                age = 26,
                gender = "Female",
                country = "USA",
                nationality = "India 🇮🇳 / USA 🇺🇸",
                city = "Austin, TX",
                bio = "Yoga teacher and documentary enthusiast 🧘🌮",
                primaryPhotoUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=600&q=80",
                isVerified = true
            )
        )
    }

    var connectionRequests by remember {
        mutableStateOf(
            listOf(
                ConnectionRequest(
                    id = "req_1",
                    senderProfile = UserProfile(
                        uid = "user_ayesha",
                        displayName = "Ayesha Malik",
                        age = 24,
                        gender = "Female",
                        country = "Pakistan",
                        nationality = "Pakistan 🇵🇰",
                        city = "Lahore, Pakistan",
                        primaryPhotoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80",
                        isVerified = true,
                        relationshipGoal = "Marriage / Long-Term 💍"
                    ),
                    introNote = "“Hi! I really loved your bio and travel interests. Would love to connect and share cultural stories! ✨”",
                    isSuperLike = true
                ),
                ConnectionRequest(
                    id = "req_2",
                    senderProfile = UserProfile(
                        uid = "user_jiwoo",
                        displayName = "Ji-woo Park",
                        age = 23,
                        gender = "Female",
                        country = "South Korea",
                        nationality = "South Korea 🇰🇷",
                        city = "Seoul, South Korea",
                        primaryPhotoUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=600&q=80",
                        isVerified = true,
                        relationshipGoal = "International Dating 💖"
                    ),
                    introNote = "“Hello from Seoul! Looking forward to learning more about you! 🌸”",
                    isSuperLike = false
                )
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Profile Boost Status Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚀", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Profile Boost Status", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (boostRemainingSeconds > 0) "Active: ${boostRemainingSeconds / 60}m ${boostRemainingSeconds % 60}s left" else "Boost profile for 5x more views",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (boostRemainingSeconds > 0) Color(0xFF00E676) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                    if (boostRemainingSeconds == 0) {
                        Button(
                            onClick = {
                                val ok = walletViewModel.startProfileBoost(30)
                                actionToast = if (ok) "Boost Activated for 30 minutes! 🚀" else "Need 80 points to boost."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Boost (80p)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sub Tabs Row
        item {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Requests & Likes (${connectionRequests.size + 4})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("Mutual Matches (${mutualMatches.size})", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedSubTab == 0) {
            // Who Liked You Section
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Who Liked You 👀", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFFD700).copy(alpha = 0.2f)
                                    ) {
                                        Text("4 New", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text("Singles who swiped right on your profile", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                            }

                            if (!isMasterMember) {
                                Button(
                                    onClick = onNavigateToPremium,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Reveal 👑", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val likedUsers = listOf(
                                Triple("Mei Lin", "24 • China 🇨🇳", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80"),
                                Triple("Zara", "25 • Pakistan 🇵🇰", "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=600&q=80"),
                                Triple("Chloe", "23 • France 🇫🇷", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=600&q=80"),
                                Triple("Hana", "24 • Japan 🇯🇵", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=600&q=80")
                            )
                            items(likedUsers) { user ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.05f),
                                    modifier = Modifier
                                        .width(110.dp)
                                        .clickable {
                                            if (!isMasterMember) {
                                                onNavigateToPremium()
                                            } else {
                                                actionToast = "Opening ${user.first}'s profile ✨"
                                            }
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(CircleShape)
                                        ) {
                                            AsyncImage(
                                                model = user.third,
                                                contentDescription = user.first,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .then(if (!isMasterMember) Modifier.blur(14.dp) else Modifier),
                                                contentScale = ContentScale.Crop
                                            )
                                            if (!isMasterMember) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.35f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(if (isMasterMember) user.first else "Secret Admirer", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(user.second, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Connection Requests with Personal Notes
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Connection Requests with Intro Notes 💌", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (connectionRequests.isEmpty()) {
                item {
                    Text("No pending connection requests.", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                }
            } else {
                items(connectionRequests, key = { it.id }) { req ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (req.isSuperLike) Color(0xFF00D2FF).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = req.senderProfile.primaryPhotoUrl,
                                    contentDescription = req.senderProfile.displayName,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${req.senderProfile.displayName}, ${req.senderProfile.age}", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                        if (req.isSuperLike) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF00D2FF).copy(alpha = 0.2f)
                                            ) {
                                                Text("⭐ SUPER LIKE", color = Color(0xFF00D2FF), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Text("📍 ${req.senderProfile.nationality} • ${req.senderProfile.relationshipGoal}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = req.introNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        connectionRequests = connectionRequests.filter { it.id != req.id }
                                        actionToast = "Declined request."
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Decline", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Button(
                                    onClick = {
                                        connectionRequests = connectionRequests.filter { it.id != req.id }
                                        onStartConversation(req.senderProfile)
                                        actionToast = "Matched with ${req.senderProfile.displayName}! 🎉"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Accept & Chat 💕", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Mutual Matches Tab
            items(mutualMatches.chunked(2)) { rowMatches ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowMatches.forEach { matchUser ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                                .clickable { onStartConversation(matchUser) }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = matchUser.primaryPhotoUrl,
                                    contentDescription = matchUser.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))))
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${matchUser.displayName}, ${matchUser.age}", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                        if (matchUser.isVerified) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF00D2FF), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Text(matchUser.nationality, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFF2D55),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Chat Now 💬", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    actionToast?.let { msg ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFFFF2D55),
                action = {
                    TextButton(onClick = { actionToast = null }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) {
                Text(msg, color = Color.White)
            }
        }
    }
}
