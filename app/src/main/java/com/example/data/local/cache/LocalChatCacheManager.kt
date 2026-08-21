package com.example.data.local.cache

import android.content.Context
import android.util.Log
import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

/**
 * Manages on-device mobile cache files for chat conversations and message history.
 * Ensures zero-cloud local privacy: all chats are permanently stored in local device cache files.
 */
class LocalChatCacheManager(private val context: Context) {

    companion object {
        private const val TAG = "LocalChatCacheManager"
        private const val CHAT_CACHE_DIR = "chat_mobile_cache"
        private const val CONVERSATIONS_INDEX_FILE = "conversations_index.json"
    }

    private val cacheDirectory: File by lazy {
        val dir = File(context.cacheDir, CHAT_CACHE_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    /**
     * Persists a list of messages for a specific conversation into a local mobile cache JSON file.
     */
    fun saveMessagesToCacheFile(conversationId: String, messages: List<ChatMessage>) {
        try {
            val file = File(cacheDirectory, "messages_${conversationId}.json")
            val jsonArray = JSONArray()

            for (msg in messages) {
                val obj = JSONObject().apply {
                    put("messageId", msg.messageId)
                    put("conversationId", msg.conversationId)
                    put("senderId", msg.senderId)
                    put("receiverId", msg.receiverId)
                    put("messageType", msg.messageType.name)
                    put("messageContent", msg.messageContent)
                    put("mediaUrl", msg.mediaUrl)
                    put("audioDurationSeconds", msg.audioDurationSeconds)
                    put("timestamp", msg.timestamp)
                    put("readStatus", msg.readStatus.name)
                    put("editedStatus", msg.editedStatus)
                    put("deletedStatus", msg.deletedStatus)
                    put("moderationStatus", msg.moderationStatus.name)
                    put("replyToMessageId", msg.replyToMessageId ?: "")
                    put("replyToContent", msg.replyToContent ?: "")
                    put("isPinned", msg.isPinned)
                }
                jsonArray.put(obj)
            }

            FileOutputStream(file).use { fos ->
                fos.write(jsonArray.toString().toByteArray(Charsets.UTF_8))
                fos.flush()
            }
            Log.d(TAG, "Cached ${messages.size} messages in mobile cache file: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving messages to mobile cache file: ${e.message}", e)
        }
    }

    /**
     * Reads messages for a conversation from its mobile cache file.
     */
    fun loadMessagesFromCacheFile(conversationId: String): List<ChatMessage> {
        val file = File(cacheDirectory, "messages_${conversationId}.json")
        if (!file.exists()) return emptyList()

        return try {
            val content = file.readText(Charsets.UTF_8)
            val jsonArray = JSONArray(content)
            val list = mutableListOf<ChatMessage>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val msg = ChatMessage(
                    messageId = obj.optString("messageId"),
                    conversationId = obj.optString("conversationId"),
                    senderId = obj.optString("senderId"),
                    receiverId = obj.optString("receiverId"),
                    messageType = try { com.example.data.model.MessageType.valueOf(obj.optString("messageType")) } catch (e: Exception) { com.example.data.model.MessageType.TEXT },
                    messageContent = obj.optString("messageContent"),
                    mediaUrl = obj.optString("mediaUrl"),
                    audioDurationSeconds = obj.optInt("audioDurationSeconds", 0),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    readStatus = try { com.example.data.model.MessageReadStatus.valueOf(obj.optString("readStatus")) } catch (e: Exception) { com.example.data.model.MessageReadStatus.READ },
                    editedStatus = obj.optBoolean("editedStatus", false),
                    deletedStatus = obj.optBoolean("deletedStatus", false),
                    moderationStatus = try { com.example.data.model.ModerationStatus.valueOf(obj.optString("moderationStatus")) } catch (e: Exception) { com.example.data.model.ModerationStatus.SAFE },
                    replyToMessageId = obj.optString("replyToMessageId").ifEmpty { null },
                    replyToContent = obj.optString("replyToContent").ifEmpty { null },
                    isPinned = obj.optBoolean("isPinned", false)
                )
                list.add(msg)
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error loading messages from mobile cache file: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Persists all active chat conversations into the mobile cache index file.
     */
    fun saveConversationsToCacheFile(conversations: List<ChatConversation>) {
        try {
            val file = File(cacheDirectory, CONVERSATIONS_INDEX_FILE)
            val jsonArray = JSONArray()

            for (c in conversations) {
                val obj = JSONObject().apply {
                    put("conversationId", c.conversationId)
                    put("otherUserId", c.otherUserId)
                    put("otherUserName", c.otherUserName)
                    put("otherUserPhoto", c.otherUserPhoto)
                    put("otherUserAge", c.otherUserAge)
                    put("otherUserCity", c.otherUserCity)
                    put("isOtherUserVerified", c.isOtherUserVerified)
                    put("isOtherUserOnline", c.isOtherUserOnline)
                    put("otherUserLastActive", c.otherUserLastActive)
                    put("lastMessage", c.lastMessage)
                    put("lastMessageType", c.lastMessageType.name)
                    put("lastMessageTimestamp", c.lastMessageTimestamp)
                    put("unreadCount", c.unreadCount)
                    put("isPinned", c.isPinned)
                    put("isMuted", c.isMuted)
                    put("isArchived", c.isArchived)
                    put("isBlocked", c.isBlocked)
                }
                jsonArray.put(obj)
            }

            FileOutputStream(file).use { fos ->
                fos.write(jsonArray.toString().toByteArray(Charsets.UTF_8))
                fos.flush()
            }
            Log.d(TAG, "Cached ${conversations.size} conversations in mobile cache index file.")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving conversations to mobile cache file: ${e.message}", e)
        }
    }

    /**
     * Loads all chat conversations from the mobile cache index file.
     */
    fun loadConversationsFromCacheFile(): List<ChatConversation> {
        val file = File(cacheDirectory, CONVERSATIONS_INDEX_FILE)
        if (!file.exists()) return emptyList()

        return try {
            val content = file.readText(Charsets.UTF_8)
            val jsonArray = JSONArray(content)
            val list = mutableListOf<ChatConversation>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val conv = ChatConversation(
                    conversationId = obj.optString("conversationId"),
                    otherUserId = obj.optString("otherUserId"),
                    otherUserName = obj.optString("otherUserName"),
                    otherUserPhoto = obj.optString("otherUserPhoto"),
                    otherUserAge = obj.optInt("otherUserAge", 24),
                    otherUserCity = obj.optString("otherUserCity"),
                    isOtherUserVerified = obj.optBoolean("isOtherUserVerified", false),
                    isOtherUserOnline = obj.optBoolean("isOtherUserOnline", true),
                    otherUserLastActive = obj.optString("otherUserLastActive", "Online now"),
                    lastMessage = obj.optString("lastMessage"),
                    lastMessageType = try { com.example.data.model.MessageType.valueOf(obj.optString("lastMessageType")) } catch (e: Exception) { com.example.data.model.MessageType.TEXT },
                    lastMessageTimestamp = obj.optLong("lastMessageTimestamp", System.currentTimeMillis()),
                    unreadCount = obj.optInt("unreadCount", 0),
                    isPinned = obj.optBoolean("isPinned", false),
                    isMuted = obj.optBoolean("isMuted", false),
                    isArchived = obj.optBoolean("isArchived", false),
                    isBlocked = obj.optBoolean("isBlocked", false)
                )
                list.add(conv)
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error loading conversations from mobile cache file: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Clears all local chat cache files from device storage.
     */
    fun clearAllCacheFiles() {
        try {
            cacheDirectory.listFiles()?.forEach { it.delete() }
            Log.d(TAG, "Cleared all local mobile chat cache files.")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache files: ${e.message}", e)
        }
    }
}
