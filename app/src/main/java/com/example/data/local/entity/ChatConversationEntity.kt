package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ChatConversation
import com.example.data.model.MessageType

@Entity(
    tableName = "local_chat_conversations",
    indices = [
        Index(value = ["otherUserId"]),
        Index(value = ["lastMessageTimestamp"])
    ]
)
data class ChatConversationEntity(
    @PrimaryKey
    @ColumnInfo(name = "conversationId")
    val conversationId: String,

    @ColumnInfo(name = "participantIds")
    val participantIds: List<String> = emptyList(),

    @ColumnInfo(name = "otherUserId")
    val otherUserId: String = "",

    @ColumnInfo(name = "otherUserName")
    val otherUserName: String = "",

    @ColumnInfo(name = "otherUserPhoto")
    val otherUserPhoto: String = "",

    @ColumnInfo(name = "otherUserAge")
    val otherUserAge: Int = 24,

    @ColumnInfo(name = "otherUserCity")
    val otherUserCity: String = "",

    @ColumnInfo(name = "isOtherUserVerified")
    val isOtherUserVerified: Boolean = false,

    @ColumnInfo(name = "isOtherUserOnline")
    val isOtherUserOnline: Boolean = true,

    @ColumnInfo(name = "otherUserLastActive")
    val otherUserLastActive: String = "Online now",

    @ColumnInfo(name = "lastMessage")
    val lastMessage: String = "",

    @ColumnInfo(name = "lastMessageType")
    val lastMessageType: String = MessageType.TEXT.name,

    @ColumnInfo(name = "lastMessageTimestamp")
    val lastMessageTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "unreadCount")
    val unreadCount: Int = 0,

    @ColumnInfo(name = "isPinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "isMuted")
    val isMuted: Boolean = false,

    @ColumnInfo(name = "isArchived")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "isBlocked")
    val isBlocked: Boolean = false
)

fun ChatConversationEntity.toDomainModel(): ChatConversation {
    return ChatConversation(
        conversationId = conversationId,
        participantIds = participantIds,
        otherUserId = otherUserId,
        otherUserName = otherUserName,
        otherUserPhoto = otherUserPhoto,
        otherUserAge = otherUserAge,
        otherUserCity = otherUserCity,
        isOtherUserVerified = isOtherUserVerified,
        isOtherUserOnline = isOtherUserOnline,
        otherUserLastActive = otherUserLastActive,
        lastMessage = lastMessage,
        lastMessageType = try { MessageType.valueOf(lastMessageType) } catch (e: Exception) { MessageType.TEXT },
        lastMessageTimestamp = lastMessageTimestamp,
        unreadCount = unreadCount,
        isPinned = isPinned,
        isMuted = isMuted,
        isArchived = isArchived,
        isBlocked = isBlocked
    )
}

fun ChatConversation.toEntity(): ChatConversationEntity {
    return ChatConversationEntity(
        conversationId = conversationId,
        participantIds = participantIds,
        otherUserId = otherUserId,
        otherUserName = otherUserName,
        otherUserPhoto = otherUserPhoto,
        otherUserAge = otherUserAge,
        otherUserCity = otherUserCity,
        isOtherUserVerified = isOtherUserVerified,
        isOtherUserOnline = isOtherUserOnline,
        otherUserLastActive = otherUserLastActive,
        lastMessage = lastMessage,
        lastMessageType = lastMessageType.name,
        lastMessageTimestamp = lastMessageTimestamp,
        unreadCount = unreadCount,
        isPinned = isPinned,
        isMuted = isMuted,
        isArchived = isArchived,
        isBlocked = isBlocked
    )
}
