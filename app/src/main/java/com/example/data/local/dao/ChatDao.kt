package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ChatConversationEntity
import com.example.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // --- Messages ---
    @Query("SELECT * FROM local_chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversationFlow(conversationId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM local_chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversation(conversationId: String): List<ChatMessageEntity>

    @Query("SELECT * FROM local_chat_messages WHERE messageId = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE local_chat_messages SET readStatus = :status WHERE conversationId = :conversationId")
    suspend fun markMessagesAsRead(conversationId: String, status: String = "READ")

    @Query("UPDATE local_chat_messages SET deletedStatus = 1, messageContent = 'This message was deleted' WHERE messageId = :messageId")
    suspend fun markMessageAsDeleted(messageId: String)

    @Query("UPDATE local_chat_messages SET isPinned = :isPinned WHERE messageId = :messageId")
    suspend fun updateMessagePinned(messageId: String, isPinned: Boolean)

    @Query("DELETE FROM local_chat_messages WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM local_chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    // --- Conversations ---
    @Query("SELECT * FROM local_chat_conversations ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getAllConversationsFlow(): Flow<List<ChatConversationEntity>>

    @Query("SELECT * FROM local_chat_conversations ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    suspend fun getAllConversations(): List<ChatConversationEntity>

    @Query("SELECT * FROM local_chat_conversations WHERE conversationId = :conversationId LIMIT 1")
    suspend fun getConversationById(conversationId: String): ChatConversationEntity?

    @Query("SELECT * FROM local_chat_conversations WHERE otherUserId = :otherUserId LIMIT 1")
    suspend fun getConversationByUserId(otherUserId: String): ChatConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConversation(conversation: ChatConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConversations(conversations: List<ChatConversationEntity>)

    @Query("UPDATE local_chat_conversations SET isPinned = :isPinned WHERE conversationId = :conversationId")
    suspend fun updateConversationPinned(conversationId: String, isPinned: Boolean)

    @Query("UPDATE local_chat_conversations SET isMuted = :isMuted WHERE conversationId = :conversationId")
    suspend fun updateConversationMuted(conversationId: String, isMuted: Boolean)

    @Query("UPDATE local_chat_conversations SET isArchived = :isArchived WHERE conversationId = :conversationId")
    suspend fun updateConversationArchived(conversationId: String, isArchived: Boolean)

    @Query("UPDATE local_chat_conversations SET isBlocked = :isBlocked WHERE otherUserId = :userId")
    suspend fun updateConversationBlocked(userId: String, isBlocked: Boolean)

    @Query("UPDATE local_chat_conversations SET unreadCount = 0 WHERE conversationId = :conversationId")
    suspend fun clearUnreadCount(conversationId: String)

    @Query("DELETE FROM local_chat_conversations WHERE conversationId = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("DELETE FROM local_chat_messages")
    suspend fun clearAllMessages()

    @Query("DELETE FROM local_chat_conversations")
    suspend fun clearAllConversations()
}
