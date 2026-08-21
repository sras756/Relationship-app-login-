package com.example.data.model

import java.text.SimpleDateFormat
import java.util.*

enum class TransactionType {
    EARNED,
    PURCHASED,
    SPENT,
    BONUS
}

data class WalletTransaction(
    val id: String = UUID.randomUUID().toString().take(8),
    val title: String,
    val description: String = "",
    val pointsDelta: Int, // e.g. +150, -15
    val type: TransactionType,
    val timestamp: Long = System.currentTimeMillis(),
    val remainingBalance: Int = 150,
    val referenceId: String = "TXN-${System.currentTimeMillis().toString().takeLast(6)}"
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()).format(Date(timestamp))
}

data class PointsPackage(
    val id: String,
    val title: String,
    val points: Int,
    val bonusPoints: Int = 0,
    val priceUsd: Double,
    val priceFormatted: String,
    val badge: String? = null,
    val isPopular: Boolean = false,
    val isBestValue: Boolean = false
) {
    val totalPoints: Int get() = points + bonusPoints
}

data class MasterPlan(
    val id: String,
    val title: String,
    val priceUsd: Double,
    val priceFormatted: String,
    val period: String,
    val savingsText: String? = null,
    val isMostPopular: Boolean = false
)

data class DailyStreakDay(
    val dayNumber: Int,
    val points: Int,
    val isClaimed: Boolean,
    val isToday: Boolean
)

data class QuestReward(
    val id: String,
    val title: String,
    val description: String,
    val pointsReward: Int,
    val isCompleted: Boolean,
    val iconName: String
)
