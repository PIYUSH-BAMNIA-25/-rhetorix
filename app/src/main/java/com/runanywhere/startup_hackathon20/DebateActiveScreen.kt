package com.runanywhere.startup_hackathon20

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

// ========== NEW: DUAL SCORE HEADER ==========
@Composable
fun DualScoreHeader(
    playerScore: Int,
    aiScore: Int,
    playerStreak: Int = 0,
    aiStreak: Int = 0,
    topic: String,
    timeRemaining: Long,
    playerName: String = "You",
    gameMode: GameMode = GameMode.AI_INTERMEDIATE
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

    val aiColor = when (gameMode) {
        GameMode.AI_BEGINNER -> BeginnerAI
        GameMode.AI_INTERMEDIATE -> IntermediateAI
        GameMode.AI_ADVANCED -> AdvancedAI
        else -> IntermediateAI
    }

    // Animate progress
    val animatedPlayerProgress by animateFloatAsState(
        targetValue = (playerScore / 100f).coerceIn(0f, 1f),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "playerProgress"
    )

    val animatedAiProgress by animateFloatAsState(
        targetValue = (aiScore / 100f).coerceIn(0f, 1f),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "aiProgress"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkCard.copy(alpha = 0.95f),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            // Top Row: Player Name <-> Timer <-> AI Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Player
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Player",
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = playerName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Center: Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Timer",
                        tint = timeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = timeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = timeColor
                    )
                }

                // Right: AI
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "AI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = aiColor
                    )
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "AI",
                        tint = aiColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Middle Row: Dual Health Bars + Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player HP Bar
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = playerScore.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanPrimary,
                        modifier = Modifier.width(36.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedPlayerProgress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(CyanPrimary, CyanLight)
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // AI HP Bar (mirrored)
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedAiProgress)
                                .fillMaxHeight()
                                .align(Alignment.CenterEnd)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF8888), aiColor)
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = aiScore.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = aiColor,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Streaks <-> Topic <-> Streaks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player Streak
                if (playerStreak >= 2) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(OrangeAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "🔥", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$playerStreak",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Topic (truncated)
                Text(
                    text = topic,
                    fontSize = 11.sp,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                    textAlign = TextAlign.Center
                )

                // AI Streak
                if (aiStreak >= 2) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(OrangeAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$aiStreak",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "🔥", fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }
        }
    }
}

