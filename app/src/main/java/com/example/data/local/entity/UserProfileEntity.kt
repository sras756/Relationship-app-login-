package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.DiscoveryPreferences
import com.example.data.model.PrivacySettings
import com.example.data.model.UserProfile
import com.example.data.model.VerificationDetails

@Entity(
    tableName = "user_profiles",
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["nationality"]),
        Index(value = ["gender"]),
        Index(value = ["updatedAt"])
    ]
)
data class UserProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "email")
    val email: String = "",

    @ColumnInfo(name = "displayName")
    val displayName: String = "",

    @ColumnInfo(name = "age")
    val age: Int = 0,

    @ColumnInfo(name = "dateOfBirth")
    val dateOfBirth: String = "",

    @ColumnInfo(name = "gender")
    val gender: String = "",

    @ColumnInfo(name = "interestedIn")
    val interestedIn: List<String> = emptyList(),

    @ColumnInfo(name = "country")
    val country: String = "",

    @ColumnInfo(name = "nationality")
    val nationality: String = "",

    @ColumnInfo(name = "secondNationality")
    val secondNationality: String? = null,

    @ColumnInfo(name = "nationalityPreferences")
    val nationalityPreferences: List<String> = emptyList(),

    @ColumnInfo(name = "relationshipStatus")
    val relationshipStatus: String = "",

    @ColumnInfo(name = "city")
    val city: String = "",

    @ColumnInfo(name = "languages")
    val languages: List<String> = emptyList(),

    @ColumnInfo(name = "relationshipGoal")
    val relationshipGoal: String = "",

    @ColumnInfo(name = "bio")
    val bio: String = "",

    @ColumnInfo(name = "photoUrls")
    val photoUrls: List<String> = emptyList(),

    @ColumnInfo(name = "primaryPhotoUrl")
    val primaryPhotoUrl: String = "",

    @ColumnInfo(name = "isVerified")
    val isVerified: Boolean = false,

    @ColumnInfo(name = "isMasterMember")
    val isMasterMember: Boolean = false,

    @ColumnInfo(name = "pointsBalance")
    val pointsBalance: Int = 150,

    @ColumnInfo(name = "boostEndTime")
    val boostEndTime: Long = 0L,

    @ColumnInfo(name = "interests")
    val interests: List<String> = emptyList(),

    @ColumnInfo(name = "promptAnswers")
    val promptAnswers: Map<String, String> = emptyMap(),

    @ColumnInfo(name = "isProfileComplete")
    val isProfileComplete: Boolean = false,

    @ColumnInfo(name = "profileSetupStep")
    val profileSetupStep: String = "basic_info",

    @ColumnInfo(name = "currentOnboardingStep")
    val currentOnboardingStep: Int = 0,

    @ColumnInfo(name = "accountStatus")
    val accountStatus: String = "active",

    @ColumnInfo(name = "statusReason")
    val statusReason: String = "",

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = 0L,


    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = 0L,

    @ColumnInfo(name = "lastLoginAt")
    val lastLoginAt: Long = 0L
)

fun UserProfileEntity.toDomainModel(): UserProfile {
    return UserProfile(
        uid = uid,
        email = email,
        displayName = displayName,
        age = age,
        dateOfBirth = dateOfBirth,
        gender = gender,
        interestedIn = interestedIn,
        country = country,
        nationality = nationality,
        secondNationality = secondNationality,
        nationalityPreferences = nationalityPreferences,
        relationshipStatus = relationshipStatus,
        city = city,
        languages = languages,
        relationshipGoal = relationshipGoal,
        bio = bio,
        photoUrls = photoUrls,
        primaryPhotoUrl = primaryPhotoUrl,
        isVerified = isVerified,
        verificationDetails = VerificationDetails(
            isPhoneVerified = false,
            isEmailVerified = email.isNotBlank(),
            isPhotoVerified = isVerified,
            isIdentityVerified = isVerified,
            authenticityScore = if (isVerified) 98 else 75
        ),
        isMasterMember = isMasterMember,
        pointsBalance = pointsBalance,
        boostEndTime = boostEndTime,
        interests = interests,
        promptAnswers = promptAnswers,
        privacySettings = PrivacySettings(),
        discoveryPreferences = DiscoveryPreferences(
            interestedIn = interestedIn,
            countryPreference = if (country.isNotBlank()) country else "All"
        ),
        isProfileComplete = isProfileComplete,
        profileSetupStep = profileSetupStep,
        accountStatus = accountStatus,
        statusReason = statusReason,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastLoginAt = lastLoginAt
    )
}

fun UserProfile.toEntity(onboardingStep: Int = 0): UserProfileEntity {
    return UserProfileEntity(
        uid = uid.ifBlank { "" },
        email = email,
        displayName = displayName,
        age = age,
        dateOfBirth = dateOfBirth,
        gender = gender,
        interestedIn = interestedIn,
        country = country,
        nationality = nationality,
        secondNationality = secondNationality,
        nationalityPreferences = nationalityPreferences,
        relationshipStatus = relationshipStatus,
        city = city,
        languages = languages,
        relationshipGoal = relationshipGoal,
        bio = bio,
        photoUrls = photoUrls,
        primaryPhotoUrl = primaryPhotoUrl,
        isVerified = isVerified,
        isMasterMember = isMasterMember,
        pointsBalance = pointsBalance,
        boostEndTime = boostEndTime,
        interests = interests,
        promptAnswers = promptAnswers,
        isProfileComplete = isProfileComplete,
        profileSetupStep = profileSetupStep,
        currentOnboardingStep = onboardingStep,
        accountStatus = accountStatus,
        statusReason = statusReason,
        createdAt = if (createdAt > 0L) createdAt else System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        lastLoginAt = lastLoginAt
    )
}


