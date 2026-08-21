package com.example.data.model

enum class MessageType {
    TEXT,
    IMAGE,
    VOICE,
    GIF,
    STICKER,
    VIDEO,
    MATCH_EVENT
}

enum class MessageReadStatus {
    SENT,
    DELIVERED,
    READ
}

enum class ModerationStatus {
    SAFE,
    SUSPICIOUS_SPAM,
    FLAGGED
}

data class ChatMessage(
    val messageId: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val messageType: MessageType = MessageType.TEXT,
    val messageContent: String = "",
    val mediaUrl: String = "",
    val audioDurationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val readStatus: MessageReadStatus = MessageReadStatus.READ,
    val editedStatus: Boolean = false,
    val deletedStatus: Boolean = false,
    val moderationStatus: ModerationStatus = ModerationStatus.SAFE,
    val replyToMessageId: String? = null,
    val replyToContent: String? = null,
    val reactions: Map<String, String> = emptyMap(), // userId -> emoji (e.g., "❤️", "😂")
    val isPinned: Boolean = false
)

data class ChatConversation(
    val conversationId: String = "",
    val participantIds: List<String> = emptyList(),
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserPhoto: String = "",
    val otherUserAge: Int = 24,
    val otherUserCity: String = "",
    val isOtherUserVerified: Boolean = false,
    val isOtherUserOnline: Boolean = true,
    val otherUserLastActive: String = "Online now",
    val lastMessage: String = "",
    val lastMessageType: MessageType = MessageType.TEXT,
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isBlocked: Boolean = false
)
