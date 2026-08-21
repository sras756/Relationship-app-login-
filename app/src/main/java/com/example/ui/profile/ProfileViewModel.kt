package com.example.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.DatingApp
import com.example.data.firebase.FirestoreManager
import com.example.data.model.*
import com.example.data.repository.UserProfileRepository
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ProfileSetupState {
    object Idle : ProfileSetupState()
    object Saving : ProfileSetupState()
    object Success : ProfileSetupState()
    data class Error(val message: String) : ProfileSetupState()
}

class ProfileViewModel(
    private val repository: UserProfileRepository = DatingApp.instance.userProfileRepository
) : ViewModel() {
    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val _userProfile = MutableStateFlow(
        UserProfile(
            uid = FirestoreManager.currentUser?.uid ?: "",
            email = FirestoreManager.currentUser?.email ?: ""
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _saveState = MutableStateFlow<ProfileSetupState>(ProfileSetupState.Idle)
    val saveState: StateFlow<ProfileSetupState> = _saveState.asStateFlow()

    private val _isLoadedFromCache = MutableStateFlow(false)
    val isLoadedFromCache: StateFlow<Boolean> = _isLoadedFromCache.asStateFlow()

    init {
        loadUserProfile()
    }

    fun resetSaveState() {
        _saveState.value = ProfileSetupState.Idle
    }

    fun loadUserProfile() {
        val currentUid = FirestoreManager.currentUser?.uid ?: return
        val currentEmail = FirestoreManager.currentUser?.email ?: ""

        viewModelScope.launch {
            // 1. Fast Local Cache Hit (Room Database for instant 0ms app launch)
            try {
                val cached = repository.getCachedProfile(currentUid)
                if (cached != null) {
                    _userProfile.value = cached.copy(
                        uid = currentUid,
                        email = if (cached.email.isNotBlank()) cached.email else currentEmail
                    )
                    val cachedEntity = repository.getLatestCachedProfile()
                    if (cachedEntity != null && !cachedEntity.isProfileComplete && cachedEntity.currentOnboardingStep > 0) {
                        _currentStep.value = cachedEntity.currentOnboardingStep
                    }
                    _isLoadedFromCache.value = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Cache load error: ${e.message}")
            }

            // 2. Background Firestore Sync
            if (FirestoreManager.isAuthenticated) {
                val result = repository.getUserProfile(currentUid, forceRemote = true)
                result.onSuccess { remoteProfile ->
                    if (remoteProfile != null) {
                        _userProfile.value = remoteProfile.copy(
                            uid = currentUid,
                            email = if (remoteProfile.email.isNotBlank()) remoteProfile.email else currentEmail
                        )
                        // Map step string to step index if onboarding in progress
                        val stepIndex = when (remoteProfile.profileSetupStep) {
                            "photos" -> 1
                            "basic_info" -> 2
                            "about_me" -> 3
                            "interests" -> 4
                            "prompts" -> 5
                            "privacy" -> 6
                            "preview" -> 7
                            else -> _currentStep.value
                        }
                        if (!remoteProfile.isProfileComplete && stepIndex > 0) {
                            _currentStep.value = stepIndex
                        }
                    }
                }.onFailure {
                    Log.w(TAG, "Failed remote profile sync: ${it.message}")
                }
            }
        }
    }

    private fun persistLocalSnapshot() {
        val currentUid = FirestoreManager.currentUser?.uid ?: return
        val profile = _userProfile.value.copy(
            uid = currentUid,
            email = if (_userProfile.value.email.isNotBlank()) _userProfile.value.email else (FirestoreManager.currentUser?.email ?: "")
        )
        viewModelScope.launch {
            try {
                repository.cacheUserProfile(profile, _currentStep.value)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to cache snapshot: ${e.message}")
            }
        }
    }

    private fun stepIndexToName(step: Int): String {
        return when (step) {
            0 -> "welcome"
            1 -> "photos"
            2 -> "basic_info"
            3 -> "about_me"
            4 -> "interests"
            5 -> "prompts"
            6 -> "privacy"
            7 -> "preview"
            else -> "basic_info"
        }
    }

    fun nextStep() {
        if (_currentStep.value < 7) {
            _currentStep.value += 1
            syncStepToFirestore(_currentStep.value)
            persistLocalSnapshot()
        }
    }

    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value -= 1
            syncStepToFirestore(_currentStep.value)
            persistLocalSnapshot()
        }
    }

    fun goToStep(step: Int) {
        if (step in 0..7) {
            _currentStep.value = step
            syncStepToFirestore(step)
            persistLocalSnapshot()
        }
    }

    private fun syncStepToFirestore(stepIndex: Int) {
        val currentUid = FirestoreManager.currentUser?.uid ?: return
        val stepName = stepIndexToName(stepIndex)
        _userProfile.value = _userProfile.value.copy(profileSetupStep = stepName)
        
        viewModelScope.launch {
            try {
                FirestoreManager.firestore
                    .collection(FirestoreManager.USERS_COLLECTION)
                    .document(currentUid)
                    .set(
                        mapOf(
                            "profileSetupStep" to stepName,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Could not sync step to Firestore: ${e.message}")
            }
        }
    }

    fun updateDisplayName(name: String) {
        _userProfile.value = _userProfile.value.copy(displayName = name)
        persistLocalSnapshot()
    }

    fun updateAge(age: Int) {
        _userProfile.value = _userProfile.value.copy(age = age)
        persistLocalSnapshot()
    }

    fun updateDateOfBirth(dob: String) {
        _userProfile.value = _userProfile.value.copy(dateOfBirth = dob)
        persistLocalSnapshot()
    }

    fun updateGender(gender: String) {
        _userProfile.value = _userProfile.value.copy(gender = gender)
        persistLocalSnapshot()
    }

    fun toggleInterestedIn(gender: String) {
        val current = _userProfile.value.interestedIn.toMutableList()
        if (current.contains(gender)) {
            if (current.size > 1) current.remove(gender)
        } else {
            current.add(gender)
        }
        val currentDiscovery = _userProfile.value.discoveryPreferences.copy(interestedIn = current)
        _userProfile.value = _userProfile.value.copy(
            interestedIn = current,
            discoveryPreferences = currentDiscovery
        )
        persistLocalSnapshot()
    }

    fun updateDiscoveryPreferences(preferences: DiscoveryPreferences) {
        _userProfile.value = _userProfile.value.copy(
            discoveryPreferences = preferences,
            interestedIn = preferences.interestedIn
        )
        persistLocalSnapshot()
    }

    fun blockUser(userId: String) {
        val current = _userProfile.value.blockedUserIds.toMutableList()
        if (!current.contains(userId)) {
            current.add(userId)
            _userProfile.value = _userProfile.value.copy(blockedUserIds = current)
            persistLocalSnapshot()
        }
    }

    fun muteUser(userId: String) {
        val current = _userProfile.value.mutedUserIds.toMutableList()
        if (!current.contains(userId)) {
            current.add(userId)
            _userProfile.value = _userProfile.value.copy(mutedUserIds = current)
        } else {
            current.remove(userId)
            _userProfile.value = _userProfile.value.copy(mutedUserIds = current)
        }
        persistLocalSnapshot()
    }

    fun updateLocation(country: String, city: String) {
        _userProfile.value = _userProfile.value.copy(country = country, city = city)
        persistLocalSnapshot()
    }

    fun toggleLanguage(lang: String) {
        val current = _userProfile.value.languages.toMutableList()
        if (current.contains(lang)) {
            if (current.size > 1) current.remove(lang)
        } else {
            current.add(lang)
        }
        _userProfile.value = _userProfile.value.copy(languages = current)
        persistLocalSnapshot()
    }

    fun updateRelationshipGoal(goal: String) {
        _userProfile.value = _userProfile.value.copy(relationshipGoal = goal)
        persistLocalSnapshot()
    }

    fun updateBio(bio: String) {
        if (bio.length <= 500) {
            _userProfile.value = _userProfile.value.copy(bio = bio)
            persistLocalSnapshot()
        }
    }

    fun generateAiBioSuggestion(style: String) {
        val name = _userProfile.value.displayName.ifBlank { "I" }
        val goal = _userProfile.value.relationshipGoal
        val city = _userProfile.value.city
        val interests = _userProfile.value.interests.take(3).joinToString(", ").ifBlank { "exploring new places" }

        val aiBio = when (style) {
            "Adventurous" -> "Hey! $name here from $city. Passionate about $interests and always up for spontaneity. Looking for $goal with someone who loves good vibes and deep chats! ✨"
            "Charming" -> "Coffee enthusiast, music lover, and full-time dreamer. I enjoy $interests. Searching for $goal—let's share stories and laugh together! ☕🎵"
            "Romantic" -> "Believer in genuine chemistry, meaningful conversations, and sunset walks. Passionate about $interests. Here looking for $goal 💫"
            else -> "Passionate about life, $interests, and creating memorable experiences. Excited to meet amazing people in $city!"
        }
        updateBio(aiBio)
    }

    fun addPhoto(uriString: String) {
        val current = _userProfile.value.photoUrls.toMutableList()
        if (!current.contains(uriString) && current.size < 6) {
            current.add(uriString)
            val primary = if (_userProfile.value.primaryPhotoUrl.isBlank()) uriString else _userProfile.value.primaryPhotoUrl
            _userProfile.value = _userProfile.value.copy(photoUrls = current, primaryPhotoUrl = primary)
            persistLocalSnapshot()
        }
    }

    fun removePhoto(uriString: String) {
        val current = _userProfile.value.photoUrls.toMutableList()
        current.remove(uriString)
        var primary = _userProfile.value.primaryPhotoUrl
        if (primary == uriString) {
            primary = current.firstOrNull() ?: ""
        }
        _userProfile.value = _userProfile.value.copy(photoUrls = current, primaryPhotoUrl = primary)
        persistLocalSnapshot()
    }

    fun setPrimaryPhoto(uriString: String) {
        if (_userProfile.value.photoUrls.contains(uriString)) {
            _userProfile.value = _userProfile.value.copy(primaryPhotoUrl = uriString)
            persistLocalSnapshot()
        }
    }

    fun toggleVerification(verified: Boolean) {
        _userProfile.value = _userProfile.value.copy(isVerified = verified)
        persistLocalSnapshot()
    }

    fun toggleInterest(interest: String) {
        val current = _userProfile.value.interests.toMutableList()
        if (current.contains(interest)) {
            current.remove(interest)
        } else {
            current.add(interest)
        }
        _userProfile.value = _userProfile.value.copy(interests = current)
        persistLocalSnapshot()
    }

    fun updatePromptAnswer(prompt: String, answer: String) {
        val current = _userProfile.value.promptAnswers.toMutableMap()
        if (answer.isBlank()) {
            current.remove(prompt)
        } else {
            current[prompt] = answer
        }
        _userProfile.value = _userProfile.value.copy(promptAnswers = current)
        persistLocalSnapshot()
    }

    fun updatePrivacySettings(settings: PrivacySettings) {
        _userProfile.value = _userProfile.value.copy(privacySettings = settings)
        persistLocalSnapshot()
    }

    fun calculateCompletionPercentage(): Int {
        var score = 0
        val profile = _userProfile.value
        if (profile.displayName.isNotBlank()) score += 15
        if (profile.photoUrls.isNotEmpty()) score += 20
        if (profile.bio.isNotBlank()) score += 20
        if (profile.interests.isNotEmpty()) score += 15
        if (profile.relationshipGoal.isNotBlank()) score += 10
        if (profile.promptAnswers.isNotEmpty()) score += 10
        if (profile.isVerified) score += 10
        return score.coerceAtMost(100)
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val currentUid = FirestoreManager.currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            _saveState.value = ProfileSetupState.Error("Please log in with your account first.")
            return
        }

        val profile = _userProfile.value
        // Validation check for mandatory onboarding requirements
        if (profile.photoUrls.isEmpty()) {
            _saveState.value = ProfileSetupState.Error("Please add at least 1 profile photo.")
            return
        }
        if (profile.displayName.isBlank()) {
            _saveState.value = ProfileSetupState.Error("Please enter your display name.")
            return
        }
        if (profile.age < 18) {
            _saveState.value = ProfileSetupState.Error("You must be at least 18 years old to join.")
            return
        }
        if (profile.gender.isBlank()) {
            _saveState.value = ProfileSetupState.Error("Please select your gender.")
            return
        }
        if (profile.interestedIn.isEmpty()) {
            _saveState.value = ProfileSetupState.Error("Please choose who you are interested in.")
            return
        }
        if (profile.country.isBlank()) {
            _saveState.value = ProfileSetupState.Error("Please provide your country/location.")
            return
        }
        if (profile.relationshipGoal.isBlank()) {
            _saveState.value = ProfileSetupState.Error("Please choose your relationship goal.")
            return
        }
        if (profile.bio.isBlank()) {
            _saveState.value = ProfileSetupState.Error("Please write a short introduction bio.")
            return
        }

        val now = System.currentTimeMillis()
        val finalProfile = profile.copy(
            uid = currentUid,
            email = if (profile.email.isNotBlank()) profile.email else (FirestoreManager.currentUser?.email ?: ""),
            isProfileComplete = true,
            profileSetupStep = "completed",
            accountStatus = "active",
            updatedAt = now
        )

        _saveState.value = ProfileSetupState.Saving
        viewModelScope.launch {
            try {
                // Save to Firestore users/{uid}
                FirestoreManager.firestore
                    .collection(FirestoreManager.USERS_COLLECTION)
                    .document(currentUid)
                    .set(finalProfile, SetOptions.merge())
                    .await()

                // Save to local Room cache
                repository.cacheUserProfile(finalProfile, step = 7)
                _userProfile.value = finalProfile
                _saveState.value = ProfileSetupState.Success
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save complete profile to Firestore", e)
                _saveState.value = ProfileSetupState.Error("Failed to save profile: ${e.localizedMessage ?: "Please try again"}")
            }
        }
    }
}