// ========== NEW: CLEAN MESSAGE BUBBLE ==========
@Composable
fun CleanMessageBubble(
    message: DebateMessage,
    isPlayer: Boolean,
    score: Int? = null,
    showScore: Boolean = false,
    gameMode: GameMode,
    showTypingEffect: Boolean = false,
    typingText: String = ""
) {
    val bubbleColor = if (isPlayer) CyanPrimary else when (gameMode) {
        GameMode.AI_BEGINNER -> BeginnerAI
        GameMode.AI_INTERMEDIATE -> IntermediateAI
        GameMode.AI_ADVANCED -> AdvancedAI
        else -> IntermediateAI
    }

    // Animated entry
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(message.id) {
        delay(50)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(400))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = if (isPlayer) Arrangement.Start else Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isPlayer) 4.dp else 16.dp,
                    topEnd = if (isPlayer) 16.dp else 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = bubbleColor.copy(alpha = 0.12f),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Message text
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

                    // Score stars (if available)
                    if (showScore && score != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat((score / 2).coerceIn(0, 5)) {
                                Text(text = "⭐", fontSize = 10.sp)
                            }
                            repeat((5 - (score / 2)).coerceIn(0, 5)) {
                                Text(text = "☆", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========== NEW: FLOATING TURN BADGE ==========
@Composable
fun FloatingTurnBadge(
    isPlayerTurn: Boolean,
    visible: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "turnGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Surface(
            modifier = Modifier.padding(top = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = if (isPlayerTurn) CyanPrimary else RedLoss,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isPlayerTurn) Icons.Default.Person else Icons.Default.Settings,
                    contentDescription = null,
                    tint = DarkBackground,
                    modifier = Modifier
                        .size(20.dp)
                        .scale(glowAlpha)
                )
                Text(
                    text = if (isPlayerTurn) "YOUR TURN!" else "AI'S TURN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBackground,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ========== NEW: MINIMALIST INPUT BAR ==========
@Composable
fun MinimalistInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkCard.copy(alpha = 0.98f),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Input field
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = DarkSurface
            ) {
                Box {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        enabled = enabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        textStyle = TextStyle(
                            color = TextWhite,
                            fontSize = 14.sp
                        ),
                        maxLines = 3,
                        decorationBox = { innerTextField ->
                            if (text.isEmpty()) {
                                Text(
                                    text = if (enabled) "Type your argument..." else "Wait for your turn...",
                                    color = TextGray,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Character count overlay
                    if (text.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 12.dp, bottom = 8.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Text(
                                text = "${text.length}/300",
                                fontSize = 10.sp,
                                color = if (text.length > 250) RedLoss else TextGray
                            )
                        }
                    }
                }
            }

            // Send button
            FloatingActionButton(
                onClick = onSend,
                modifier = Modifier.size(48.dp),
                containerColor = if (enabled && text.isNotBlank() && text.length <= 300) CyanPrimary else TextGray.copy(
                    alpha = 0.3f
                ),
                contentColor = DarkBackground,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ========== NEW: TYPING INDICATOR ==========
@Composable
fun TypingIndicator(
    aiColor: Color
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .widthIn(max = 100.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = aiColor.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    BouncingDot(delay = index * 150, color = aiColor)
                }
            }
        }
    }
}

@Composable
fun BouncingDot(delay: Int, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot$delay")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = delay),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .offset(y = offsetY.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// ========== REDESIGNED MAIN SCREEN WITH P2P SUPPORT ==========
@Composable
fun DebateActiveScreen(
    gameMode: GameMode = GameMode.AI_INTERMEDIATE,
    sessionId: String? = null,
    currentUserId: String? = null,
    currentUserName: String? = null,
    aiViewModel: DebateViewModel = viewModel(),
    p2pViewModel: P2PDebateViewModel = viewModel()
) {
    val currentSession by aiViewModel.currentSession.collectAsState()
    val isLoading by aiViewModel.isLoading.collectAsState()
    val statusMessage by aiViewModel.statusMessage.collectAsState()
    val showScorePopup by aiViewModel.showScorePopup.collectAsState()
    val currentTurnScore by aiViewModel.currentTurnScore.collectAsState()
    val aiTypingText by aiViewModel.aiTypingText.collectAsState()
    val isAITyping by aiViewModel.isAITyping.collectAsState()
    val accumulatedScores by aiViewModel.accumulatedScores.collectAsState()
    
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
            .windowInsetsPadding(WindowInsets.systemBars)
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
                            if (showScorePopup || currentSession?.status == DebateStatus.JUDGING)
                                Modifier.blur(4.dp)
                            else
                                Modifier
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

                    // Show typing indicator when AI is typing
                    if (isAITyping) {
                        item {
                            TypingIndicator(
                                aiColor = when (currentSession?.gameMode) {
                                    GameMode.AI_BEGINNER -> BeginnerAI
                                    GameMode.AI_INTERMEDIATE -> IntermediateAI
                                    GameMode.AI_ADVANCED -> AdvancedAI
                                    else -> IntermediateAI
                                }
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

            // Input Section (Bottom) - Hide when judging
            if (currentSession?.status != DebateStatus.JUDGING) {
                currentSession?.let { session ->
                    val playerScore = accumulatedScores?.playerTotalScore ?: 0
                    val aiScore = accumulatedScores?.aiTotalScore ?: 0

                    DebateInputSection(
                        inputText = inputText,
                        onInputChange = { inputText = it },
                        onSendMessage = {
                            if (inputText.isNotBlank() && inputText.length <= 300) {
                                aiViewModel.sendDebateMessage(inputText)
                                inputText = ""
                            }
                        },
                        isEnabled = session.currentTurn == session.player1.id && !isAITyping && session.status == DebateStatus.IN_PROGRESS,
                        statusMessage = statusMessage,
                        playerName = session.player1.name,
                        playerScore = playerScore,
                        aiScore = aiScore
                    )
                }
            }
        }

        // GAME OVER OVERLAY - Show when status is JUDGING
        if (currentSession?.status == DebateStatus.JUDGING) {
            GameOverOverlay()
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
fun GameOverOverlay() {
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
                        style = androidx.compose.ui.text.TextStyle(
                            letterSpacing = 4.sp
                        )
                    )
                    // Main text
                    Text(
                        text = "GAME OVER",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        style = androidx.compose.ui.text.TextStyle(
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
