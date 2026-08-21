package com.example.ui.profile.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppTextField
import com.example.ui.components.GradientButton
import com.example.ui.theme.PrimaryGradient

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BasicInfoStep(
    displayName: String,
    age: Int,
    gender: String,
    interestedIn: List<String>,
    country: String,
    city: String,
    languages: List<String>,
    relationshipGoal: String,
    onUpdateDisplayName: (String) -> Unit,
    onUpdateAge: (Int) -> Unit,
    onUpdateGender: (String) -> Unit,
    onToggleInterestedIn: (String) -> Unit,
    onUpdateLocation: (String, String) -> Unit,
    onToggleLanguage: (String) -> Unit,
    onUpdateRelationshipGoal: (String) -> Unit,
    onNext: () -> Unit
) {
    val myGenderOptions = listOf("Female", "Male")
    val interestedInOptions = listOf("Female", "Male")
    val goals = listOf(
        "Long-term connection 💖",
        "Casual dating 🥂",
        "New friends 🌟",
        "Marriage-minded 💍",
        "Figuring it out 🤔"
    )
    val availableLanguages = listOf("English", "Spanish", "French", "Japanese", "Mandarin", "German", "Korean", "Arabic")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
            .systemBarsPadding(),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Basic Details 👤",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Help us match you with compatible people who share your wavelength.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display Name Input
        AppTextField(
            value = displayName,
            onValueChange = onUpdateDisplayName,
            label = "Display Name"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Age Selector
        Text(
            text = "Age: $age years old",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = age.toFloat(),
            onValueChange = { onUpdateAge(it.toInt()) },
            valueRange = 18f..80f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF2D55),
                activeTrackColor = Color(0xFFFF2D55),
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // My Gender
        Text(
            text = "My Gender",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            myGenderOptions.forEach { g ->
                val isSelected = gender == g
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFFFF2D55).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUpdateGender(g) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = g,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Who are you interested in? ❤️
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Who are you interested in? ❤️",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "Select one or both options to customize match recommendations.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            interestedInOptions.forEach { targetGender ->
                val isSelected = interestedIn.contains(targetGender)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFFFF2D55).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleInterestedIn(targetGender) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = targetGender,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFFFF2D55),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Location Input
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AppTextField(
                    value = country,
                    onValueChange = { onUpdateLocation(it, city) },
                    label = "Country"
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                AppTextField(
                    value = city,
                    onValueChange = { onUpdateLocation(country, it) },
                    label = "City"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Spoken Languages
        Text(
            text = "Spoken Languages",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableLanguages.forEach { lang ->
                val isSelected = languages.contains(lang)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleLanguage(lang) },
                    label = { Text(lang, color = Color.White) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF2D55),
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Relationship Goal
        Text(
            text = "Relationship Goal",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            goals.forEach { goal ->
                val isSelected = relationshipGoal == goal
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFFFF2D55).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateRelationshipGoal(goal) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onUpdateRelationshipGoal(goal) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF2D55))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = goal,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        var validationError by remember { mutableStateOf<String?>(null) }

        androidx.compose.animation.AnimatedVisibility(visible = validationError != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = validationError ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        GradientButton(
            text = "Continue →",
            onClick = {
                when {
                    displayName.trim().length < 2 -> validationError = "Please enter your name (at least 2 characters)."
                    age < 18 -> validationError = "You must be at least 18 years old."
                    gender.isBlank() -> validationError = "Please select your gender."
                    interestedIn.isEmpty() -> validationError = "Please select who you are interested in."
                    country.trim().isBlank() -> validationError = "Please enter your country."
                    relationshipGoal.isBlank() -> validationError = "Please choose your relationship goal."
                    else -> {
                        validationError = null
                        onNext()
                    }
                }
            },
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}


