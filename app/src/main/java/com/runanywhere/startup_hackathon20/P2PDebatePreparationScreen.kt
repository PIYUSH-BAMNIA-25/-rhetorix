package com.runanywhere.startup_hackathon20

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.cos

// Color Palette (same as AI mode)
private val CyanPrimary = Color(0xFF00D9FF)
private val PurpleAccent = Color(0xFF9D4EDD)
private val DarkBackground = Color(0xFF0A0A0F)
private val DarkCard = Color(0xFF16213E)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFFB0B0B0)
private val GreenWin = Color(0xFF4ADE80)
private val OrangeAccent = Color(0xFFFB923C)
private val RedLoss = Color(0xFFFF6B6B)
private val GoldCoin = Color(0xFFFFD700)

enum class P2PPrepStage {
    VS_ANIMATION,      // Show both player names
    TOPIC_REVEAL,      // Show topic
    SIDE_ASSIGNMENT,   // Show sides
    SERVER_DECISION,   // Server decides who starts
    COUNTDOWN,         // 3-2-1 countdown
    READY              // Transition to debate
}

/**
 * P2P Debate Preparation Screen
 * Similar to AI mode but uses real player names and server-decided turn order
 */
@Composable
fun P2PDebatePreparationScreen(
    sessionId: String,
    currentUserId: String,
    currentUserName: String,
    onPreparationComplete: () -> Unit,
    p2pViewModel: P2PDebateViewModel = viewModel()
) {
    var currentStage by remember { mutableStateOf(P2PPrepStage.VS_ANIMATION) }

    // Initialize session
    LaunchedEffect(sessionId) {
        p2pViewModel.initializeSession(sessionId, currentUserId, currentUserName)
    }

    // Get session data
    val sessionData by p2pViewModel.sessionData.collectAsState()
    val opponentName by p2pViewModel.opponentName.collectAsState()

    // Auto-progress through stages
    LaunchedEffect(currentStage, sessionData) {
        when (currentStage) {
            P2PPrepStage.VS_ANIMATION -> {
                delay(3000)
                currentStage = P2PPrepStage.TOPIC_REVEAL
            }

            P2PPrepStage.TOPIC_REVEAL -> {
                delay(4000)
                currentStage = P2PPrepStage.SIDE_ASSIGNMENT
            }

            P2PPrepStage.SIDE_ASSIGNMENT -> {
                delay(2500)
                currentStage = P2PPrepStage.SERVER_DECISION
            }

            P2PPrepStage.SERVER_DECISION -> {
                delay(3000)
                currentStage = P2PPrepStage.COUNTDOWN
            }

            P2PPrepStage.COUNTDOWN -> {
                delay(3000)
                currentStage = P2PPrepStage.READY
            }

            P2PPrepStage.READY -> {
                onPreparationComplete()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        Color(0xFF1A1A2E),
                        DarkBackground
                    )
                )
            )
    ) {
        // Animated background particles
        P2PAnimatedBackground()

        // Main content based on stage
        sessionData?.let { session ->
            when (currentStage) {
                P2PPrepStage.VS_ANIMATION -> {
                    P2PVSAnimation(currentUserName, opponentName ?: "Opponent")
                }

                P2PPrepStage.TOPIC_REVEAL -> {
                    P2PTopicReveal(session.topic_title, session.topic_description ?: "")
                }

                P2PPrepStage.SIDE_ASSIGNMENT -> {
                    val playerSide =
                        if (session.player1_id == currentUserId) session.player1_side else session.player2_side
                    val opponentSide =
                        if (session.player1_id == currentUserId) session.player2_side else session.player1_side
                    P2PSideAssignment(
                        currentUserName,
                        playerSide,
                        opponentName ?: "Opponent",
                        opponentSide
                    )
                }

                P2PPrepStage.SERVER_DECISION -> {
                    val playerStarts = session.current_turn == currentUserId
                    P2PServerDecision(currentUserName, opponentName ?: "Opponent", playerStarts)
                }

                P2PPrepStage.COUNTDOWN -> {
                    val playerStarts = session.current_turn == currentUserId
                    P2PCountdown(playerStarts, currentUserName, opponentName ?: "Opponent")
                }

                P2PPrepStage.READY -> {
                    // Transition
                }
            }
        }
    }
}

