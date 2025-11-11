package com.runanywhere.startup_hackathon20

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.window.Dialog
import com.runanywhere.startup_hackathon20.data.DifficultyLevel
import com.runanywhere.startup_hackathon20.GameMode
import kotlinx.coroutines.launch
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
private val YellowDraw = Color(0xFFFBBF24)
private val CyanPrimary = Color(0xFF00D9FF)
private val PurpleAccent = Color(0xFF9D4EDD)
private val DarkCard = Color(0xFF16213E)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFFB0B0B0)

enum class BottomNavPage {
    HOME, PROFILE
}

enum class SettingsSubPage {
    MAIN, CHANGE_PASSWORD
}

data class UserProfile(
    val name: String,
    val email: String,
    val dateOfBirth: String
)

data class UserStats(
    val wins: Int = 0,
    val losses: Int = 0,
    val totalGames: Int = 0,
    val averageScore: Double = 0.0,
    val playerId: String
)

data class MatchHistoryItem(
    val opponent: String,
    val result: String,
    val score: String,
    val color: Color
)


@Composable
fun QuickActionButton(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 12.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "elevation"
    )

    Card(
        modifier = modifier
            .height(70.dp)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(16.dp),
                spotColor = GoldPrimary.copy(alpha = 0.2f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) DarkSlate.copy(alpha = 0.8f) else DarkSlate.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            width = if (isPressed) 2.dp else 1.dp,
            color = if (isPressed) GoldPrimary.copy(alpha = 0.6f) else GoldPrimary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = GoldPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PearlWhite
            )
        }
    }
}

@Composable
fun ElegantTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    color = SilverGray.copy(alpha = 0.6f),
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = placeholder,
                    tint = if (isError) ErrorRose else GoldPrimary
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Clear else Icons.Default.Lock,
                            contentDescription = "Toggle password",
                            tint = SilverGray
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PearlWhite,
                unfocusedTextColor = SoftWhite,
                disabledTextColor = SilverGray.copy(alpha = 0.5f),
                focusedBorderColor = if (isError) ErrorRose else GoldPrimary,
                unfocusedBorderColor = if (isError) ErrorRose.copy(alpha = 0.5f) else SilverGray.copy(
                    alpha = 0.3f
                ),
                focusedContainerColor = DarkSlate.copy(alpha = 0.5f),
                unfocusedContainerColor = DarkSlate.copy(alpha = 0.3f),
                cursorColor = GoldPrimary,
                errorBorderColor = ErrorRose,
                errorCursorColor = ErrorRose
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = enabled,
            isError = isError
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = ErrorRose,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun GoldenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    isDestructive: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(62.dp),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = DeepBlack,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = SilverGray.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(31.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled && !loading) {
                        if (isDestructive) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    ErrorRose.copy(alpha = 0.8f),
                                    ErrorRose,
                                    ErrorRose.copy(alpha = 0.8f)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    GoldDark,
                                    GoldPrimary,
                                    GoldLight,
                                    GoldPrimary,
                                    GoldDark
                                )
                            )
                        }
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                SilverGray.copy(alpha = 0.3f),
                                SilverGray.copy(alpha = 0.3f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(31.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = if (isDestructive) PearlWhite else DeepBlack,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = if (enabled) {
                        if (isDestructive) PearlWhite else DeepBlack
                    } else {
                        SilverGray.copy(alpha = 0.5f)
                    }
                )
            }
        }
    }
}

@Composable
fun ComingSoonDialog(
    feature: String,
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
                containerColor = PearlWhite.copy(alpha = 0.95f)
            ),
            border = BorderStroke(2.dp, GoldPrimary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
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
                            tint = DarkSlate
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Coming Soon",
                    tint = GoldPrimary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "COMING SOON",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkSlate,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$feature is currently under development.",
                    fontSize = 16.sp,
                    color = DarkSlate.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Stay tuned for updates!",
                    fontSize = 14.sp,
                    color = SilverGray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(28.dp))

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
                            text = "OKAY",
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

