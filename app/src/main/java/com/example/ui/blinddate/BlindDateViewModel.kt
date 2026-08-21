package com.example.ui.blinddate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreBlindDateManager
import com.example.data.firebase.FirestoreManager
import com.example.data.model.*
import com.example.ui.wallet.WalletViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BlindDateScreenState {
    HUB,
    MATCHMAKING,
    ACTIVE_DATE,
    MUTUAL_CELEBRATION,
    HISTORY
}

class BlindDateViewModel : ViewModel() {

    private val _screenState = MutableStateFlow(BlindDateScreenState.HUB)
    val screenState: StateFlow<BlindDateScreenState> = _screenState.asStateFlow()

    private val _preferences = MutableStateFlow(
        BlindDatePreferences(
            interestedIn = listOf("Female"),
            selectedCountries = listOf("All"),
            minAge = 18,
            maxAge = 35,
            languagePreference = "All",
            relationshipGoal = "All"
        )
    )
    val preferences: StateFlow<BlindDatePreferences> = _preferences.asStateFlow()

    private val _activeSession = MutableStateFlow<BlindDateSession?>(null)
    val activeSession: StateFlow<BlindDateSession?> = _activeSession.asStateFlow()

    private val _matchedPartner = MutableStateFlow<UserProfile?>(null)
    val matchedPartner: StateFlow<UserProfile?> = _matchedPartner.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(600) // 10 minutes default
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchErrorMessage = MutableStateFlow<String?>(null)
    val searchErrorMessage: StateFlow<String?> = _searchErrorMessage.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _currentIcebreaker = MutableStateFlow(BlindDateIcebreakers.getRandom())
    val currentIcebreaker: StateFlow<String> = _currentIcebreaker.asStateFlow()

    private val _showRevealDialog = MutableStateFlow(false)
    val showRevealDialog: StateFlow<Boolean> = _showRevealDialog.asStateFlow()

    private val _showHowItWorks = MutableStateFlow(false)
    val showHowItWorks: StateFlow<Boolean> = _showHowItWorks.asStateFlow()

    private val _showSafetyInfo = MutableStateFlow(false)
    val showSafetyInfo: StateFlow<Boolean> = _showSafetyInfo.asStateFlow()

    private val _showPreferencesSheet = MutableStateFlow(false)
    val showPreferencesSheet: StateFlow<Boolean> = _showPreferencesSheet.asStateFlow()

    private val _history = MutableStateFlow<List<BlindDateHistoryItem>>(
        listOf(
            BlindDateHistoryItem(
                sessionId = "bdate_hist_1",
                userId = "user_me",
                partnerId = "user_prev_1",
                partnerMaskedName = "Mystery Match 💫",
                result = BlindDateResult.REVEAL_DECLINED,
                compatibilityPercentage = 78,
                timestamp = System.currentTimeMillis() - 86400000L * 2,
                durationMinutes = 10
            )
        )
    )
    val history: StateFlow<List<BlindDateHistoryItem>> = _history.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var timerJob: Job? = null

    fun setShowHowItWorks(show: Boolean) { _showHowItWorks.value = show }
    fun setShowSafetyInfo(show: Boolean) { _showSafetyInfo.value = show }
    fun setShowPreferencesSheet(show: Boolean) { _showPreferencesSheet.value = show }
    fun setShowRevealDialog(show: Boolean) { _showRevealDialog.value = show }
    fun clearSnackbar() { _snackbarMessage.value = null }

    fun updatePreferences(newPrefs: BlindDatePreferences) {
        _preferences.value = newPrefs
        _snackbarMessage.value = "Blind Date preferences updated! ⚙️"
    }

    fun nextIcebreaker() {
        _currentIcebreaker.value = BlindDateIcebreakers.getRandom(_currentIcebreaker.value)
    }

    /**
     * Initiates the Blind Date matching flow.
     */
    fun startBlindDate(
        currentUser: UserProfile,
        fallbackCandidates: List<UserProfile>,
        walletViewModel: WalletViewModel
    ) {
        val eligibility = FirestoreBlindDateManager.checkEligibility(currentUser)
        if (!eligibility.isEligible) {
            _snackbarMessage.value = eligibility.reasons.firstOrNull() ?: "You are not eligible for Blind Date."
            return
        }

        _isSearching.value = true
        _searchErrorMessage.value = null
        _screenState.value = BlindDateScreenState.MATCHMAKING

        viewModelScope.launch {
            // Simulated pulse animation delay for match finding experience
            delay(2800)

            val matchResult = FirestoreBlindDateManager.findMatchAndCreateSession(
                currentUser = currentUser,
                preferences = _preferences.value,
                fallbackCandidates = fallbackCandidates
            )

            _isSearching.value = false

            matchResult.fold(
                onSuccess = { (session, partner) ->
                    _activeSession.value = session
                    _matchedPartner.value = partner
                    _remainingSeconds.value = session.durationSeconds
                    _currentIcebreaker.value = session.currentIcebreaker
                    _screenState.value = BlindDateScreenState.ACTIVE_DATE

                    // Initial welcome system message in chat
                    _messages.value = listOf(
                        ChatMessage(
                            messageId = "sys_msg_1",
                            conversationId = session.sessionId,
                            senderId = "system",
                            messageType = MessageType.MATCH_EVENT,
                            messageContent = "🔒 You are connected with a Mystery Match! Full profiles stay private until mutual reveal at the end. Enjoy your conversation!",
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    startCountdownTimer()
                },
                onFailure = { error ->
                    _searchErrorMessage.value = error.message ?: "No compatible Blind Date is available right now. Try again later."
                }
            )
        }
    }

    private fun startCountdownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            // Timer expired -> Trigger reveal prompt
            _showRevealDialog.value = true
        }
    }

