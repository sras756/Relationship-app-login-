package com.example.data.repository

import android.util.Log
import com.example.data.firebase.FirestoreManager
import com.example.data.firebase.UnauthenticatedUserException
import com.example.data.firebase.UnauthorizedProfileAccessException
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.toDomainModel
import com.example.data.local.entity.toEntity
import com.example.data.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Data repository that provides secure methods to save, retrieve, and synchronize
 * user profiles between Firebase Firestore and the local Room database cache.
 */
class UserProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val firestoreManager: FirestoreManager = FirestoreManager
) {
    companion object {
        private const val TAG = "UserProfileRepository"
    }

    /**
     * Local Room database reactive stream for the latest user profile.
     */
    val latestProfileFlow: Flow<UserProfile?> = userProfileDao.getLatestUserProfileFlow()
        .map { it?.toDomainModel() }

    /**
     * Local Room database reactive stream for a specific UID.
     */
    fun getCachedUserProfileFlow(uid: String): Flow<UserProfile?> {
        return userProfileDao.getUserProfileFlow(uid).map { it?.toDomainModel() }
    }

    /**
     * Retrieves the profile from the local Room database cache.
     */
    suspend fun getCachedProfile(uid: String): UserProfile? {
        return userProfileDao.getUserProfile(uid)?.toDomainModel()
    }

    /**
     * Retrieves the latest cached profile entity.
     */
    suspend fun getLatestCachedProfile(): UserProfileEntity? {
        return userProfileDao.getLatestUserProfile()
    }

    /**
     * Saves user profile both to Cloud Firestore and the local Room database.
     * Enforces that the session user matches the profile UID.
     */
    suspend fun saveUserProfile(profile: UserProfile, step: Int = 0): Result<Unit> {
        return runCatching {
            val sessionUid = firestoreManager.requireCurrentUserId()
            val targetUid = profile.uid.ifBlank { sessionUid }

            // Security check: ensure user is writing to their own profile
            if (sessionUid != targetUid) {
                throw UnauthorizedProfileAccessException("Authenticated user $sessionUid cannot modify profile for $targetUid")
            }

            val finalProfile = profile.copy(uid = targetUid)

            // 1. Persist to local Room cache immediately
            userProfileDao.insertOrUpdateProfile(finalProfile.toEntity(onboardingStep = step))

            // 2. Persist to Cloud Firestore
            firestoreManager.firestore
                .collection(FirestoreManager.USERS_COLLECTION)
                .document(targetUid)
                .set(finalProfile, SetOptions.merge())
                .await()

            Log.d(TAG, "User profile successfully saved to Firestore and local Room cache for UID: $targetUid")
        }
    }

    /**
     * Retrieves user profile from Cloud Firestore with automatic local cache fallback.
     */
    suspend fun getUserProfile(uid: String? = null, forceRemote: Boolean = false): Result<UserProfile?> {
        return runCatching {
            val targetUid = uid ?: firestoreManager.requireCurrentUserId()

            if (!forceRemote) {
                val cached = userProfileDao.getUserProfile(targetUid)
                if (cached != null) {
                    return@runCatching cached.toDomainModel()
                }
            }

            try {
                val snapshot = firestoreManager.firestore
                    .collection(FirestoreManager.USERS_COLLECTION)
                    .document(targetUid)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    val remoteProfile = snapshot.toObject(UserProfile::class.java)
                    if (remoteProfile != null) {
                        // Update local cache
                        userProfileDao.insertOrUpdateProfile(remoteProfile.toEntity())
                        return@runCatching remoteProfile
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch from Firestore, falling back to cache: ${e.message}")
            }

            // Fallback to local Room cache
            userProfileDao.getUserProfile(targetUid)?.toDomainModel()
        }
    }

    /**
     * Real-time reactive stream that listens to Firestore changes and synchronizes with Room cache.
     */
    fun observeUserProfile(uid: String? = null): Flow<Result<UserProfile?>> = callbackFlow {
        val targetUid = try {
            uid ?: firestoreManager.requireCurrentUserId()
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close(e)
            return@callbackFlow
        }

        val docRef = firestoreManager.firestore
            .collection(FirestoreManager.USERS_COLLECTION)
            .document(targetUid)

        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing Firestore user profile", error)
                trySend(Result.failure(error))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                trySend(Result.success(profile))
            } else {
                trySend(Result.success(null))
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Safely updates specific fields in the user's Firestore document.
     */
    suspend fun updateProfileFields(fields: Map<String, Any>, uid: String? = null): Result<Unit> {
        return runCatching {
            val targetUid = uid ?: firestoreManager.requireCurrentUserId()
            firestoreManager.verifyUserSession(targetUid)

            firestoreManager.firestore
                .collection(FirestoreManager.USERS_COLLECTION)
                .document(targetUid)
                .update(fields)
                .await()

            Log.d(TAG, "Updated profile fields in Firestore for UID: $targetUid")
        }
    }

    /**
     * Caches user profile locally in Room.
     */
    suspend fun cacheUserProfile(profile: UserProfile, step: Int = 0) {
        userProfileDao.insertOrUpdateProfile(profile.toEntity(onboardingStep = step))
    }

    /**
     * Updates the onboarding step in Room.
     */
    suspend fun updateOnboardingStep(uid: String, step: Int) {
        userProfileDao.updateOnboardingStep(uid, step)
    }

    /**
     * Deletes user profile from both Firestore and local database.
     */
    suspend fun deleteUserProfile(uid: String? = null): Result<Unit> {
        return runCatching {
            val targetUid = uid ?: firestoreManager.requireCurrentUserId()
            firestoreManager.verifyUserSession(targetUid)

            firestoreManager.firestore
                .collection(FirestoreManager.USERS_COLLECTION)
                .document(targetUid)
                .delete()
                .await()

            userProfileDao.deleteProfile(targetUid)
            Log.d(TAG, "Deleted user profile for UID: $targetUid")
        }
    }

    /**
     * Clears all cached profile data.
     */
    suspend fun clearCache() {
        userProfileDao.clearAll()
    }
}
