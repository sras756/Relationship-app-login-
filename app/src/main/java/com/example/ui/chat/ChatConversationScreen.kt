package com.example.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.CameraCaptureView
import com.example.ui.theme.PrimaryGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    conversation: ChatConversation,
    messages: List<ChatMessage>,
    isPartnerTyping: Boolean,
    onBack: () -> Unit,
    onSendMessage: (content: String, type: MessageType, mediaUrl: String, duration: Int, replyId: String?, replyContent: String?) -> Unit,
    onReaction: (messageId: String, emoji: String) -> Unit,
    onPinMessage: (messageId: String) -> Unit,
    onDeleteMessage: (messageId: String) -> Unit,
    onStartCall: (CallType) -> Unit,
    onToggleMute: () -> Unit,
    onTogglePin: () -> Unit,
    onBlockUser: (String) -> Unit,
    onReportUser: (String, String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var selectedMessageForOptions by remember { mutableStateOf<ChatMessage?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showCameraView by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            onSendMessage(
                "Photo",
                MessageType.IMAGE,
                it.toString(),
                0,
                replyingToMessage?.messageId,
                replyingToMessage?.messageContent
            )
            replyingToMessage = null
        }
    }

    // Scroll to bottom on new message
    LaunchedEffect(messages.size, isPartnerTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Simulated Voice Recording Timer
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingDuration = 0
            while (isRecordingVoice) {
                delay(1000)
                recordingDuration++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        // Partner Avatar with Online Dot
                        Box(modifier = Modifier.size(42.dp)) {
                            AsyncImage(
                                model = conversation.otherUserPhoto,
                                contentDescription = conversation.otherUserName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (conversation.isOtherUserOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0xFF00E676), CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = conversation.otherUserName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                if (conversation.isOtherUserVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF00D2FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isPartnerTyping) "typing..." else if (conversation.isOtherUserOnline) "Online now" else conversation.otherUserLastActive,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPartnerTyping) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Top Action Icons (Voice, Video, More)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onStartCall(CallType.VOICE) }) {
                            Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = Color.White)
                        }
                        IconButton(onClick = { onStartCall(CallType.VIDEO) }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                        }
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (conversation.isPinned) "Unpin Chat" else "Pin Chat") },
                                    onClick = {
                                        onTogglePin()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (conversation.isMuted) "Unmute" else "Mute Notifications") },
                                    onClick = {
                                        onToggleMute()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.VolumeOff, contentDescription = null) }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Report Profile", color = Color(0xFFFF9800)) },
                                    onClick = {
                                        showMoreMenu = false
                                        showReportDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFFFF9800)) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Block User", color = Color(0xFFFF2D55)) },
                                    onClick = {
                                        showMoreMenu = false
                                        showBlockConfirmDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFFF2D55)) }
                                )
                            }
                        }
                    }
                }
            }

            // Safety Warning Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFF2D55).copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFFFF2D55),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Safety Tip: Never send money or share bank info with matches.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Romantic Match Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFFF2D55), CircleShape)
                        ) {
                            AsyncImage(
                                model = conversation.otherUserPhoto,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You and ${conversation.otherUserName} matched 💕",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${conversation.otherUserCity} • ${conversation.otherUserAge} yrs old",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                items(messages, key = { it.messageId }) { msg ->
                    val isOutgoing = msg.senderId == "current_user"
                    MessageBubbleItem(
                        message = msg,
                        isOutgoing = isOutgoing,
                        onLongClick = { selectedMessageForOptions = msg },
                        onReaction = { emoji -> onReaction(msg.messageId, emoji) }
                    )
                }

                if (isPartnerTyping) {
                    item {
                        TypingIndicatorBubble(partnerName = conversation.otherUserName)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Replying Context Banner
            if (replyingToMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Replying to message",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF2D55),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = replyingToMessage?.messageContent ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = { replyingToMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = Color.White)
                        }
                    }
                }
            }

            // AI Smart Assistant & Translation Toolbar
            AiAssistantBar(
                conversation = conversation,
                currentInputText = messageText,
                onSelectSuggestion = { messageText = it }
            )

            // Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                if (isRecordingVoice) {
                    // Voice Recording Active Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(0xFFFF2D55), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording audio... 0:${String.format(Locale.US, "%02d", recordingDuration)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { isRecordingVoice = false }) {
                                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                            }
                            Button(
                                onClick = {
                                    val duration = if (recordingDuration == 0) 3 else recordingDuration
                                    onSendMessage(
                                        "Voice Message",
                                        MessageType.VOICE,
                                        "audio_recording_uri",
                                        duration,
                                        replyingToMessage?.messageId,
                                        replyingToMessage?.messageContent
                                    )
                                    isRecordingVoice = false
                                    replyingToMessage = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55))
                            ) {
                                Text("Send 🎙️")
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Action: CameraX
                        IconButton(
                            onClick = { showCameraView = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = Color(0xFFFF2D55)
                            )
                        }

                        // Quick Action: Gallery Photos
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        // Message Text Field
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Type a message...", color = Color.White.copy(alpha = 0.4f)) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = false,
                            maxLines = 4
                        )

                        if (messageText.isBlank()) {
                            // Voice Record Button
                            IconButton(
                                onClick = { isRecordingVoice = true },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Record Voice",
                                    tint = Color.White
                                )
                            }
                        } else {
                            // Send Button
                            IconButton(
                                onClick = {
                                    val textToSend = messageText.trim()
                                    if (textToSend.isNotBlank()) {
                                        onSendMessage(
                                            textToSend,
                                            MessageType.TEXT,
                                            "",
                                            0,
                                            replyingToMessage?.messageId,
                                            replyingToMessage?.messageContent
                                        )
                                        messageText = ""
                                        replyingToMessage = null
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Brush.linearGradient(PrimaryGradient), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // CameraX Capture Fullscreen Overlay
        AnimatedVisibility(
            visible = showCameraView,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            CameraCaptureView(
                onPhotoCaptured = { uri ->
                    onSendMessage(
                        "Photo",
                        MessageType.IMAGE,
                        uri.toString(),
                        0,
                        replyingToMessage?.messageId,
                        replyingToMessage?.messageContent
                    )
                    showCameraView = false
                    replyingToMessage = null
                },
                onDismiss = { showCameraView = false }
            )
        }

        // Message Action BottomSheet / Reaction Popup
        if (selectedMessageForOptions != null) {
            val targetMsg = selectedMessageForOptions!!
            AlertDialog(
                onDismissRequest = { selectedMessageForOptions = null },
                title = { Text("Message Options") },
                text = {
                    Column {
                        // Quick Reaction Picker
                        Text("React:", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("❤️", "😂", "🔥", "😮", "👏", "😍").forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                        .clickable {
                                            onReaction(targetMsg.messageId, emoji)
                                            selectedMessageForOptions = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    replyingToMessage = targetMsg
                                    selectedMessageForOptions = null
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Reply, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Reply to message")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(targetMsg.messageContent))
                                    selectedMessageForOptions = null
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Copy text")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPinMessage(targetMsg.messageId)
                                    selectedMessageForOptions = null
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(if (targetMsg.isPinned) "Unpin message" else "Pin message")
                        }

                        if (targetMsg.senderId == "current_user") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDeleteMessage(targetMsg.messageId)
                                        selectedMessageForOptions = null
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF2D55))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Delete message", color = Color(0xFFFF2D55))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedMessageForOptions = null }) {
                        Text("Close")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Block Dialog
        if (showBlockConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showBlockConfirmDialog = false },
                title = { Text("Block ${conversation.otherUserName}?") },
                text = { Text("They will no longer be able to message you or see your profile. You can unblock anytime from Settings.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onBlockUser(conversation.otherUserId)
                            showBlockConfirmDialog = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55))
                    ) {
                        Text("Block")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBlockConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Report Dialog
        if (showReportDialog) {
            var reportReason by remember { mutableStateOf("Inappropriate messages") }
            val reasons = listOf("Inappropriate messages", "Scam or commercial", "Fake profile", "Harassment", "Other")

            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                title = { Text("Report Profile") },
                text = {
                    Column {
                        Text("Select a reason for reporting:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        reasons.forEach { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { reportReason = r }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = reportReason == r,
                                    onClick = { reportReason = r },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF2D55))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(r)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onReportUser(conversation.otherUserId, reportReason)
                            showReportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55))
                    ) {
                        Text("Submit Report")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun MessageBubbleItem(
    message: ChatMessage,
    isOutgoing: Boolean,
    onLongClick: () -> Unit,
    onReaction: (String) -> Unit
) {
    val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        // Pinned status tag
        if (message.isPinned) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = Color(0xFFFF2D55),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Pinned",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF2D55)
                )
            }
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isOutgoing) 18.dp else 4.dp,
                        bottomEnd = if (isOutgoing) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isOutgoing) Brush.linearGradient(PrimaryGradient)
                    else Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongClick() }
                    )
                }
                .padding(12.dp)
        ) {
            Column {
                // Reply quote banner if present
                if (message.replyToContent != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = message.replyToContent,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(6.dp),
                            maxLines = 1
                        )
                    }
                }

                // Suspicious spam warning badge
                if (message.moderationStatus == ModerationStatus.SUSPICIOUS_SPAM) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Potential Suspicious Message",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFC107),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Message Type Rendering
                var isTranslated by remember { mutableStateOf(false) }
                when (message.messageType) {
                    MessageType.TEXT -> {
                        Column {
                            Text(
                                text = if (isTranslated) "🌐 Translation: “${message.messageContent} (Understood perfectly in English/Local language!)”" else message.messageContent,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            if (!isOutgoing && message.messageContent.length > 5) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.clickable { isTranslated = !isTranslated },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isTranslated) "Show Original" else "Translate 🌐",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF00D2FF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    MessageType.IMAGE -> {
                        if (message.mediaUrl.isNotEmpty()) {
                            AsyncImage(
                                model = message.mediaUrl,
                                contentDescription = "Attached image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (message.messageContent.isNotBlank() && message.messageContent != "Photo") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.messageContent,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                    MessageType.VOICE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Simulated audio waveform lines
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                listOf(8, 16, 24, 12, 20, 14, 18, 10, 22, 12).forEach { h ->
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(h.dp)
                                            .background(Color.White, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "0:${String.format(Locale.US, "%02d", message.audioDurationSeconds)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    MessageType.MATCH_EVENT -> {
                        Text(
                            text = "🎉 ${message.messageContent}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF69B4),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    else -> {
                        Text(
                            text = message.messageContent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time + Read Status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = when (message.readStatus) {
                                MessageReadStatus.SENT -> Icons.Default.Check
                                MessageReadStatus.DELIVERED -> Icons.Default.DoneAll
                                MessageReadStatus.READ -> Icons.Default.DoneAll
                            },
                            contentDescription = null,
                            tint = if (message.readStatus == MessageReadStatus.READ) Color(0xFF00D2FF) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // Reaction chips attached to bottom
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.reactions.values.distinct().forEach { emoji ->
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E1428),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.clickable { onReaction(emoji) }
                    ) {
                        Text(
                            text = emoji,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble(partnerName: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$partnerName is typing",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(Color(0xFFFF2D55), CircleShape)
                    )
                }
            }
        }
    }
}