    /**
     * Sends a text, voice, or media message in the Blind Date temporary room with point integration.
     */
    fun sendMessage(
        content: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        audioDuration: Int = 0,
        walletViewModel: WalletViewModel
    ) {
        val session = _activeSession.value ?: return
        val currentUid = FirestoreManager.currentUser?.uid ?: "current_user"
        val isMaster = walletViewModel.isMasterMember.value

        // Deduct points based on message type
        val pointCost = when (type) {
            MessageType.TEXT -> if (isMaster) 0 else 15
            MessageType.VOICE -> if (isMaster) 0 else 20
            MessageType.IMAGE, MessageType.VIDEO -> if (isMaster) 10 else 40
            else -> 15
        }

        if (pointCost > 0) {
            val success = walletViewModel.spendPoints(pointCost, "Blind Date ${type.name.lowercase()} message")
            if (!success) {
                _snackbarMessage.value = "Insufficient points! Requires $pointCost pts."
                return
            }
        }

        val newMsg = ChatMessage(
            messageId = "bmsg_${System.currentTimeMillis()}",
            conversationId = session.sessionId,
            senderId = currentUid,
            receiverId = session.userBId,
            messageType = type,
            messageContent = content,
            mediaUrl = mediaUrl,
            audioDurationSeconds = audioDuration,
            timestamp = System.currentTimeMillis()
        )

        _messages.value = _messages.value + newMsg

        // Send to Firestore
        viewModelScope.launch {
            FirestoreBlindDateManager.sendBlindDateMessage(session.sessionId, newMsg)

            // Simulate natural interactive conversational response from match
            if (type == MessageType.TEXT && _messages.value.size <= 4) {
                delay(1800)
                val replyText = when (_messages.value.size) {
                    2 -> "Hello! So wonderful to meet you here. I love that this keeps things spontaneous! ✨"
                    3 -> "That's really interesting! I completely agree with you."
                    else -> "Haha, tell me more about that! ☕"
                }
                val partnerMsg = ChatMessage(
                    messageId = "bmsg_partner_${System.currentTimeMillis()}",
                    conversationId = session.sessionId,
                    senderId = session.userBId,
                    receiverId = currentUid,
                    messageType = MessageType.TEXT,
                    messageContent = replyText,
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + partnerMsg
            }
        }
    }

    /**
     * Submits the user's choice to reveal profile or decline.
     */
    fun submitRevealConsent(
        wantsToReveal: Boolean,
        walletViewModel: WalletViewModel
    ) {
        val session = _activeSession.value ?: return
        val partner = _matchedPartner.value ?: return
        val currentUid = FirestoreManager.currentUser?.uid ?: "current_user"
        _showRevealDialog.value = false
        timerJob?.cancel()

        viewModelScope.launch {
            val result = FirestoreBlindDateManager.submitRevealChoice(
                session = session,
                currentUserId = currentUid,
                partnerProfile = partner,
                wantsToReveal = wantsToReveal
            )

            result.fold(
                onSuccess = { updatedSession ->
                    _activeSession.value = updatedSession
                    if (updatedSession.status == BlindDateSessionStatus.MUTUAL_REVEALED) {
                        // Reward participation bonus points
                        walletViewModel.addRewardPoints(25, "Blind Date Mutual Match Bonus! 💖")
                        _screenState.value = BlindDateScreenState.MUTUAL_CELEBRATION
                    } else {
                        _snackbarMessage.value = "Blind Date concluded. Profile was kept private."
                        _screenState.value = BlindDateScreenState.HUB
                    }
                },
                onFailure = {
                    _snackbarMessage.value = "Error updating reveal status."
                    _screenState.value = BlindDateScreenState.HUB
                }
            )
        }
    }

    /**
     * Ends the session immediately upon user request.
     */
    fun endDateEarly(reason: String = "User ended date") {
        val session = _activeSession.value ?: return
        val currentUid = FirestoreManager.currentUser?.uid ?: "current_user"
        timerJob?.cancel()

        viewModelScope.launch {
            FirestoreBlindDateManager.endSessionEarly(session.sessionId, currentUid, reason)
            _screenState.value = BlindDateScreenState.HUB
            _snackbarMessage.value = "Blind Date ended 👋"
            _activeSession.value = null
        }
    }

    /**
     * Blocks or reports user, terminating session and adding safety flags.
     */
    fun blockOrReportPartner(reason: String, isBlock: Boolean) {
        val session = _activeSession.value ?: return
        val partner = _matchedPartner.value ?: return
        val currentUid = FirestoreManager.currentUser?.uid ?: "current_user"
        timerJob?.cancel()

        viewModelScope.launch {
            FirestoreBlindDateManager.blockOrReportPartner(
                sessionId = session.sessionId,
                currentUserId = currentUid,
                partnerId = partner.uid,
                reason = reason,
                isBlock = isBlock
            )
            _screenState.value = BlindDateScreenState.HUB
            _snackbarMessage.value = if (isBlock) "User blocked and session ended." else "Report submitted to moderators. Session ended."
            _activeSession.value = null
        }
    }

    fun openHistory() {
        _screenState.value = BlindDateScreenState.HISTORY
    }

    fun backToHub() {
        timerJob?.cancel()
        _screenState.value = BlindDateScreenState.HUB
        _activeSession.value = null
        _searchErrorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
