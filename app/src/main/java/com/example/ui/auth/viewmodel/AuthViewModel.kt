package com.example.ui.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreManager
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun login(email: String, pass: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Please enter both email and password.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(trimmedEmail, pass).await()
                val user = result.user
                if (user != null) {
                    // Update lastLoginAt in Firestore if document exists
                    try {
                        FirestoreManager.firestore
                            .collection(FirestoreManager.USERS_COLLECTION)
                            .document(user.uid)
                            .set(mapOf("lastLoginAt" to System.currentTimeMillis()), SetOptions.merge())
                            .await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not update lastLoginAt: ${e.message}")
                    }
                }
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("user-not-found", ignoreCase = true) == true -> "No account found with this email."
                    e.message?.contains("wrong-password", ignoreCase = true) == true || e.message?.contains("invalid-credential", ignoreCase = true) == true -> "Incorrect password or email."
                    e.message?.contains("invalid-email", ignoreCase = true) == true -> "Please enter a valid email address."
                    e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your internet connection."
                    else -> e.localizedMessage ?: "Login failed. Please try again."
                }
                _authState.value = AuthState.Error(msg)
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.isBlank()) {
            _authState.value = AuthState.Error("Please enter your name.")
            return
        }
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }
        if (pass.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters long.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. Create Firebase Authentication account
                val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, pass).await()
                val user = authResult.user ?: throw IllegalStateException("Failed to retrieve created user account.")

                // 2. Send verification email immediately
                try {
                    user.sendEmailVerification().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Email verification sending failed: ${e.message}")
                }

                // 3. Initialize minimal user document in Firestore users/{uid}
                // Do NOT pre-fill fake or default attributes!
                val now = System.currentTimeMillis()
                val initialUserDoc = hashMapOf(
                    "uid" to user.uid,
                    "email" to trimmedEmail,
                    "displayName" to trimmedName,
                    "isProfileComplete" to false,
                    "profileSetupStep" to "basic_info",
                    "accountStatus" to "active",
                    "createdAt" to now,
                    "updatedAt" to now,
                    "lastLoginAt" to now
                )

                FirestoreManager.firestore
                    .collection(FirestoreManager.USERS_COLLECTION)
                    .document(user.uid)
                    .set(initialUserDoc, SetOptions.merge())
                    .await()

                Log.d(TAG, "Successfully created user doc in Firestore for UID: ${user.uid}")
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("email-already-in-use", ignoreCase = true) == true -> "This email is already registered. Please log in instead."
                    e.message?.contains("weak-password", ignoreCase = true) == true -> "The password is too weak. Choose a stronger password."
                    e.message?.contains("network", ignoreCase = true) == true -> "Network connection failed. Please check your internet connection."
                    else -> e.localizedMessage ?: "Registration failed. Please try again."
                }
                _authState.value = AuthState.Error(msg)
            }
        }
    }

    fun resetPassword(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.sendPasswordResetEmail(trimmedEmail).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("user-not-found", ignoreCase = true) == true -> "No account found for this email address."
                    e.message?.contains("invalid-email", ignoreCase = true) == true -> "Invalid email address format."
                    else -> e.localizedMessage ?: "Failed to send password reset email."
                }
                _authState.value = AuthState.Error(msg)
            }
        }
    }
}


