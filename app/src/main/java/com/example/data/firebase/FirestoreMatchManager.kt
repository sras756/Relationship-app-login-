package com.example.data.firebase

import android.util.Log
import com.example.data.model.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Handles Firestore-based matching engine, swiping persistence,
 * potential connection records, and mutual match detections.
 */
object FirestoreMatchManager {
    private const val TAG = "FirestoreMatchManager"
    const val INTERACTIONS_COLLECTION = "interactions"
    const val MATCHES_COLLECTION = "matches"
    const val POTENTIAL_CONNECTIONS_COLLECTION = "potential_connections"

    private val db get() = FirestoreManager.firestore

    /**
     * Records a swipe interaction (LIKE, DISLIKE, SUPER_LIKE) into Firestore.
     * Checks if the other user has already liked the current user.
     * Returns true if this interaction triggered a MUTUAL MATCH!
     */
    suspend fun recordSwipeInteraction(
        fromUserId: String,
        targetProfile: UserProfile,
        action: InteractionType,
        introNote: String = ""
    ): Result<Boolean> {
        return runCatching {
            val toUserId = targetProfile.uid
            val interactionDocId = "${fromUserId}_${toUserId}"
            val timestamp = System.currentTimeMillis()

            val interactionData = hashMapOf(
                "interactionId" to interactionDocId,
                "fromUserId" to fromUserId,
                "toUserId" to toUserId,
                "action" to action.name,
                "introNote" to introNote,
                "targetDisplayName" to targetProfile.displayName,
                "targetPhotoUrl" to targetProfile.primaryPhotoUrl,
                "targetNationality" to targetProfile.nationality,
                "timestamp" to timestamp
            )

            // 1. Save interaction to Firestore
            db.collection(INTERACTIONS_COLLECTION)
                .document(interactionDocId)
                .set(interactionData, SetOptions.merge())
                .await()

            Log.d(TAG, "Recorded interaction: $fromUserId -> $toUserId ($action)")

            // Also record potential connection request in subcollection or index
            if (action == InteractionType.LIKE || action == InteractionType.SUPER_LIKE) {
                val connectionData = hashMapOf(
                    "id" to interactionDocId,
                    "senderUserId" to fromUserId,
                    "receiverUserId" to toUserId,
                    "introNote" to introNote,
                    "isSuperLike" to (action == InteractionType.SUPER_LIKE),
                    "timestamp" to timestamp,
                    "status" to "PENDING"
                )
                db.collection(POTENTIAL_CONNECTIONS_COLLECTION)
                    .document(interactionDocId)
                    .set(connectionData, SetOptions.merge())
                    .await()
            }

            // 2. Check for mutual match if action was positive (LIKE or SUPER_LIKE)
            if (action == InteractionType.LIKE || action == InteractionType.SUPER_LIKE) {
                val reverseDocId = "${toUserId}_${fromUserId}"
                val reverseSnapshot = try {
                    db.collection(INTERACTIONS_COLLECTION)
                        .document(reverseDocId)
                        .get()
                        .await()
                } catch (e: Exception) {
                    Log.w(TAG, "Could not fetch reverse interaction: ${e.message}")
                    null
                }

                if (reverseSnapshot != null && reverseSnapshot.exists()) {
                    val reverseAction = reverseSnapshot.getString("action") ?: ""
                    if (reverseAction == InteractionType.LIKE.name || reverseAction == InteractionType.SUPER_LIKE.name) {
                        // IT'S A MUTUAL MATCH!
                        Log.d(TAG, "🎉 MUTUAL MATCH DETECTED between $fromUserId and $toUserId!")
                        createMutualMatchRecord(fromUserId, targetProfile)
                        return@runCatching true
                    }
                }
            }

            false
        }
    }

    /**
     * Creates a mutual match document in Firestore when both users have liked each other.
     */
    private suspend fun createMutualMatchRecord(
        currentUserId: String,
        otherUser: UserProfile
    ) {
        val user1 = minOf(currentUserId, otherUser.uid)
        val user2 = maxOf(currentUserId, otherUser.uid)
        val matchId = "${user1}_${user2}"
        val timestamp = System.currentTimeMillis()

        val matchData = hashMapOf(
            "matchId" to matchId,
            "user1Id" to user1,
            "user2Id" to user2,
            "matchedAt" to timestamp,
            "lastActive" to timestamp,
            "isMutual" to true,
            "participants" to listOf(user1, user2)
        )

        db.collection(MATCHES_COLLECTION)
            .document(matchId)
            .set(matchData, SetOptions.merge())
            .await()

        // Create or update conversation room in Firestore conversations collection
        val conversationData = hashMapOf(
            "conversationId" to "conv_$matchId",
            "participants" to listOf(user1, user2),
            "createdAt" to timestamp,
            "lastMessage" to "You matched! Say hello 💕",
            "lastMessageTimestamp" to timestamp
        )
        db.collection("conversations")
            .document("conv_$matchId")
            .set(conversationData, SetOptions.merge())
            .await()

        Log.d(TAG, "Successfully created mutual match & conversation: $matchId")
    }

    /**
     * Returns a list of target user IDs that the current user has already swiped on (liked, passed, or super-liked).
     */
    suspend fun getSwipedUserIds(currentUserId: String): Set<String> {
        return try {
            val snapshot = db.collection(INTERACTIONS_COLLECTION)
                .whereEqualTo("fromUserId", currentUserId)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.getString("toUserId") }.toSet()
        } catch (e: Exception) {
            Log.w(TAG, "Error retrieving swiped user IDs: ${e.message}")
            emptySet()
        }
    }

    /**
     * Observes real-time incoming connection requests and likes from other users.
     */
    fun observeIncomingLikes(currentUserId: String): Flow<List<PotentialConnection>> = callbackFlow {
        val query = db.collection(INTERACTIONS_COLLECTION)
            .whereEqualTo("toUserId", currentUserId)

        val listener: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing incoming likes: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    val actionStr = doc.getString("action") ?: ""
                    if (actionStr == InteractionType.LIKE.name || actionStr == InteractionType.SUPER_LIKE.name) {
                        PotentialConnection(
                            id = doc.id,
                            fromUserId = doc.getString("fromUserId") ?: "",
                            toUserId = doc.getString("toUserId") ?: "",
                            action = try { InteractionType.valueOf(actionStr) } catch (e: Exception) { InteractionType.LIKE },
                            introNote = doc.getString("introNote") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                    } else null
                }
                trySend(list)
            }
        }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Observes mutual matches involving the current user.
     */
    fun observeMutualMatches(currentUserId: String): Flow<List<MutualMatch>> = callbackFlow {
        val query = db.collection(MATCHES_COLLECTION)
            .whereArrayContains("participants", currentUserId)

        val listener: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing mutual matches: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    val matchId = doc.getString("matchId") ?: doc.id
                    val u1 = doc.getString("user1Id") ?: ""
                    val u2 = doc.getString("user2Id") ?: ""
                    val otherUid = if (u1 == currentUserId) u2 else u1
                    val matchedAt = doc.getLong("matchedAt") ?: System.currentTimeMillis()

                    MutualMatch(
                        matchId = matchId,
                        user1Id = u1,
                        user2Id = u2,
                        matchedAt = matchedAt,
                        otherUserProfile = UserProfile(uid = otherUid)
                    )
                }
                trySend(list)
            }
        }

        awaitClose {
            listener.remove()
        }
    }
}
