package com.example.ui.community

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import coil.compose.AsyncImage
import com.example.data.model.CommunityRoom
import com.example.data.model.RoomChatMessage
import com.example.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityRoomsScreen(
    onOpenDirectChatWithUser: (UserProfile) -> Unit
) {
    val rooms = remember {
        listOf(
            CommunityRoom("r1", "Global Travelers & Expats", "Share backpacking adventures, local guides & travel stories! ✈️", "✈️", 148, 0xFF6A11CB, 0xFF2575FC, "Travel"),
            CommunityRoom("r2", "Language & Culture Exchange", "Practice English, Urdu, Chinese, Japanese, Spanish & more! 🌐", "🌐", 112, 0xFFFF416C, 0xFFFF4B2B, "Culture"),
            CommunityRoom("r3", "Cozy Café & Late Night Chats", "Casual heartfelt banter, coffee debates & chill vibes ☕", "☕", 95, 0xFFF7971E, 0xFFFFD200, "Lifestyle"),
            CommunityRoom("r4", "Gaming, Anime & Pop Culture", "Discuss favorite anime, RPGs, cosplay & movies 🎮", "🎮", 84, 0xFF11998E, 0xFF38EF7D, "Entertainment"),
            CommunityRoom("r5", "Global Music & Concert Buddies", "Share Spotify playlists, favorite artists & festivals 🎵", "🎵", 67, 0xFF8E2DE2, 0xFF4A00E0, "Music")
        )
    }

    var selectedRoom by remember { mutableStateOf<CommunityRoom?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }

    if (selectedRoom != null) {
        RoomLiveChatView(
            room = selectedRoom!!,
            onBack = { selectedRoom = null },
            onOpenProfile = onOpenDirectChatWithUser
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Community Rooms 💬",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Connect with singles across the world in real-time topic hubs",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Categories Filter
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val categories = listOf("All", "Travel", "Culture", "Lifestyle", "Entertainment", "Music")
                    items(categories) { cat ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedCategory == cat) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            val filteredRooms = if (selectedCategory == "All") rooms else rooms.filter { it.category == selectedCategory }

            items(filteredRooms, key = { it.roomId }) { room ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedRoom = room }
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(Color(room.bannerColorStart), Color(room.bannerColorEnd))),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(room.bannerColorStart).copy(alpha = 0.35f),
                                        Color(room.bannerColorEnd).copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(room.emoji, fontSize = 28.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(room.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(room.topic, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f), maxLines = 2)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFF00E676), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${room.activeMembers} online singles", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E676), fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = "Enter", tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RoomLiveChatView(
    room: CommunityRoom,
    onBack: () -> Unit,
    onOpenProfile: (UserProfile) -> Unit
) {
    val now = System.currentTimeMillis()
    var inputMessage by remember { mutableStateOf("") }

    var roomMessages by remember {
        mutableStateOf(
            listOf(
                RoomChatMessage("m1", room.roomId, "Ayesha Malik", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80", "Pakistan 🇵🇰", isMasterMember = true, isVerified = true, "Greetings from Lahore! Anyone else planning a trip to Asia this winter? ✈️", now - 180000),
                RoomChatMessage("m2", room.roomId, "Lucas Zhang", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=600&q=80", "China 🇨🇳", isMasterMember = false, isVerified = true, "Yes! I'm visiting Beijing and Tokyo next month, looking for travel recommendations! 🍜", now - 120000),
                RoomChatMessage("m3", room.roomId, "Elena Rostova", "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=600&q=80", "Russia 🇷🇺", isMasterMember = true, isVerified = true, "I love how international this room is! Connecting across cultures is so beautiful ✨", now - 60000)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Room Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(room.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(room.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${room.activeMembers} members active", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E676))
                }
            }
        }

        // Message Feed
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🛡️ Community Guidelines: Be kind, respectful, and never share financial credentials. Keep conversations safe for everyone.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            items(roomMessages, key = { it.id }) { msg ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onOpenProfile(
                                UserProfile(
                                    uid = "user_${msg.senderName.replace(" ", "_").lowercase()}",
                                    displayName = msg.senderName,
                                    primaryPhotoUrl = msg.senderPhoto,
                                    nationality = msg.senderNationality,
                                    isVerified = msg.isVerified,
                                    isMasterMember = msg.isMasterMember,
                                    bio = "Active member in ${room.name} room!"
                                )
                            )
                        }
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        AsyncImage(
                            model = msg.senderPhoto,
                            contentDescription = msg.senderName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(msg.senderName, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(msg.senderNationality, fontSize = 11.sp)
                                if (msg.isMasterMember) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("👑 VIP", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                if (msg.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF00D2FF), modifier = Modifier.size(13.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(msg.messageText, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
            }
        }

        // Room Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("Say something to ${room.name}...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val text = inputMessage.trim()
                        if (text.isNotBlank()) {
                            val newMsg = RoomChatMessage(
                                id = "msg_${System.currentTimeMillis()}",
                                roomId = room.roomId,
                                senderName = "You",
                                senderPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80",
                                senderNationality = "United States 🇺🇸",
                                isMasterMember = false,
                                isVerified = true,
                                messageText = text,
                                timestamp = System.currentTimeMillis()
                            )
                            roomMessages = roomMessages + newMsg
                            inputMessage = ""
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFFF2D55), CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