@Composable
fun P2PVSAnimation(player1Name: String, player2Name: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "vs")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Player 1
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -it }
        ) {
            P2PPlayerCard(player1Name, CyanPrimary, glowAlpha, Icons.Default.Person)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // VS
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PurpleAccent.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    ),
                    radius = size.width / 2
                )
            }
            Text(
                text = "VS",
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Player 2
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(1000, delayMillis = 300)) + slideInVertically(
                tween(
                    1000,
                    delayMillis = 300
                )
            ) { it }
        ) {
            P2PPlayerCard(player2Name, PurpleAccent, glowAlpha, Icons.Default.Face)
        }
    }
}

@Composable
fun P2PPlayerCard(
    name: String,
    color: Color,
    glowAlpha: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.8f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = color.copy(alpha = glowAlpha)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = color,
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                text = name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
    }
}

@Composable
fun P2PTopicReveal(topic: String, description: String) {
    var revealed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        revealed = true
    }

    val scale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn() + expandVertically()
        ) {
            Text(
                text = "📜 TOPIC REVEALED!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OrangeAccent,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        Card(
            modifier = Modifier
                .padding(24.dp)
                .scale(scale),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkCard
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = CyanPrimary
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = topic,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun P2PSideAssignment(
    player1Name: String,
    player1Side: String,
    player2Name: String,
    player2Side: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚔️ SIDES ASSIGNED!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PurpleAccent,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        P2PSideCard(player1Name, player1Side, CyanPrimary, Icons.Default.Person, true)
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "VS", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextGray)
        Spacer(modifier = Modifier.height(32.dp))
        P2PSideCard(player2Name, player2Side, PurpleAccent, Icons.Default.Face, false)
    }
}

@Composable
fun P2PSideCard(
    name: String,
    side: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPlayer: Boolean
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(600)) + slideInHorizontally(
            tween(600),
            initialOffsetX = { if (isPlayer) -it else it }
        )
    ) {
        Card(
            modifier = Modifier
                .width(300.dp)
                .height(120.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkCard
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 3.dp,
                color = color
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column {
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Arguing: $side",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun P2PServerDecision(player1Name: String, player2Name: String, playerStarts: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "server")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .rotate(rotation),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldCoin.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
            }
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(GoldCoin.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Server decision",
                    tint = DarkBackground,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "🎲 Server Deciding...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = GoldCoin
        )
    }
}

@Composable
fun P2PCountdown(playerStarts: Boolean, playerName: String, opponentName: String) {
    var countdownNumber by remember { mutableStateOf(3) }

    LaunchedEffect(Unit) {
        for (i in 3 downTo 1) {
            countdownNumber = i
            delay(1000)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (countdownNumber > 0) 1.5f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "countdown"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (playerStarts) CyanPrimary.copy(alpha = 0.2f)
                else PurpleAccent.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (playerStarts) "🎉 You Start First!" else "⏳ Opponent Starts First!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (playerStarts) playerName else opponentName,
                    fontSize = 18.sp,
                    color = if (playerStarts) CyanPrimary else PurpleAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        if (countdownNumber > 0) {
            Text(
                text = countdownNumber.toString(),
                fontSize = 120.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyanPrimary,
                modifier = Modifier.scale(scale)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Get Ready!",
                fontSize = 20.sp,
                color = TextGray
            )
        } else {
            Text(
                text = "BEGIN!",
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GreenWin,
                modifier = Modifier.scale(scale)
            )
        }
    }
}

@Composable
fun P2PAnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val animatedOffsets = remember {
        List(6) {
            Pair(
                (0..360).random().toFloat(),
                (80..200).random().toFloat()
            )
        }
    }

    val animatedValues = List(6) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 25000 + index * 3000,
                    easing = LinearEasing
                )
            ),
            label = "particle$index"
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        animatedOffsets.forEachIndexed { index, (angle, radius) ->
            val time = animatedValues[index].value + angle
            val x = size.width / 2 + cos(Math.toRadians(time.toDouble())).toFloat() * radius
            val y = size.height / 2 + sin(Math.toRadians(time.toDouble())).toFloat() * radius

            drawCircle(
                color = if (index % 2 == 0) PurpleAccent.copy(alpha = 0.2f)
                else CyanPrimary.copy(alpha = 0.15f),
                radius = (10 + index * 3).toFloat(),
                center = Offset(x, y)
            )
        }
    }
}