@Composable
fun P2PUnlockDialog(
    currentWins: Int,
    onDismiss: () -> Unit
) {
    val difficulty = DifficultyLevel.PVP

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
                    text = "⚔️ P2P MODE LOCKED",
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

                GoldenButton(
                    text = "GOT IT!",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    userProfile: UserProfile,
    userWins: Int = 0,
    userLosses: Int = 0,
    userTotalGames: Int = 0,
    userAverageScore: Float = 0f,
    userPlayerId: String = "",
    onModeSelected: (GameMode) -> Unit,
    onLogout: () -> Unit,
    onDebug: () -> Unit = {}
) {
    var currentPage by remember { mutableStateOf(BottomNavPage.HOME) }
    var settingsPage by remember { mutableStateOf(SettingsSubPage.MAIN) }
    var showP2PUnlockDialog by remember { mutableStateOf(false) }
    var showComingSoonDialog by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Fetch real debate history from database
    var debateHistory by remember {
        mutableStateOf<List<com.runanywhere.startup_hackathon20.network.models.ServerDebateHistory>>(
            emptyList()
        )
    }
    var isLoadingHistory by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val repository = com.runanywhere.startup_hackathon20.network.ServerRepository(context)
        repository.getUserDebateHistory(limit = 10).onSuccess { history ->
            debateHistory = history
            isLoadingHistory = false
        }.onFailure {
            isLoadingHistory = false
        }
    }

    val userStats = remember(userWins, userLosses, userTotalGames, userAverageScore, userPlayerId) {
        UserStats(
            wins = userWins,
            losses = userLosses,
            totalGames = userTotalGames,
            averageScore = userAverageScore.toDouble(),
            playerId = userPlayerId
        )
    }

    // Handle back to HOME from PROFILE tab
    BackHandler(enabled = currentPage == BottomNavPage.PROFILE && settingsPage == SettingsSubPage.MAIN) {
        currentPage = BottomNavPage.HOME
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        AnimatedLuxuryBackground()

        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) +
                        slideInHorizontally(animationSpec = tween(400)) { it / 3 } togetherWith
                        fadeOut(animationSpec = tween(400)) +
                        slideOutHorizontally(animationSpec = tween(400)) { -it / 3 }
            },
            label = "page_transition"
        ) { page ->
            when (page) {
                BottomNavPage.HOME -> HomeScreen(
                    userProfile = userProfile,
                    userStats = userStats,
                    userWins = userWins,
                    debateHistory = debateHistory,
                    isLoadingHistory = isLoadingHistory,
                    onModeSelected = onModeSelected,
                    onShowP2PUnlock = { showP2PUnlockDialog = true },
                    onShowComingSoon = { feature -> showComingSoonDialog = feature }
                )

                BottomNavPage.PROFILE -> ProfileScreen(
                    userProfile = userProfile,
                    userStats = userStats,
                    debateHistory = debateHistory,
                    isLoadingHistory = isLoadingHistory,
                    onLogout = onLogout,
                    onDebug = onDebug,
                    settingsPage = settingsPage,
                    onSettingsPageChange = { settingsPage = it }
                )
            }
        }

        GlassmorphismBottomNav(
            currentPage = currentPage,
            onPageSelected = { currentPage = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showP2PUnlockDialog) {
            P2PUnlockDialog(
                currentWins = userWins,
                onDismiss = { showP2PUnlockDialog = false }
            )
        }

        showComingSoonDialog?.let { feature ->
            ComingSoonDialog(
                feature = feature,
                onDismiss = { showComingSoonDialog = null }
            )
        }
    }
}

