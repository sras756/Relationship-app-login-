package com.example.data.firebase

import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.MessageReadStatus
import com.example.data.model.MessageType
import com.example.data.model.ModerationStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Ephemeral Firebase Realtime Database Transit Relay.
 * 
 * Messages are relayed through Realtime Database strictly for instant transmission.
 * As soon as a message is delivered to the recipient client, it is immediately deleted
 * from Firebase Realtime Database and stored purely in on-device local storage / mobile cache files.
 */
object RealtimeDatabaseRelayManager {

    private const val TAG = "RTDBRelayManager"
    private const val TRANSIT_INBOX_NODE = "transit_inbox"
    private const val TYPING_NODE = "typing_signals"
    private const val PRESENCE_NODE = "user_presence"

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val rtdb: FirebaseDatabase by lazy {
        val database = FirebaseDatabase.getInstance()
        try {
            // Ephemeral signaling does not need local RTDB disk persistence because
            // all message history is preserved in Room & mobile cache files.
            database.setPersistenceEnabled(false)
        } catch (e: Exception) {
            Log.w(TAG, "RTDB persistence already configured: ${e.message}")
        }
        database
    }

    /**
     * Relays a message through Firebase Realtime Database to the recipient's transit inbox.
     */
    suspend fun relayMessage(message: ChatMessage): Result<Unit> {
        return runCatching {
            val receiverId = message.receiverId
            if (receiverId.isBlank()) {
                throw IllegalArgumentException("Receiver ID cannot be blank for message transit relay.")
            }

            val transitRef = rtdb.getReference(TRANSIT_INBOX_NODE)
                .child(receiverId)
                .child(message.messageId)

            val payload = mapOf(
                "messageId" to message.messageId,
                "conversationId" to message.conversationId,
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "messageType" to message.messageType.name,
                "messageContent" to message.messageContent,
                "mediaUrl" to message.mediaUrl,
                "audioDurationSeconds" to message.audioDurationSeconds,
                "timestamp" to message.timestamp,
                "readStatus" to message.readStatus.name,
                "editedStatus" to message.editedStatus,
                "deletedStatus" to message.deletedStatus,
                "moderationStatus" to message.moderationStatus.name,
                "replyToMessageId" to (message.replyToMessageId ?: ""),
                "replyToContent" to (message.replyToContent ?: "")
            )

            transitRef.setValue(payload).await()
            Log.d(TAG, "Message ${message.messageId} queued in transit relay for recipient $receiverId")
        }
    }

    /**
     * Listens for incoming transit messages for the current user.
     * When received, passes to [onMessageReceived] and IMMEDIATELY deletes from Realtime Database
     * to ensure zero cloud retention (stored only in local mobile cache).
     */
    fun startEphemeralTransitListener(
        recipientUid: String,
        onMessageReceived: (ChatMessage) -> Unit
    ): ValueEventListener {
        val inboxRef = rtdb.getReference(TRANSIT_INBOX_NODE).child(recipientUid)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                for (child in snapshot.children) {
                    try {
                        val messageId = child.child("messageId").getValue(String::class.java) ?: child.key ?: continue
                        val conversationId = child.child("conversationId").getValue(String::class.java) ?: ""
                        val senderId = child.child("senderId").getValue(String::class.java) ?: ""
                        val receiverId = child.child("receiverId").getValue(String::class.java) ?: recipientUid
                        val msgTypeStr = child.child("messageType").getValue(String::class.java) ?: MessageType.TEXT.name
                        val content = child.child("messageContent").getValue(String::class.java) ?: ""
                        val mediaUrl = child.child("mediaUrl").getValue(String::class.java) ?: ""
                        val audioDuration = child.child("audioDurationSeconds").getValue(Int::class.java) ?: 0
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                        val readStatusStr = child.child("readStatus").getValue(String::class.java) ?: MessageReadStatus.DELIVERED.name
                        val replyToId = child.child("replyToMessageId").getValue(String::class.java)?.ifEmpty { null }
                        val replyToText = child.child("replyToContent").getValue(String::class.java)?.ifEmpty { null }

                        val message = ChatMessage(
                            messageId = messageId,
                            conversationId = conversationId,
                            senderId = senderId,
                            receiverId = receiverId,
                            messageType = try { MessageType.valueOf(msgTypeStr) } catch (e: Exception) { MessageType.TEXT },
                            messageContent = content,
                            mediaUrl = mediaUrl,
                            audioDurationSeconds = audioDuration,
                            timestamp = timestamp,
                            readStatus = try { MessageReadStatus.valueOf(readStatusStr) } catch (e: Exception) { MessageReadStatus.DELIVERED },
                            replyToMessageId = replyToId,
                            replyToContent = replyToText
                        )

                        // 1. Deliver to local storage handler
                        onMessageReceived(message)

                        // 2. CRITICAL: Immediately delete from Firebase RTDB so it is NOT stored in the cloud
                        child.ref.removeValue().addOnSuccessListener {
                            Log.d(TAG, "Ephemeral message $messageId wiped from Firebase RTDB server. Preserved in mobile cache.")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing ephemeral message: ${e.message}", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Ephemeral transit listener cancelled: ${error.message}")
            }
        }

        inboxRef.addValueEventListener(listener)
        return listener
    }

    /**
     * Stops listening to the transit inbox.
     */
    fun stopEphemeralTransitListener(recipientUid: String, listener: ValueEventListener) {
        val inboxRef = rtdb.getReference(TRANSIT_INBOX_NODE).child(recipientUid)
        inboxRef.removeEventListener(listener)
    }

    /**
     * Broadcasts typing status in Firebase RTDB with automatic onDisconnect cleanup.
     */
    fun setTypingStatus(conversationId: String, userId: String, isTyping: Boolean) {
        try {
            val typingRef = rtdb.getReference(TYPING_NODE).child(conversationId).child(userId)
            if (isTyping) {
                typingRef.setValue(true)
                typingRef.onDisconnect().removeValue()
            } else {
                typingRef.removeValue()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error setting typing status: ${e.message}")
        }
    }

    /**
     * Observes typing status for a conversation from Realtime Database.
     */
    fun observeTypingStatus(conversationId: String): Flow<Map<String, Boolean>> = callbackFlow {
        val typingRef = rtdb.getReference(TYPING_NODE).child(conversationId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = mutableMapOf<String, Boolean>()
                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    val isTyping = child.getValue(Boolean::class.java) ?: false
                    map[uid] = isTyping
                }
                trySend(map)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        typingRef.addValueEventListener(listener)
        awaitClose {
            typingRef.removeEventListener(listener)
        }
    }

    /**
     * Registers user online presence in Realtime Database.
     */
    fun setOnlinePresence(userId: String) {
        try {
            val presenceRef = rtdb.getReference(PRESENCE_NODE).child(userId)
            val infoConnectedRef = rtdb.getReference(".info/connected")

            infoConnectedRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    if (connected) {
                        presenceRef.setValue(mapOf(
                            "isOnline" to true,
                            "lastActive" to System.currentTimeMillis()
                        ))
                        presenceRef.onDisconnect().setValue(mapOf(
                            "isOnline" to false,
                            "lastActive" to ServerValue.TIMESTAMP
                        ))
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            Log.w(TAG, "Error configuring user presence: ${e.message}")
        }
    }
}
