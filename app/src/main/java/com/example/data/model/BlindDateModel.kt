package com.example.data.model

enum class BlindDateSessionStatus {
    SEARCHING,
    ACTIVE,
    EXPIRED,
    MUTUAL_REVEALED,
    REVEAL_DECLINED,
    ENDED_EARLY,
    BLOCKED
}

enum class BlindDateResult {
    MUTUAL_MATCH,
    REVEAL_DECLINED,
    ENDED_EARLY,
    EXPIRED_NO_RESPONSE,
    BLOCKED_OR_REPORTED
}

data class BlindDateSession(
    val sessionId: String = "",
    val userAId: String = "",
    val userBId: String = "",
    val status: BlindDateSessionStatus = BlindDateSessionStatus.ACTIVE,
    val startedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (10 * 60 * 1000L), // 10 minutes default
    val durationSeconds: Int = 600,
    val userAConsent: Boolean? = null,
    val userBConsent: Boolean? = null,
    val userARevealed: Boolean = false,
    val userBRevealed: Boolean = false,
    val endedBy: String? = null,
    val endReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val compatibilityPercentage: Int = 85,
    val commonInterests: List<String> = emptyList(),
    val currentIcebreaker: String = "What hobby could you talk about all day? 🌟",
    val maskedPartnerAge: String = "24-26",
    val maskedPartnerCountry: String = "International 🌏",
    val maskedPartnerLanguages: List<String> = listOf("English"),
    val maskedPartnerInterests: List<String> = listOf("Travel", "Music", "Coffee"),
    val maskedPartnerGoal: String = "Long-term connection 💖",
    val partnerProfileSnapshot: UserProfile? = null
)

data class BlindDatePreferences(
    val interestedIn: List<String> = listOf("Male", "Female"), // "Male", "Female"
    val selectedCountries: List<String> = listOf("All"), // Pakistan, China, South Korea, Japan, Russia, Myanmar, United States, European countries, All
    val minAge: Int = 18,
    val maxAge: Int = 40,
    val languagePreference: String = "All",
    val relationshipGoal: String = "All",
    val sharedInterestsOnly: Boolean = false,
    val maxDistanceMiles: Int = 100
)

data class BlindDateHistoryItem(
    val sessionId: String = "",
    val userId: String = "",
    val partnerId: String = "",
    val partnerMaskedName: String = "Mystery Match 💫",
    val partnerRevealedProfile: UserProfile? = null,
    val result: BlindDateResult = BlindDateResult.MUTUAL_MATCH,
    val compatibilityPercentage: Int = 85,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 10
)

data class BlindDateEligibility(
    val isEligible: Boolean = true,
    val reasons: List<String> = emptyList(),
    val hasCompletedProfile: Boolean = true,
    val isVerified: Boolean = true,
    val meetsAgeRequirement: Boolean = true,
    val isSuspendedOrBanned: Boolean = false,
    val freeDatesRemainingToday: Int = 1,
    val pointsBalance: Int = 150,
    val isMasterMember: Boolean = false
)

object BlindDateIcebreakers {
    val questions = listOf(
        "What hobby could you talk about all day? 🌟",
        "What's your favorite movie or anime? 🎬",
        "If you could travel anywhere tomorrow, where would you go? ✈️",
        "What's something that always makes you smile? 😊",
        "What's your perfect weekend morning look like? ☕",
        "What kind of music do you listen to when relaxing? 🎵",
        "What is one traditional dish from your culture that everyone should try? 🍲",
        "Are you more of an early bird 🌅 or a night owl 🌙?",
        "What is a small goal you're working on this year? 🎯",
        "What quality do you value most in a close friend or partner? 💖",
        "If you could learn any new language instantly, which one would it be? 🗣️",
        "What was the most memorable trip or adventure you've ever taken? 🏔️",
        "Do you prefer deep cozy conversations or spontaneous outdoor adventures? 🏕️",
        "What's the best book, podcast, or show you recently enjoyed? 📖"
    )

    fun getRandom(current: String? = null): String {
        val pool = if (current != null) questions.filter { it != current } else questions
        return pool.randomOrNull() ?: questions.first()
    }
}
