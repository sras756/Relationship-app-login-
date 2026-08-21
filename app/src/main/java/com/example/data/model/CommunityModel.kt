package com.example.data.model

data class CommunityRoom(
    val roomId: String,
    val name: String,
    val topic: String,
    val emoji: String,
    val activeMembers: Int,
    val bannerColorStart: Long,
    val bannerColorEnd: Long,
    val category: String
)

data class RoomChatMessage(
    val id: String,
    val roomId: String,
    val senderName: String,
    val senderPhoto: String,
    val senderNationality: String,
    val isMasterMember: Boolean,
    val isVerified: Boolean,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ConnectionRequest(
    val id: String,
    val senderProfile: UserProfile,
    val introNote: String,
    val isSuperLike: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // PENDING, ACCEPTED, DECLINED
)