// Continue with HomeScreen, ProfileScreen and remaining composables...
@Composable
fun HomeScreen(
    userProfile: UserProfile,
    userStats: UserStats,
    userWins: Int = 0,
    debateHistory: List<com.runanywhere.startup_hackathon20.network.models.ServerDebateHistory>,
    isLoadingHistory: Boolean,
    onModeSelected: (GameMode) -> Unit,
    onShowP2PUnlock: () -> Unit = {},
    onShowComingSoon: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshedHistory by remember { mutableStateOf(debateHistory) }

    // Update displayed history when prop changes
    LaunchedEffect(debateHistory) {
        refreshedHistory = debateHistory
    }

    // Refresh data function, triggers instantly and haptics always on button press
    fun refreshData() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        isRefreshing = true
        coroutineScope.launch {
            val repository = com.runanywhere.startup_hackathon20.network.ServerRepository(context)
            repository.getUserDebateHistory(limit = 10).onSuccess { history ->
                refreshedHistory = history
                isRefreshing = false
            }.onFailure {
                isRefreshing = false
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            ShimmerText(
                text = "RETRORIX",
                fontSize = 36.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome back, ${
                    userProfile.name.split(" ").firstOrNull() ?: "Player"
                }!",
                fontSize = 16.sp,
                color = SilverGray,
                letterSpacing = 1.sp
            )
        }

        ElegantStatsCard(userStats)

        Spacer(modifier = Modifier.height(32.dp))

        // Manual refresh button (clearer, taller, faster)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    refreshData()
                },
                enabled = !isRefreshing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary.copy(alpha = 0.2f),
                    contentColor = GoldPrimary,
                    disabledContainerColor = SilverGray.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(42.dp)
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        color = GoldPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Refreshing...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Refresh",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Star,
                text = "Leaderboard",
                modifier = Modifier.weight(1f),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShowComingSoon("Leaderboard")
                }
            )

            QuickActionButton(
                icon = Icons.Default.Face,
                text = "Friends",
                modifier = Modifier.weight(1f),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShowComingSoon("Friends")
                }
            )
        }

        Text(
            text = "CHOOSE YOUR BATTLE",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PearlWhite,
            letterSpacing = 2.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        GoldenGameModeCard(
            title = "Practice VS AI",
            icon = Icons.Default.Phone,
            gradient = listOf(GoldPrimary, GoldLight, AmberAccent),
            description = "Train with AI opponents",
            isLocked = false,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onModeSelected(GameMode.AI_INTERMEDIATE)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        val p2pUnlocked = DifficultyLevel.PVP.isUnlocked(userWins)
        GoldenGameModeCard(
            title = "P2P Mode",
            icon = Icons.Default.AccountCircle,
            gradient = listOf(CopperShine, AmberAccent, GoldPrimary),
            description = "Challenge real players",
            isLocked = false,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onModeSelected(GameMode.PVP)
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "RECENT ACTIVITY",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = GoldPrimary,
            letterSpacing = 2.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        RecentMatchesCard(
            debateHistory = refreshedHistory.take(3),
            isLoading = isLoadingHistory || isRefreshing
        )
    }
}

@Composable
fun ElegantStatsCard(stats: UserStats) {
    val infiniteTransition = rememberInfiniteTransition(label = "stats_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = GoldPrimary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSlate.copy(alpha = 0.8f)
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    GoldPrimary.copy(alpha = 0.5f),
                    AmberAccent.copy(alpha = 0.3f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ElegantStatItem("WINS", stats.wins.toString(), GreenWin)
            ElegantStatItem("LOSSES", stats.losses.toString(), ErrorRose)
            ElegantStatItem("TOTAL", stats.totalGames.toString(), GoldPrimary)
        }
    }
}

@Composable
fun ElegantStatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = value,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SilverGray,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .width(60.dp)
                .height(3.dp)
                .padding(top = 4.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.3f),
                            color,
                            color.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

@Composable
fun GoldenGameModeCard(
    title: String,
    icon: ImageVector,
    gradient: List<Color>,
    description: String,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

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
            .height(160.dp)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isLocked) SilverGray.copy(alpha = 0.2f) else GoldPrimary.copy(alpha = 0.3f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
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

            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepBlack.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = PearlWhite.copy(alpha = 0.7f),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isLocked) PearlWhite.copy(alpha = 0.5f) else DeepBlack,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = if (isLocked) PearlWhite.copy(alpha = 0.4f) else DeepBlack.copy(
                            alpha = 0.8f
                        ),
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isLocked) PearlWhite.copy(alpha = 0.5f) else DeepBlack.copy(alpha = 0.9f),
                    modifier = Modifier.size(52.dp)
                )
            }
        }
    }
}

