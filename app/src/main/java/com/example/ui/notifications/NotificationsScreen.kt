package com.example.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.PrimaryGradient

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val iconType: String,
    val avatarUrl: String? = null,
    val isUnread: Boolean = false
)

@Composable
fun NotificationsScreen(
    onOpenChat: () -> Unit
) {
    val sampleNotifications = listOf(
        NotificationItem(
            id = "n1",
            title = "New Match! 💕",
            description = "Sophia liked your profile back. Send the first message!",
            timeAgo = "10m ago",
            iconType = "match",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80",
            isUnread = true
        ),
        NotificationItem(
            id = "n2",
            title = "Someone Liked You ❤️",
            description = "A new person in Los Angeles liked your photo.",
            timeAgo = "1h ago",
            iconType = "like",
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=600&q=80",
            isUnread = true
        ),
        NotificationItem(
            id = "n3",
            title = "Profile Verification Complete ✓",
            description = "Your blue verification badge has been activated.",
            timeAgo = "2h ago",
            iconType = "verified",
            isUnread = false
        ),
        NotificationItem(
            id = "n4",
            title = "Safety & Privacy Update 🛡️",
            description = "End-to-end encryption is enabled for your private messages and calls.",
            timeAgo = "1d ago",
            iconType = "safety",
            isUnread = false
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Activity & Alerts 🔔",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sampleNotifications, key = { it.id }) { item ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (item.isUnread) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (item.isUnread) Color(0xFFFF2D55).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.06f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (item.iconType == "match") onOpenChat() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.avatarUrl != null) {
                            Box(modifier = Modifier.size(48.dp)) {
                                AsyncImage(
                                    model = item.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(0xFFFF2D55), CircleShape)
                                        .align(Alignment.BottomEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (item.iconType == "verified") Color(0xFF00D2FF).copy(alpha = 0.2f)
                                        else Color(0xFFFF2D55).copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.iconType == "verified") Icons.Default.Verified else Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = if (item.iconType == "verified") Color(0xFF00D2FF) else Color(0xFFFF2D55),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = if (item.isUnread) FontWeight.Bold else FontWeight.SemiBold
                                )
                                Text(
                                    text = item.timeAgo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
