package com.example.ui.wallet

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.PrimaryGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    walletViewModel: WalletViewModel,
    onNavigateToPremium: () -> Unit
) {
    val pointsBalance by walletViewModel.pointsBalance.collectAsState()
    val isMasterMember by walletViewModel.isMasterMember.collectAsState()
    val transactions by walletViewModel.transactions.collectAsState()
    val streakDays by walletViewModel.dailyStreakDays.collectAsState()
    val currentStreak by walletViewModel.currentStreak.collectAsState()
    val isRewardClaimedToday by walletViewModel.isRewardClaimedToday.collectAsState()
    val quests by walletViewModel.quests.collectAsState()
    val boostRemainingSeconds by walletViewModel.boostRemainingSeconds.collectAsState()

    var selectedFilterTab by remember { mutableStateOf("ALL") } // ALL, SPENT, EARNED, PURCHASED
    var showPurchaseDialogForPkg by remember { mutableStateOf<PointsPackage?>(null) }
    var successToastMessage by remember { mutableStateOf<String?>(null) }

    val pointsPackages = remember {
        listOf(
            PointsPackage("pkg_1", "Starter Pack", points = 100, bonusPoints = 0, priceUsd = 1.99, priceFormatted = "$1.99"),
            PointsPackage("pkg_2", "Popular Pack", points = 350, bonusPoints = 50, priceUsd = 4.99, priceFormatted = "$4.99", badge = "+50 BONUS", isPopular = true),
            PointsPackage("pkg_3", "Super Value", points = 800, bonusPoints = 150, priceUsd = 9.99, priceFormatted = "$9.99", badge = "+150 BONUS", isBestValue = true),
            PointsPackage("pkg_4", "Mega Pack", points = 2000, bonusPoints = 500, priceUsd = 19.99, priceFormatted = "$19.99", badge = "+500 BONUS")
        )
    }

    val filteredTransactions = remember(transactions, selectedFilterTab) {
        when (selectedFilterTab) {
            "SPENT" -> transactions.filter { it.type == TransactionType.SPENT }
            "EARNED" -> transactions.filter { it.type == TransactionType.EARNED || it.type == TransactionType.BONUS }
            "PURCHASED" -> transactions.filter { it.type == TransactionType.PURCHASED }
            else -> transactions
        }
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
            // Main Balance Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF2D55), Color(0xFF9C27B0))),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF2A1535),
                                    Color(0xFF160E24)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "POINTS BALANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$pointsBalance",
                                        style = MaterialTheme.typography.displayMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PTS",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (isMasterMember) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("👑 Master VIP", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = onNavigateToPremium,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Go VIP 👑", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick action rate transparent guide
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CostRateChip("💬 Text", "15 pts", isMasterMember)
                            CostRateChip("🎙️ Voice", "20 pts", isMasterMember)
                            CostRateChip("🖼️ Photo", "40 pts", isMasterMember)
                            CostRateChip("📹 Video", "60 pts", isMasterMember)
                        }

                        if (isMasterMember) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✨ Master Member Active: All communication costs are 0 points (Unlimited Free)!",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Daily Login Streak Reward Section
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
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
                                Text("Daily Login Streak 🔥", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFF8C42).copy(alpha = 0.2f)
                                ) {
                                    Text("$currentStreak Days", color = Color(0xFFFF8C42), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text("Check in every day to claim free points bonus!", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                        }

                        Button(
                            onClick = {
                                val awarded = walletViewModel.claimDailyReward()
                                if (awarded > 0) {
                                    successToastMessage = "Claimed +$awarded Bonus Points! 🎉"
                                }
                            },
                            enabled = !isRewardClaimedToday,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF2D55),
                                disabledContainerColor = Color.White.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isRewardClaimedToday) "Claimed ✓" else "Claim 🎁", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(streakDays) { day ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    day.isClaimed -> Color(0xFF00E676).copy(alpha = 0.15f)
                                    day.isToday -> Color(0xFFFF2D55).copy(alpha = 0.25f)
                                    else -> Color.White.copy(alpha = 0.05f)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when {
                                        day.isClaimed -> Color(0xFF00E676)
                                        day.isToday -> Color(0xFFFF2D55)
                                        else -> Color.White.copy(alpha = 0.1f)
                                    }
                                ),
                                modifier = Modifier.width(52.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Day ${day.dayNumber}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("+${day.points}", style = MaterialTheme.typography.bodySmall, color = if (day.isClaimed) Color(0xFF00E676) else Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(if (day.isClaimed) "✓" else "🎁", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Profile Boost Section
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFFFF2D55))), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚀", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Profile Boost 🚀", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (boostRemainingSeconds > 0) "Boost Active: ${boostRemainingSeconds / 60}m ${boostRemainingSeconds % 60}s remaining"
                                else "Get 5x more views & matches for 30 mins",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (boostRemainingSeconds > 0) Color(0xFF00E676) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val success = walletViewModel.startProfileBoost(30)
                            if (success) {
                                successToastMessage = "Profile Boost activated for 30 minutes! 🚀"
                            } else {
                                successToastMessage = "Need 80 points to activate Boost."
                            }
                        },
                        enabled = boostRemainingSeconds == 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (boostRemainingSeconds > 0) "Active" else "Boost (80p)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Buy Points Packages Grid
        item {
            Text(
                text = "Buy Points Packages 💎",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Non-cash virtual points for messages, calls & connection features",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        items(pointsPackages.chunked(2)) { rowPackages ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowPackages.forEach { pkg ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (pkg.isPopular) Color(0xFFFF2D55) else if (pkg.isBestValue) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showPurchaseDialogForPkg = pkg }
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (pkg.badge != null) {
                                Surface(
                                    shape = RoundedCornerShape(bottomStart = 8.dp),
                                    color = if (pkg.isPopular) Color(0xFFFF2D55) else Color(0xFFFFD700),
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = pkg.badge,
                                        color = if (pkg.isPopular) Color.White else Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("💎", fontSize = 26.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(pkg.title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${pkg.totalPoints}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(" pts", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFF2D55),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = pkg.priceFormatted,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Earn Points Free Quests
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Earn Free Points Tasks 🏆",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        items(quests) { quest ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(quest.title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(quest.description, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (quest.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.15f)
                        ) {
                            Text("Completed ✓", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                walletViewModel.completeQuest(quest.id)
                                successToastMessage = "Task completed! +${quest.pointsReward} Points added 🏆"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C42)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("+${quest.pointsReward} pts", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Transaction History Ledger
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History 📜",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Filter row chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("ALL", "SPENT", "EARNED").forEach { tab ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedFilterTab == tab) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.clickable { selectedFilterTab = tab }
                        ) {
                            Text(
                                text = tab,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Text(
                    text = "No transactions found in this category.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(filteredTransactions, key = { it.id }) { txn ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.04f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(txn.title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                            if (txn.description.isNotBlank()) {
                                Text(txn.description, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                            }
                            Text(
                                text = "${txn.formattedDate} • ${txn.referenceId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (txn.pointsDelta > 0) "+${txn.pointsDelta} pts" else if (txn.pointsDelta < 0) "${txn.pointsDelta} pts" else "VIP 👑",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (txn.pointsDelta > 0) Color(0xFF00E676) else if (txn.pointsDelta < 0) Color(0xFFFF2D55) else Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bal: ${txn.remainingBalance}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Purchase Dialog Confirmation
    if (showPurchaseDialogForPkg != null) {
        val pkg = showPurchaseDialogForPkg!!
        AlertDialog(
            onDismissRequest = { showPurchaseDialogForPkg = null },
            title = { Text("Confirm Points Purchase 💎") },
            text = {
                Column {
                    Text("Package: ${pkg.title}", fontWeight = FontWeight.Bold)
                    Text("Points: ${pkg.points} + ${pkg.bonusPoints} bonus (${pkg.totalPoints} pts total)")
                    Text("Price: ${pkg.priceFormatted}")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Points are virtual currency for relationship messaging, media, and call connections.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        walletViewModel.purchasePackage(pkg)
                        showPurchaseDialogForPkg = null
                        successToastMessage = "Purchased ${pkg.totalPoints} points successfully! ✨"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55))
                ) {
                    Text("Pay ${pkg.priceFormatted}")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDialogForPkg = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Snackbar / Toast Notification
    successToastMessage?.let { msg ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFFFF2D55),
                action = {
                    TextButton(onClick = { successToastMessage = null }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) {
                Text(msg, color = Color.White)
            }
        }
    }
}

@Composable
private fun CostRateChip(label: String, cost: String, isMaster: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
            Text(
                text = if (isMaster) "0 pts (VIP)" else cost,
                fontSize = 11.sp,
                color = if (isMaster) Color(0xFFFFD700) else Color(0xFFFF8C42),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
