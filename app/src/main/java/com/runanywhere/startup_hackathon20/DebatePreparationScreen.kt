package com.runanywhere.startup_hackathon20

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI
import kotlin.random.Random

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
private val GoldCoin = Color(0xFFFFD700)

// AI Colors
private val BeginnerAI = Color(0xFF4ADE80)
private val IntermediateAI = Color(0xFF00D9FF)
private val AdvancedAI = Color(0xFFFF6B6B)

enum class PrepStage {
    VS_ANIMATION,      // Hologram VS effect
    TOPIC_REVEAL,      // Surprise topic reveal
    SIDE_ASSIGNMENT,   // Show sides (FOR/AGAINST)
    COIN_CHOICE,       // Player chooses Heads or Tails
    COIN_TOSS,         // Coin flip animation
    COIN_RESULT,       // Show result of coin toss
    COUNTDOWN,         // 3-2-1 countdown
    READY              // Transition to debate
}

// ========== NEW: SHAKE CONTROLLER ==========
class ShakeController {
    val offset = Animatable(0f)

    suspend fun shake() {
        for (i in 0 until 8) {
            offset.animateTo(
                targetValue = if (i % 2 == 0) 10f else -10f,
                animationSpec = tween(50)
            )
        }
        offset.animateTo(0f)
    }
}

@Composable
fun rememberShakeController(): ShakeController {
    return remember { ShakeController() }
}

// ========== NEW: SECTOR UTILITIES ==========
fun getSectorIcon(description: String): String {
    return when {
        description.contains("AI", ignoreCase = true) -> "🤖"
        description.contains("Climate", ignoreCase = true) -> "🌍"
        description.contains("Health", ignoreCase = true) -> "❤️"
        description.contains("Economics", ignoreCase = true) -> "💰"
        description.contains("Geopolitics", ignoreCase = true) -> "🌐"
        description.contains("Technology", ignoreCase = true) -> "💻"
        description.contains("Education", ignoreCase = true) -> "📚"
        description.contains("Space", ignoreCase = true) -> "🚀"
        description.contains("Science", ignoreCase = true) -> "🔬"
        description.contains("Social", ignoreCase = true) -> "👥"
        else -> "📜"
    }
}

fun getSectorColor(description: String): Color {
    return when {
        description.contains("AI", ignoreCase = true) -> Color(0xFF9D4EDD)
        description.contains("Climate", ignoreCase = true) -> Color(0xFF4ADE80)
        description.contains("Health", ignoreCase = true) -> Color(0xFFFF6B6B)
        description.contains("Economics", ignoreCase = true) -> Color(0xFFFFD700)
        description.contains("Geopolitics", ignoreCase = true) -> Color(0xFF00D9FF)
        description.contains("Technology", ignoreCase = true) -> Color(0xFFFB923C)
        description.contains("Education", ignoreCase = true) -> Color(0xFF60A5FA)
        description.contains("Space", ignoreCase = true) -> Color(0xFFA78BFA)
        description.contains("Science", ignoreCase = true) -> Color(0xFF10B981)
        description.contains("Social", ignoreCase = true) -> Color(0xFFF59E0B)
        else -> CyanPrimary
    }
}

// ========== NEW: PROGRESS INDICATOR ==========
@Composable
fun PrepProgressIndicator(currentStage: PrepStage) {
    val stages = PrepStage.values().filter { it != PrepStage.READY }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { index, stage ->
            val isActive = stage.ordinal <= currentStage.ordinal
            val isCurrent = stage == currentStage

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) CyanPrimary
                        else TextGray.copy(alpha = 0.3f)
                    )
                    .animateContentSize()
            )

            if (index < stages.size - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

// ========== NEW: TYPEWRITER TEXT ==========
@Composable
fun TypewriterText(
    text: String,
    fontSize: TextUnit = 24.sp,
    color: Color = TextWhite,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Center,
    modifier: Modifier = Modifier
) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        text.forEachIndexed { index, _ ->
            delay(30) // 30ms per character
            visibleText = text.substring(0, index + 1)
        }
    }

    Text(
        text = visibleText,
        fontSize = fontSize,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = 32.sp,
        modifier = modifier
    )
}

// ========== NEW: AI SPEECH BUBBLE ==========
@Composable
fun AISpeechBubble(
    text: String,
    aiColor: Color,
    visible: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 }
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.85f),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 4.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = aiColor.copy(alpha = 0.2f)
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, aiColor)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💬", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = TextWhite,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

// ========== NEW: CAMERA ZOOM BOX ==========
@Composable
fun CameraZoomBox(
    zoomIn: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (zoomIn) 1.1f else 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "camera_zoom"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale),
        content = content
    )
}

