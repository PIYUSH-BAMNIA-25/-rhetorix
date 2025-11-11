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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin

// Color Palette (same as AI mode)
private val CyanPrimary = Color(0xFF00D9FF)
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

/**
 * P2P Debate Results Screen
 * Shows winner, scores, feedback with REMATCH option
 */
@Composable
fun P2PDebateResultsScreen(
    myName: String,
    opponentName: String,
    myScore: Int,
    opponentScore: Int,
    iWon: Boolean,
    myStrengths: List<String>,
    myWeaknesses: List<String>,
    topic: String,
    onRematch: () -> Unit,
    onBackToMenu: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
    }

    val motivationalQuote = if (iWon) {
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
            "\"The greatest glory in living lies not in never falling, but in rising every time we fall!\" - Nelson Mandela",
            "\"It's not whether you get knocked down, it's whether you get up!\" - Vince Lombardi",
            "\"I have not failed. I've just found 10,000 ways that won't work!\" - Thomas Edison"
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
        P2PAnimatedResultsBackground(iWon)

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

                    // 1. WINNER/LOSER ANNOUNCEMENT
                    P2PWinnerAnnouncement(
                        iWon = iWon,
                        myScore = myScore,
                        opponentScore = opponentScore,
                        myName = myName,
                        opponentName = opponentName
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 2. MOTIVATIONAL QUOTE
                    P2PMotivationalQuote(motivationalQuote)

                    Spacer(modifier = Modifier.height(40.dp))

                    // 3. MATCH RATING
                    P2PMatchRating(myScore, opponentScore)

                    Spacer(modifier = Modifier.height(40.dp))

                    // 4. MY STRENGTHS
                    P2PStrengthsSection(myStrengths)

                    Spacer(modifier = Modifier.height(32.dp))

                    // 5. MY WEAKNESSES
                    P2PWeaknessesSection(myWeaknesses)

                    Spacer(modifier = Modifier.height(40.dp))

                    // 6. ACTION BUTTONS
                    P2PActionButtons(onRematch, onBackToMenu)

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun P2PWinnerAnnouncement(
    iWon: Boolean,
    myScore: Int,
    opponentScore: Int,
    myName: String,
    opponentName: String
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
            if (iWon) {
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                        .rotate(rotation)
                ) {
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
                        if (iWon) {
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
                    imageVector = if (iWon) Icons.Default.Star else Icons.Default.Close,
                    contentDescription = if (iWon) "Victory" else "Defeat",
                    tint = if (iWon) GoldStar else RedLoss,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Victory/Defeat Text
        Text(
            text = if (iWon) "🏆 VICTORY!" else "💪 DEFEATED",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (iWon) GoldStar else RedLoss,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Score Display
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
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
                // My Score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = myName,
                        fontSize = 14.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = myScore.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (iWon) GreenWin else RedLoss
                    )
                }

                // VS Divider
                Text(
                    text = "-",
                    fontSize = 32.sp,
                    color = TextGray
                )

                // Opponent Score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = opponentName,
                        fontSize = 14.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = opponentScore.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (!iWon) GreenWin else TextGray
                    )
                }
            }
        }
    }
}

@Composable
fun P2PMotivationalQuote(quote: String) {
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
fun P2PMatchRating(myScore: Int, opponentScore: Int) {
    val totalScore = myScore + opponentScore
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
fun P2PStrengthsSection(strengths: List<String>) {
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
                contentDescription = "Strengths",
                tint = GoldStar,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "✨ YOUR STRENGTHS",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldStar
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Strengths List
        strengths.take(3).forEachIndexed { index, strength ->
            P2PStrengthCard(strength, index)
            if (index < 2) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun P2PStrengthCard(strength: String, index: Int) {
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

                // Strength text
                Text(
                    text = strength,
                    fontSize = 15.sp,
                    color = TextWhite,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )

                // Check icon
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Strength",
                    tint = GreenWin,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun P2PWeaknessesSection(weaknesses: List<String>) {
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
                contentDescription = "Weaknesses",
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

        // Weaknesses List
        weaknesses.take(3).forEachIndexed { index, weakness ->
            P2PWeaknessCard(weakness, index)
            if (index < 2) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun P2PWeaknessCard(weakness: String, index: Int) {
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

                // Weakness text
                Text(
                    text = weakness,
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
fun P2PActionButtons(
    onRematch: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rematch Button
        Button(
            onClick = onRematch,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PurpleAccent
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Rematch",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "FIND NEW OPPONENT",
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
fun P2PAnimatedResultsBackground(iWon: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val color = if (iWon) GoldStar else RedLoss

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