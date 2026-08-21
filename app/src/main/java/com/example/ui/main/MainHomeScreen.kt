package com.example.ui.main

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.firebase.FirestoreManager
import com.example.data.firebase.FirestoreMatchManager
import com.example.data.model.*
import kotlinx.coroutines.launch
import com.example.ui.blinddate.*
import com.example.ui.chat.*
import com.example.ui.community.CommunityRoomsScreen
import com.example.ui.discovery.DiscoveryPreferenceSheet
import com.example.ui.discovery.DiscoveryViewModel
import com.example.ui.discovery.SwipeableProfileCard
import com.example.ui.match.MatchCelebrationDialog
import com.example.ui.match.MatchesAndRequestsScreen
import com.example.ui.notifications.NotificationsScreen
import com.example.ui.premium.PremiumSubscriptionScreen
import com.example.ui.profile.ProfileViewModel
import com.example.ui.safety.SafetyCenterScreen
import com.example.ui.theme.PrimaryGradient
import com.example.ui.wallet.WalletScreen
import com.example.ui.wallet.WalletViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    walletViewModel: WalletViewModel = viewModel(),
    blindDateViewModel: BlindDateViewModel = viewModel(),
    onEditProfileRequested: () -> Unit
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val conversations by chatViewModel.conversations.collectAsState()
    val activeConversation by chatViewModel.activeConversation.collectAsState()
    val messagesMap by chatViewModel.messages.collectAsState()
    val partnerTypingMap by chatViewModel.isPartnerTyping.collectAsState()
    val searchQuery by chatViewModel.searchQuery.collectAsState()
    val activeCallState by chatViewModel.activeCallState.collectAsState()

    val pointsBalance by walletViewModel.pointsBalance.collectAsState()
    val isMasterMember by walletViewModel.isMasterMember.collectAsState()
    val boostSeconds by walletViewModel.boostRemainingSeconds.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Discover, 1: Community, 2: Chats, 3: Matches, 4: Wallet, 5: Profile
    var showDiscoveryFilterSheet by remember { mutableStateOf(false) }
    var showPremiumScreen by remember { mutableStateOf(false) }
    var showSafetyScreen by remember { mutableStateOf(false) }
    var showBlindDateScreen by remember { mutableStateOf(false) }
    var matchedPartnerForCelebration by remember { mutableStateOf<UserProfile?>(null) }
    var actionSnackbarMessage by remember { mutableStateOf<String?>(null) }
    var superLikeDialogPartner by remember { mutableStateOf<UserProfile?>(null) }
    var superLikeIntroNote by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // International Recommendation Candidates Database
    val allCandidates = remember {
        listOf(
            UserProfile(
                uid = "cand_1",
                displayName = "Sophia Chen",
                age = 25,
                gender = "Female",
                country = "United States",
                nationality = "United States 🇺🇸",
                secondNationality = "China 🇨🇳",
                city = "San Francisco, CA",
                bio = "Coffee lover, weekend hiker, and architecture enthusiast. Looking for someone to share cozy conversations and explore scenic trails! ☕🌲",
                primaryPhotoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80",
                photoUrls = listOf("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80"),
                isVerified = true,
                isMasterMember = true,
                relationshipGoal = "Long-term connection 💖",
                interests = listOf("☕ Coffee", "📸 Photography", "✈️ Travel", "🌲 Hiking"),
                languages = listOf("English", "Mandarin"),
                promptAnswers = mapOf("I get overly excited about" to "Finding hidden specialty coffee shops in new cities!")
            ),
            UserProfile(
                uid = "cand_2",
                displayName = "Ayesha Malik",
                age = 24,
                gender = "Female",
                country = "Pakistan",
                nationality = "Pakistan 🇵🇰",
                city = "Lahore, Pakistan",
                bio = "Literature graduate, passionate about cultural heritage, poetry, and classical music. Seeking genuine marriage-minded companionship! 🌸📚",
                primaryPhotoUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=600&q=80",
                photoUrls = listOf("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=600&q=80"),
                isVerified = true,
                isMasterMember = false,
                relationshipGoal = "Marriage / Long-term 💍",
                interests = listOf("📚 Literature", "☕ Chai", "🏛️ Heritage", "✈️ Travel"),
                languages = listOf("Urdu", "English"),
                promptAnswers = mapOf("The key to my heart is" to "Thoughtful conversations over warm chai.")
            ),
            UserProfile(
                uid = "cand_3",
                displayName = "Ji-woo Park",
                age = 23,
                gender = "Female",
                country = "South Korea",
                nationality = "South Korea 🇰🇷",
                city = "Seoul, South Korea",
                bio = "Graphic designer and café hopper in Hongdae. Love K-indie music, street photography, and cozy rainy day chats! 🎨☕",
                primaryPhotoUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=600&q=80",
                photoUrls = listOf("https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=600&q=80"),
                isVerified = true,
                isMasterMember = true,
                relationshipGoal = "International Dating 💖",
                interests = listOf("🎨 Design", "🎵 K-Indie", "🍜 Foodie", "📸 Photography"),
                languages = listOf("Korean", "English"),
                promptAnswers = mapOf("My favorite date" to "Exploring local art galleries and finding hidden street food spots.")
            ),
            UserProfile(
                uid = "cand_4",
                displayName = "Lucas Zhang",
                age = 27,
                gender = "Male",
                country = "China",
                nationality = "China 🇨🇳",
                city = "Shanghai, China",
                bio = "Tech entrepreneur and amateur chef. Love badminton, international road trips, and learning new languages! 🏸🍳",
                primaryPhotoUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=600&q=80",
                photoUrls = listOf("https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=600&q=80"),
                isVerified = true,
                isMasterMember = false,
                relationshipGoal = "Long-term connection 💖",
                interests = listOf("💻 Tech", "🍳 Cooking", "🏸 Badminton", "✈️ Travel"),
                languages = listOf("Mandarin", "English"),
                promptAnswers = mapOf("My simple pleasures" to "Cooking authentic handmade dumplings for friends.")
            ),
            UserProfile(
                uid = "cand_5",
                displayName = "Elena Rostova",
                age = 24,
                gender = "Female",
                country = "Russia",
                nationality = "Russia 🇷🇺",
                city = "Moscow, Russia",
                bio = "Classical pianist and modern art curator. Always searching for beautiful architecture and heartfelt conversations. 🎹✨",
                primaryPhotoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=600&q=80",
                photoUrls = listOf("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=600&q=80"),
                isVerified = true,
                isMasterMember = true,
                relationshipGoal = "Long-term connection 💖",
                interests = listOf("🎹 Piano", "🎨 Art", "🍵 Matcha", "🌅 Sunsets"),
                languages = listOf("Russian", "English", "French"),
                promptAnswers = mapOf("The key to my heart is" to "Classical piano music and deep, sincere honesty.")
            ),
            UserProfile(
                uid = "cand_6",
                displayName = "David Vance",
                age = 28,
                gender = "Male",
                country = "United States",
                nationality = "United States 🇺🇸",
                city = "Chicago, IL",
                bio = "Architect and dog dad 🐕. Love live concerts, rooftop bars, and weekend road trips along the coast.",
                primaryPhotoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=600&q=80",
                photoUrls = listOf("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=600&q=80"),
                isVerified = true,
                isMasterMember = false,
                relationshipGoal = "Long-term connection 💖",
                interests = listOf("🐕 Dogs", "🏛️ Architecture", "🎸 Rock", "🍷 Wine"),
                languages = listOf("English"),
                promptAnswers = mapOf("A non-negotiable for me is" to "Being kind to service staff and loving animals.")
            )
        )
    }

    // Filter Candidates by User Discovery Preferences
    val userPreferences = userProfile.discoveryPreferences
    val filteredCandidates = remember(userPreferences, userProfile.interestedIn, userProfile.blockedUserIds) {
        allCandidates.filter { candidate ->
            val genderMatch = userProfile.interestedIn.isEmpty() || userProfile.interestedIn.contains(candidate.gender)
            val ageMatch = candidate.age in userPreferences.minAge..userPreferences.maxAge
            val notBlocked = !userProfile.blockedUserIds.contains(candidate.uid)
            val verifiedMatch = !userPreferences.verifiedOnly || candidate.isVerified
            val countryMatch = userPreferences.selectedCountries.contains("All") ||
                    userPreferences.selectedCountries.any { candidate.nationality.contains(it.replace("All", "").trim()) }
            genderMatch && ageMatch && notBlocked && verifiedMatch && countryMatch
        }
    }

    var currentCandidateIndex by remember { mutableIntStateOf(0) }
    val totalUnreadMessages = conversations.sumOf { it.unreadCount }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showBlindDateScreen) {
            BlindDateHubScreen(
                userProfile = userProfile,
                blindDateViewModel = blindDateViewModel,
                walletViewModel = walletViewModel,
                allCandidates = allCandidates,
                onBack = { showBlindDateScreen = false },
                onNavigateToChat = { partner ->
                    showBlindDateScreen = false
                    val conv = chatViewModel.startConversationWithUser(partner)
                    chatViewModel.openConversation(conv)
                    selectedTab = 2
                },
                onNavigateToProfileSetup = {
                    showBlindDateScreen = false
                    selectedTab = 5
                }
            )
        } else if (showPremiumScreen) {
            PremiumSubscriptionScreen(
                walletViewModel = walletViewModel,
                onBack = { showPremiumScreen = false }
            )
        } else if (showSafetyScreen) {
            SafetyCenterScreen(
                profileViewModel = profileViewModel,
                onBack = { showSafetyScreen = false }
            )
        } else {
            Scaffold(
                topBar = {
                    if (selectedTab != 2 || activeConversation == null) {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "MATCH & CONNECT",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("💖", fontSize = 14.sp)
                                }
                            },
                            actions = {
                                // Points Pill & Master Crown
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isMasterMember) Color(0xFFFFD700).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isMasterMember) Color(0xFFFFD700) else Color(0xFFFF2D55)),
                                    modifier = Modifier.clickable { selectedTab = 4 } // Go to Wallet
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isMasterMember) {
                                            Text("👑 VIP", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        } else {
                                            Text("💎 $pointsBalance pts", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Safety & Verification Shield Button
                                IconButton(onClick = { showSafetyScreen = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Safety & Verification",
                                        tint = Color(0xFF00D2FF)
                                    )
                                }

                                if (selectedTab == 0) {
                                    IconButton(onClick = { showDiscoveryFilterSheet = true }) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "Preferences",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                        )
                    }
                },
                bottomBar = {
                    if (activeConversation == null) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Favorite, contentDescription = "Discover") },
                                label = { Text("Discover", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF2D55))
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.Groups, contentDescription = "Community") },
                                label = { Text("Community", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF2D55))
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    if (totalUnreadMessages > 0) {
                                        BadgedBox(badge = { Badge { Text("$totalUnreadMessages") } }) {
                                            Icon(Icons.Default.Forum, contentDescription = "Chats")
                                        }
                                    } else {
                                        Icon(Icons.Default.Forum, contentDescription = "Chats")
                                    }
                                },
                                label = { Text("Chats", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF2D55))
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Matches") },
                                label = { Text("Matches", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF2D55))
                            )
                            NavigationBarItem(
                                selected = selectedTab == 4,
                                onClick = { selectedTab = 4 },
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                                label = { Text("Wallet", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF2D55))
                            )
                            NavigationBarItem(
                                selected = selectedTab == 5,
                                onClick = { selectedTab = 5 },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFF2D55))
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (activeConversation != null && selectedTab == 2) PaddingValues(0.dp) else paddingValues)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // DISCOVER TAB WITH SWIPEABLE PROFILE CARDS & FIRESTORE MATCHING
                            val candidateList = if (filteredCandidates.isNotEmpty()) filteredCandidates else allCandidates
                            val isDeckEmpty = currentCandidateIndex >= candidateList.size
                            val currentCandidate = candidateList.getOrNull(currentCandidateIndex)
                            val nextCandidate = candidateList.getOrNull(currentCandidateIndex + 1)
                            val coroutineScope = rememberCoroutineScope()

                            val onSwipeAction: (UserProfile, SwipeDirection, String) -> Unit = { targetUser, direction, note ->
                                val currentUid = FirestoreManager.currentUser?.uid ?: "current_user"
                                val action = when (direction) {
                                    SwipeDirection.RIGHT -> InteractionType.LIKE
                                    SwipeDirection.LEFT -> InteractionType.DISLIKE
                                    SwipeDirection.UP -> InteractionType.SUPER_LIKE
                                }

                                actionSnackbarMessage = when (direction) {
                                    SwipeDirection.RIGHT -> "Liked ${targetUser.displayName} 💖"
                                    SwipeDirection.LEFT -> "Passed on ${targetUser.displayName}"
                                    SwipeDirection.UP -> "Super Liked ${targetUser.displayName}! ⭐"
                                }
                                currentCandidateIndex++

                                coroutineScope.launch {
                                    val isMutual = FirestoreMatchManager.recordSwipeInteraction(
                                        fromUserId = currentUid,
                                        targetProfile = targetUser,
                                        action = action,
                                        introNote = note
                                    ).getOrDefault(false)

                                    if (isMutual || (action == InteractionType.LIKE && (targetUser.uid == "cand_1" || targetUser.uid == "cand_3" || targetUser.uid == "cand_5"))) {
                                        matchedPartnerForCelebration = targetUser
                                    }
                                }
                            }

                            if (!isDeckEmpty && currentCandidate != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Prominent 💘 Blind Date Option on Discover Page
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp)
                                            .clickable { showBlindDateScreen = true },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.Transparent
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(
                                                            Color(0xFFFF2D55),
                                                            Color(0xFF8A2387),
                                                            Color(0xFF2C1E40)
                                                        )
                                                    )
                                                )
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color.Black.copy(alpha = 0.25f),
                                                        modifier = Modifier.size(38.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text("💘", fontSize = 18.sp)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = "Blind Date",
                                                                fontWeight = FontWeight.Black,
                                                                color = Color.White,
                                                                fontSize = 14.sp
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = Color(0xFFFFD700).copy(alpha = 0.25f)
                                                            ) {
                                                                Text(
                                                                    text = "✨ 1 Free Daily",
                                                                    color = Color(0xFFFFD700),
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = "Meet someone new without seeing their profile first.",
                                                            color = Color.White.copy(alpha = 0.85f),
                                                            fontSize = 11.sp,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                                            }
                                        }
                                    }

                                    // Interactive Swipeable Card Deck
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    ) {
                                        SwipeableProfileCard(
                                            profile = currentCandidate,
                                            nextProfile = nextCandidate,
                                            onSwiped = { profile, direction ->
                                                onSwipeAction(profile, direction, "")
                                            },
                                            onSuperLikeRequested = { profile ->
                                                superLikeDialogPartner = profile
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Action Buttons Bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Rewind Button
                                        IconButton(
                                            onClick = {
                                                if (currentCandidateIndex > 0) {
                                                    currentCandidateIndex--
                                                    actionSnackbarMessage = "Rewound to previous profile 🔄"
                                                } else {
                                                    actionSnackbarMessage = "Already at the beginning"
                                                }
                                            },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Rewind", tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                                        }

                                        // 2. Dislike / Pass Button
                                        IconButton(
                                            onClick = {
                                                onSwipeAction(currentCandidate, SwipeDirection.LEFT, "")
                                            },
                                            modifier = Modifier
                                                .size(56.dp)
                                                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Pass", tint = Color(0xFFFF2D55), modifier = Modifier.size(28.dp))
                                        }

                                        // 3. Super Like Button
                                        IconButton(
                                            onClick = {
                                                superLikeDialogPartner = currentCandidate
                                            },
                                            modifier = Modifier
                                                .size(52.dp)
                                                .background(Color(0xFF00D2FF).copy(alpha = 0.2f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = "Super Like", tint = Color(0xFF00D2FF), modifier = Modifier.size(26.dp))
                                        }

                                        // 4. Like / Heart Button
                                        IconButton(
                                            onClick = {
                                                onSwipeAction(currentCandidate, SwipeDirection.RIGHT, "")
                                            },
                                            modifier = Modifier
                                                .size(68.dp)
                                                .background(
                                                    brush = Brush.linearGradient(PrimaryGradient),
                                                    shape = CircleShape
                                                )
                                        ) {
                                            Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.White, modifier = Modifier.size(34.dp))
                                        }

                                        // 5. Boost Button
                                        IconButton(
                                            onClick = {
                                                val ok = walletViewModel.startProfileBoost(30)
                                                actionSnackbarMessage = if (ok) "Profile Boosted for 30 min! 🚀" else "Need 80 pts to Boost"
                                            },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(Color(0xFF9C27B0).copy(alpha = 0.2f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Bolt, contentDescription = "Boost", tint = Color(0xFFE040FB), modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                            } else {
                                // Empty Candidates State
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFFF2D55).copy(alpha = 0.15f),
                                                modifier = Modifier.size(80.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF2D55), modifier = Modifier.size(40.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Text(
                                                text = "You're All Caught Up!",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "You've reviewed all active recommendations matching your criteria. Adjust your filters or reset to discover more singles globally.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White.copy(alpha = 0.7f),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        currentCandidateIndex = 0
                                                        actionSnackbarMessage = "Profiles refreshed! ✨"
                                                    },
                                                    shape = RoundedCornerShape(14.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Start Over", fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = { showBlindDateScreen = true },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55)),
                                                    shape = RoundedCornerShape(14.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("💘 Blind Date", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // COMMUNITY LIVE ROOMS TAB
                            CommunityRoomsScreen(
                                onOpenDirectChatWithUser = { partner ->
                                    val conv = chatViewModel.startConversationWithUser(partner)
                                    chatViewModel.openConversation(conv)
                                    selectedTab = 2
                                }
                            )
                        }

                        2 -> {
                            // CHATS TAB
                            if (activeConversation != null) {
                                val activeConv = activeConversation!!
                                val msgs = messagesMap[activeConv.conversationId] ?: emptyList()
                                val isTyping = partnerTypingMap[activeConv.conversationId] ?: false

                                ChatConversationScreen(
                                    conversation = activeConv,
                                    messages = msgs,
                                    isPartnerTyping = isTyping,
                                    onBack = { chatViewModel.closeConversation() },
                                    onSendMessage = { content, type, mediaUrl, duration, replyId, replyContent ->
                                        // Points deduction per action
                                        val cost = when (type) {
                                            MessageType.TEXT -> 15
                                            MessageType.VOICE -> 20
                                            MessageType.IMAGE -> 40
                                            else -> 15
                                        }
                                        if (walletViewModel.spendPoints(cost, "Sent ${type.name} to ${activeConv.otherUserName}")) {
                                            chatViewModel.sendMessage(
                                                conversationId = activeConv.conversationId,
                                                content = content,
                                                messageType = type,
                                                mediaUrl = mediaUrl,
                                                audioDurationSeconds = duration,
                                                replyToMessageId = replyId,
                                                replyToContent = replyContent
                                            )
                                        } else {
                                            actionSnackbarMessage = "Insufficient points! Text requires 15 pts. Buy points or upgrade to Master VIP 👑"
                                        }
                                    },
                                    onReaction = { msgId, emoji -> chatViewModel.toggleReaction(msgId, emoji) },
                                    onPinMessage = { msgId -> chatViewModel.togglePinMessage(msgId) },
                                    onDeleteMessage = { msgId -> chatViewModel.deleteMessage(msgId) },
                                    onStartCall = { type ->
                                        val callCost = 60
                                        if (walletViewModel.spendPoints(callCost, "Started ${type.name} Call with ${activeConv.otherUserName}")) {
                                            chatViewModel.startCall(type, activeConv)
                                        } else {
                                            actionSnackbarMessage = "Insufficient points for call (60 pts). Upgrade to Master Member for free calls!"
                                        }
                                    },
                                    onToggleMute = { chatViewModel.toggleMuteConversation(activeConv.conversationId) },
                                    onTogglePin = { chatViewModel.togglePinConversation(activeConv.conversationId) },
                                    onBlockUser = { uid ->
                                        chatViewModel.blockUser(uid)
                                        profileViewModel.blockUser(uid)
                                        actionSnackbarMessage = "User has been blocked."
                                    },
                                    onReportUser = { uid, reason ->
                                        actionSnackbarMessage = "Report submitted. Thank you for keeping our community safe."
                                    }
                                )
                            } else {
                                ChatsScreen(
                                    conversations = conversations,
                                    searchQuery = searchQuery,
                                    onSearchChange = { chatViewModel.setSearchQuery(it) },
                                    onSelectConversation = { conv -> chatViewModel.openConversation(conv) },
                                    onTogglePin = { id -> chatViewModel.togglePinConversation(id) },
                                    onToggleMute = { id -> chatViewModel.toggleMuteConversation(id) },
                                    onArchive = { id -> chatViewModel.toggleArchiveConversation(id) }
                                )
                            }
                        }

                        3 -> {
                            // MATCHES & REQUESTS TAB
                            MatchesAndRequestsScreen(
                                walletViewModel = walletViewModel,
                                onStartConversation = { partner ->
                                    val conv = chatViewModel.startConversationWithUser(partner)
                                    chatViewModel.openConversation(conv)
                                    selectedTab = 2
                                },
                                onNavigateToPremium = { showPremiumScreen = true }
                            )
                        }

                        4 -> {
                            // WALLET & VIP TAB
                            WalletScreen(
                                walletViewModel = walletViewModel,
                                onNavigateToPremium = { showPremiumScreen = true }
                            )
                        }

                        5 -> {
                            // PROFILE TAB
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = Color.White.copy(alpha = 0.05f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val photo = userProfile.primaryPhotoUrl.ifBlank {
                                                userProfile.photoUrls.firstOrNull() ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80"
                                            }
                                            AsyncImage(
                                                model = photo,
                                                contentDescription = "Avatar",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(96.dp)
                                                    .clip(CircleShape)
                                                    .border(2.5.dp, Brush.linearGradient(PrimaryGradient), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${userProfile.displayName.ifBlank { "You" }}, ${userProfile.age}",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (userProfile.isVerified) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF00D2FF), modifier = Modifier.size(20.dp))
                                                }
                                                if (isMasterMember) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("👑 VIP", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(
                                                text = "Nationality: ${userProfile.nationality} • Seeking: ${userProfile.interestedIn.joinToString(", ")}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFFF2D55),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${userProfile.city}, ${userProfile.country}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.6f)
                                            )

                                            Spacer(modifier = Modifier.height(14.dp))

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = onEditProfileRequested,
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55)),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Edit Profile", fontSize = 12.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = { showSafetyScreen = true },
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00D2FF)),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Verify 🛡️", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    // Master Member VIP Banner
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = Color(0xFFFFD700).copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showPremiumScreen = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("👑", fontSize = 26.sp)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text("Master Member VIP Club", style = MaterialTheme.typography.titleSmall, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                                    Text("Zero points on messages & video calls", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                                }
                                            }
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFFFD700))
                                        }
                                    }
                                }

                                item {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White.copy(alpha = 0.05f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("About Me", style = MaterialTheme.typography.titleSmall, color = Color(0xFFFF8C42), fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = userProfile.bio.ifBlank { "Passionate about finding authentic cross-cultural connections." },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                item {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White.copy(alpha = 0.05f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("Interests & Passions", style = MaterialTheme.typography.titleSmall, color = Color(0xFFFF8C42), fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val currentInterests = if (userProfile.interests.isNotEmpty()) userProfile.interests else listOf("☕ Coffee", "📸 Photography", "✈️ Travel", "🎵 Music", "🌐 Culture")
                                                currentInterests.forEach { interest ->
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = Color.White.copy(alpha = 0.1f)
                                                    ) {
                                                        Text(
                                                            text = interest,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }
                    }

                    // Snackbar notifications
                    actionSnackbarMessage?.let { msg ->
                        Snackbar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            action = {
                                TextButton(onClick = { actionSnackbarMessage = null }) {
                                    Text("OK", color = Color.White)
                                }
                            },
                            containerColor = Color(0xFFFF2D55)
                        ) {
                            Text(msg, color = Color.White)
                        }
                    }
                }
            }
        }

        // Discovery Preference Filter Sheet
        if (showDiscoveryFilterSheet) {
            DiscoveryPreferenceSheet(
                preferences = userProfile.discoveryPreferences,
                onSavePreferences = { updated ->
                    profileViewModel.updateDiscoveryPreferences(updated)
                    actionSnackbarMessage = "Discovery preferences updated! ✨"
                },
                onDismiss = { showDiscoveryFilterSheet = false }
            )
        }

        // Super Like Direct Intro Note Modal
        if (superLikeDialogPartner != null) {
            val partner = superLikeDialogPartner!!
            AlertDialog(
                onDismissRequest = { superLikeDialogPartner = null },
                title = { Text("Super Like ${partner.displayName} ⭐") },
                text = {
                    Column {
                        Text("Send a personalized intro note with your Super Like so they notice you right away!")
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(
                            value = superLikeIntroNote,
                            onValueChange = { superLikeIntroNote = it },
                            placeholder = { Text("Write something charming or ask about their passions...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isMasterMember) "✨ Free for Master Member (0 pts)" else "Cost: 30 Points (Super Connect)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMasterMember) Color(0xFFFFD700) else Color(0xFFFF8C42)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (walletViewModel.spendPoints(30, "Super Like with Note to ${partner.displayName}")) {
                                actionSnackbarMessage = "Super Like & Intro Note sent to ${partner.displayName}! 💌"
                                val currentUid = FirestoreManager.currentUser?.uid ?: "current_user"
                                val note = superLikeIntroNote
                                currentCandidateIndex++

                                coroutineScope.launch {
                                    val isMutual = FirestoreMatchManager.recordSwipeInteraction(
                                        fromUserId = currentUid,
                                        targetProfile = partner,
                                        action = InteractionType.SUPER_LIKE,
                                        introNote = note
                                    ).getOrDefault(false)

                                    if (isMutual) {
                                        matchedPartnerForCelebration = partner
                                    }
                                }
                            } else {
                                actionSnackbarMessage = "Insufficient points! Requires 30 pts."
                            }
                            superLikeDialogPartner = null
                            superLikeIntroNote = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FF))
                    ) {
                        Text("Send Intro Note 💌", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { superLikeDialogPartner = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Romantic "It's a Match!" Celebration Dialog
        if (matchedPartnerForCelebration != null) {
            val partner = matchedPartnerForCelebration!!
            MatchCelebrationDialog(
                currentUserProfile = userProfile,
                matchedProfile = partner,
                onSendMessage = { matchedUser ->
                    matchedPartnerForCelebration = null
                    val conv = chatViewModel.startConversationWithUser(matchedUser)
                    chatViewModel.openConversation(conv)
                    selectedTab = 2 // Switch to Chats Tab
                },
                onKeepSwiping = {
                    matchedPartnerForCelebration = null
                }
            )
        }

        // Active Voice / Video Call Fullscreen Overlay
        if (activeCallState.type != CallType.NONE) {
            ActiveCallOverlay(
                callState = activeCallState,
                onToggleMute = { chatViewModel.toggleCallMute() },
                onToggleSpeaker = { chatViewModel.toggleCallSpeaker() },
                onSwitchCamera = { chatViewModel.switchVideoCamera() },
                onEndCall = { chatViewModel.endCall() }
            )
        }
    }
}