// ========== NEW: STAGE BACKGROUND COLOR ==========
fun getStageColor(stage: PrepStage): Color {
    return when (stage) {
        PrepStage.VS_ANIMATION -> Color(0xFF1A1A2E)
        PrepStage.TOPIC_REVEAL -> Color(0xFF2D1B4E) // Purple tint
        PrepStage.SIDE_ASSIGNMENT -> Color(0xFF1E3A5F) // Blue tint
        PrepStage.COIN_CHOICE -> Color(0xFF3D2E1F) // Gold tint
        PrepStage.COIN_TOSS -> Color(0xFF3D2E1F)
        PrepStage.COIN_RESULT -> Color(0xFF1F3D1F) // Green tint
        PrepStage.COUNTDOWN -> Color(0xFF3D1F1F) // Red tint
        else -> DarkBackground
    }
}

@Composable
fun AnimatedStageBackground(currentStage: PrepStage) {
    val backgroundColor by animateColorAsState(
        targetValue = getStageColor(currentStage),
        animationSpec = tween(1000),
        label = "background_color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.7f),
                        DarkBackground
                    )
                )
            )
    )
}

@Composable
fun DebatePreparationScreen(
    playerName: String,
    aiName: String,
    topic: String,
    topicDescription: String,
    playerSide: String,
    aiSide: String,
    gameMode: GameMode,
    onPreparationComplete: (Boolean) -> Unit
) {
    var currentStage by remember { mutableStateOf(PrepStage.VS_ANIMATION) }
    var playerStarts by remember { mutableStateOf(false) }
    var playerChoice by remember { mutableStateOf("") }
    var coinResult by remember { mutableStateOf("") }

    // Auto-progress through stages
    LaunchedEffect(currentStage) {
        when (currentStage) {
            PrepStage.VS_ANIMATION -> {
                delay(3000) // 3 seconds VS animation
                currentStage = PrepStage.TOPIC_REVEAL
            }

            PrepStage.TOPIC_REVEAL -> {
                delay(4000) // 4 seconds topic reveal (increased for typewriter)
                currentStage = PrepStage.SIDE_ASSIGNMENT
            }

            PrepStage.SIDE_ASSIGNMENT -> {
                delay(2500) // 2.5 seconds side display
                currentStage = PrepStage.COIN_CHOICE
            }

            PrepStage.COIN_CHOICE -> {
                // AI Mode: wait for player input (coin choice)
                // Player will click Heads or Tails button
            }

            PrepStage.COIN_TOSS -> {
                delay(3000) // 3 seconds coin toss
                currentStage = PrepStage.COIN_RESULT
            }

            PrepStage.COIN_RESULT -> {
                // Generate random coin result: Heads or Tails
                coinResult = listOf("Heads", "Tails").random()

                // Player wins if their choice matches the coin result
                playerStarts = (playerChoice == coinResult)

                android.util.Log.d(
                    "DebatePrep",
                    "🪙 Coin toss: Player chose $playerChoice, coin landed $coinResult, player starts: $playerStarts"
                )

                delay(2500) // 2.5 seconds result display
                currentStage = PrepStage.COUNTDOWN
            }

            PrepStage.COUNTDOWN -> {
                delay(3000) // 3 seconds countdown
                currentStage = PrepStage.READY
            }

            PrepStage.READY -> {
                android.util.Log.d(
                    "DebatePrep",
                    "✅ Preparation complete, calling onPreparationComplete($playerStarts)"
                )
                onPreparationComplete(playerStarts)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Animated background with stage colors
        AnimatedStageBackground(currentStage)

        // Animated background particles
        AnimatedBackgroundEffect()

        // Progress indicator at top
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (currentStage != PrepStage.READY) {
                PrepProgressIndicator(currentStage)
            }
        }

        // Main content based on stage
        when (currentStage) {
            PrepStage.VS_ANIMATION -> {
                VSHologramAnimation(playerName, aiName, gameMode)
            }

            PrepStage.TOPIC_REVEAL -> {
                TopicRevealAnimation(topic, topicDescription)
            }

            PrepStage.SIDE_ASSIGNMENT -> {
                SideAssignmentAnimation(playerName, playerSide, aiName, aiSide, gameMode)
            }

            PrepStage.COIN_CHOICE -> {
                CoinChoiceAnimation { choice ->
                    android.util.Log.d("DebatePrep", "🎮 Player chose: $choice")
                    playerChoice = choice
                    currentStage = PrepStage.COIN_TOSS
                }
            }

            PrepStage.COIN_TOSS -> {
                CoinTossAnimation()
            }

            PrepStage.COIN_RESULT -> {
                CoinResultAnimation(playerChoice, coinResult, playerStarts)
            }

            PrepStage.COUNTDOWN -> {
                CountdownAnimation(playerStarts, playerName, aiName)
            }

            PrepStage.READY -> {
                // This will trigger navigation
            }
        }
    }
}

@Composable
fun VSHologramAnimation(
    playerName: String,
    aiName: String,
    gameMode: GameMode
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hologram")

    // Glitch effect
    val glitchOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitch"
    )

    // Hologram scan lines
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )

    // Pulsing glow
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val aiColor = when (gameMode) {
        GameMode.AI_BEGINNER -> BeginnerAI
        GameMode.AI_INTERMEDIATE -> IntermediateAI
        GameMode.AI_ADVANCED -> AdvancedAI
        else -> IntermediateAI
    }

    // AI speech bubble
    var showSpeech by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1500)
        showSpeech = true
    }

    CameraZoomBox(zoomIn = true) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Player Section
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -it }
            ) {
                HologramPlayerCard(
                    name = playerName,
                    color = CyanPrimary,
                    glitchOffset = glitchOffset,
                    glowAlpha = glowAlpha,
                    icon = Icons.Default.Person
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // VS Text with epic effect
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                // Outer glow ring
                Canvas(modifier = Modifier.size(120.dp)) {
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

                // VS Text
                Text(
                    text = "VS",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    modifier = Modifier.alpha(glowAlpha + 0.2f)
                )

                // Rotating ring
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawCircle(
                        color = CyanPrimary,
                        radius = size.width / 2,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // AI Section
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(1000, delayMillis = 300)) +
                        slideInVertically(tween(1000, delayMillis = 300)) { it }
            ) {
                HologramPlayerCard(
                    name = aiName,
                    color = aiColor,
                    glitchOffset = -glitchOffset,
                    glowAlpha = glowAlpha,
                    icon = Icons.Default.Settings
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Speech Bubble
            AISpeechBubble(
                text = "Let's see what you've got!",
                aiColor = aiColor,
                visible = showSpeech
            )
        }
    }
}

@Composable
fun HologramPlayerCard(
    name: String,
    color: Color,
    glitchOffset: Float,
    glowAlpha: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = glowAlpha * 0.3f),
                        Color.Transparent
                    )
                )
            )
        }

        // Main card with glitch effect
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = glitchOffset.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkCard.copy(alpha = 0.8f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        color.copy(alpha = glowAlpha),
                        color.copy(alpha = 0.3f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.8f),
                                    color.copy(alpha = 0.4f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Name
                Text(
                    text = name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        }

        // Scan line effect
        Canvas(modifier = Modifier.matchParentSize()) {
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = Offset(0f, size.height * 0.3f),
                end = Offset(size.width, size.height * 0.3f),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun TopicRevealAnimation(
    topic: String,
    description: String
) {
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

    val rotation by animateFloatAsState(
        targetValue = if (revealed) 0f else 180f,
        animationSpec = tween(800),
        label = "rotation"
    )

    // Get sector-specific icon and color
    val sectorIcon = getSectorIcon(description)
    val sectorColor = getSectorColor(description)

    // AI speech bubble
    var showSpeech by remember { mutableStateOf(false) }
    LaunchedEffect(revealed) {
        if (revealed) {
            delay(2000)
            showSpeech = true
        }
    }

    CameraZoomBox(zoomIn = revealed) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // "Topic Revealed!" text
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

            // Topic card with flip animation
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .scale(scale)
                    .rotate(rotation),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkCard
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            sectorColor,
                            sectorColor.copy(alpha = 0.5f),
                            CyanPrimary
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sector icon (emoji)
                    Text(
                        text = sectorIcon,
                        fontSize = 56.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Topic title with typewriter effect
                    if (revealed) {
                        TypewriterText(
                            text = topic,
                            fontSize = 24.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Speech Bubble
            AISpeechBubble(
                text = "Interesting topic... I'm ready.",
                aiColor = sectorColor,
                visible = showSpeech
            )
        }
    }
}

@Composable
fun SideAssignmentAnimation(
    playerName: String,
    playerSide: String,
    aiName: String,
    aiSide: String,
    gameMode: GameMode
) {
    val aiColor = when (gameMode) {
        GameMode.AI_BEGINNER -> BeginnerAI
        GameMode.AI_INTERMEDIATE -> IntermediateAI
        GameMode.AI_ADVANCED -> AdvancedAI
        else -> IntermediateAI
    }

    // AI speech bubble
    var showSpeech by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1500)
        showSpeech = true
    }

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

        // Player Side
        SideCard(
            name = playerName,
            side = playerSide,
            color = CyanPrimary,
            icon = Icons.Default.Person,
            isPlayer = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // VS indicator
        Text(
            text = "VS",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // AI Side
        SideCard(
            name = aiName,
            side = aiSide,
            color = aiColor,
            icon = Icons.Default.Settings,
            isPlayer = false
        )

        Spacer(modifier = Modifier.height(24.dp))

        // AI Speech Bubble
        AISpeechBubble(
            text = "I'll argue $aiSide, bring it on!",
            aiColor = aiColor,
            visible = showSpeech
        )
    }
}

@Composable
fun SideCard(
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
fun CoinChoiceAnimation(onChoice: (String) -> Unit) {
    // AI speech bubble
    var showSpeech by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(800)
        showSpeech = true
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🪙 COIN TOSS!",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = GoldCoin,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Choose Your Side",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Heads or Tails Choice Cards
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Heads Button
            CoinChoiceCard(
                label = "HEADS",
                emoji = "👤",
                color = CyanPrimary,
                onClick = { onChoice("Heads") },
                modifier = Modifier.weight(1f)
            )

            // Tails Button
            CoinChoiceCard(
                label = "TAILS",
                emoji = "🔢",
                color = PurpleAccent,
                onClick = { onChoice("Tails") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Winner starts the debate first!",
            fontSize = 16.sp,
            color = TextGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AI Speech Bubble
        AISpeechBubble(
            text = "May the best debater win!",
            aiColor = GoldCoin,
            visible = showSpeech
        )
    }
}

@Composable
fun CoinChoiceCard(
    label: String,
    emoji: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = modifier
            .height(200.dp)
            .scale(scale)
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 3.dp,
            color = color
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emoji Icon
            Text(
                text = emoji,
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Label
            Text(
                text = label,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

@Composable
fun CoinTossAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "coin")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f * 3, // 3 full rotations
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Spinning coin
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .rotate(rotation),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow
            Canvas(modifier = Modifier.size(220.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldCoin.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
            }

            // Coin
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GoldCoin,
                                GoldCoin.copy(alpha = 0.7f),
                                Color(0xFFFFA500)
                            )
                        )
                    )
                    .border(8.dp, Color(0xFFFFD700).copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "R",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Deciding who starts...",
            fontSize = 18.sp,
            color = TextGray
        )
    }
}

@Composable
fun CoinResultAnimation(playerChoice: String, coinResult: String, playerStarts: Boolean) {
    val scale by animateFloatAsState(
        targetValue = 1.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "result_scale"
    )

    // Screen shake effect
    val shakeController = rememberShakeController()
    LaunchedEffect(Unit) {
        delay(200)
        shakeController.shake()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = shakeController.offset.value.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Result Icon
            Text(
                text = if (playerStarts) "🎉" else "😔",
                fontSize = 80.sp,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .scale(scale)
            )

            // Winner Announcement
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(0.9f)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (playerStarts)
                        GreenWin.copy(alpha = 0.2f)
                    else
                        RedLoss.copy(alpha = 0.2f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 3.dp,
                    color = if (playerStarts) GreenWin else RedLoss
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (playerStarts) "🎊 YOU WIN! 🎊" else "AI WINS!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (playerStarts) GreenWin else RedLoss,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "You chose: $playerChoice",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Coin landed: $coinResult",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (playerStarts)
                            "✨ You'll start the debate first!"
                        else
                            "🤖 AI will start the debate first!",
                        fontSize = 16.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CountdownAnimation(
    playerStarts: Boolean,
    playerName: String,
    aiName: String
) {
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
        label = "countdown_scale"
    )

    // Screen shake effect on each countdown number
    val shakeController = rememberShakeController()
    LaunchedEffect(countdownNumber) {
        if (countdownNumber > 0) {
            shakeController.shake()
        }
    }

    // AI speech bubble
    var showSpeech by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(800)
        showSpeech = true
    }

    CameraZoomBox(zoomIn = countdownNumber <= 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = shakeController.offset.value.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Result announcement
                Card(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (playerStarts) CyanPrimary.copy(alpha = 0.2f)
                        else IntermediateAI.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (playerStarts) "🎉 You Start First!" else "🤖 AI Starts First!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (playerStarts) playerName else aiName,
                            fontSize = 18.sp,
                            color = if (playerStarts) CyanPrimary else IntermediateAI
                        )
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))

                // Countdown number
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

                Spacer(modifier = Modifier.height(24.dp))

                // AI Speech Bubble
                AISpeechBubble(
                    text = "3... 2... 1... Time to debate!",
                    aiColor = CyanPrimary,
                    visible = showSpeech
                )
            }
        }
    }
}

@Composable
fun AnimatedBackgroundEffect() {
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

            val x =
                size.width / 2 + kotlin.math.cos(Math.toRadians(time.toDouble())).toFloat() * radius
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

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DebatePreparationScreenPreview() {
    DebatePreparationScreen(
        playerName = "Player One",
        aiName = "AI Thinker",
        topic = "Social Media Does More Harm Than Good",
        topicDescription = "Discuss the impact of social media on society",
        playerSide = "FOR",
        aiSide = "AGAINST",
        gameMode = GameMode.AI_INTERMEDIATE,
        onPreparationComplete = {}
    )
}
