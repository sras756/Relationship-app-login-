package com.example.ui.discovery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreManager
import com.example.data.firebase.FirestoreMatchManager
import com.example.data.model.*
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DiscoveryViewModel : ViewModel() {
    companion object {
        private const val TAG = "DiscoveryViewModel"
    }

    private val _candidates = MutableStateFlow<List<UserProfile>>(emptyList())
    val candidates: StateFlow<List<UserProfile>> = _candidates.asStateFlow()

    private val _currentCandidateIndex = MutableStateFlow(0)
    val currentCandidateIndex: StateFlow<Int> = _currentCandidateIndex.asStateFlow()

    private val _mutualMatchEvent = MutableSharedFlow<UserProfile>()
    val mutualMatchEvent: SharedFlow<UserProfile> = _mutualMatchEvent.asSharedFlow()

    private val _incomingConnections = MutableStateFlow<List<PotentialConnection>>(emptyList())
    val incomingConnections: StateFlow<List<PotentialConnection>> = _incomingConnections.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // History stack for rewind functionality
    private val swipedHistory = mutableListOf<Pair<UserProfile, InteractionType>>()

    init {
        loadCandidates()
        observeIncomingLikes()
    }

    fun loadCandidates() {
        val currentUid = FirestoreManager.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch swiped IDs so we don't present profiles user already swiped on
                val swipedIds = FirestoreMatchManager.getSwipedUserIds(currentUid)

                val snapshot = FirestoreManager.firestore
                    .collection(FirestoreManager.USERS_COLLECTION)
                    .whereEqualTo("isProfileComplete", true)
                    .limit(50)
                    .get()
                    .await()

                val profiles = snapshot.documents.mapNotNull { doc ->
                    try {
                        val profile = doc.toObject(UserProfile::class.java)
                        if (profile != null && profile.uid != currentUid && !swipedIds.contains(profile.uid) && profile.accountStatus == "active") {
                            profile
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                _candidates.value = profiles
                _currentCandidateIndex.value = 0
                Log.d(TAG, "Loaded ${profiles.size} active completed candidate profiles from Firestore.")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load candidates from Firestore: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeIncomingLikes() {
        val currentUid = FirestoreManager.currentUser?.uid ?: return
        viewModelScope.launch {
            FirestoreMatchManager.observeIncomingLikes(currentUid).collect { connections ->
                _incomingConnections.value = connections
            }
        }
    }

    /**
     * Executes swipe action with Firestore persistence and mutual match detection.
     */
    fun onSwipe(
        profile: UserProfile,
        direction: SwipeDirection,
        introNote: String = ""
    ) {
        val action = when (direction) {
            SwipeDirection.RIGHT -> InteractionType.LIKE
            SwipeDirection.LEFT -> InteractionType.DISLIKE
            SwipeDirection.UP -> InteractionType.SUPER_LIKE
        }

        swipedHistory.add(profile to action)
        _currentCandidateIndex.value = _currentCandidateIndex.value + 1

        val currentUid = FirestoreManager.currentUser?.uid ?: return
        viewModelScope.launch {
            val result = FirestoreMatchManager.recordSwipeInteraction(
                fromUserId = currentUid,
                targetProfile = profile,
                action = action,
                introNote = introNote
            )

            // If mutual match occurred in Firestore, trigger celebration dialog!
            val isMutualMatch = result.getOrDefault(false)
            if (isMutualMatch) {
                _mutualMatchEvent.emit(profile)
            }
        }
    }

    fun rewind(): Boolean {
        if (swipedHistory.isNotEmpty() && _currentCandidateIndex.value > 0) {
            swipedHistory.removeAt(swipedHistory.size - 1)
            _currentCandidateIndex.value = _currentCandidateIndex.value - 1
            return true
        }
        return false
    }

    fun resetCandidates() {
        _currentCandidateIndex.value = 0
        swipedHistory.clear()
        loadCandidates()
    }
}

