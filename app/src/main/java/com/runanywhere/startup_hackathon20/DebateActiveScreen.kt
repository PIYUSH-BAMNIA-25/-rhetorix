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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

// Color Palette
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

// AI Colors based on difficulty
private val BeginnerAI = Color(0xFF4ADE80)  // Green - Easy
private val IntermediateAI = Color(0xFF00D9FF)  // Cyan - Medium
private val AdvancedAI = Color(0xFFFF6B6B)  // Red - Hard

@Composable
fun DebateActiveScreen(
    viewModel: DebateViewModel = viewModel()
) {
    val currentSession by viewModel.currentSession.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val showScorePopup by viewModel.showScorePopup.collectAsState()
    val currentTurnScore by viewModel.currentTurnScore.collectAsState()
    val aiTypingText by viewModel.aiTypingText.collectAsState()
    val isAITyping by viewModel.isAITyping.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(currentSession?.messages?.size) {
        if (currentSession?.messages?.isNotEmpty() == true) {
            listState.animateScrollToItem(currentSession!!.messages.size - 1)
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Topic and Timer
            currentSession?.let { session ->
                DebateHeader(
                    topic = session.topic.title,
                    timeRemaining = session.timeRemaining,
                    playerSide = session.player1Side.name
                )
            }

            // Debate Messages (Middle)
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .then(
                            if (showScorePopup) Modifier.blur(4.dp) else Modifier
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    currentSession?.messages?.let { messages ->
                        items(messages) { message ->
                            DebateMessageBubble(
                                message = message,
                                isPlayer = message.playerId != "ai_opponent",
                                gameMode = currentSession?.gameMode ?: GameMode.AI_INTERMEDIATE,
                                showTypingEffect = isAITyping && message.playerId == "ai_opponent" && message == messages.lastOrNull(),
                                typingText = aiTypingText
                            )
                        }
                    }
                }
                
                // Score Pop-up Overlay
                if (showScorePopup && currentTurnScore != null) {
                    ScorePopupAnimation(
                        score = currentTurnScore!!,
                        onDismiss = { /* Will auto-dismiss */ }
                    )
                }
            }

            // Input Section (Bottom)
            currentSession?.let { session ->
                val playerScore = session.scores?.player1Score?.totalScore ?: 0
                val aiScore = session.scores?.player2Score?.totalScore ?: 0

                DebateInputSection(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSendMessage = {
                        if (inputText.isNotBlank() && inputText.length <= 300) {
                            viewModel.sendDebateMessage(inputText)
                            inputText = ""
                        }
                    },
                    isEnabled = session.currentTurn == session.player1.id && !isLoading,
                    statusMessage = statusMessage,
                    playerName = session.player1.name,
                    playerScore = playerScore,
                    aiScore = aiScore
                )
            }
        }
    }
}

@Composable
fun DebateHeader(
    topic: String,
    timeRemaining: Long,
    playerSide: String
) {
    val timeProgress = (timeRemaining / 600000f).coerceIn(0f, 1f)
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    lineHeight = 18.sp,
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
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Your Side: ",
                    fontSize = 12.sp,
                    color = TextGray
                )
                Text(
                    text = playerSide,
                    fontSize = 12.sp,
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
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Time Remaining",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
                Text(
                    text = timeText,
                    fontSize = 16.sp,
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
fun DebateMessageBubble(
    message: DebateMessage,
    isPlayer: Boolean,
    gameMode: GameMode,
    showTypingEffect: Boolean,
    typingText: String
) {
    val aiColor = when (gameMode) {
        GameMode.AI_BEGINNER -> BeginnerAI
        GameMode.AI_INTERMEDIATE -> IntermediateAI
        GameMode.AI_ADVANCED -> AdvancedAI
        else -> IntermediateAI
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isPlayer) Arrangement.End else Arrangement.Start
    ) {
        if (!isPlayer) {
            // AI Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(aiColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "AI",
                    tint = aiColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Message Bubble
        Card(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = if (isPlayer) 16.dp else 4.dp,
                topEnd = if (isPlayer) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isPlayer) {
                    CyanPrimary.copy(alpha = 0.2f)
                } else {
                    aiColor.copy(alpha = 0.15f)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Sender name
                Text(
                    text = message.playerName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlayer) CyanPrimary else aiColor
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Message text (with typing effect for AI)
                if (showTypingEffect && typingText.isNotEmpty()) {
                    Text(
                        text = typingText,
                        fontSize = 14.sp,
                        color = TextWhite,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(
                        text = message.message,
                        fontSize = 14.sp,
                        color = TextWhite,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Turn number and timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Turn ${message.turnNumber}",
                        fontSize = 10.sp,
                        color = TextGray
                    )
                }
            }
        }

        if (isPlayer) {
            Spacer(modifier = Modifier.width(8.dp))
            // Player Icon
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
                    contentDescription = "Player",
                    tint = CyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ScorePopupAnimation(
    score: TurnScore,
    onDismiss: () -> Unit
) {
    // Auto-dismiss after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }

    val isPlayerScore = score.speaker.contains("Player", ignoreCase = true)
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

    Dialog(onDismissRequest = onDismiss) {
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
                // Header
                Text(
                    text = if (isPlayerScore) "YOUR SCORE" else "AI SCORE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlayerScore) CyanPrimary else IntermediateAI,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Score with emoji
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
                
                // Reasoning
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
                
                // Additional feedback
                if (score.hasProfanity) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = RedLoss,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Inappropriate language detected",
                            fontSize = 11.sp,
                            color = RedLoss
                        )
                    }
                }
                
                if (score.factCheck.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Fact check",
                            tint = CyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = score.factCheck,
                            fontSize = 11.sp,
                            color = TextGray,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebateInputSection(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isEnabled: Boolean,
    statusMessage: String,
    playerName: String,
    playerScore: Int,
    aiScore: Int
) {
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
                // Player Total Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Player",
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = playerName,
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = playerScore.toString(),
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
                
                // AI Total Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "AI",
                            tint = IntermediateAI,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "AI Debater",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = aiScore.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntermediateAI
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
                        fontSize = 13.sp,
                        color = if (isEnabled) GreenWin else CyanPrimary,
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
                            fontSize = 13.sp
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

                // Send Button
                Button(
                    onClick = onSendMessage,
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

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DebateActiveScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Text("Debate Active Screen", color = Color.White)
    }
}
