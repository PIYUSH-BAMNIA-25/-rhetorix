package com.runanywhere.startup_hackathon20

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.runanywhere.startup_hackathon20.data.DifficultyLevel

// Color Palette - Same as main page
private val CyanPrimary = Color(0xFF00D9FF)
private val CyanLight = Color(0xFF5FEDFF)
private val PurpleAccent = Color(0xFF9D4EDD)
private val DarkBackground = Color(0xFF0A0A0F)
private val DarkSurface = Color(0xFF1A1A2E)
private val DarkCard = Color(0xFF16213E)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFFB0B0B0)
private val GreenAccent = Color(0xFF4ADE80)
private val OrangeAccent = Color(0xFFFB923C)
private val RedAccent = Color(0xFFF87171)

@Composable
fun AIPracticeModeScreen(
    userWins: Int = 0, // Pass user's total wins
    onDifficultySelected: (GameMode) -> Unit,
    onBack: () -> Unit
) {
    var showUnlockDialog by remember { mutableStateOf<DifficultyLevel?>(null) }

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
        // Animated Background
        AIPracticeBackgroundParticles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button and Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CyanPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Text(
                    text = "Retrorix",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyanPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                // Spacer to balance the layout
                Spacer(modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title Section
            Text(
                text = "AI Practice Arena",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Choose your challenge level",
                fontSize = 16.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )

            // Stats Badge
            Card(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkCard.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = "🏆 Total Wins: $userWins",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenAccent,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Fresh Mind Mode (Beginner) - ALWAYS UNLOCKED
            val beginnerUnlocked = DifficultyLevel.BEGINNER.isUnlocked(userWins)
            AIDifficultyCard(
                title = "Fresh Mind",
                icon = Icons.Filled.Face,
                gradient = listOf(GreenAccent, Color(0xFF34D399)),
                description = "Perfect for beginners",
                subDescription = "Build your confidence with simple debates and gentle AI responses",
                difficulty = "Easy",
                isLocked = !beginnerUnlocked,
                requiredWins = DifficultyLevel.BEGINNER.requiredWins,
                currentWins = userWins,
                modelInfo = "Using: Llama 1B",
                onClick = {
                    if (beginnerUnlocked) {
                        onDifficultySelected(GameMode.AI_BEGINNER)
                    } else {
                        showUnlockDialog = DifficultyLevel.BEGINNER
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Thinker Mode (Intermediate) - UNLOCK AT 2 WINS
            val intermediateUnlocked = DifficultyLevel.INTERMEDIATE.isUnlocked(userWins)
            AIDifficultyCard(
                title = "Thinker",
                icon = Icons.Filled.Build,
                gradient = listOf(CyanPrimary, CyanLight),
                description = "Ready to level up",
                subDescription = "Face moderate challenges and sharpen your argumentative skills",
                difficulty = "Medium",
                isLocked = !intermediateUnlocked,
                requiredWins = DifficultyLevel.INTERMEDIATE.requiredWins,
                currentWins = userWins,
                modelInfo = "Using: Qwen 3B (Advanced AI)",
                onClick = {
                    if (intermediateUnlocked) {
                        onDifficultySelected(GameMode.AI_INTERMEDIATE)
                    } else {
                        showUnlockDialog = DifficultyLevel.INTERMEDIATE
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Speaker Mode (Advanced) - UNLOCK AT 5 WINS
            val advancedUnlocked = DifficultyLevel.ADVANCED.isUnlocked(userWins)
            AIDifficultyCard(
                title = "Speaker",
                icon = Icons.Filled.Star,
                gradient = listOf(OrangeAccent, Color(0xFFF59E0B)),
                description = "Master debater challenge",
                subDescription = "Test your limits against advanced AI with complex arguments",
                difficulty = "Hard",
                isLocked = !advancedUnlocked,
                requiredWins = DifficultyLevel.ADVANCED.requiredWins,
                currentWins = userWins,
                modelInfo = "Using: Qwen 3B (Maximum Difficulty)",
                onClick = {
                    if (advancedUnlocked) {
                        onDifficultySelected(GameMode.AI_ADVANCED)
                    } else {
                        showUnlockDialog = DifficultyLevel.ADVANCED
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkCard.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Info",
                        tint = CyanPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Win matches to unlock higher difficulty levels! The AI evolves as you improve.",
                        fontSize = 14.sp,
                        color = TextGray,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Unlock Dialog
        showUnlockDialog?.let { difficulty ->
            UnlockDialog(
                difficulty = difficulty,
                currentWins = userWins,
                onDismiss = { showUnlockDialog = null }
            )
        }
    }
}

@Composable
fun AIDifficultyCard(
    title: String,
    icon: ImageVector,
    gradient: List<Color>,
    description: String,
    subDescription: String,
    difficulty: String,
    isLocked: Boolean = false,
    requiredWins: Int = 0,
    currentWins: Int = 0,
    modelInfo: String = "",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isLocked) 160.dp else 200.dp)
            .scale(scale)
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isLocked) {
                        Brush.horizontalGradient(
                            listOf(
                                TextGray.copy(alpha = 0.3f),
                                TextGray.copy(alpha = 0.2f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(gradient)
                    }
                )
        ) {
            // Decorative circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = if (isLocked) 0.05f else 0.1f),
                    radius = 100f,
                    center = Offset(size.width * 0.85f, size.height * 0.3f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = if (isLocked) 0.02f else 0.05f),
                    radius = 60f,
                    center = Offset(size.width * 0.15f, size.height * 0.7f)
                )
            }

            // Lock Overlay
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Locked",
                            tint = TextWhite,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Win ${requiredWins - currentWins} more ${if (requiredWins - currentWins == 1) "match" else "matches"} to unlock",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text Content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Difficulty Badge
                    Surface(
                        color = Color.White.copy(alpha = if (isLocked) 0.1f else 0.25f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = difficulty,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite.copy(alpha = if (isLocked) 0.5f else 1f)
                    )
                    
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite.copy(alpha = if (isLocked) 0.4f else 0.9f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (!isLocked) {
                        Text(
                            text = subDescription,
                            fontSize = 12.sp,
                            color = TextWhite.copy(alpha = 0.75f),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        // Model Info
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = modelInfo,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextWhite.copy(alpha = 0.9f),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Icon
                Icon(
                    imageVector = if (isLocked) Icons.Filled.Lock else icon,
                    contentDescription = title,
                    tint = TextWhite.copy(alpha = if (isLocked) 0.5f else 1f),
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

@Composable
fun UnlockDialog(
    difficulty: DifficultyLevel,
    currentWins: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Locked",
                tint = OrangeAccent,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "${difficulty.displayName} Mode Locked",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = difficulty.description,
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Progress indicator
                Text(
                    text = "Progress: $currentWins / ${difficulty.requiredWins} wins",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(8.dp)
                        .background(TextGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(currentWins.toFloat() / difficulty.requiredWins.toFloat())
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(CyanPrimary, GreenAccent)
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }

                Text(
                    text = "Win ${difficulty.requiredWins - currentWins} more ${if (difficulty.requiredWins - currentWins == 1) "match" else "matches"} to unlock!",
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = DarkBackground
                )
            ) {
                Text("Got it!")
            }
        },
        containerColor = DarkCard,
        shape = RoundedCornerShape(24.dp)
    )
}

// ==================== ANIMATED BACKGROUND ====================
@Composable
fun AIPracticeBackgroundParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

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
            val y = size.height / 2 + kotlin.math.sin(Math.toRadians(time.toDouble()))
                .toFloat() * radius

            drawCircle(
                color = if (index % 2 == 0) PurpleAccent.copy(alpha = 0.2f) else CyanPrimary.copy(
                    alpha = 0.15f
                ),
                radius = (10 + index * 3).toFloat(),
                center = Offset(x, y)
            )
        }
    }
}

// ==================== PREVIEW ====================
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AIPracticeModeScreenPreview() {
    AIPracticeModeScreen(
        userWins = 1, // Only 1 win - Intermediate and Advanced locked
        onDifficultySelected = {},
        onBack = {}
    )
}
