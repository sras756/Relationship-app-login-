package com.example.data.model

enum class SwipeDirection {
    LEFT,    // Dislike / Pass
    RIGHT,   // Like
    UP       // Super Like
}

enum class InteractionType {
    LIKE,
    DISLIKE,
    SUPER_LIKE
}

data class PotentialConnection(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val action: InteractionType = InteractionType.LIKE,
    val introNote: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isMutualMatch: Boolean = false,
    val targetProfile: UserProfile? = null
)

data class MutualMatch(
    val matchId: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val matchedAt: Long = System.currentTimeMillis(),
    val otherUserProfile: UserProfile = UserProfile(),
    val isNew: Boolean = true
)
