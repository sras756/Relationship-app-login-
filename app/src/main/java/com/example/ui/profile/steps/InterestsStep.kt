package com.example.ui.profile.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GradientButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsStep(
    selectedInterests: List<String>,
    onToggleInterest: (String) -> Unit,
    onNext: () -> Unit
) {
    val interests = listOf(
        "🎮 Gaming", "🎬 Anime & Movies", "🎵 Music", "📚 Books",
        "✈️ Travel", "🍳 Cooking", "⚽ Sports", "🎨 Art",
        "💻 Technology", "📸 Photography", "🐾 Animals", "🌿 Nature",
        "☕ Coffee & Cafes", "🧘 Fitness & Yoga", "💃 Dancing", "🍷 Wine & Dining"
    )

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
            text = "Your Interests 🎯",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select at least 3 topics you love to help us find great matches.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Counter badge
        Text(
            text = "${selectedInterests.size} selected",
            style = MaterialTheme.typography.labelSmall,
            color = if (selectedInterests.size >= 3) Color(0xFF00D2FF) else Color(0xFFFF2D55),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            interests.forEach { item ->
                val isSelected = selectedInterests.contains(item)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleInterest(item) },
                    label = {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF2D55),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.05f),
                        labelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color.White.copy(alpha = 0.15f),
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        GradientButton(
            text = "Continue →",
            onClick = onNext,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}
