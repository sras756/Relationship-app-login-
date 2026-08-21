package com.example.ui.discovery

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.InteractionType
import com.example.data.model.SwipeDirection
import com.example.data.model.UserProfile
import com.example.ui.theme.PrimaryGradient
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwipeableProfileCard(
    profile: UserProfile,
    nextProfile: UserProfile? = null,
    modifier: Modifier = Modifier,
    onSwiped: (UserProfile, SwipeDirection) -> Unit,
    onSuperLikeRequested: (UserProfile) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    var currentPhotoIndex by remember(profile.uid) { mutableIntStateOf(0) }
    val photos = remember(profile) {
        if (profile.photoUrls.isNotEmpty()) profile.photoUrls else listOf(profile.primaryPhotoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" })
    }

    val swipeThreshold = screenWidthPx * 0.35f
    val verticalThreshold = screenWidthPx * 0.40f

    // Calculate rotation and indicator alphas based on drag offset
    val rotation = (offsetX.value / 35f).coerceIn(-20f, 20f)
    val likeAlpha = (offsetX.value / swipeThreshold).coerceIn(0f, 1f)
    val nopeAlpha = (-offsetX.value / swipeThreshold).coerceIn(0f, 1f)
    val superLikeAlpha = (-offsetY.value / verticalThreshold).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Background Card in Deck (Next Candidate Preview)
        if (nextProfile != null) {
            val backgroundScale = (0.93f + (abs(offsetX.value) / screenWidthPx) * 0.07f).coerceIn(0.93f, 1f)
            val backgroundTranslationY = (24f - (abs(offsetX.value) / screenWidthPx) * 24f).coerceIn(0f, 24f)

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = backgroundScale
                        scaleY = backgroundScale
                        translationY = backgroundTranslationY
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = nextProfile.primaryPhotoUrl.ifEmpty { nextProfile.photoUrls.firstOrNull() },
                        contentDescription = nextProfile.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Dim overlay on back card
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                }
            }
        }

        // 2. Active Front Draggable Card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .graphicsLayer {
                    rotationZ = rotation
                    shadowElevation = 12.dp.toPx()
                }
                .pointerInput(profile.uid) {
                    detectDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                when {
                                    // Swiped Right -> LIKE
                                    offsetX.value > swipeThreshold -> {
                                        offsetX.animateTo(
                                            targetValue = screenWidthPx * 1.5f,
                                            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
                                        )
                                        onSwiped(profile, SwipeDirection.RIGHT)
                                    }
                                    // Swiped Left -> DISLIKE / PASS
                                    offsetX.value < -swipeThreshold -> {
                                        offsetX.animateTo(
                                            targetValue = -screenWidthPx * 1.5f,
                                            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
                                        )
                                        onSwiped(profile, SwipeDirection.LEFT)
                                    }
                                    // Swiped Up -> SUPER LIKE
                                    offsetY.value < -verticalThreshold -> {
                                        offsetY.animateTo(
                                            targetValue = -screenWidthPx * 1.8f,
                                            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
                                        )
                                        onSwiped(profile, SwipeDirection.UP)
                                    }
                                    // Cancelled / Snapped back to center
                                    else -> {
                                        launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                                        launch { offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                                    }
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                offsetY.snapTo(offsetY.value + dragAmount.y)
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(photos.size) {
                        detectTapGestures { offset ->
                            val width = size.width
                            if (offset.x < width * 0.35f && currentPhotoIndex > 0) {
                                currentPhotoIndex--
                            } else if (offset.x > width * 0.65f && currentPhotoIndex < photos.size - 1) {
                                currentPhotoIndex++
                            }
                        }
                    }
            ) {
                // Profile Image
                AsyncImage(
                    model = photos.getOrNull(currentPhotoIndex) ?: profile.primaryPhotoUrl,
                    contentDescription = profile.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Story Progress Bars if multiple photos
                if (photos.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        photos.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.5.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (index == currentPhotoIndex) Color.White else Color.White.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }
                }

                // Top Compatibility Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = if (photos.size > 1) 28.dp else 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚡ 95% Compatibility", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                // Gradient Overlay at bottom for readable text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Black.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                // Profile Details Bottom Info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${profile.displayName}, ${profile.age}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(profile.nationality, fontSize = 16.sp)
                        if (profile.isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF00D2FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (profile.isMasterMember) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("👑 VIP", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "📍 ${profile.city} • ${profile.relationshipGoal}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )

                    if (profile.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.92f),
                            maxLines = 3
                        )
                    }

                    if (profile.interests.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            profile.interests.take(4).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Swipe Feedback Stamps
                // LIKE STAMP (Green)
                if (likeAlpha > 0.05f) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(24.dp)
                            .rotate(-15f)
                            .graphicsLayer { alpha = likeAlpha },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF00E676).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF00E676))
                    ) {
                        Text(
                            text = "LIKE",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                // NOPE STAMP (Red)
                if (nopeAlpha > 0.05f) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                            .rotate(15f)
                            .graphicsLayer { alpha = nopeAlpha },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF2D55).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFFF2D55))
                    ) {
                        Text(
                            text = "NOPE",
                            color = Color(0xFFFF2D55),
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                // SUPER LIKE STAMP (Blue)
                if (superLikeAlpha > 0.05f) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 120.dp)
                            .graphicsLayer { alpha = superLikeAlpha },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF00D2FF).copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF00D2FF))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF00D2FF), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SUPER LIKE",
                                color = Color(0xFF00D2FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
