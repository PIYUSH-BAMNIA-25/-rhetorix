package com.runanywhere.startup_hackathon20

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin

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
private val GoldStar = Color(0xFFFFD700)
private val YellowWarning = Color(0xFFFBBF24)

@Composable
fun DebateResultsScreen(
    playerName: String,
    aiName: String,
    playerScore: Int,
    aiScore: Int,
    playerWon: Boolean,
    shiningPoints: List<String>,
    lackingPoints: List<String>,
    topic: String,
    gameMode: GameMode,
    comprehensiveFeedback: String = "",
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    var showDetailedFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
    }

    val motivationalQuote = if (playerWon) {
        listOf(
            "\"Victory belongs to those who believe in it the most!\" - Napoleon",
            "\"Success is not final, failure is not fatal!\" - Winston Churchill",
            "\"The only way to do great work is to love what you do!\" - Steve Jobs",
            "\"Excellence is not a skill, it's an attitude!\" - Ralph Marston",
            "\"Champions keep playing until they get it right!\" - Billie Jean King"
        ).random()
    } else {
        listOf(
            "\"Failure is the opportunity to begin again more intelligently!\" - Henry Ford",
            "\"Every master was once a disaster!\" - T. Harv Eker",
            "\"I have not failed. I've just found 10,000 ways that won't work!\" - Thomas Edison",
            "\"The greatest glory in living lies not in never falling, but in rising every time we fall!\" - Nelson Mandela",
            "\"It's not whether you get knocked down, it's whether you get up!\" - Vince Lombardi"
        ).random()
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
        // Animated background
        AnimatedResultsBackground(playerWon)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(800)) + expandVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // 1. WINNER/DEFEATED ANNOUNCEMENT (Golden Ratio: larger section)
                    WinnerAnnouncementSection(
                        playerWon = playerWon,
                        playerScore = playerScore,
                        aiScore = aiScore
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 2. MOTIVATIONAL QUOTE
                    MotivationalQuoteCard(motivationalQuote)

                    Spacer(modifier = Modifier.height(40.dp))

                    // 3. MATCH RATING
                    MatchRatingCard(playerScore, aiScore)

                    Spacer(modifier = Modifier.height(40.dp))

                    // 4. SHINING POINTS (3 things you did great)
                    ShiningPointsSection(shiningPoints)

                    Spacer(modifier = Modifier.height(32.dp))

                    // 5. LACKING POINTS (3 things to improve)
                    LackingPointsSection(lackingPoints)

                    Spacer(modifier = Modifier.height(40.dp))

                    // 6. COMPREHENSIVE FEEDBACK (NEW!)
                    if (comprehensiveFeedback.isNotBlank()) {
                        ComprehensiveFeedbackSection(
                            feedback = comprehensiveFeedback,
                            expanded = showDetailedFeedback,
                            onToggle = { showDetailedFeedback = !showDetailedFeedback }
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    // 7. ACTION BUTTONS
                    ActionButtonsSection(onPlayAgain, onBackToMenu)

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ComprehensiveFeedbackSection(
    feedback: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Section Header with toggle
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = PurpleAccent.copy(alpha = 0.2f)
            ),
            onClick = onToggle
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Coach",
                        tint = PurpleAccent,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "🎯 COACH'S ANALYSIS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PurpleAccent
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = PurpleAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expandable feedback content
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(400)) + expandVertically(tween(400)),
            exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkCard.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = feedback,
                        fontSize = 14.sp,
                        color = TextWhite,
                        lineHeight = 22.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            letterSpacing = 0.3.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun WinnerAnnouncementSection(
    playerWon: Boolean,
    playerScore: Int,
    aiScore: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "winner_glow")

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Trophy/Defeat Icon
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow ring (rotating)
            if (playerWon) {
                Canvas(modifier = Modifier
                    .size(180.dp)
                    .rotate(rotation)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldStar.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
                }
            }

            // Icon with scale animation
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(glowScale)
                    .clip(CircleShape)
                    .background(
                        if (playerWon) {
                            Brush.radialGradient(
                                colors = listOf(
                                    GoldStar.copy(alpha = 0.3f),
                                    GreenWin.copy(alpha = 0.1f)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    RedLoss.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playerWon) Icons.Default.Star else Icons.Default.Close,
                    contentDescription = if (playerWon) "Victory" else "Defeat",
                    tint = if (playerWon) GoldStar else RedLoss,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Victory/Defeat Text
        Text(
            text = if (playerWon) "🏆 VICTORY!" else "💪 DEFEATED",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (playerWon) GoldStar else RedLoss,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Score Display
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkCard.copy(alpha = 0.8f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player Score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "YOU",
                        fontSize = 14.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playerScore.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (playerWon) GreenWin else RedLoss
                    )
                }

                // VS Divider
                Text(
                    text = "-",
                    fontSize = 32.sp,
                    color = TextGray
                )

                // AI Score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AI",
                        fontSize = 14.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = aiScore.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (!playerWon) GreenWin else TextGray
                    )
                }
            }
        }
    }
}

