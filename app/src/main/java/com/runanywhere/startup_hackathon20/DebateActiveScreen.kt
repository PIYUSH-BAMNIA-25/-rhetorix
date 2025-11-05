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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.max

// Color Palette - Same as main theme
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
            // AI Player Section (Top)
            currentSession?.let { session ->
                AIPlayerSection(
                    name = session.player2?.name ?: "AI Debater",
                    score = session.scores?.player2Score?.totalScore ?: 0,
                    gameMode = session.gameMode,
                    timeRemaining = session.timeRemaining,
                    isThinking = session.currentTurn == "ai_opponent" && isLoading
                )
            }

            // Debate Messages (Middle)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                currentSession?.messages?.let { messages ->
                    items(messages) { message ->
                        DebateMessageBubble(
                            message = message,
                            isPlayer = message.playerId != "ai_opponent",
                            gameMode = currentSession?.gameMode ?: GameMode.AI_INTERMEDIATE
                        )
                    }
                }
            }

            // Player Section (Bottom)
            currentSession?.let { session ->
                PlayerSection(
                    name = session.player1.name,
                    score = session.scores?.player1Score?.totalScore ?: 0,
                    timeRemaining = session.timeRemaining,
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSendMessage = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendDebateMessage(inputText)
                            inputText = ""
                        }
                    },
                    isEnabled = session.currentTurn == session.player1.id && !isLoading,
                    statusMessage = statusMessage
                )
            }
        }
    }
}

@Composable
fun AIPlayerSection(
    name: String,
    score: Int,
    gameMode: GameMode,
    timeRemaining: Long,
    isThinking: Boolean
) {
    val aiColor = when (gameMode) {
        GameMode.AI_BEGINNER -> BeginnerAI
        GameMode.AI_INTERMEDIATE -> IntermediateAI
        GameMode.AI_ADVANCED -> AdvancedAI
        else -> IntermediateAI
    }

    val timeProgress = (timeRemaining / 600000f).coerceIn(0f, 1f)
    val timeSeconds = (timeRemaining / 1000).toInt()
    val minutes = timeSeconds / 60
    val seconds = timeSeconds % 60
    val timeText = String.format("%d:%02d", minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AI Square Profile Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    aiColor,
                                    aiColor.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "AI",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Name and Score
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Score",
                            tint = OrangeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Score: $score",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                }

                // AI Thinking Indicator
                if (isThinking) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(aiColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = aiColor,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Progress Bar
            Column {
                // Visual progress bar
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
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        aiColor,
                                        aiColor.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Time text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Time",
                            tint = aiColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Time Remaining",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                    Text(
                        text = timeText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (timeProgress > 0.3f) aiColor else RedLoss
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerSection(
    name: String,
    score: Int,
    timeRemaining: Long,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isEnabled: Boolean,
    statusMessage: String
) {
    val timeProgress = (timeRemaining / 600000f).coerceIn(0f, 1f)
    val timeSeconds = (timeRemaining / 1000).toInt()
    val minutes = timeSeconds / 60
    val seconds = timeSeconds % 60
    val timeText = String.format("%d:%02d", minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Player Square Profile Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    CyanPrimary,
                                    PurpleAccent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Player",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Name and Score
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Score",
                            tint = OrangeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Score: $score",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                }

                // Turn Indicator
                if (isEnabled) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GreenWin.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Your turn",
                            tint = GreenWin,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Progress Bar
            Column {
                // Visual progress bar
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
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        CyanPrimary,
                                        PurpleAccent
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Time text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Time",
                            tint = CyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Time Remaining",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                    Text(
                        text = timeText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (timeProgress > 0.3f) CyanPrimary else RedLoss
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Message
            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    fontSize = 12.sp,
                    color = if (isEnabled) GreenWin else TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Input Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = if (isEnabled) "Type your argument..." else "Wait for your turn...",
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
                    maxLines = 3
                )

                // Send Button
                Button(
                    onClick = onSendMessage,
                    enabled = isEnabled && inputText.isNotBlank(),
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
fun DebateMessageBubble(
    message: DebateMessage,
    isPlayer: Boolean,
    gameMode: GameMode
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
            modifier = Modifier.widthIn(max = 280.dp),
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
                    color = TextGray,
                    textAlign = if (isPlayer) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
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

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DebateActiveScreenPreview() {
    // Mock data for preview
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AIPlayerSection(
                name = "AI Thinker",
                score = 35,
                gameMode = GameMode.AI_INTERMEDIATE,
                timeRemaining = 480000,
                isThinking = true
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            PlayerSection(
                name = "Player One",
                score = 40,
                timeRemaining = 480000,
                inputText = "",
                onInputChange = {},
                onSendMessage = {},
                isEnabled = true,
                statusMessage = "Your turn! Make your argument."
            )
        }
    }
}
