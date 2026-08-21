package com.example.ui.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.profile.steps.*
import com.example.ui.theme.PrimaryGradient

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileSetupNavigation(
    onProfileSetupComplete: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val currentStep by profileViewModel.currentStep.collectAsState()
    val saveState by profileViewModel.saveState.collectAsState()

    val completionPercentage = profileViewModel.calculateCompletionPercentage()

    Scaffold(
        topBar = {
            if (currentStep > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { profileViewModel.previousStep() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // Progress indicator text
                        Text(
                            text = "Profile Completion: $completionPercentage%",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFFF2D55),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Animated Progress Bar
                    LinearProgressIndicator(
                        progress = { completionPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color(0xFFFF2D55),
                        trackColor = Color.White.copy(alpha = 0.1f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "StepTransition"
            ) { step ->
                when (step) {
                    0 -> WelcomeIntroStep(
                        onStart = { profileViewModel.nextStep() }
                    )
                    1 -> PhotosStep(
                        photoUrls = userProfile.photoUrls,
                        primaryPhotoUrl = userProfile.primaryPhotoUrl,
                        isVerified = userProfile.isVerified,
                        onAddPhoto = { profileViewModel.addPhoto(it) },
                        onRemovePhoto = { profileViewModel.removePhoto(it) },
                        onSetPrimaryPhoto = { profileViewModel.setPrimaryPhoto(it) },
                        onToggleVerification = { profileViewModel.toggleVerification(it) },
                        onNext = { profileViewModel.nextStep() }
                    )
                    2 -> BasicInfoStep(
                        displayName = userProfile.displayName,
                        age = userProfile.age,
                        gender = userProfile.gender,
                        interestedIn = userProfile.interestedIn,
                        country = userProfile.country,
                        city = userProfile.city,
                        languages = userProfile.languages,
                        relationshipGoal = userProfile.relationshipGoal,
                        onUpdateDisplayName = { profileViewModel.updateDisplayName(it) },
                        onUpdateAge = { profileViewModel.updateAge(it) },
                        onUpdateGender = { profileViewModel.updateGender(it) },
                        onToggleInterestedIn = { profileViewModel.toggleInterestedIn(it) },
                        onUpdateLocation = { c, ci -> profileViewModel.updateLocation(c, ci) },
                        onToggleLanguage = { profileViewModel.toggleLanguage(it) },
                        onUpdateRelationshipGoal = { profileViewModel.updateRelationshipGoal(it) },
                        onNext = { profileViewModel.nextStep() }
                    )
                    3 -> AboutMeStep(
                        bio = userProfile.bio,
                        onUpdateBio = { profileViewModel.updateBio(it) },
                        onGenerateAiBio = { style -> profileViewModel.generateAiBioSuggestion(style) },
                        onNext = { profileViewModel.nextStep() }
                    )
                    4 -> InterestsStep(
                        selectedInterests = userProfile.interests,
                        onToggleInterest = { profileViewModel.toggleInterest(it) },
                        onNext = { profileViewModel.nextStep() }
                    )
                    5 -> PromptsStep(
                        promptAnswers = userProfile.promptAnswers,
                        onUpdatePromptAnswer = { q, a -> profileViewModel.updatePromptAnswer(q, a) },
                        onNext = { profileViewModel.nextStep() }
                    )
                    6 -> PrivacySettingsStep(
                        privacySettings = userProfile.privacySettings,
                        onUpdatePrivacySettings = { profileViewModel.updatePrivacySettings(it) },
                        onNext = { profileViewModel.nextStep() }
                    )
                    7 -> ProfilePreviewStep(
                        userProfile = userProfile,
                        onEditProfile = { profileViewModel.goToStep(1) },
                        onCompleteProfile = {
                            profileViewModel.saveProfile {
                                onProfileSetupComplete()
                            }
                        },
                        isSaving = saveState is ProfileSetupState.Saving,
                        errorMessage = (saveState as? ProfileSetupState.Error)?.message
                    )

                }
            }
        }
    }
}
