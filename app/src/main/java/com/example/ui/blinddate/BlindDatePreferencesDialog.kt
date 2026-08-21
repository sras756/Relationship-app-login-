package com.example.ui.blinddate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BlindDatePreferences

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlindDatePreferencesDialog(
    currentPreferences: BlindDatePreferences,
    onSavePreferences: (BlindDatePreferences) -> Unit,
    onDismiss: () -> Unit
) {
    var interestedInGender by remember { mutableStateOf(currentPreferences.interestedIn) }
    var selectedCountries by remember { mutableStateOf(currentPreferences.selectedCountries) }
    var minAge by remember { mutableFloatStateOf(currentPreferences.minAge.toFloat()) }
    var maxAge by remember { mutableFloatStateOf(currentPreferences.maxAge.toFloat()) }
    var selectedGoal by remember { mutableStateOf(currentPreferences.relationshipGoal) }
    var selectedLanguage by remember { mutableStateOf(currentPreferences.languagePreference) }

    val countriesList = listOf(
        "All 🌍",
        "Pakistan 🇵🇰",
        "China 🇨🇳",
        "South Korea 🇰🇷",
        "Japan 🇯🇵",
        "Russia 🇷🇺",
        "Myanmar 🇲🇲",
        "United States 🇺🇸",
        "European countries 🇪🇺"
    )

    val goalsList = listOf("All", "Long-term connection", "Marriage / Serious", "International Dating", "Deep Conversations")
    val languagesList = listOf("All", "English", "Urdu", "Mandarin", "Korean", "Russian", "Japanese")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFFFF2D55))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Blind Date Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Gender Preference
                    item {
                        Text(
                            text = "Gender Preference (Interested in)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF85A1)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Male", "Female", "Both").forEach { genderOption ->
                                val isSelected = if (genderOption == "Both") {
                                    interestedInGender.contains("Male") && interestedInGender.contains("Female")
                                } else {
                                    interestedInGender.contains(genderOption) && !(interestedInGender.contains("Male") && interestedInGender.contains("Female"))
                                }

                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        interestedInGender = when (genderOption) {
                                            "Both" -> listOf("Male", "Female")
                                            else -> listOf(genderOption)
                                        }
                                    },
                                    label = { Text(genderOption) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF2D55),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // 2. Nationality & Country Preference
                    item {
                        Text(
                            text = "Country / Nationality Preferences (Multi-Select)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF85A1)
                        )
                        Text(
                            text = "Preferences guide matchmaking without strictly limiting connection",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            countriesList.forEach { country ->
                                val cleanCountry = country.split(" ").first()
                                val isSelected = selectedCountries.contains("All") && country.contains("All") ||
                                        selectedCountries.any { country.contains(it) }

                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (country.contains("All")) {
                                            selectedCountries = listOf("All")
                                        } else {
                                            val current = selectedCountries.filter { it != "All" }.toMutableList()
                                            if (current.contains(cleanCountry)) {
                                                current.remove(cleanCountry)
                                            } else {
                                                current.add(cleanCountry)
                                            }
                                            selectedCountries = if (current.isEmpty()) listOf("All") else current
                                        }
                                    },
                                    label = { Text(country, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00D2FF),
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    // 3. Age Range Slider
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Age Range",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF85A1)
                            )
                            Text(
                                text = "${minAge.toInt()} - ${maxAge.toInt()} years",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        RangeSlider(
                            value = minAge..maxAge,
                            onValueChange = { range ->
                                minAge = range.start
                                maxAge = range.endInclusive
                            },
                            valueRange = 18f..50f,
                            steps = 31,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF2D55),
                                activeTrackColor = Color(0xFFFF2D55)
                            )
                        )
                    }

                    // 4. Relationship Goal
                    item {
                        Text(
                            text = "Relationship Goal",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF85A1)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            goalsList.forEach { goal ->
                                val isSelected = selectedGoal == goal
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedGoal = goal },
                                    label = { Text(goal, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF2D55),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // 5. Language Preference
                    item {
                        Text(
                            text = "Preferred Language",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF85A1)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            languagesList.forEach { lang ->
                                val isSelected = selectedLanguage == lang
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedLanguage = lang },
                                    label = { Text(lang, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00D2FF),
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save Button
                Button(
                    onClick = {
                        val newPrefs = BlindDatePreferences(
                            interestedIn = interestedInGender,
                            selectedCountries = selectedCountries,
                            minAge = minAge.toInt(),
                            maxAge = maxAge.toInt(),
                            relationshipGoal = selectedGoal,
                            languagePreference = selectedLanguage
                        )
                        onSavePreferences(newPrefs)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Preferences", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
