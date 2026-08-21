package com.example.data.model

data class VerificationDetails(
    val isPhoneVerified: Boolean = false,
    val isEmailVerified: Boolean = false,
    val isPhotoVerified: Boolean = false,
    val isIdentityVerified: Boolean = false,
    val authenticityScore: Int = 50
)

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val age: Int = 0,
    val dateOfBirth: String = "",
    val gender: String = "", // "Male", "Female"
    val interestedIn: List<String> = emptyList(), // "Male", "Female", or both
    val country: String = "",
    val nationality: String = "",
    val secondNationality: String? = null,
    val nationalityPreferences: List<String> = emptyList(),
    val relationshipStatus: String = "", // "Single", "Divorced", "Widowed"
    val city: String = "",
    val languages: List<String> = emptyList(),
    val relationshipGoal: String = "",
    val bio: String = "",
    val photoUrls: List<String> = emptyList(),
    val primaryPhotoUrl: String = "",
    val isVerified: Boolean = false,
    val verificationDetails: VerificationDetails = VerificationDetails(),
    val isMasterMember: Boolean = false,
    val pointsBalance: Int = 150,
    val boostEndTime: Long = 0L,
    val interests: List<String> = emptyList(),
    val promptAnswers: Map<String, String> = emptyMap(),
    val privacySettings: PrivacySettings = PrivacySettings(),
    val discoveryPreferences: DiscoveryPreferences = DiscoveryPreferences(),
    val blockedUserIds: List<String> = emptyList(),
    val mutedUserIds: List<String> = emptyList(),
    val isProfileComplete: Boolean = false,
    val profileSetupStep: String = "basic_info", // "basic_info", "about_me", "interests", "prompts", "photos", "privacy", "preview", "completed"
    val accountStatus: String = "active", // "active", "suspended", "banned", "deleted"
    val statusReason: String = "",
    val createdAt: Long = 0L,

    val updatedAt: Long = 0L,
    val lastLoginAt: Long = 0L
) {
    /**
     * Checks if all mandatory fields are completed according to strict onboarding rules.
     */
    fun hasAllRequiredInformation(): Boolean {
        return displayName.isNotBlank() &&
                age >= 18 &&
                gender.isNotBlank() &&
                interestedIn.isNotEmpty() &&
                country.isNotBlank() &&
                relationshipGoal.isNotBlank() &&
                bio.isNotBlank() &&
                photoUrls.isNotEmpty()
    }
}

data class DiscoveryPreferences(
    val interestedIn: List<String> = emptyList(), // "Male", "Female"
    val minAge: Int = 18,
    val maxAge: Int = 50,
    val maxDistanceMiles: Int = 100,
    val countryPreference: String = "All",
    val selectedCountries: List<String> = listOf("All"),
    val languagePreference: String = "All",
    val relationshipGoal: String = "All",
    val verifiedOnly: Boolean = false,
    val onlineOnly: Boolean = false,
    val sharedInterestsOnly: Boolean = false
)

data class PrivacySettings(
    val showCity: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val showDistance: Boolean = true,
    val readReceipts: Boolean = true,
    val typingIndicator: Boolean = true,
    val mediaPermissions: Boolean = true,
    val profileVisibility: String = "Everyone", // "Everyone" or "Matches Only"
    val whoCanMessage: String = "Everyone", // "Everyone", "Matches Only", or "Verified Only"
    val allowMatchRequests: Boolean = true
)


