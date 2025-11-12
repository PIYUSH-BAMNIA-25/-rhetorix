package com.runanywhere.startup_hackathon20

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

// Color Palette (same as AI mode)
private val CyanPrimary = Color(0xFF00D9FF)
private val CyanLight = Color(0xFF5FEDFF)
private val PurpleAccent = Color(0xFF9D4EDD)
private val DarkBackground = Color(0xFF0A0A0F)
private val DarkSurface = Color(0xFF1A1A2E)
private val DarkCard = Color(0xFF16213E)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFFB0B0B0)
private val GreenWin = Color(0xFF4ADE80)
private val OrangeAccent = Color(0xFFFB923C)
private val RedLoss = Color(0xFFFF6B6B)
private val YellowWarning = Color(0xFFFBBF24)
private val GoldStar = Color(0xFFFFD700)

/**
 * P2P Debate Active Screen
 * Turn-based chat with real opponent
 */
@Composable
fun P2PDebateActiveScreen(
    sessionId: String,
    currentUserId: String,
    currentUserName: String,
    onDebateFinished: () -> Unit,
    p2pViewModel: P2PDebateViewModel = viewModel()
) {
    // Initialize session if not already done
    LaunchedEffect(sessionId) {
        if (p2pViewModel.sessionId.value != sessionId) {
            p2pViewModel.initializeSession(sessionId, currentUserId, currentUserName)
        }
    }

    // Observe state
    val sessionData by p2pViewModel.sessionData.collectAsState()
    val messages by p2pViewModel.messages.collectAsState()
    val isMyTurn by p2pViewModel.isMyTurn.collectAsState()
    val statusMessage by p2pViewModel.statusMessage.collectAsState()
    val showScorePopup by p2pViewModel.showScorePopup.collectAsState()
    val currentTurnScore by p2pViewModel.currentTurnScore.collectAsState()
    val accumulatedScores by p2pViewModel.accumulatedScores.collectAsState()
    val debateStatus by p2pViewModel.debateStatus.collectAsState()
    val opponentName by p2pViewModel.opponentName.collectAsState()
    val clientTimeRemaining by p2pViewModel.clientTimeRemaining.collectAsState() // NEW: Use client timer

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Navigate to results when finished
    LaunchedEffect(debateStatus) {
        if (debateStatus == "FINISHED") {
            onDebateFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface,
                        DarkBackground
                    )
                )
            )
    ) {
        // Main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Topic and Timer
            sessionData?.let { session ->
                P2PDebateHeader(
                    topic = session.topic_title,
                    timeRemaining = clientTimeRemaining, // Use clientTimeRemaining instead of session.debate_time_remaining
                    mySide = if (session.player1_id == currentUserId) session.player1_side else session.player2_side
                )
            }

            // Messages (Middle)
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .then(
                            if (showScorePopup || debateStatus == "JUDGING")
                                Modifier.blur(4.dp)
                            else
                                Modifier
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(messages) { message ->
                        P2PMessageBubble(
                            message = message,
                            isMe = message.playerId == currentUserId,
                            myName = currentUserName,
                            opponentName = opponentName ?: "Opponent"
                        )
                    }
                }

                // Score Pop-up Overlay
                if (showScorePopup && currentTurnScore != null) {
                    P2PScorePopup(
                        score = currentTurnScore!!,
                        onDismiss = { /* Auto-dismiss */ }
                    )
                }
            }

            // Input Section (Bottom) - Hide when judging
            if (debateStatus != "JUDGING" && debateStatus != "FINISHED") {
                sessionData?.let { session ->
                    val myScore = accumulatedScores?.playerTotalScore ?: 0
                    val opponentScore = accumulatedScores?.aiTotalScore ?: 0

                    P2PInputSection(
                        inputText = inputText,
                        onInputChange = { inputText = it },
                        onSendMessage = {
                            if (inputText.isNotBlank() && inputText.length <= 300) {
                                p2pViewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        isEnabled = isMyTurn && debateStatus == "IN_PROGRESS",
                        statusMessage = statusMessage,
                        myName = currentUserName,
                        opponentName = opponentName ?: "Opponent",
                        myScore = myScore,
                        opponentScore = opponentScore
                    )
                }
            }
        }

        // GAME OVER OVERLAY - Show when judging
        if (debateStatus == "JUDGING") {
            P2PGameOverOverlay()
        }
    }
}

@Composable
fun P2PDebateHeader(
    topic: String,
    timeRemaining: Long,
    mySide: String
) {
    val timeProgress = (timeRemaining / 900000f).coerceIn(0f, 1f)
    val timeSeconds = (timeRemaining / 1000).toInt()
    val minutes = timeSeconds / 60
    val seconds = timeSeconds % 60
    val timeText = String.format("%d:%02d", minutes, seconds)

    val timeColor = when {
        timeProgress > 0.5f -> GreenWin
        timeProgress > 0.25f -> YellowWarning
        else -> RedLoss
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Topic
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Topic",
                    tint = CyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = topic,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Your Side
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Your side",
                    tint = PurpleAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Your Side: ",
                    fontSize = 13.sp,
                    color = TextGray
                )
                Text(
                    text = mySide,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleAccent
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Time",
                        tint = timeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Time Remaining",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
                Text(
                    text = timeText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = timeColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TextGray.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(timeProgress)
                        .fillMaxHeight()
                        .background(timeColor)
                )
            }
        }
    }
}

