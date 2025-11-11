package com.runanywhere.startup_hackathon20

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon20.data.DifficultyLevel
import kotlin.math.cos
import kotlin.math.sin
import androidx.activity.compose.BackHandler

// Elegant Dark Theme Color Palette with Golden Accents
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
private val SoftWhite = Color(0xFFE8E8ED)
private val ErrorRose = Color(0xFFFF6B6B)
private val SuccessGreen = Color(0xFF51CF66)
private val GreenWin = Color(0xFF4ADE80)
private val EmeraldAccent = Color(0xFF10B981)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFFB0B0B0)

@Composable
fun AIPracticeModeScreen(
    userWins: Int = 0,
    onDifficultySelected: (GameMode) -> Unit,
    onBack: () -> Unit
) {
    var showUnlockDialog by remember { mutableStateOf<DifficultyLevel?>(null) }
    var showModelDownloadDialog by remember { mutableStateOf(false) }
    var pendingGameMode by remember { mutableStateOf<GameMode?>(null) }
    
    // Get DebateViewModel to access model management
    val debateViewModel: DebateViewModel = viewModel()
    val availableModels by debateViewModel.availableModels.collectAsState()
    val downloadProgress by debateViewModel.downloadProgress.collectAsState()
    val currentModelId by debateViewModel.currentModelId.collectAsState()
    val statusMessage by debateViewModel.statusMessage.collectAsState()

    // Handle back button
    BackHandler {
        onBack()
    }

    // Function to check model and start game
    fun checkModelAndStart(gameMode: GameMode) {
        // Cancel any previous pending mode
        pendingGameMode = null

        // Check if model is ready
        val requiredModelName = "Qwen 2.5 3B Instruct Q6_K"
        val requiredModel = availableModels.find { it.name == requiredModelName }

        if (requiredModel != null && requiredModel.isDownloaded && currentModelId == requiredModel.id) {
            // Model is ready! Start immediately
            android.util.Log.d("AIPracticeMode", "✅ Model ready, starting ${gameMode.name}")
            onDifficultySelected(gameMode)
        } else {
            // Model not ready, show dialog
            android.util.Log.d(
                "AIPracticeMode",
                "⚠️ Model not ready, showing dialog for ${gameMode.name}"
            )
            pendingGameMode = gameMode
            showModelDownloadDialog = true
        }
    }

    // Watch for model becoming ready
    LaunchedEffect(currentModelId, showModelDownloadDialog) {
        // Only proceed if we have a pending mode AND model just became ready
        if (pendingGameMode != null && showModelDownloadDialog) {
            val requiredModelName = "Qwen 2.5 3B Instruct Q6_K"
            val model = availableModels.find { it.name == requiredModelName }

            if (model != null && model.isDownloaded && currentModelId == model.id) {
                // Model is now ready!
                android.util.Log.d(
                    "AIPracticeMode",
                    "✅ Model loaded, starting ${pendingGameMode?.name}"
                )
                val modeToStart = pendingGameMode
                pendingGameMode = null
                showModelDownloadDialog = false

                // Small delay for smooth transition
                kotlinx.coroutines.delay(100)
                modeToStart?.let { onDifficultySelected(it) }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Animated luxury background
        AnimatedLuxuryBackground()

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = DarkSlate.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Animated Retrorix Logo
                AnimatedRetrorixLogo()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title Section with shimmer effect
            ShimmerText(
                text = "AI PRACTICE",
                fontSize = 34.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Choose Your Challenge Level",
                fontSize = 16.sp,
                color = SilverGray,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            // Stats Badge with animation
            AnimatedStatsBadge(userWins = userWins)

            Spacer(modifier = Modifier.height(40.dp))

            // Fresh Mind Mode (Beginner) - ALWAYS UNLOCKED
            val beginnerUnlocked = DifficultyLevel.BEGINNER.isUnlocked(userWins)
            ElegantDifficultyCard(
                title = "Fresh Mind",
                icon = Icons.Filled.Face,
                gradient = listOf(GreenWin, EmeraldAccent, SuccessGreen),
                description = "Perfect for beginners",
                subDescription = "Build your confidence with simple debates",
                difficulty = "EASY",
                isLocked = !beginnerUnlocked,
                requiredWins = DifficultyLevel.BEGINNER.requiredWins,
                currentWins = userWins,
                modelInfo = "Qwen 2.5 3B",
                onClick = {
                    if (beginnerUnlocked) {
                        android.util.Log.d("AIPracticeMode", "🎮 Beginner clicked")
                        checkModelAndStart(GameMode.AI_BEGINNER)
                    } else {
                        showUnlockDialog = DifficultyLevel.BEGINNER
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Thinker Mode (Intermediate) - ALWAYS UNLOCKED (requiredWins = 0)
            val intermediateUnlocked = DifficultyLevel.INTERMEDIATE.isUnlocked(userWins)
            ElegantDifficultyCard(
                title = "Thinker",
                icon = Icons.Filled.Build,
                gradient = listOf(GoldPrimary, GoldLight, AmberAccent),
                description = "Ready to level up",
                subDescription = "Sharpen your argumentative skills",
                difficulty = "MEDIUM",
                isLocked = !intermediateUnlocked,
                requiredWins = DifficultyLevel.INTERMEDIATE.requiredWins,
                currentWins = userWins,
                modelInfo = "Qwen 2.5 3B",
                onClick = {
                    if (intermediateUnlocked) {
                        android.util.Log.d("AIPracticeMode", "🎮 Intermediate clicked")
                        checkModelAndStart(GameMode.AI_INTERMEDIATE)
                    } else {
                        showUnlockDialog = DifficultyLevel.INTERMEDIATE
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Speaker Mode (Advanced) - ALWAYS UNLOCKED (requiredWins = 0)
            val advancedUnlocked = DifficultyLevel.ADVANCED.isUnlocked(userWins)
            ElegantDifficultyCard(
                title = "Speaker",
                icon = Icons.Filled.Star,
                gradient = listOf(CopperShine, AmberAccent, Color(0xFFFF9500)),
                description = "Master debater challenge",
                subDescription = "Test your limits with complex arguments",
                difficulty = "HARD",
                isLocked = !advancedUnlocked,
                requiredWins = DifficultyLevel.ADVANCED.requiredWins,
                currentWins = userWins,
                modelInfo = "Qwen 2.5 3B",
                onClick = {
                    if (advancedUnlocked) {
                        android.util.Log.d("AIPracticeMode", "🎮 Advanced clicked")
                        checkModelAndStart(GameMode.AI_ADVANCED)
                    } else {
                        showUnlockDialog = DifficultyLevel.ADVANCED
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Card
            ElegantInfoCard()
        }

        // Unlock Dialog
        showUnlockDialog?.let { difficulty ->
            ElegantUnlockDialog(
                difficulty = difficulty,
                currentWins = userWins,
                onDismiss = { showUnlockDialog = null }
            )
        }
        
        // Model Download Dialog
        if (showModelDownloadDialog && pendingGameMode != null) {
            ModelDownloadDialog(
                gameMode = pendingGameMode!!,
                availableModels = availableModels,
                downloadProgress = downloadProgress,
                currentModelId = currentModelId,
                statusMessage = statusMessage,
                onDownload = { modelId ->
                    android.util.Log.d("AIPracticeMode", "📥 Starting download for $modelId")
                    debateViewModel.downloadModel(modelId)
                },
                onLoad = { modelId ->
                    android.util.Log.d("AIPracticeMode", "🔄 Loading model $modelId")
                    debateViewModel.loadModel(modelId)
                },
                onRefresh = {
                    android.util.Log.d("AIPracticeMode", "🔄 Refreshing models")
                    debateViewModel.refreshModels()
                },
                onCancel = {
                    android.util.Log.d("AIPracticeMode", "❌ User cancelled model dialog")
                    showModelDownloadDialog = false
                    pendingGameMode = null
                }
            )
        }
    }
}

@Composable
fun AnimatedRetrorixLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_rotation")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Rotating ring
        Canvas(
            modifier = Modifier
                .size(50.dp)
                .rotate(rotation)
        ) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        GoldPrimary,
                        GoldLight,
                        AmberAccent,
                        GoldPrimary
                    )
                ),
                radius = size.width / 2,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // Center text
        Text(
            text = "R",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = GoldPrimary
        )
    }
}

@Composable
fun AnimatedStatsBadge(userWins: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Card(
        modifier = Modifier
            .padding(top = 16.dp)
            .scale(scale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = GoldPrimary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSlate.copy(alpha = 0.8f)
        ),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Trophy",
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Total Wins: $userWins",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )
        }
    }
}

@Composable
fun ElegantDifficultyCard(
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
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 10.dp else 20.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "elevation"
    )
    
    val borderWidth by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isLocked) 160.dp else 210.dp)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isLocked) SilverGray.copy(alpha = 0.2f) else GoldPrimary.copy(alpha = 0.3f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = if (!isLocked && borderWidth > 0.dp) BorderStroke(
            width = borderWidth,
            color = DeepBlack.copy(alpha = 0.3f)
        ) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isLocked) {
                        Brush.horizontalGradient(
                            listOf(
                                SilverGray.copy(alpha = 0.3f),
                                SilverGray.copy(alpha = 0.2f)
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
                    color = Color.White.copy(alpha = if (isLocked) 0.03f else 0.15f),
                    radius = 120f,
                    center = Offset(size.width * 0.85f, size.height * 0.3f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = if (isLocked) 0.02f else 0.08f),
                    radius = 80f,
                    center = Offset(size.width * 0.15f, size.height * 0.7f)
                )
            }

            // Lock Overlay
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepBlack.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Locked",
                            tint = PearlWhite.copy(alpha = 0.7f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Win ${requiredWins - currentWins} more",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PearlWhite.copy(alpha = 0.9f),
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
                        color = DeepBlack.copy(alpha = if (isLocked) 0.3f else 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = difficulty,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PearlWhite.copy(alpha = if (isLocked) 0.6f else 1f),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                        text = title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isLocked) PearlWhite.copy(alpha = 0.5f) else DeepBlack,
                        letterSpacing = 0.5.sp
                    )
                    
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLocked) PearlWhite.copy(alpha = 0.4f) else DeepBlack.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (!isLocked) {
                        Text(
                            text = subDescription,
                            fontSize = 12.sp,
                            color = DeepBlack.copy(alpha = 0.7f),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        // Model Info Badge
                        Surface(
                            color = DeepBlack.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Build,
                                    contentDescription = "AI Model",
                                    tint = DeepBlack.copy(alpha = 0.8f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = modelInfo,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepBlack.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Icon
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isLocked) PearlWhite.copy(alpha = 0.5f) else DeepBlack.copy(alpha = 0.9f),
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

@Composable
fun ElegantInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = GoldPrimary.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSlate.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
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
                tint = GoldPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Progressive Unlock System",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PearlWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Win matches to unlock higher difficulty levels. The AI evolves as you improve!",
                    fontSize = 13.sp,
                    color = SilverGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ElegantUnlockDialog(
    difficulty: DifficultyLevel,
    currentWins: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = GoldPrimary.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSlate
            ),
            border = BorderStroke(2.dp, GoldPrimary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SilverGray
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = GoldPrimary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "🔒 ${difficulty.displayName.uppercase()} LOCKED",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PearlWhite,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = difficulty.description,
                    fontSize = 14.sp,
                    color = SilverGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Progress: $currentWins / ${difficulty.requiredWins} wins",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(8.dp)
                        .background(SilverGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(currentWins.toFloat() / difficulty.requiredWins.toFloat())
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GoldDark, GoldPrimary, GoldLight)
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Win ${difficulty.requiredWins - currentWins} more ${if (difficulty.requiredWins - currentWins == 1) "match" else "matches"} to unlock!",
                    fontSize = 14.sp,
                    color = SoftWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        GoldDark,
                                        GoldPrimary,
                                        GoldLight
                                    )
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GOT IT!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBlack,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}

// ==================== ANIMATED BACKGROUND ====================
// AnimatedLuxuryBackground and ShimmerText are imported from AuthScreen.kt

// ==================== MODEL DOWNLOAD DIALOG ===================
@Composable
fun ModelDownloadDialog(
    gameMode: GameMode,
    availableModels: List<com.runanywhere.sdk.models.ModelInfo>,
    downloadProgress: Float?,
    currentModelId: String?,
    statusMessage: String,
    onDownload: (String) -> Unit,
    onLoad: (String) -> Unit,
    onRefresh: () -> Unit,
    onCancel: () -> Unit
) {
    // Determine required model name
    val requiredModelName = when (gameMode) {
        GameMode.AI_BEGINNER -> "Qwen 2.5 3B Instruct Q6_K"
        GameMode.AI_INTERMEDIATE, GameMode.AI_ADVANCED -> "Qwen 2.5 3B Instruct Q6_K"
        else -> ""
    }
    
    val requiredModel = availableModels.find { it.name == requiredModelName }
    val isModelReady = requiredModel != null && requiredModel.isDownloaded && currentModelId == requiredModel.id
    val isDownloading = downloadProgress != null
    
    // Pulse animation for the dialog
    val infiniteTransition = rememberInfiniteTransition(label = "dialog_pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_pulse"
    )

    Dialog(
        onDismissRequest = { if (isModelReady) onCancel() },
        properties = DialogProperties(
            dismissOnBackPress = isModelReady,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepBlack.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            // Animated background circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldPrimary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.3f, size.height * 0.3f),
                        radius = 300f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AmberAccent.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.7f, size.height * 0.7f),
                        radius = 400f
                    )
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.75f)
                    .shadow(
                        elevation = 32.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = GoldPrimary.copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RichBlack
                ),
                border = BorderStroke(2.dp, GoldPrimary.copy(alpha = borderAlpha))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close button (only visible when model is ready)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isModelReady) {
                            IconButton(
                                onClick = onCancel,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = DarkSlate.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Icon with rotating ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(100.dp)
                    ) {
                        // Rotating ring
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "ring_rotation"
                        )
                        
                        Canvas(
                            modifier = Modifier
                                .size(100.dp)
                                .rotate(rotation)
                        ) {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        GoldPrimary,
                                        AmberAccent,
                                        GoldLight,
                                        GoldPrimary
                                    )
                                ),
                                radius = size.width / 2,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                        
                        Icon(
                            imageVector = if (isModelReady) Icons.Default.CheckCircle else Icons.Filled.Build,
                            contentDescription = "Model Status",
                            tint = if (isModelReady) SuccessGreen else GoldPrimary,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title
                    Text(
                        text = if (isModelReady) "🎉 MODEL READY!" else "🤖 AI MODEL REQUIRED",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isModelReady) SuccessGreen else GoldPrimary,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    Text(
                        text = if (isModelReady) 
                            "The AI model is loaded and ready! You can now start your debate." 
                        else 
                            "To debate with AI, you need to download the required model first.",
                        fontSize = 15.sp,
                        color = SilverGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Model Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSlate.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Model",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Required Model",
                                        fontSize = 13.sp,
                                        color = SilverGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = requiredModelName,
                                        fontSize = 16.sp,
                                        color = PearlWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (requiredModel != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Status indicator
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = when {
                                                    isModelReady -> SuccessGreen
                                                    requiredModel.isDownloaded -> AmberAccent
                                                    else -> ErrorRose
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            isModelReady -> "✓ Downloaded & Loaded"
                                            requiredModel.isDownloaded -> "✓ Downloaded (needs loading)"
                                            isDownloading -> "Downloading..."
                                            else -> "Not downloaded"
                                        },
                                        fontSize = 14.sp,
                                        color = when {
                                            isModelReady -> SuccessGreen
                                            requiredModel.isDownloaded -> AmberAccent
                                            else -> ErrorRose
                                        },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Download progress
                                if (isDownloading) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    LinearProgressIndicator(
                                        progress = { downloadProgress ?: 0f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = GoldPrimary,
                                        trackColor = DarkSlate,
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "${((downloadProgress ?: 0f) * 100).toInt()}% complete",
                                        fontSize = 13.sp,
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "⚠️ Model not found. Please refresh.",
                                    fontSize = 14.sp,
                                    color = ErrorRose
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Status Message
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = DarkSlate.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = SilverGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = statusMessage,
                                fontSize = 13.sp,
                                color = SilverGray,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Action Buttons
                    if (requiredModel != null) {
                        when {
                            isModelReady -> {
                                // Start Debate Button
                                Button(
                                    onClick = onCancel,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        SuccessGreen,
                                                        EmeraldAccent
                                                    )
                                                ),
                                                shape = RoundedCornerShape(28.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Start",
                                                tint = DeepBlack,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "START DEBATE",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepBlack,
                                                letterSpacing = 2.sp
                                            )
                                        }
                                    }
                                }
                            }
                            requiredModel.isDownloaded -> {
                                // Load Model Button
                                Button(
                                    onClick = { onLoad(requiredModel.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    enabled = !isDownloading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        GoldDark,
                                                        GoldPrimary,
                                                        GoldLight
                                                    )
                                                ),
                                                shape = RoundedCornerShape(28.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Load",
                                                tint = DeepBlack,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "LOAD MODEL",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepBlack,
                                                letterSpacing = 2.sp
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
                                // Download Model Button
                                Button(
                                    onClick = { onDownload(requiredModel.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    enabled = !isDownloading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = if (isDownloading)
                                                        listOf(SilverGray, SilverGray)
                                                    else
                                                        listOf(
                                                            GoldDark,
                                                            GoldPrimary,
                                                            GoldLight
                                                        )
                                                ),
                                                shape = RoundedCornerShape(28.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDownloading) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = DeepBlack,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "DOWNLOADING...",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepBlack,
                                                    letterSpacing = 2.sp
                                                )
                                            }
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Build,
                                                    contentDescription = "Download",
                                                    tint = DeepBlack,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "DOWNLOAD MODEL",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepBlack,
                                                    letterSpacing = 2.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Refresh Button
                    OutlinedButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isDownloading,
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(2.dp, GoldPrimary.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GoldPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REFRESH MODELS",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cancel Button (only when not downloading)
                    if (!isDownloading) {
                        TextButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 15.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== PREVIEW ====================
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AIPracticeModeScreenPreview() {
    AIPracticeModeScreen(
        userWins = 1,
        onDifficultySelected = {},
        onBack = {}
    )
}
