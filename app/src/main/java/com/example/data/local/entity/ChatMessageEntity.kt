package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ChatMessage
import com.example.data.model.MessageReadStatus
import com.example.data.model.MessageType
import com.example.data.model.ModerationStatus

@Entity(
    tableName = "local_chat_messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["senderId"]),
        Index(value = ["receiverId"]),
        Index(value = ["timestamp"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "messageId")
    val messageId: String,

    @ColumnInfo(name = "conversationId")
    val conversationId: String,

    @ColumnInfo(name = "senderId")
    val senderId: String,

    @ColumnInfo(name = "receiverId")
    val receiverId: String,

    @ColumnInfo(name = "messageType")
    val messageType: String = MessageType.TEXT.name,

    @ColumnInfo(name = "messageContent")
    val messageContent: String = "",

    @ColumnInfo(name = "mediaUrl")
    val mediaUrl: String = "",

    @ColumnInfo(name = "audioDurationSeconds")
    val audioDurationSeconds: Int = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "readStatus")
    val readStatus: String = MessageReadStatus.READ.name,

    @ColumnInfo(name = "editedStatus")
    val editedStatus: Boolean = false,

    @ColumnInfo(name = "deletedStatus")
    val deletedStatus: Boolean = false,

    @ColumnInfo(name = "moderationStatus")
    val moderationStatus: String = ModerationStatus.SAFE.name,

    @ColumnInfo(name = "replyToMessageId")
    val replyToMessageId: String? = null,

    @ColumnInfo(name = "replyToContent")
    val replyToContent: String? = null,

    @ColumnInfo(name = "reactions")
    val reactions: Map<String, String> = emptyMap(),

    @ColumnInfo(name = "isPinned")
    val isPinned: Boolean = false
)

fun ChatMessageEntity.toDomainModel(): ChatMessage {
    return ChatMessage(
        messageId = messageId,
        conversationId = conversationId,
        senderId = senderId,
        receiverId = receiverId,
        messageType = try { MessageType.valueOf(messageType) } catch (e: Exception) { MessageType.TEXT },
        messageContent = messageContent,
        mediaUrl = mediaUrl,
        audioDurationSeconds = audioDurationSeconds,
        timestamp = timestamp,
        readStatus = try { MessageReadStatus.valueOf(readStatus) } catch (e: Exception) { MessageReadStatus.READ },
        editedStatus = editedStatus,
        deletedStatus = deletedStatus,
        moderationStatus = try { ModerationStatus.valueOf(moderationStatus) } catch (e: Exception) { ModerationStatus.SAFE },
        replyToMessageId = replyToMessageId,
        replyToContent = replyToContent,
        reactions = reactions,
        isPinned = isPinned
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        messageId = messageId,
        conversationId = conversationId,
        senderId = senderId,
        receiverId = receiverId,
        messageType = messageType.name,
        messageContent = messageContent,
        mediaUrl = mediaUrl,
        audioDurationSeconds = audioDurationSeconds,
        timestamp = timestamp,
        readStatus = readStatus.name,
        editedStatus = editedStatus,
        deletedStatus = deletedStatus,
        moderationStatus = moderationStatus.name,
        replyToMessageId = replyToMessageId,
        replyToContent = replyToContent,
        reactions = reactions,
        isPinned = isPinned
    )
}
