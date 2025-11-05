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

@Composable
fun AIPracticeModeScreen(
    onDifficultySelected: (GameMode) -> Unit,
    onBack: () -> Unit
) {
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
        AnimatedBackgroundParticles()

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

            Spacer(modifier = Modifier.height(56.dp))

            // Fresh Mind Mode (Beginner)
            AIDifficultyCard(
                title = "Fresh Mind",
                icon = Icons.Filled.Face,
                gradient = listOf(GreenAccent, Color(0xFF34D399)),
                description = "Perfect for beginners",
                subDescription = "Build your confidence with simple debates and gentle AI responses",
                difficulty = "Easy",
                onClick = { onDifficultySelected(GameMode.AI_BEGINNER) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Thinker Mode (Intermediate)
            AIDifficultyCard(
                title = "Thinker",
                icon = Icons.Filled.Build,
                gradient = listOf(CyanPrimary, CyanLight),
                description = "Ready to level up",
                subDescription = "Face moderate challenges and sharpen your argumentative skills",
                difficulty = "Medium",
                onClick = { onDifficultySelected(GameMode.AI_INTERMEDIATE) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Speaker Mode (Advanced)
            AIDifficultyCard(
                title = "Speaker",
                icon = Icons.Filled.Star,
                gradient = listOf(OrangeAccent, Color(0xFFF59E0B)),
                description = "Master debater challenge",
                subDescription = "Test your limits against advanced AI with complex arguments",
                difficulty = "Hard",
                onClick = { onDifficultySelected(GameMode.AI_ADVANCED) }
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
                        text = "Each mode adapts to your skill level and provides real-time feedback!",
                        fontSize = 14.sp,
                        color = TextGray,
                        lineHeight = 20.sp
                    )
                }
            }
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
            .height(180.dp)
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
                    Brush.horizontalGradient(gradient)
                )
        ) {
            // Decorative circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = 100f,
                    center = Offset(size.width * 0.85f, size.height * 0.3f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = 60f,
                    center = Offset(size.width * 0.15f, size.height * 0.7f)
                )
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
                        color = Color.White.copy(alpha = 0.25f),
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
                        color = TextWhite
                    )
                    
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Text(
                        text = subDescription,
                        fontSize = 12.sp,
                        color = TextWhite.copy(alpha = 0.75f),
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Icon
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = TextWhite,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

// ==================== PREVIEW ====================
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AIPracticeModeScreenPreview() {
    AIPracticeModeScreen(
        onDifficultySelected = {},
        onBack = {}
    )
}
