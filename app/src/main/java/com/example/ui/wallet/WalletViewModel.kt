package com.example.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class WalletViewModel : ViewModel() {

    private val _pointsBalance = MutableStateFlow(150) // 150 Starting Welcome Pack points
    val pointsBalance: StateFlow<Int> = _pointsBalance.asStateFlow()

    private val _isMasterMember = MutableStateFlow(false)
    val isMasterMember: StateFlow<Boolean> = _isMasterMember.asStateFlow()

    private val _masterPlanExpiry = MutableStateFlow<String?>(null)
    val masterPlanExpiry: StateFlow<String?> = _masterPlanExpiry.asStateFlow()

    private val _boostRemainingSeconds = MutableStateFlow(0)
    val boostRemainingSeconds: StateFlow<Int> = _boostRemainingSeconds.asStateFlow()

    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    private val _currentStreak = MutableStateFlow(3)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _isRewardClaimedToday = MutableStateFlow(false)
    val isRewardClaimedToday: StateFlow<Boolean> = _isRewardClaimedToday.asStateFlow()

    private val _dailyStreakDays = MutableStateFlow<List<DailyStreakDay>>(emptyList())
    val dailyStreakDays: StateFlow<List<DailyStreakDay>> = _dailyStreakDays.asStateFlow()

    private val _quests = MutableStateFlow<List<QuestReward>>(emptyList())
    val quests: StateFlow<List<QuestReward>> = _quests.asStateFlow()

    init {
        initInitialData()
    }

    private fun initInitialData() {
        val now = System.currentTimeMillis()
        _transactions.value = listOf(
            WalletTransaction(
                id = "txn_welcome",
                title = "Welcome Bonus Pack 🎁",
                description = "Starter bonus for new registered profile",
                pointsDelta = 150,
                type = TransactionType.BONUS,
                timestamp = now - 86400000 * 2,
                remainingBalance = 150
            ),
            WalletTransaction(
                id = "txn_streak1",
                title = "Daily Login Streak (Day 1) 🌟",
                description = "Daily active connection bonus",
                pointsDelta = 10,
                type = TransactionType.EARNED,
                timestamp = now - 86400000,
                remainingBalance = 160
            ),
            WalletTransaction(
                id = "txn_msg1",
                title = "Sent Text Message 💬",
                description = "Conversation with Sophia Chen",
                pointsDelta = -15,
                type = TransactionType.SPENT,
                timestamp = now - 3600000 * 4,
                remainingBalance = 145
            ),
            WalletTransaction(
                id = "txn_bonus_profile",
                title = "Profile Verification Reward 🛡️",
                description = "Phone & photo verification completed",
                pointsDelta = 20,
                type = TransactionType.BONUS,
                timestamp = now - 1800000,
                remainingBalance = 165
            )
        )
        _pointsBalance.value = 165

        _dailyStreakDays.value = listOf(
            DailyStreakDay(1, 10, isClaimed = true, isToday = false),
            DailyStreakDay(2, 15, isClaimed = true, isToday = false),
            DailyStreakDay(3, 20, isClaimed = true, isToday = false),
            DailyStreakDay(4, 25, isClaimed = false, isToday = true),
            DailyStreakDay(5, 30, isClaimed = false, isToday = false),
            DailyStreakDay(6, 40, isClaimed = false, isToday = false),
            DailyStreakDay(7, 50, isClaimed = false, isToday = false)
        )

        _quests.value = listOf(
            QuestReward("q1", "Complete 100% Profile", "Add 3+ photos and detailed bio", 30, isCompleted = true, "photo"),
            QuestReward("q2", "Photo Liveness Verification", "Take a real-time selfie verification", 25, isCompleted = true, "verified"),
            QuestReward("q3", "Send 5 Romantic Messages", "Break the ice with new matches", 20, isCompleted = false, "chat"),
            QuestReward("q4", "Join an International Room", "Say hello in any community room", 15, isCompleted = false, "group"),
            QuestReward("q5", "Invite a Global Friend", "Earn bonus points when they join", 50, isCompleted = false, "share")
        )
    }

    /**
     * Attempts to consume points for an action.
     * Returns true if successful (or if Master Member where cost is 0), false if insufficient points.
     */
    fun canAfford(cost: Int): Boolean {
        if (_isMasterMember.value) return true
        return _pointsBalance.value >= cost
    }

    fun spendPoints(cost: Int, actionTitle: String, description: String = ""): Boolean {
        if (_isMasterMember.value) {
            // Master member gets 0 point deduction!
            return true
        }
        if (_pointsBalance.value < cost) {
            return false
        }
        val newBalance = _pointsBalance.value - cost
        _pointsBalance.value = newBalance
        val txn = WalletTransaction(
            title = actionTitle,
            description = description,
            pointsDelta = -cost,
            type = TransactionType.SPENT,
            remainingBalance = newBalance
        )
        _transactions.value = listOf(txn) + _transactions.value
        return true
    }

    fun claimDailyReward(): Int {
        if (_isRewardClaimedToday.value) return 0
        val todayDay = _dailyStreakDays.value.find { it.isToday } ?: return 0
        val pointsToAward = todayDay.points
        val newBalance = _pointsBalance.value + pointsToAward
        _pointsBalance.value = newBalance
        _isRewardClaimedToday.value = true

        _dailyStreakDays.value = _dailyStreakDays.value.map {
            if (it.dayNumber == todayDay.dayNumber) it.copy(isClaimed = true) else it
        }
        _currentStreak.value += 1

        val txn = WalletTransaction(
            title = "Claimed Day ${todayDay.dayNumber} Streak Reward 🎁",
            description = "Daily login streak reward",
            pointsDelta = pointsToAward,
            type = TransactionType.BONUS,
            remainingBalance = newBalance
        )
        _transactions.value = listOf(txn) + _transactions.value
        return pointsToAward
    }

    fun completeQuest(questId: String) {
        val quest = _quests.value.find { it.id == questId && !it.isCompleted } ?: return
        val newBalance = _pointsBalance.value + quest.pointsReward
        _pointsBalance.value = newBalance
        _quests.value = _quests.value.map {
            if (it.id == questId) it.copy(isCompleted = true) else it
        }
        val txn = WalletTransaction(
            title = "Completed: ${quest.title} 🏆",
            description = quest.description,
            pointsDelta = quest.pointsReward,
            type = TransactionType.EARNED,
            remainingBalance = newBalance
        )
        _transactions.value = listOf(txn) + _transactions.value
    }

    fun purchasePackage(pkg: PointsPackage) {
        val newBalance = _pointsBalance.value + pkg.totalPoints
        _pointsBalance.value = newBalance
        val txn = WalletTransaction(
            title = "Purchased ${pkg.title} 💎",
            description = "${pkg.points} pts + ${pkg.bonusPoints} bonus pts (${pkg.priceFormatted})",
            pointsDelta = pkg.totalPoints,
            type = TransactionType.PURCHASED,
            remainingBalance = newBalance
        )
        _transactions.value = listOf(txn) + _transactions.value
    }

    fun activateMasterMember(plan: MasterPlan) {
        _isMasterMember.value = true
        _masterPlanExpiry.value = "Active until ${Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.time}"
        val txn = WalletTransaction(
            title = "Upgraded to Master Member VIP 👑",
            description = "${plan.title} (${plan.priceFormatted}/${plan.period}) - Unlimited free communications activated!",
            pointsDelta = 0,
            type = TransactionType.PURCHASED,
            remainingBalance = _pointsBalance.value
        )
        _transactions.value = listOf(txn) + _transactions.value
    }

    fun cancelMasterMember() {
        _isMasterMember.value = false
        _masterPlanExpiry.value = null
    }

    fun addRewardPoints(points: Int, description: String = "Reward Bonus") {
        val newBalance = _pointsBalance.value + points
        _pointsBalance.value = newBalance
        val txn = WalletTransaction(
            title = description,
            description = "Blind Date Reward Bonus",
            pointsDelta = points,
            type = TransactionType.BONUS,
            remainingBalance = newBalance
        )
        _transactions.value = listOf(txn) + _transactions.value
    }

    fun startProfileBoost(durationMinutes: Int = 30): Boolean {
        val cost = 80
        if (!canAfford(cost)) return false
        spendPoints(cost, "Activated Profile Boost 🚀", "5x higher exposure in Discover & Radar for $durationMinutes mins")
        _boostRemainingSeconds.value = durationMinutes * 60

        viewModelScope.launch {
            while (_boostRemainingSeconds.value > 0) {
                delay(1000)
                _boostRemainingSeconds.value -= 1
            }
        }
        return true
    }
}
