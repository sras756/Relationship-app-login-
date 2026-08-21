package com.example.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Custom exceptions for secure authentication and authorization handling.
 */
class UnauthenticatedUserException(message: String = "User is not authenticated with Firebase.") : SecurityException(message)
class UnauthorizedProfileAccessException(message: String = "User is not authorized to access or modify this profile.") : SecurityException(message)

/**
 * Singleton helper class for secure Firebase and Firestore initialization and session handling.
 */
object FirestoreManager {
    private const val TAG = "FirestoreManager"
    const val USERS_COLLECTION = "users"
    const val CONVERSATIONS_COLLECTION = "conversations"
    const val MESSAGES_COLLECTION = "messages"
    const val COMMUNITY_ROOMS_COLLECTION = "community_rooms"

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val firestore: FirebaseFirestore by lazy {
        val db = FirebaseFirestore.getInstance()
        try {
            // Configure offline persistent cache for responsive UI and resilience
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                )
                .build()
            db.firestoreSettings = settings
            Log.d(TAG, "Firestore initialized with persistent offline cache.")
        } catch (e: Exception) {
            Log.w(TAG, "Firestore settings were already initialized or could not be updated: ${e.message}")
        }
        db
    }

    /**
     * Returns current authenticated Firebase user or null if not signed in.
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Checks if a valid user session is active.
     */
    val isAuthenticated: Boolean
        get() = auth.currentUser != null

    /**
     * Returns the UID of the current user, or throws [UnauthenticatedUserException] if unauthenticated.
     */
    fun requireCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw UnauthenticatedUserException()
    }

    /**
     * Verifies that the requested UID matches the authenticated session user.
     */
    fun verifyUserSession(targetUid: String) {
        val currentUid = requireCurrentUserId()
        if (currentUid != targetUid) {
            throw UnauthorizedProfileAccessException("Session UID ($currentUid) does not match target UID ($targetUid)")
        }
    }

    /**
     * Reactive stream of Firebase authentication state changes.
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * Safely signs out the current user session.
     */
    fun signOut() {
        auth.signOut()
    }
}
