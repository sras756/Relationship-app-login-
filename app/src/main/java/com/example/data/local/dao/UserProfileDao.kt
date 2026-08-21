package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    /**
     * Live observable flow for a specific user profile by UID.
     */
    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    fun getUserProfileFlow(uid: String): Flow<UserProfileEntity?>

    /**
     * Live observable flow for the most recently active/updated user profile.
     */
    @Query("SELECT * FROM user_profiles ORDER BY updatedAt DESC LIMIT 1")
    fun getLatestUserProfileFlow(): Flow<UserProfileEntity?>

    /**
     * Direct query to get a user profile by UID.
     */
    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    suspend fun getUserProfile(uid: String): UserProfileEntity?

    /**
     * Direct query to get the most recently active profile.
     */
    @Query("SELECT * FROM user_profiles ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestUserProfile(): UserProfileEntity?

    /**
     * Inserts or replaces a user profile entity.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    /**
     * Batch insert or replace of profiles.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfiles(profiles: List<UserProfileEntity>)

    /**
     * Updates the onboarding progress step and timestamp.
     */
    @Query("UPDATE user_profiles SET currentOnboardingStep = :step, updatedAt = :timestamp WHERE uid = :uid")
    suspend fun updateOnboardingStep(uid: String, step: Int, timestamp: Long = System.currentTimeMillis())

    /**
     * Updates user's bio.
     */
    @Query("UPDATE user_profiles SET bio = :bio, updatedAt = :timestamp WHERE uid = :uid")
    suspend fun updateBio(uid: String, bio: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Updates user's verification status.
     */
    @Query("UPDATE user_profiles SET isVerified = :isVerified, updatedAt = :timestamp WHERE uid = :uid")
    suspend fun updateVerificationStatus(uid: String, isVerified: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Deletes a profile by UID.
     */
    @Query("DELETE FROM user_profiles WHERE uid = :uid")
    suspend fun deleteProfile(uid: String)

    /**
     * Clears all cached user profiles.
     */
    @Query("DELETE FROM user_profiles")
    suspend fun clearAll()
}
