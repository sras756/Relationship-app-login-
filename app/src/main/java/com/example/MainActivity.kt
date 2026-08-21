package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.firebase.FirestoreManager
import com.example.data.model.UserProfile
import com.example.ui.auth.AccountStatusScreen
import com.example.ui.auth.AuthNavigation
import com.example.ui.auth.EmailVerificationScreen
import com.example.ui.main.MainHomeScreen
import com.example.ui.profile.ProfileSetupNavigation
import com.example.ui.profile.ProfileViewModel
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

sealed class AppNavigationState {
    object Initializing : AppNavigationState()
    object Unauthenticated : AppNavigationState()
    data class AccountBlocked(val status: String, val reason: String) : AppNavigationState()
    data class EmailVerificationPending(val email: String) : AppNavigationState()
    object ProfileSetupRequired : AppNavigationState()
    object ReadyForHome : AppNavigationState()
}

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val profileViewModel: ProfileViewModel = viewModel()
                val userProfile by profileViewModel.userProfile.collectAsState()

                var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
                var isAuthInitialized by remember { mutableStateOf(false) }
                var isProfileCheckComplete by remember { mutableStateOf(false) }
                var isEditingProfileInApp by remember { mutableStateOf(false) }
                var isEmailVerificationBypassed by remember { mutableStateOf(false) }

                // 1. Listen to Firebase Authentication State Changes
                DisposableEffect(Unit) {
                    val authListener = FirebaseAuth.AuthStateListener { auth ->
                        currentUser = auth.currentUser
                        isAuthInitialized = true
                        if (auth.currentUser != null) {
                            profileViewModel.loadUserProfile()
                        }
                    }
                    FirebaseAuth.getInstance().addAuthStateListener(authListener)
                    onDispose {
                        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
                    }
                }

                // 2. Fetch and Validate Real Profile Status from Firestore for current user
                LaunchedEffect(currentUser?.uid) {
                    val user = currentUser
                    if (user != null) {
                        isProfileCheckComplete = false
                        try {
                            // Reload auth to get fresh email verification status
                            user.reload().await()
                        } catch (e: Exception) {
                            Log.w(TAG, "Auth reload note: ${e.message}")
                        }

                        try {
                            val snapshot = FirestoreManager.firestore
                                .collection(FirestoreManager.USERS_COLLECTION)
                                .document(user.uid)
                                .get()
                                .await()

                            if (snapshot.exists()) {
                                val remote = snapshot.toObject(UserProfile::class.java)
                                if (remote != null) {
                                    DatingApp.instance.userProfileRepository.cacheUserProfile(remote)
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Profile initial sync: ${e.message}")
                        } finally {
                            isProfileCheckComplete = true
                        }
                    } else {
                        isProfileCheckComplete = true
                    }
                }

                // 3. Compute Navigation State Machine strictly from Verified Source of Truth
                val appState: AppNavigationState = remember(currentUser, isAuthInitialized, isProfileCheckComplete, userProfile, isEditingProfileInApp, isEmailVerificationBypassed) {
                    val user = currentUser
                    when {
                        !isAuthInitialized || (!isProfileCheckComplete && user != null) -> {
                            AppNavigationState.Initializing
                        }
                        user == null -> {
                            AppNavigationState.Unauthenticated
                        }
                        userProfile.accountStatus.equals("banned", ignoreCase = true) ||
                        userProfile.accountStatus.equals("suspended", ignoreCase = true) ||
                        userProfile.accountStatus.equals("deleted", ignoreCase = true) -> {
                            AppNavigationState.AccountBlocked(
                                status = userProfile.accountStatus,
                                reason = userProfile.statusReason
                            )
                        }
                        !user.isEmailVerified && !isEmailVerificationBypassed -> {
                            AppNavigationState.EmailVerificationPending(user.email ?: "")
                        }
                        isEditingProfileInApp -> {
                            AppNavigationState.ProfileSetupRequired
                        }
                        !userProfile.isProfileComplete || !userProfile.hasAllRequiredInformation() -> {
                            AppNavigationState.ProfileSetupRequired
                        }
                        else -> {
                            AppNavigationState.ReadyForHome
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(
                        targetState = appState,
                        label = "AppNavTransition"
                    ) { state ->
                        when (state) {
                            is AppNavigationState.Initializing -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(
                                            color = Color(0xFFFF2D55),
                                            strokeWidth = 3.dp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Connecting...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            is AppNavigationState.Unauthenticated -> {
                                AuthNavigation(
                                    onAuthComplete = {
                                        currentUser = FirebaseAuth.getInstance().currentUser
                                        profileViewModel.loadUserProfile()
                                    }
                                )
                            }
                            is AppNavigationState.AccountBlocked -> {
                                AccountStatusScreen(
                                    status = state.status,
                                    reason = state.reason,
                                    onSignOut = {
                                        currentUser = null
                                        isEditingProfileInApp = false
                                        isEmailVerificationBypassed = false
                                    }
                                )
                            }
                            is AppNavigationState.EmailVerificationPending -> {
                                EmailVerificationScreen(
                                    userEmail = state.email,
                                    onEmailVerified = {
                                        currentUser = FirebaseAuth.getInstance().currentUser
                                        profileViewModel.loadUserProfile()
                                    },
                                    onContinueLater = {
                                        isEmailVerificationBypassed = true
                                    },
                                    onSignOut = {
                                        currentUser = null
                                        isEditingProfileInApp = false
                                        isEmailVerificationBypassed = false
                                    }
                                )
                            }
                            is AppNavigationState.ProfileSetupRequired -> {
                                ProfileSetupNavigation(
                                    profileViewModel = profileViewModel,
                                    onProfileSetupComplete = {
                                        isEditingProfileInApp = false
                                        profileViewModel.loadUserProfile()
                                    }
                                )
                            }
                            is AppNavigationState.ReadyForHome -> {
                                MainHomeScreen(
                                    profileViewModel = profileViewModel,
                                    onEditProfileRequested = {
                                        isEditingProfileInApp = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

