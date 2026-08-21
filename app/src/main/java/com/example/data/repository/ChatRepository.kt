package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.firebase.RealtimeDatabaseRelayManager
import com.example.data.local.cache.LocalChatCacheManager
import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.toDomainModel
import com.example.data.local.entity.toEntity
import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import com.example.data.model.MessageReadStatus
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Chat repository that orchestrates:
 * 1. Fast Room Database storage for instant reactive UI.
 * 2. Mobile cache files (.json files in app cache) for robust on-device backups.
 * 3. Firebase Realtime Database as an ephemeral transit relay: messages are transmitted
 *    across users and immediately deleted from the Firebase server upon delivery.
 */
class ChatRepository(
    private val chatDao: ChatDao,
    context: Context
) {
    companion object {
        private const val TAG = "ChatRepository"
    }

    private val localCacheManager = LocalChatCacheManager(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    private var activeTransitListener: ValueEventListener? = null
    private var listeningUid: String? = null

    /**
     * Observes conversations from local Room database.
     */
    fun getAllConversationsFlow(): Flow<List<ChatConversation>> {
        return chatDao.getAllConversationsFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Observes messages for a conversation from local Room database.
     */
    fun getMessagesForConversationFlow(conversationId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForConversationFlow(conversationId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Retrieves messages from local Room cache, falling back to local mobile cache files.
     */
    suspend fun getMessages(conversationId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        val roomMessages = chatDao.getMessagesForConversation(conversationId).map { it.toDomainModel() }
        if (roomMessages.isNotEmpty()) {
            return@withContext roomMessages
        }
        // Fallback to local mobile cache file
        val cachedFiles = localCacheManager.loadMessagesFromCacheFile(conversationId)
        if (cachedFiles.isNotEmpty()) {
            chatDao.insertOrUpdateMessages(cachedFiles.map { it.toEntity() })
        }
        cachedFiles
    }

    /**
     * Sends a message:
     * 1. Stores immediately in local Room database.
     * 2. Stores in local mobile cache file on device.
     * 3. Relays through Firebase Realtime Database transit to recipient.
     */
    suspend fun sendMessage(
        message: ChatMessage,
        conversation: ChatConversation? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Save locally in Room
            chatDao.insertOrUpdateMessage(message.toEntity())

            if (conversation != null) {
                chatDao.insertOrUpdateConversation(conversation.toEntity())
            }

            // 2. Backup to local mobile cache files on device
            val allMessages = chatDao.getMessagesForConversation(message.conversationId).map { it.toDomainModel() }
            localCacheManager.saveMessagesToCacheFile(message.conversationId, allMessages)

            val allConversations = chatDao.getAllConversations().map { it.toDomainModel() }
            localCacheManager.saveConversationsToCacheFile(allConversations)

            // 3. Relay through Firebase Realtime Database ephemeral transit
            if (message.receiverId.isNotBlank()) {
                RealtimeDatabaseRelayManager.relayMessage(message)
            }
            Log.d(TAG, "Message ${message.messageId} saved locally and queued into transit relay.")
            Unit
        }
    }

    /**
     * Handles incoming transit message received from Firebase Realtime Database:
     * 1. Saves to local Room DB.
     * 2. Backs up to local mobile cache file.
     * 3. Updates conversation summary.
     */
    suspend fun onIncomingTransitMessageReceived(message: ChatMessage) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Processing incoming ephemeral transit message: ${message.messageId}")

        // 1. Save message to Room
        chatDao.insertOrUpdateMessage(message.toEntity())

        // 2. Update or create conversation locally
        val existingConv = chatDao.getConversationById(message.conversationId)
        if (existingConv != null) {
            val updatedConv = existingConv.copy(
                lastMessage = message.messageContent,
                lastMessageType = message.messageType.name,
                lastMessageTimestamp = message.timestamp,
                unreadCount = existingConv.unreadCount + 1
            )
            chatDao.insertOrUpdateConversation(updatedConv)
        } else {
            val newConv = ChatConversation(
                conversationId = message.conversationId,
                otherUserId = message.senderId,
                otherUserName = "Match User",
                lastMessage = message.messageContent,
                lastMessageType = message.messageType,
                lastMessageTimestamp = message.timestamp,
                unreadCount = 1
            )
            chatDao.insertOrUpdateConversation(newConv.toEntity())
        }

        // 3. Save to local mobile cache file
        val allMessages = chatDao.getMessagesForConversation(message.conversationId).map { it.toDomainModel() }
        localCacheManager.saveMessagesToCacheFile(message.conversationId, allMessages)

        val allConversations = chatDao.getAllConversations().map { it.toDomainModel() }
        localCacheManager.saveConversationsToCacheFile(allConversations)
    }

    /**
     * Starts listening to incoming messages for the active user via Firebase Realtime Database.
     * Delivered messages are deleted from Firebase server immediately on arrival.
     */
    fun startListeningForMessages(userUid: String) {
        if (listeningUid == userUid && activeTransitListener != null) return

        stopListeningForMessages()
        listeningUid = userUid

        activeTransitListener = RealtimeDatabaseRelayManager.startEphemeralTransitListener(userUid) { incomingMsg ->
            scope.launch {
                onIncomingTransitMessageReceived(incomingMsg)
            }
        }
        RealtimeDatabaseRelayManager.setOnlinePresence(userUid)
        Log.d(TAG, "Started listening to transit inbox for UID: $userUid")
    }

    /**
     * Stops listening to the transit inbox.
     */
    fun stopListeningForMessages() {
        val uid = listeningUid
        val listener = activeTransitListener
        if (uid != null && listener != null) {
            RealtimeDatabaseRelayManager.stopEphemeralTransitListener(uid, listener)
            activeTransitListener = null
            listeningUid = null
            Log.d(TAG, "Stopped listening to transit inbox.")
        }
    }

    /**
     * Seeds initial conversation data into local Room and mobile cache files if empty.
     */
    suspend fun seedInitialConversationsIfEmpty(initialList: List<ChatConversation>, initialMsgs: Map<String, List<ChatMessage>>) = withContext(Dispatchers.IO) {
        val existing = chatDao.getAllConversations()
        if (existing.isEmpty()) {
            chatDao.insertOrUpdateConversations(initialList.map { it.toEntity() })
            localCacheManager.saveConversationsToCacheFile(initialList)

            initialMsgs.forEach { (convId, msgs) ->
                chatDao.insertOrUpdateMessages(msgs.map { it.toEntity() })
                localCacheManager.saveMessagesToCacheFile(convId, msgs)
            }
            Log.d(TAG, "Seeded initial chat conversations into Room and mobile cache files.")
        }
    }

    suspend fun markMessagesAsRead(conversationId: String) = withContext(Dispatchers.IO) {
        chatDao.markMessagesAsRead(conversationId, MessageReadStatus.READ.name)
        chatDao.clearUnreadCount(conversationId)
    }

    suspend fun saveConversation(conversation: ChatConversation) = withContext(Dispatchers.IO) {
        chatDao.insertOrUpdateConversation(conversation.toEntity())
    }

    suspend fun deleteMessage(messageId: String, conversationId: String) = withContext(Dispatchers.IO) {
        chatDao.markMessageAsDeleted(messageId)
        val msgs = chatDao.getMessagesForConversation(conversationId).map { it.toDomainModel() }
        localCacheManager.saveMessagesToCacheFile(conversationId, msgs)
    }

    suspend fun togglePinMessage(messageId: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        chatDao.updateMessagePinned(messageId, isPinned)
    }

    suspend fun togglePinConversation(conversationId: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        chatDao.updateConversationPinned(conversationId, isPinned)
    }

    suspend fun toggleMuteConversation(conversationId: String, isMuted: Boolean) = withContext(Dispatchers.IO) {
        chatDao.updateConversationMuted(conversationId, isMuted)
    }

    suspend fun toggleArchiveConversation(conversationId: String, isArchived: Boolean) = withContext(Dispatchers.IO) {
        chatDao.updateConversationArchived(conversationId, isArchived)
    }

    suspend fun blockUser(userId: String) = withContext(Dispatchers.IO) {
        chatDao.updateConversationBlocked(userId, true)
    }

    suspend fun unblockUser(userId: String) = withContext(Dispatchers.IO) {
        chatDao.updateConversationBlocked(userId, false)
    }
}
