package com.example.ui.premium

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
import com.example.data.model.MasterPlan
import com.example.ui.wallet.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSubscriptionScreen(
    walletViewModel: WalletViewModel,
    onBack: () -> Unit
) {
    val isMasterMember by walletViewModel.isMasterMember.collectAsState()
    val planExpiry by walletViewModel.masterPlanExpiry.collectAsState()

    val plans = remember {
        listOf(
            MasterPlan("plan_week", "Weekly VIP", priceUsd = 4.99, priceFormatted = "$4.99", period = "week"),
            MasterPlan("plan_month", "Monthly VIP", priceUsd = 14.99, priceFormatted = "$14.99", period = "month", savingsText = "SAVE 25%", isMostPopular = true),
            MasterPlan("plan_quarter", "3-Month VIP", priceUsd = 29.99, priceFormatted = "$29.99", period = "3 months", savingsText = "SAVE 50%"),
            MasterPlan("plan_life", "Lifetime VIP", priceUsd = 79.99, priceFormatted = "$79.99", period = "lifetime", savingsText = "BEST VALUE")
        )
    }

    var selectedPlan by remember { mutableStateOf(plans[1]) }
    var showSuccessToast by remember { mutableStateOf<String?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header Hero
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF2D55), Color(0xFFFF8C42))),
                        RoundedCornerShape(28.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF381A05),
                                    Color(0xFF1E0E02),
                                    Color(0xFF0F0801)
                                )
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8C42))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👑", fontSize = 36.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "MASTER MEMBER VIP",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )

                        Text(
                            text = "Unlock limitless international connection, zero-point communication, and priority matching.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        if (isMasterMember) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF00E676).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                            ) {
                                Text(
                                    text = "Status: ACTIVE VIP SUBSCRIBER ✓",
                                    color = Color(0xFF00E676),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Key Perks List
        item {
            Text(
                text = "Exclusive Master Perks ✨",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PerkRowItem("💬", "Zero-Point Messaging", "Unlimited text, voice notes, photos & video files with 0 point deductions.")
                PerkRowItem("📹", "Unlimited Voice & Video Calls", "Talk face-to-face across the globe without time restrictions.")
                PerkRowItem("👀", "See Who Liked You", "View all admirers and instant match without waiting.")
                PerkRowItem("🚀", "Free Monthly 5x Profile Boost", "Top placement in Discover and Radar in your target countries.")
                PerkRowItem("🌍", "Advanced Nationality Filters", "Filter by specific countries, languages, and verified status.")
                PerkRowItem("👑", "Golden Master Crown Badge", "Distinctive glowing VIP badge next to your name and chat bubbles.")
                PerkRowItem("🛡️", "VIP Privacy & Ghost Mode", "Browse profiles anonymously and hide your last seen.")
            }
        }

        // Comparison Table
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Free vs Master Member ⚖️",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Feature", fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.weight(1.5f), fontSize = 12.sp)
                        Text("Free", fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Text("Master 👑", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), textAlign = TextAlign.Center, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))

                    ComparisonRow("Text Messages", "15 pts / msg", "Unlimited Free ✓")
                    ComparisonRow("Voice Notes", "20 pts / note", "Unlimited Free ✓")
                    ComparisonRow("Photo Sharing", "40 pts / photo", "Unlimited Free ✓")
                    ComparisonRow("Video Calls", "60 pts / call", "Unlimited Free ✓")
                    ComparisonRow("Who Liked You", "Blurred", "Full Access ✓")
                    ComparisonRow("Profile Boost", "80 pts", "1 Free / Mo ✓")
                    ComparisonRow("Nationality Filter", "Standard", "Advanced Multi ✓")
                }
            }
        }

        // Plan Selector
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose Your Membership Plan 💳",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        items(plans) { plan ->
            val isSelected = selectedPlan.id == plan.id
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color(0xFFFFD700).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedPlan = plan }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedPlan = plan },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFD700))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(plan.title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                if (plan.savingsText != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFF2D55)
                                    ) {
                                        Text(plan.savingsText, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text("Billed per ${plan.period}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                        }
                    }

                    Text(
                        text = plan.priceFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Action CTA
        item {
            Spacer(modifier = Modifier.height(6.dp))
            if (isMasterMember) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF2D55)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manage / Cancel Subscription")
                    }
                }
            } else {
                Button(
                    onClick = {
                        walletViewModel.activateMasterMember(selectedPlan)
                        showSuccessToast = "Welcome to Master Member VIP! 👑 Unlimited communications unlocked."
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Upgrade to Master Member (${selectedPlan.priceFormatted})",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Auto-renews until cancelled. Cancel anytime in Google Play Settings. Terms apply.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Master VIP?") },
            text = { Text("You will revert to the standard points economy where messages cost points.") },
            confirmButton = {
                Button(
                    onClick = {
                        walletViewModel.cancelMasterMember()
                        showCancelDialog = false
                        showSuccessToast = "Subscription cancelled."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55))
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep VIP")
                }
            }
        )
    }

    showSuccessToast?.let { msg ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black,
                action = {
                    TextButton(onClick = { showSuccessToast = null }) {
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
private fun PerkRowItem(emoji: String, title: String, description: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ComparisonRow(title: String, free: String, master: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall, color = Color.White, modifier = Modifier.weight(1.5f), fontSize = 11.sp)
        Text(free, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.weight(1f), fontSize = 11.sp)
        Text(master, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f), fontSize = 11.sp)
    }
}
