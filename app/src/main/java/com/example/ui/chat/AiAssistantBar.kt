package com.example.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatConversation

enum class AiMode {
    NONE,
    ICEBREAKERS,
    TRANSLATION,
    TONE_POLISH,
    CULTURAL_TIPS
}

@Composable
fun AiAssistantBar(
    conversation: ChatConversation,
    currentInputText: String,
    onSelectSuggestion: (String) -> Unit
) {
    var activeMode by remember { mutableStateOf(AiMode.NONE) }
    var selectedTargetLanguage by remember { mutableStateOf("Urdu 🇵🇰") }

    val languages = listOf(
        "Urdu 🇵🇰", "Chinese 🇨🇳", "Spanish 🇪🇸", "Japanese 🇯🇵", "Korean 🇰🇷",
        "Russian 🇷🇺", "French 🇫🇷", "German 🇩🇪", "Arabic 🇦🇪", "Burmese 🇲🇲", "English 🇺🇸"
    )

    val icebreakerSuggestions = remember(conversation.otherUserName) {
        listOf(
            "“Hey ${conversation.otherUserName}! If we were planning our first date in ${conversation.otherUserCity}, what hidden gem would you take me to? ☕”",
            "“Loved your profile! What’s your absolute favorite travel memory so far? ✈️”",
            "“Hi ${conversation.otherUserName}! Quick debate: cozy home-cooked dinner or exploring rooftop restaurants? 🌮✨”",
            "“Your photos have such vibrant energy! What passions get you most excited lately? 🎨”"
        )
    }

    val culturalTips = remember(conversation.otherUserName) {
        listOf(
            "💡 Cross-Cultural Tip: Express genuine interest in learning about their local traditions, favorite festivals, and authentic culinary spots.",
            "💡 Language Exchange: Asking them how to say 'Good morning' or 'Thank you' in their native language is a charming conversation starter!",
            "💡 Respect & Politeness: Keep conversations respectful and ask open-ended questions about their favorite travel destinations."
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F112B))
    ) {
        // AI Header Toggle Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00D2FF).copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🤖 AI Assistant", color = Color(0xFF00D2FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AiToolButton("💡 Icebreaker", activeMode == AiMode.ICEBREAKERS) {
                    activeMode = if (activeMode == AiMode.ICEBREAKERS) AiMode.NONE else AiMode.ICEBREAKERS
                }
                AiToolButton("🌐 Translate", activeMode == AiMode.TRANSLATION) {
                    activeMode = if (activeMode == AiMode.TRANSLATION) AiMode.NONE else AiMode.TRANSLATION
                }
                AiToolButton("✍️ Polish", activeMode == AiMode.TONE_POLISH) {
                    activeMode = if (activeMode == AiMode.TONE_POLISH) AiMode.NONE else AiMode.TONE_POLISH
                }
                AiToolButton("🌍 Etiquette", activeMode == AiMode.CULTURAL_TIPS) {
                    activeMode = if (activeMode == AiMode.CULTURAL_TIPS) AiMode.NONE else AiMode.CULTURAL_TIPS
                }
            }
        }

        // Expanded Options Content
        AnimatedVisibility(visible = activeMode != AiMode.NONE) {
            Surface(
                color = Color.Black.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    when (activeMode) {
                        AiMode.ICEBREAKERS -> {
                            Text("Tap an icebreaker to use it in conversation:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(icebreakerSuggestions) { suggestion ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.08f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00D2FF).copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .width(260.dp)
                                            .clickable {
                                                onSelectSuggestion(suggestion.replace("“", "").replace("”", ""))
                                                activeMode = AiMode.NONE
                                            }
                                    ) {
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        AiMode.TRANSLATION -> {
                            Text("Select Target Language for instant translation:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(languages) { lang ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selectedTargetLanguage == lang) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.clickable {
                                            selectedTargetLanguage = lang
                                            if (currentInputText.isNotBlank()) {
                                                // Dynamic instant simulated translation
                                                val translated = when {
                                                    lang.contains("Urdu") -> "آپ سے بات کر کے بہت اچھا لگا! کیا آپ کا دن اچھا گزر رہا ہے؟ ✨"
                                                    lang.contains("Chinese") -> "很高兴认识你！你今天过得怎么样？ ✨"
                                                    lang.contains("Spanish") -> "¡Qué gusto saludarte! ¿Cómo estuvo tu día? ✨"
                                                    lang.contains("Japanese") -> "お話しできて嬉しいです！今日はどんな一日でしたか？ ✨"
                                                    lang.contains("Korean") -> "만나서 반가워요! 오늘 하루 어떠셨나요? ✨"
                                                    lang.contains("Russian") -> "Очень приятно познакомиться! Как прошел твой день? ✨"
                                                    else -> currentInputText
                                                }
                                                onSelectSuggestion(translated)
                                                activeMode = AiMode.NONE
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = lang,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        AiMode.TONE_POLISH -> {
                            Text("Enhance your message tone with Gemini AI:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(6.dp))
                            val input = currentInputText.ifBlank { "Hey, how are you doing today?" }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    ToneChip("💕 Romantic", "“I was just thinking about you—hope your day is as bright and wonderful as your smile! 💫”") {
                                        onSelectSuggestion(it)
                                        activeMode = AiMode.NONE
                                    }
                                }
                                item {
                                    ToneChip("😄 Witty", "“On a scale from 1 to 10, how likely are we to grab the best coffee in town this week? ☕😉”") {
                                        onSelectSuggestion(it)
                                        activeMode = AiMode.NONE
                                    }
                                }
                                item {
                                    ToneChip("🤝 Polite", "“Hi ${conversation.otherUserName}, thank you for connecting! I’d love to learn more about your interests.”") {
                                        onSelectSuggestion(it)
                                        activeMode = AiMode.NONE
                                    }
                                }
                            }
                        }

                        AiMode.CULTURAL_TIPS -> {
                            Text("Cross-Cultural Dating Etiquette:", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            culturalTips.forEach { tip ->
                                Text(tip, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun AiToolButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.08f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ToneChip(label: String, sample: String, onClick: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick(sample.replace("“", "").replace("”", "")) }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(label, color = Color(0xFFFF8C42), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(sample, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, maxLines = 2)
        }
    }
}
