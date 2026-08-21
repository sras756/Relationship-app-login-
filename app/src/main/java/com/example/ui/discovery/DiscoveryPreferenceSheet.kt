package com.example.ui.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DiscoveryPreferences
import com.example.ui.components.GradientButton

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryPreferenceSheet(
    preferences: DiscoveryPreferences,
    onSavePreferences: (DiscoveryPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    var interestedIn by remember { mutableStateOf(preferences.interestedIn) }
    var minAge by remember { mutableFloatStateOf(preferences.minAge.toFloat()) }
    var maxAge by remember { mutableFloatStateOf(preferences.maxAge.toFloat()) }
    var maxDistance by remember { mutableFloatStateOf(preferences.maxDistanceMiles.toFloat()) }
    var selectedGoal by remember { mutableStateOf(preferences.relationshipGoal) }
    var selectedLanguage by remember { mutableStateOf(preferences.languagePreference) }
    var sharedInterestsOnly by remember { mutableStateOf(preferences.sharedInterestsOnly) }

    val goals = listOf("All", "Long-term connection", "Casual dating", "Marriage-minded", "New friends")
    val languages = listOf("All", "English", "Spanish", "French", "Japanese", "Mandarin", "German")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color(0xFFFF2D55),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Discovery Preferences",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interested in: Male / Female / Both
            Text(
                text = "Interested in ❤️",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Female", "Male").forEach { option ->
                    val isSelected = interestedIn.contains(option)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFFF2D55).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val current = interestedIn.toMutableList()
                                if (current.contains(option)) {
                                    if (current.size > 1) current.remove(option)
                                } else {
                                    current.add(option)
                                }
                                interestedIn = current
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                color = Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFFF2D55), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Age Range Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Age Range",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${minAge.toInt()} - ${maxAge.toInt()} yrs",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF2D55),
                    fontWeight = FontWeight.Bold
                )
            }
            RangeSlider(
                value = minAge..maxAge,
                onValueChange = { range ->
                    minAge = range.start
                    maxAge = range.endInclusive
                },
                valueRange = 18f..70f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF2D55),
                    activeTrackColor = Color(0xFFFF2D55),
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Max Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Maximum Distance",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${maxDistance.toInt()} miles",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF2D55),
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = maxDistance,
                onValueChange = { maxDistance = it },
                valueRange = 5f..200f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF2D55),
                    activeTrackColor = Color(0xFFFF2D55),
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Relationship Goal Filter
            Text(
                text = "Relationship Goal",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                goals.forEach { goal ->
                    FilterChip(
                        selected = selectedGoal == goal,
                        onClick = { selectedGoal = goal },
                        label = { Text(goal, color = Color.White) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF2D55),
                            containerColor = Color.White.copy(alpha = 0.05f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Language Preference
            Text(
                text = "Language Preference",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languages.forEach { lang ->
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = { selectedLanguage = lang },
                        label = { Text(lang, color = Color.White) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF2D55),
                            containerColor = Color.White.copy(alpha = 0.05f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Country / Region Multi-Preference
            Text(
                text = "Countries & Regions 🌍",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Discover matches from your preferred international locations",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            var selectedCountries by remember { mutableStateOf(preferences.selectedCountries) }
            val internationalCountries = listOf(
                "All", "Pakistan 🇵🇰", "China 🇨🇳", "South Korea 🇰🇷", "Japan 🇯🇵",
                "Russia 🇷🇺", "Myanmar 🇲🇲", "United States 🇺🇸", "Europe 🇪🇺", "Turkey 🇹🇷", "Brazil 🇧🇷"
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                internationalCountries.forEach { country ->
                    val isSelected = selectedCountries.contains(country)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val current = selectedCountries.toMutableList()
                            if (country == "All") {
                                selectedCountries = listOf("All")
                            } else {
                                current.remove("All")
                                if (current.contains(country)) {
                                    current.remove(country)
                                    if (current.isEmpty()) current.add("All")
                                } else {
                                    current.add(country)
                                }
                                selectedCountries = current
                            }
                        },
                        label = { Text(country, color = Color.White) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF2D55),
                            containerColor = Color.White.copy(alpha = 0.05f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verified Only Toggle
            var verifiedOnly by remember { mutableStateOf(preferences.verifiedOnly) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Verified Profiles Only 🛡️",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Show only photo & identity verified users",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = verifiedOnly,
                    onCheckedChange = { verifiedOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF2D55)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shared Interests Only Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Shared Interests Only",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Show profiles that share at least 1 mutual hobby.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = sharedInterestsOnly,
                    onCheckedChange = { sharedInterestsOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF2D55)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = "Apply Discovery Filters ✨",
                onClick = {
                    onSavePreferences(
                        DiscoveryPreferences(
                            interestedIn = interestedIn,
                            minAge = minAge.toInt(),
                            maxAge = maxAge.toInt(),
                            maxDistanceMiles = maxDistance.toInt(),
                            selectedCountries = selectedCountries,
                            relationshipGoal = selectedGoal,
                            languagePreference = selectedLanguage,
                            verifiedOnly = verifiedOnly,
                            sharedInterestsOnly = sharedInterestsOnly
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
