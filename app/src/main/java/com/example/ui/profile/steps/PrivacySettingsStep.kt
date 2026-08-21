package com.example.ui.profile.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PrivacySettings
import com.example.ui.components.GradientButton

@Composable
fun PrivacySettingsStep(
    privacySettings: PrivacySettings,
    onUpdatePrivacySettings: (PrivacySettings) -> Unit,
    onNext: () -> Unit
) {
    var acceptedGuidelines by remember { mutableStateOf(true) }

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
            text = "Privacy & Controls 🛡️",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Control what information is visible to other members. Your phone number & email are always private.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle switches card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PrivacyToggleRow(
                    title = "Show City Location",
                    subtitle = "Display ${if (privacySettings.showCity) "your city on profile" else "hidden"}",
                    checked = privacySettings.showCity,
                    onCheckedChange = { onUpdatePrivacySettings(privacySettings.copy(showCity = it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

                PrivacyToggleRow(
                    title = "Show Online Status",
                    subtitle = "Let matches see when you are active",
                    checked = privacySettings.showOnlineStatus,
                    onCheckedChange = { onUpdatePrivacySettings(privacySettings.copy(showOnlineStatus = it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

                PrivacyToggleRow(
                    title = "Show Distance",
                    subtitle = "Show approximate distance in miles",
                    checked = privacySettings.showDistance,
                    onCheckedChange = { onUpdatePrivacySettings(privacySettings.copy(showDistance = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Messaging Control Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Who Can Message You",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                listOf("Everyone", "Verified Profiles Only").forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUpdatePrivacySettings(privacySettings.copy(whoCanMessage = option)) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = privacySettings.whoCanMessage == option,
                            onClick = { onUpdatePrivacySettings(privacySettings.copy(whoCanMessage = option)) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF2D55))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Community Guidelines Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFF2D55).copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF2D55).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = acceptedGuidelines,
                    onCheckedChange = { acceptedGuidelines = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF2D55))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I agree to community guidelines: treat others with kindness, no harassment, no fake accounts, and 18+ age verification.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        GradientButton(
            text = "Preview Profile →",
            onClick = onNext,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun PrivacyToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFF2D55)
            )
        )
    }
}
