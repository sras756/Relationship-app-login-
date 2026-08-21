package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.DatingApp
import com.example.data.firebase.FirestoreManager
import com.example.data.firebase.RealtimeDatabaseRelayManager
import com.example.data.model.*
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

enum class CallType {
    NONE,
    VOICE,
    VIDEO
}

data class ActiveCallState(
    val type: CallType = CallType.NONE,
    val conversation: ChatConversation? = null,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isVideoFrontFacing: Boolean = true,
    val durationSeconds: Int = 0,
    val isConnected: Boolean = false
)

class ChatViewModel(
    private val chatRepository: ChatRepository = DatingApp.instance.chatRepository
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _activeConversation = MutableStateFlow<ChatConversation?>(null)
    val activeConversation: StateFlow<ChatConversation?> = _activeConversation.asStateFlow()

    private val _isPartnerTyping = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isPartnerTyping: StateFlow<Map<String, Boolean>> = _isPartnerTyping.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeCallState = MutableStateFlow(ActiveCallState())
    val activeCallState: StateFlow<ActiveCallState> = _activeCallState.asStateFlow()

    private var activeConversationMessagesJob: Job? = null
    private var activeTypingJob: Job? = null

    init {
        initChatSystem()
    }

    private fun initChatSystem() {
        val currentUid = FirestoreManager.currentUser?.uid ?: "current_user"

        // 1. Observe local Room database conversations stream
        viewModelScope.launch {
            chatRepository.getAllConversationsFlow().collectLatest { convList ->
                if (convList.isEmpty()) {
                    seedDefaultData()
                } else {
                    _conversations.value = convList
                }
            }
        }

        // 2. Start listening to Firebase Realtime Database transit messages
        chatRepository.startListeningForMessages(currentUid)
    }

    private fun seedDefaultData() {
        val now = System.currentTimeMillis()
        val initConvs = listOf(
            ChatConversation(
                conversationId = "conv_sophia",
                otherUserId = "user_sophia",
                otherUserName = "Sophia Chen",
                otherUserPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80",
                otherUserAge = 25,
                otherUserCity = "San Francisco, CA",
                isOtherUserVerified = true,
                isOtherUserOnline = true,
                otherUserLastActive = "Online now",
                lastMessage = "I'd love to try that coffee spot this weekend! ☕",
                lastMessageType = MessageType.TEXT,
                lastMessageTimestamp = now - 120_000,
                unreadCount = 1,
                isPinned = true
            ),
            ChatConversation(
                conversationId = "conv_elena",
                otherUserId = "user_elena",
                otherUserName = "Elena Rostova",
                otherUserPhoto = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=600&q=80",
                otherUserAge = 24,
                otherUserCity = "New York, NY",
                isOtherUserVerified = true,
                isOtherUserOnline = false,
                otherUserLastActive = "Active 15m ago",
                lastMessage = "Check out this sunset from my hiking trip! 🌅",
                lastMessageType = MessageType.IMAGE,
                lastMessageTimestamp = now - 3_600_000,
                unreadCount = 0,
                isPinned = false
            ),
            ChatConversation(
                conversationId = "conv_maya",
                otherUserId = "user_maya",
                otherUserName = "Maya Patel",
                otherUserPhoto = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=600&q=80",
                otherUserAge = 26,
                otherUserCity = "Austin, TX",
                isOtherUserVerified = true,
                isOtherUserOnline = true,
                otherUserLastActive = "Online now",
                lastMessage = "Haha that's hilarious 😂 what kind of music do you listen to?",
                lastMessageType = MessageType.TEXT,
                lastMessageTimestamp = now - 86_400_000,
                unreadCount = 0,
                isPinned = false
            ),
            ChatConversation(
                conversationId = "conv_lucas",
                otherUserId = "user_lucas",
                otherUserName = "Lucas Miller",
                otherUserPhoto = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=600&q=80",
                otherUserAge = 27,
                otherUserCity = "Seattle, WA",
                isOtherUserVerified = false,
                isOtherUserOnline = false,
                otherUserLastActive = "Active 2h ago",
                lastMessage = "Hey! Great profile, love your photography shots.",
                lastMessageType = MessageType.TEXT,
                lastMessageTimestamp = now - 172_800_000,
                unreadCount = 0,
                isPinned = false
            )
        )

        val initMsgs = mapOf(
            "conv_sophia" to listOf(
                ChatMessage(
                    messageId = "msg_s1",
                    conversationId = "conv_sophia",
                    senderId = "user_sophia",
                    receiverId = "current_user",
                    messageType = MessageType.TEXT,
                    messageContent = "Hey there! Loved your travel prompt about Japan! 🗾",
                    timestamp = now - 600_000,
                    readStatus = MessageReadStatus.READ
                ),
                ChatMessage(
                    messageId = "msg_s2",
                    conversationId = "conv_sophia",
                    senderId = "current_user",
                    receiverId = "user_sophia",
                    messageType = MessageType.TEXT,
                    messageContent = "Thanks Sophia! Kyoto in the spring was unforgettable. Have you visited Asia before?",
                    timestamp = now - 480_000,
                    readStatus = MessageReadStatus.READ
                ),
                ChatMessage(
                    messageId = "msg_s3",
                    conversationId = "conv_sophia",
                    senderId = "user_sophia",
                    receiverId = "current_user",
                    messageType = MessageType.TEXT,
                    messageContent = "Yes, spent 2 weeks in Tokyo! We should definitely swap travel stories.",
                    timestamp = now - 300_000,
                    readStatus = MessageReadStatus.READ
                ),
                ChatMessage(
                    messageId = "msg_s4",
                    conversationId = "conv_sophia",
                    senderId = "user_sophia",
                    receiverId = "current_user",
                    messageType = MessageType.TEXT,
                    messageContent = "I'd love to try that coffee spot this weekend! ☕",
                    timestamp = now - 120_000,
                    readStatus = MessageReadStatus.DELIVERED
                )
            ),
            "conv_elena" to listOf(
                ChatMessage(
                    messageId = "msg_e1",
                    conversationId = "conv_elena",
                    senderId = "user_elena",
                    receiverId = "current_user",
                    messageType = MessageType.TEXT,
                    messageContent = "Hey! Happy to match with you! 😊",
                    timestamp = now - 7_200_000,
                    readStatus = MessageReadStatus.READ
                ),
                ChatMessage(
                    messageId = "msg_e2",
                    conversationId = "conv_elena",
                    senderId = "user_elena",
                    receiverId = "current_user",
                    messageType = MessageType.IMAGE,
                    messageContent = "Check out this sunset from my hiking trip! 🌅",
                    mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
                    timestamp = now - 3_600_000,
                    readStatus = MessageReadStatus.READ
                )
            )
        )

        _conversations.value = initConvs
        _messages.value = initMsgs

        viewModelScope.launch {
            chatRepository.seedInitialConversationsIfEmpty(initConvs, initMsgs)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openConversation(conv: ChatConversation) {
        _activeConversation.value = conv

        // Observe messages from local Room DB
        activeConversationMessagesJob?.cancel()
        activeConversationMessagesJob = viewModelScope.launch {
            chatRepository.getMessagesForConversationFlow(conv.conversationId).collectLatest { msgList ->
                val current = _messages.value.toMutableMap()
                current[conv.conversationId] = if (msgList.isNotEmpty()) msgList else (_messages.value[conv.conversationId] ?: emptyList())
                _messages.value = current
            }
        }

        // Observe RTDB typing status for this conversation
        activeTypingJob?.cancel()
        activeTypingJob = viewModelScope.launch {
            RealtimeDatabaseRelayManager.observeTypingStatus(conv.conversationId).collectLatest { statusMap ->
                val isTyping = statusMap[conv.otherUserId] ?: false
                _isPartnerTyping.value = _isPartnerTyping.value + (conv.conversationId to isTyping)
            }
        }

        // Mark read in local Room DB and update in-memory state
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(conv.conversationId)
            val updated = _conversations.value.map {
                if (it.conversationId == conv.conversationId) it.copy(unreadCount = 0) else it
            }
            _conversations.value = updated
        }
    }

    fun closeConversation() {
        activeConversationMessagesJob?.cancel()
        activeTypingJob?.cancel()
        _activeConversation.value = null
    }

    fun startConversationWithUser(profile: UserProfile): ChatConversation {
        val existing = _conversations.value.find { it.otherUserId == profile.uid }
        if (existing != null) {
            openConversation(existing)
            return existing
        }

        val newConv = ChatConversation(
            conversationId = "conv_${profile.uid}",
            otherUserId = profile.uid,
            otherUserName = profile.displayName,
            otherUserPhoto = profile.primaryPhotoUrl.ifEmpty { profile.photoUrls.firstOrNull() ?: "" },
            otherUserAge = profile.age,
            otherUserCity = profile.city,
            isOtherUserVerified = profile.isVerified,
            isOtherUserOnline = true,
            otherUserLastActive = "Online now",
            lastMessage = "You matched! Say hello 💕",
            lastMessageType = MessageType.MATCH_EVENT,
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0
        )

        _conversations.value = listOf(newConv) + _conversations.value
        viewModelScope.launch {
            chatRepository.saveConversation(newConv)
        }
        openConversation(newConv)
        return newConv
    }

    fun setTyping(isTyping: Boolean) {
        val conv = _activeConversation.value ?: return
        val currentUid = FirestoreManager.currentUser?.uid ?: "current_user"
        RealtimeDatabaseRelayManager.setTypingStatus(conv.conversationId, currentUid, isTyping)
    }

    fun sendMessage(
        conversationId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        audioDurationSeconds: Int = 0,
        replyToMessageId: String? = null,
        replyToContent: String? = null
    ) {
        val now = System.currentTimeMillis()
        val moderation = moderateContent(content)
        val currentUid = FirestoreManager.currentUser?.uid ?: "current_user"
        val receiverId = _activeConversation.value?.otherUserId ?: ""

        val newMsg = ChatMessage(
            messageId = "msg_${UUID.randomUUID().toString().take(8)}",
            conversationId = conversationId,
            senderId = currentUid,
            receiverId = receiverId,
            messageType = messageType,
            messageContent = content,
            mediaUrl = mediaUrl,
            audioDurationSeconds = audioDurationSeconds,
            timestamp = now,
            readStatus = MessageReadStatus.SENT,
            moderationStatus = moderation,
            replyToMessageId = replyToMessageId,
            replyToContent = replyToContent
        )

        val currentList = _messages.value[conversationId] ?: emptyList()
        val updatedList = currentList + newMsg
        _messages.value = _messages.value + (conversationId to updatedList)

        // Update conversation summary
        var targetConv: ChatConversation? = null
        val updatedConvs = _conversations.value.map { conv ->
            if (conv.conversationId == conversationId) {
                val updated = conv.copy(
                    lastMessage = if (messageType == MessageType.TEXT) content else "Sent a ${messageType.name.lowercase()}",
                    lastMessageType = messageType,
                    lastMessageTimestamp = now
                )
                targetConv = updated
                updated
            } else conv
        }
        _conversations.value = updatedConvs

        // Persist to Room + Mobile Cache Files and relay through Firebase RTDB
        viewModelScope.launch {
            chatRepository.sendMessage(newMsg, targetConv)
        }

        // Trigger interactive simulated reply for demo contacts
        if (receiverId.startsWith("user_") || receiverId.startsWith("cand_")) {
            simulatePartnerResponse(conversationId, content)
        }
    }

    private fun moderateContent(text: String): ModerationStatus {
        val lower = text.lowercase()
        val scamKeywords = listOf("send money", "crypto", "bitcoin", "wire transfer", "cashapp", "western union", "gift card", "bank account", "telegram @", "whatsapp +")
        return if (scamKeywords.any { lower.contains(it) }) {
            ModerationStatus.SUSPICIOUS_SPAM
        } else {
            ModerationStatus.SAFE
        }
    }

    private fun simulatePartnerResponse(conversationId: String, userMessage: String) {
        viewModelScope.launch {
            delay(1500)
            _isPartnerTyping.value = _isPartnerTyping.value + (conversationId to true)
            delay(2500)
            _isPartnerTyping.value = _isPartnerTyping.value + (conversationId to false)

            val conv = _conversations.value.find { it.conversationId == conversationId }

            val replies = listOf(
                "That sounds wonderful! Tell me more about that 😊",
                "Haha I totally agree with you on that! ✨",
                "I love how thoughtful your answers are!",
                "Are you free for coffee or drinks sometime this week? 🥂",
                "That made my day! You have great energy."
            )
            val replyText = replies.random()

            val replyMsg = ChatMessage(
                messageId = "msg_${UUID.randomUUID().toString().take(8)}",
                conversationId = conversationId,
                senderId = conv?.otherUserId ?: "other_user",
                receiverId = FirestoreManager.currentUser?.uid ?: "current_user",
                messageType = MessageType.TEXT,
                messageContent = replyText,
                timestamp = System.currentTimeMillis(),
                readStatus = MessageReadStatus.READ
            )

            val currentList = _messages.value[conversationId] ?: emptyList()
            _messages.value = _messages.value + (conversationId to (currentList + replyMsg))

            var updatedConv: ChatConversation? = null
            _conversations.value = _conversations.value.map { c ->
                if (c.conversationId == conversationId) {
                    val u = c.copy(
                        lastMessage = replyText,
                        lastMessageType = MessageType.TEXT,
                        lastMessageTimestamp = System.currentTimeMillis()
                    )
                    updatedConv = u
                    u
                } else c
            }

            chatRepository.sendMessage(replyMsg, updatedConv)
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        val convId = _activeConversation.value?.conversationId ?: return
        val currentList = _messages.value[convId] ?: return
        val updated = currentList.map { msg ->
            if (msg.messageId == messageId) {
                val currentReactions = msg.reactions.toMutableMap()
                if (currentReactions["current_user"] == emoji) {
                    currentReactions.remove("current_user")
                } else {
                    currentReactions["current_user"] = emoji
                }
                msg.copy(reactions = currentReactions)
            } else msg
        }
        _messages.value = _messages.value + (convId to updated)
    }

    fun togglePinMessage(messageId: String) {
        val convId = _activeConversation.value?.conversationId ?: return
        val currentList = _messages.value[convId] ?: return
        var newPinnedState = false
        val updated = currentList.map { msg ->
            if (msg.messageId == messageId) {
                newPinnedState = !msg.isPinned
                msg.copy(isPinned = newPinnedState)
            } else msg
        }
        _messages.value = _messages.value + (convId to updated)
        viewModelScope.launch {
            chatRepository.togglePinMessage(messageId, newPinnedState)
        }
    }

    fun deleteMessage(messageId: String) {
        val convId = _activeConversation.value?.conversationId ?: return
        val currentList = _messages.value[convId] ?: return
        val updated = currentList.map { msg ->
            if (msg.messageId == messageId) msg.copy(deletedStatus = true, messageContent = "This message was deleted") else msg
        }
        _messages.value = _messages.value + (convId to updated)
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId, convId)
        }
    }

    fun togglePinConversation(conversationId: String) {
        var newPinned = false
        _conversations.value = _conversations.value.map {
            if (it.conversationId == conversationId) {
                newPinned = !it.isPinned
                it.copy(isPinned = newPinned)
            } else it
        }
        viewModelScope.launch {
            chatRepository.togglePinConversation(conversationId, newPinned)
        }
    }

    fun toggleMuteConversation(conversationId: String) {
        var newMuted = false
        _conversations.value = _conversations.value.map {
            if (it.conversationId == conversationId) {
                newMuted = !it.isMuted
                it.copy(isMuted = newMuted)
            } else it
        }
        viewModelScope.launch {
            chatRepository.toggleMuteConversation(conversationId, newMuted)
        }
    }

    fun toggleArchiveConversation(conversationId: String) {
        var newArchived = false
        _conversations.value = _conversations.value.map {
            if (it.conversationId == conversationId) {
                newArchived = !it.isArchived
                it.copy(isArchived = newArchived)
            } else it
        }
        viewModelScope.launch {
            chatRepository.toggleArchiveConversation(conversationId, newArchived)
        }
    }

    fun blockUser(userId: String) {
        _conversations.value = _conversations.value.map {
            if (it.otherUserId == userId) it.copy(isBlocked = true) else it
        }
        viewModelScope.launch {
            chatRepository.blockUser(userId)
        }
        closeConversation()
    }

    fun unblockUser(userId: String) {
        _conversations.value = _conversations.value.map {
            if (it.otherUserId == userId) it.copy(isBlocked = false) else it
        }
        viewModelScope.launch {
            chatRepository.unblockUser(userId)
        }
    }

    fun startCall(type: CallType, conversation: ChatConversation) {
        _activeCallState.value = ActiveCallState(
            type = type,
            conversation = conversation,
            isConnected = false,
            durationSeconds = 0
        )
        viewModelScope.launch {
            delay(1500)
            _activeCallState.value = _activeCallState.value.copy(isConnected = true)
            while (_activeCallState.value.type != CallType.NONE) {
                delay(1000)
                _activeCallState.value = _activeCallState.value.copy(
                    durationSeconds = _activeCallState.value.durationSeconds + 1
                )
            }
        }
    }

    fun toggleCallMute() {
        _activeCallState.value = _activeCallState.value.copy(isMuted = !_activeCallState.value.isMuted)
    }

    fun toggleCallSpeaker() {
        _activeCallState.value = _activeCallState.value.copy(isSpeakerOn = !_activeCallState.value.isSpeakerOn)
    }

    fun switchVideoCamera() {
        _activeCallState.value = _activeCallState.value.copy(isVideoFrontFacing = !_activeCallState.value.isVideoFrontFacing)
    }

    fun endCall() {
        _activeCallState.value = ActiveCallState(type = CallType.NONE)
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.stopListeningForMessages()
    }
}