@Composable
fun P2PMessageBubble(
    message: DebateMessage,
    isMe: Boolean,
    myName: String,
    opponentName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            // Opponent Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PurpleAccent.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Opponent",
                    tint = PurpleAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Message Bubble
        Card(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = if (isMe) 16.dp else 4.dp,
                topEnd = if (isMe) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) {
                    CyanPrimary.copy(alpha = 0.2f)
                } else {
                    PurpleAccent.copy(alpha = 0.15f)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Sender name
                Text(
                    text = if (isMe) myName else opponentName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMe) CyanPrimary else PurpleAccent
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Message text
                Text(
                    text = message.message,
                    fontSize = 14.sp,
                    color = TextWhite,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Turn number
                Text(
                    text = "Turn ${message.turnNumber}",
                    fontSize = 10.sp,
                    color = TextGray
                )
            }
        }

        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            // My Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                CyanPrimary.copy(alpha = 0.5f),
                                PurpleAccent.copy(alpha = 0.5f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Me",
                    tint = CyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun P2PScorePopup(
    score: TurnScore,
    onDismiss: () -> Unit
) {
    // Auto-dismiss after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }

    val scoreColor = when {
        score.score >= 8 -> GreenWin
        score.score >= 6 -> YellowWarning
        else -> RedLoss
    }

    val emoji = when {
        score.score >= 9 -> "🌟"
        score.score >= 8 -> "⭐"
        score.score >= 6 -> "💡"
        score.score >= 4 -> "⚠️"
        else -> "❌"
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (score.score >= 8) {
            P2PConfettiAnimation()
        }

        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkCard
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR SCORE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 40.sp
                        )
                        Text(
                            text = "${score.score}/10",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurface.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = score.reasoning,
                            fontSize = 13.sp,
                            color = TextWhite,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun P2PConfettiAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")

    val confettiColors = listOf(GreenWin, GoldStar, CyanPrimary, PurpleAccent, OrangeAccent)

    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(50) { index ->
            val progress = (System.currentTimeMillis() % 3000) / 3000f
            val x = (size.width * (index % 10) / 10f)
            val y = size.height * progress
            val color = confettiColors[index % confettiColors.size]

            drawCircle(
                color = color.copy(alpha = (1f - progress)),
                radius = 8f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun P2PInputSection(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isEnabled: Boolean,
    statusMessage: String,
    myName: String,
    opponentName: String,
    myScore: Int,
    opponentScore: Int
) {
    val haptics = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Score Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // My Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Me",
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = myName,
                            fontSize = 13.sp,
                            color = TextGray,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = myScore.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                }

                // VS
                Text(
                    text = "VS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )

                // Opponent Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Opponent",
                            tint = PurpleAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = opponentName,
                            fontSize = 13.sp,
                            color = TextGray,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = opponentScore.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = TextGray.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Status Message
            if (statusMessage.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (!isEnabled) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CyanPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = statusMessage,
                        fontSize = 14.sp,
                        color = if (isEnabled) GreenWin else PurpleAccent,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Input Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = if (isEnabled) "Type your argument (max 300 chars)..." else "Wait for your turn...",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    },
                    enabled = isEnabled,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        disabledTextColor = TextGray,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                        disabledBorderColor = TextGray.copy(alpha = 0.2f),
                        focusedContainerColor = DarkSurface.copy(alpha = 0.5f),
                        unfocusedContainerColor = DarkSurface.copy(alpha = 0.3f),
                        disabledContainerColor = DarkSurface.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    supportingText = {
                        Text(
                            text = "${inputText.length}/300",
                            fontSize = 11.sp,
                            color = if (inputText.length > 250) RedLoss else TextGray,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )

                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSendMessage()
                    },
                    enabled = isEnabled && inputText.isNotBlank() && inputText.length <= 300,
                    modifier = Modifier.size(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = DarkBackground,
                        disabledContainerColor = TextGray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun P2PGameOverOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "gameover")

    // Pulsing glow effect
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Rotation for loading circle
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        // Animated background particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val particleCount = 20
            for (i in 0 until particleCount) {
                val angle = (i * 360f / particleCount) + rotation
                val radius = 150f + (i % 3) * 30f
                val x = size.width / 2 + kotlin.math.cos(Math.toRadians(angle.toDouble()))
                    .toFloat() * radius
                val y = size.height / 2 + kotlin.math.sin(Math.toRadians(angle.toDouble()))
                    .toFloat() * radius

                drawCircle(
                    color = CyanPrimary.copy(alpha = 0.2f * glowAlpha),
                    radius = 8f + (i % 2) * 4f,
                    center = Offset(x, y)
                )
            }
        }

        // Glass card with content
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(32.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkCard.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Rotating loading circle
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow ring
                    Canvas(
                        modifier = Modifier
                            .size(140.dp)
                            .rotate(rotation)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    CyanPrimary.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            radius = size.width / 2
                        )
                    }

                    // Progress circle
                    CircularProgressIndicator(
                        modifier = Modifier.size(100.dp),
                        color = CyanPrimary,
                        strokeWidth = 6.dp,
                        trackColor = CyanPrimary.copy(alpha = 0.2f)
                    )

                    // Center icon
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Judging",
                        tint = CyanPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // "GAME OVER" text with glow
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Glow behind text
                    Text(
                        text = "GAME OVER",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanPrimary.copy(alpha = glowAlpha),
                        style = TextStyle(
                            letterSpacing = 4.sp
                        )
                    )
                    // Main text
                    Text(
                        text = "GAME OVER",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        style = TextStyle(
                            letterSpacing = 4.sp
                        )
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CyanPrimary.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Status text
                Text(
                    text = "⚖️ Final Judging in Progress",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Analyzing debate performance...",
                    fontSize = 16.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )

                // Animated dots
                val dotCount by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing)
                    ),
                    label = "dots"
                )

                Text(
                    text = "Please wait" + ".".repeat(dotCount.toInt() + 1),
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}