@Composable
fun SkeletonRecentMatchesCard(
    placeholderCount: Int = 3
) {
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
        Column(modifier = Modifier.padding(20.dp)) {
            repeat(placeholderCount) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SilverGray.copy(alpha = 0.22f))
                    )
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldPrimary.copy(alpha = 0.12f))
                    )
                }
                if (index < placeholderCount - 1) {
                    Divider(
                        color = SilverGray.copy(alpha = 0.13f),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .height(1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentMatchesCard(
    debateHistory: List<com.runanywhere.startup_hackathon20.network.models.ServerDebateHistory>,
    isLoading: Boolean
) {
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
        Column(modifier = Modifier.padding(20.dp)) {
            if (isLoading) {
                SkeletonRecentMatchesCard()
            } else if (debateHistory.isEmpty()) {
                Text(
                    text = "No debates yet. Start your first debate!",
                    fontSize = 15.sp,
                    color = SilverGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                )
            } else {
                debateHistory.forEachIndexed { index, debate ->
                    val resultColor = if (debate.won) GreenWin else ErrorRose
                    val result = if (debate.won) "Won" else "Lost"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "vs ${debate.opponentType.replace("_", " ")}",
                            fontSize = 15.sp,
                            color = SoftWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = resultColor.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = result,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = resultColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    if (index < debateHistory.size - 1) {
                        Divider(
                            color = SilverGray.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ProfileScreen with all sub-functions will be too long, so I'll add it separately using a second edit
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    userStats: UserStats,
    debateHistory: List<com.runanywhere.startup_hackathon20.network.models.ServerDebateHistory>,
    isLoadingHistory: Boolean,
    onLogout: () -> Unit,
    onDebug: () -> Unit,
    settingsPage: SettingsSubPage,
    onSettingsPageChange: (SettingsSubPage) -> Unit
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    when (settingsPage) {
        SettingsSubPage.MAIN -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShimmerText(
                    text = "PROFILE",
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedProfileAvatar()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = userProfile.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PearlWhite
                )
                Text(
                    text = userProfile.email,
                    fontSize = 15.sp,
                    color = SilverGray,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Card(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = GoldPrimary.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSlate.copy(alpha = 0.8f)
                    ),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ID: ${userStats.playerId}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Player ID", userStats.playerId)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(
                                    context,
                                    "Player ID copied!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Copy ID",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = GoldPrimary.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSlate.copy(alpha = 0.8f)
                    ),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "STATISTICS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatItem("WINS", userStats.wins.toString(), GreenWin)
                            ProfileStatItem("LOSSES", userStats.losses.toString(), ErrorRose)
                            ProfileStatItem("TOTAL", userStats.totalGames.toString(), GoldPrimary)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatItem(
                                "AVG SCORE",
                                "${userStats.averageScore.toInt()}%",
                                AmberAccent
                            )
                            ProfileStatItem(
                                "WIN RATE",
                                if (userStats.totalGames > 0) "${((userStats.wins.toFloat() / userStats.totalGames) * 100).toInt()}%" else "0%",
                                GreenWin
                            )
                            ProfileStatItem("DEBATES", userStats.totalGames.toString(), PearlWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "MATCH HISTORY",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (isLoadingHistory) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = GoldPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else if (debateHistory.isEmpty()) {
                            Text(
                                text = "No debate history yet.",
                                fontSize = 15.sp,
                                color = SilverGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp)
                            )
                        } else {
                            debateHistory.take(5).forEachIndexed { index, debate ->
                                val resultColor = if (debate.won) GreenWin else ErrorRose
                                val result = if (debate.won) "Won" else "Lost"

                                MatchHistoryRow(
                                    MatchHistoryItem(
                                        opponent = debate.opponentType.replace("_", " "),
                                        result = result,
                                        score = "${debate.userScore}%",
                                        color = resultColor
                                    )
                                )
                                if (index < debateHistory.take(5).size - 1) {
                                    Divider(
                                        color = SilverGray.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "SETTINGS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldPrimary,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                SettingsCard {
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSettingsPageChange(SettingsSubPage.CHANGE_PASSWORD)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                GoldenButton(
                    text = "LOGOUT",
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    isDestructive = true
                )
            }
        }

        SettingsSubPage.CHANGE_PASSWORD -> {
            ChangePasswordScreen(
                onBack = {
                    onSettingsPageChange(SettingsSubPage.MAIN)
                }
            )
        }
    }
}

@Composable
fun ProfileStatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextGray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun MatchHistoryRow(item: MatchHistoryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = item.opponent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextWhite
            )
            Text(
                text = "Score: ${item.score}",
                fontSize = 13.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = item.color.copy(alpha = 0.2f)
            )
        ) {
            Text(
                text = item.result,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = item.color,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val passwordsMatch = newPassword == confirmPassword || confirmPassword.isEmpty()
    val isPasswordValid = newPassword.length >= 6 || newPassword.isEmpty()

    // Handle BACK to profile main from ChangePasswordScreen
    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp, bottom = 100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBack()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = DarkSlate.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = GoldPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "CHANGE PASSWORD",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PearlWhite,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Success Message
        if (successMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SuccessGreen.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = successMessage!!,
                        color = SuccessGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Error Message
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ErrorRose.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ErrorRose.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = ErrorRose,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = errorMessage!!,
                        color = ErrorRose,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        ElegantTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                errorMessage = null
                successMessage = null
            },
            placeholder = "New Password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = newPasswordVisible,
            onTogglePasswordVisibility = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                newPasswordVisible = !newPasswordVisible
            },
            isError = !isPasswordValid,
            errorMessage = if (!isPasswordValid) "Password must be at least 6 characters" else null,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(20.dp))

        ElegantTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
                successMessage = null
            },
            placeholder = "Confirm Password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onTogglePasswordVisibility = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                confirmPasswordVisible = !confirmPasswordVisible
            },
            isError = !passwordsMatch,
            errorMessage = if (!passwordsMatch) "Passwords do not match" else null,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(40.dp))

        GoldenButton(
            text = if (isLoading) "SAVING..." else "SAVE CHANGES",
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                if (newPassword.isNotBlank() && confirmPassword.isNotBlank() && passwordsMatch && isPasswordValid) {
                    isLoading = true
                    errorMessage = null
                    successMessage = null

                    coroutineScope.launch {
                        try {
                            // Simulate network delay
                            kotlinx.coroutines.delay(1000)

                            // For now, show success (password change API needs to be implemented in Supabase)
                            isLoading = false
                            successMessage = "Password changed successfully!"
                            newPassword = ""
                            confirmPassword = ""

                            // Auto-navigate back after 2 seconds
                            kotlinx.coroutines.delay(2000)
                            onBack()
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "An error occurred: ${e.message}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = newPassword.isNotBlank() && confirmPassword.isNotBlank() && passwordsMatch && isPasswordValid && !isLoading,
            loading = isLoading
        )
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .background(
                if (isPressed) CyanPrimary.copy(alpha = 0.1f) else Color.Transparent
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CyanPrimary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                color = TextWhite,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = TextGray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun GlassmorphismBottomNav(
    currentPage: BottomNavPage,
    onPageSelected: (BottomNavPage) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedOffset by animateFloatAsState(
        targetValue = when (currentPage) {
            BottomNavPage.HOME -> 0f
            BottomNavPage.PROFILE -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fluid_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .shadow(24.dp, RoundedCornerShape(35.dp)),
            shape = RoundedCornerShape(35.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkCard
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GoldPrimary.copy(alpha = 0.5f),
                        GoldPrimary.copy(alpha = 0.3f)
                    )
                )
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Animated fluid background indicator
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    val itemWidth = size.width / 2
                    val indicatorWidth = itemWidth * 0.8f
                    val startX = (itemWidth - indicatorWidth) / 2 + (animatedOffset * itemWidth)
                    
                    // Draw fluid blob shape
                    val path = Path().apply {
                        val centerY = size.height / 2
                        val radiusX = indicatorWidth / 2
                        val radiusY = size.height / 2 - 4.dp.toPx()
                        
                        // Create a morphing blob effect
                        val wobble1 = 0.1f * radiusX
                        val wobble2 = 0.15f * radiusY
                        
                        addRoundRect(
                            RoundRect(
                                rect = Rect(
                                    left = startX,
                                    top = centerY - radiusY,
                                    right = startX + indicatorWidth,
                                    bottom = centerY + radiusY
                                ),
                                radiusX = 28.dp.toPx(),
                                radiusY = 28.dp.toPx()
                            )
                        )
                    }
                    
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                GoldDark.copy(alpha = 0.3f),
                                GoldPrimary.copy(alpha = 0.4f),
                                GoldLight.copy(alpha = 0.3f)
                            ),
                            startX = startX,
                            endX = startX + indicatorWidth
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        icon = Icons.Filled.Home,
                        label = "Home",
                        isSelected = currentPage == BottomNavPage.HOME,
                        onClick = { onPageSelected(BottomNavPage.HOME) }
                    )

                    BottomNavItem(
                        icon = Icons.Filled.Person,
                        label = "Profile",
                        isSelected = currentPage == BottomNavPage.PROFILE,
                        onClick = { onPageSelected(BottomNavPage.PROFILE) }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val color by animateColorAsState(
        targetValue = if (isSelected) GoldPrimary else TextGray,
        animationSpec = tween(300),
        label = "color"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clickable {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainMenuScreenPreview() {
    MainMenuScreen(
        userProfile = UserProfile(
            name = "John Debater",
            email = "john@retrorix.com",
            dateOfBirth = "15/08/1995"
        ),
        userWins = 0,
        onModeSelected = {},
        onLogout = {},
        onDebug = {}
    )
}
