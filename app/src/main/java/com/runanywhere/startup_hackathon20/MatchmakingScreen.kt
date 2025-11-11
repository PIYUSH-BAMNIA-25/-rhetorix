package com.runanywhere.startup_hackathon20

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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// Luxury Theme Colors (matching your app)
private val DeepBlack = Color(0xFF0D0D12)
private val RichBlack = Color(0xFF16161D)
private val DarkSlate = Color(0xFF1E1E28)
private val GoldPrimary = Color(0xFFD4AF37)
private val GoldLight = Color(0xFFF4E6B8)
private val GoldDark = Color(0xFFB8963C)
private val AmberAccent = Color(0xFFFFB84D)
private val CopperShine = Color(0xFFE8985E)
private val SilverGray = Color(0xFF9BA4B5)
private val PearlWhite = Color(0xFFF5F5F7)

/**
 * Elegant Matchmaking Screen with Luxury Design
 * Shows "Finding Opponent..." with animated effects
 */
@Composable
fun MatchmakingScreen(
    playerName: String,
    onMatchFound: (opponentName: String, sessionId: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatchmakingViewModel = viewModel()
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "matchmaking")

    val searchingDotsCount by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 3,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )

    // Elapsed time counter
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Animated luxury background
        AnimatedSearchBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Main Content (centered)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Search Spinner
                AnimatedSearchSpinner()

                Spacer(modifier = Modifier.height(48.dp))

                // "Finding Opponent" Title
                Text(
                    text = "Finding Opponent${".".repeat(searchingDotsCount.toInt())}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle
                Text(
                    text = "Searching for worthy adversaries",
                    fontSize = 16.sp,
                    color = SilverGray.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Elegant Info Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Player Info Card
                    ElegantInfoCard(
                        icon = Icons.Default.Person,
                        label = "Your Name",
                        value = playerName
                    )

                    // Time Searching Card
                    ElegantInfoCard(
                        icon = Icons.Default.Star,
                        label = "Searching for",
                        value = "${elapsedSeconds}s"
                    )

                    // Match Type Card
                    ElegantInfoCard(
                        icon = Icons.Default.Face,
                        label = "Match Type",
                        value = "Player vs Player"
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Searching Status
                AnimatedSearchingStatus()
            }

            // Cancel Button at Bottom
            OutlinedButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.cancelMatchmaking()
                    onCancel()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(GoldDark, GoldPrimary, GoldLight)
                    )
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DarkSlate.copy(alpha = 0.3f),
                    contentColor = GoldPrimary
                ),
                shape = RoundedCornerShape(31.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "CANCEL SEARCH",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Animated Search Spinner with Golden Rings
 */
@Composable
fun AnimatedSearchSpinner() {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")

    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation1"
    )

    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation2"
    )

    val rotation3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation3"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring glow
        Canvas(modifier = Modifier.size(240.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GoldPrimary.copy(alpha = 0.3f),
                        GoldPrimary.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                radius = size.width / 2
            )
        }

        // Ring 1 (Outer)
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .rotate(rotation1)
        ) {
            val radius = size.width / 2
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        GoldPrimary,
                        GoldLight,
                        AmberAccent,
                        Color.Transparent,
                        Color.Transparent,
                        GoldPrimary
                    )
                ),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Ring 2 (Middle)
        Canvas(
            modifier = Modifier
                .size(160.dp)
                .rotate(rotation2)
        ) {
            val radius = size.width / 2
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        AmberAccent,
                        CopperShine,
                        GoldLight,
                        Color.Transparent,
                        Color.Transparent,
                        AmberAccent
                    )
                ),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Ring 3 (Inner)
        Canvas(
            modifier = Modifier
                .size(120.dp)
                .rotate(rotation3)
        ) {
            val radius = size.width / 2
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        CopperShine,
                        GoldPrimary,
                        GoldLight,
                        Color.Transparent,
                        Color.Transparent,
                        CopperShine
                    )
                ),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Center Icon (pulsing)
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(pulseScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldPrimary.copy(alpha = 0.3f),
                            DarkSlate.copy(alpha = 0.8f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            GoldPrimary,
                            GoldLight,
                            GoldPrimary
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Searching",
                tint = GoldPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/**
 * Elegant Info Card with Icon
 */
@Composable
fun ElegantInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RichBlack.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = GoldPrimary.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldPrimary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = GoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Text Content
            Column {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = SilverGray.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 18.sp,
                    color = PearlWhite,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Animated Searching Status with Dots
 */
@Composable
fun AnimatedSearchingStatus() {
    val infiniteTransition = rememberInfiniteTransition(label = "status")

    // Create 5 dots with staggered animations
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1200,
                        delayMillis = index * 150,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )

            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1200,
                        delayMillis = index * 150,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha$index"
            )

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldPrimary,
                                GoldPrimary.copy(alpha = 0.5f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * Animated Search Background with Floating Particles
 */
@Composable
fun AnimatedSearchBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    // Create floating particles
    val particles = remember {
        List(8) { index ->
            Triple(
                (0..100).random() / 100f, // x position
                (0..100).random() / 100f, // y position
                (20..50).random() // size
            )
        }
    }

    val animatedValues = particles.mapIndexed { index, _ ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 12000 + index * 2000,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "particle$index"
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    DeepBlack,
                    RichBlack,
                    DeepBlack
                )
            )
        )

        // Floating particles with circular motion
        particles.forEachIndexed { index, (xPos, yPos, particleSize) ->
            val angle = animatedValues[index].value
            val radiusX = size.width * 0.1f
            val radiusY = size.height * 0.08f

            val centerX = size.width * xPos
            val centerY = size.height * yPos

            val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * radiusX
            val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * radiusY

            // Particle glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        when (index % 3) {
                            0 -> GoldPrimary.copy(alpha = 0.12f)
                            1 -> AmberAccent.copy(alpha = 0.1f)
                            else -> CopperShine.copy(alpha = 0.08f)
                        },
                        Color.Transparent
                    )
                ),
                radius = particleSize.toFloat(),
                center = Offset(x, y)
            )
        }
    }
}