@Composable
fun MotivationalQuoteCard(quote: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Quote",
                tint = PurpleAccent,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = quote,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = TextWhite,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MatchRatingCard(playerScore: Int, aiScore: Int) {
    val totalScore = playerScore + aiScore
    val rating = when {
        totalScore >= 90 -> "Legendary" to GoldStar
        totalScore >= 75 -> "Excellent" to GreenWin
        totalScore >= 60 -> "Good" to CyanPrimary
        totalScore >= 45 -> "Fair" to OrangeAccent
        else -> "Needs Work" to RedLoss
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MATCH RATING",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rating badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(rating.second.copy(alpha = 0.2f))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = rating.first,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = rating.second
                )
            }
        }
    }
}

@Composable
fun ShiningPointsSection(points: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Shining",
                tint = GoldStar,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "✨ SHINING POINTS",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldStar
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Points List
        points.take(3).forEachIndexed { index, point ->
            ShiningPointCard(point, index)
            if (index < 2) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ShiningPointCard(point: String, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((index * 150).toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInHorizontally(tween(500)) { -it }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = GreenWin.copy(alpha = 0.15f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = GreenWin.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Number badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GreenWin.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenWin
                    )
                }

                // Point text
                Text(
                    text = point,
                    fontSize = 15.sp,
                    color = TextWhite,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )

                // Check icon
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Shine",
                    tint = GreenWin,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun LackingPointsSection(points: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Lacking",
                tint = YellowWarning,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "📉 AREAS TO IMPROVE",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = YellowWarning
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Points List
        points.take(3).forEachIndexed { index, point ->
            LackingPointCard(point, index)
            if (index < 2) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun LackingPointCard(point: String, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((index * 150).toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInHorizontally(tween(500)) { it }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = OrangeAccent.copy(alpha = 0.15f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = OrangeAccent.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Number badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent
                    )
                }

                // Point text
                Text(
                    text = point,
                    fontSize = 15.sp,
                    color = TextWhite,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )

                // Arrow up icon (improve)
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Improve",
                    tint = YellowWarning,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ActionButtonsSection(
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Play Again Button
        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenWin
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Play Again",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "PLAY AGAIN",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Back to Menu Button
        OutlinedButton(
            onClick = onBackToMenu,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = CyanPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Menu",
                tint = CyanPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "MAIN MENU",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CyanPrimary,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun AnimatedResultsBackground(playerWon: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val color = if (playerWon) GoldStar else RedLoss

    val animatedOffsets = remember {
        List(8) {
            Pair(
                (0..360).random().toFloat(),
                (100..250).random().toFloat()
            )
        }
    }

    val animatedValues = List(8) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 30000 + index * 2000,
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
                color = if (index % 2 == 0) color.copy(alpha = 0.1f)
                else CyanPrimary.copy(alpha = 0.08f),
                radius = (12 + index * 2).toFloat(),
                center = Offset(x, y)
            )
        }
    }
}

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DebateResultsScreenPreview() {
    DebateResultsScreen(
        playerName = "Player One",
        aiName = "AI Thinker",
        playerScore = 42,
        aiScore = 38,
        playerWon = true,
        shiningPoints = listOf(
            "Excellent use of logical reasoning and clear structure",
            "Strong evidence with relevant real-world examples",
            "Maintained respectful tone throughout the debate"
        ),
        lackingPoints = listOf(
            "Could address opponent's counter-arguments more directly",
            "Some claims lacked supporting evidence",
            "Repetition of similar points in later turns"
        ),
        topic = "Social Media Does More Harm Than Good",
        gameMode = GameMode.AI_INTERMEDIATE,
        comprehensiveFeedback = "Behavior: Good\nTurn Analysis: Strong\nCommunication Style: Clear\nStrategic Analysis: Excellent\nAreas for Improvement: Counter Arguments",
        onPlayAgain = {},
        onBackToMenu = {}
    )
}
