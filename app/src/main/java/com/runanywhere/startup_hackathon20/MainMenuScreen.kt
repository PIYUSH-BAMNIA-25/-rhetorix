package com.runanywhere.startup_hackathon20

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
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
private val RedLoss = Color(0xFFFF6B6B)
private val YellowDraw = Color(0xFFFBBF24)

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
    val draws: Int = 0,
    val averageScore: Double = 0.0,
    val likes: Int = 0,
    val playerId: String = "RX${(10000..99999).random()}"
)

@Composable
fun MainMenuScreen(
    userProfile: UserProfile,
    onModeSelected: (GameMode) -> Unit,
    onLogout: () -> Unit,
    onDebug: () -> Unit = {}
) {
    var currentPage by remember { mutableStateOf(BottomNavPage.HOME) }
    var settingsPage by remember { mutableStateOf(SettingsSubPage.MAIN) }
    val userStats = remember { UserStats(wins = 15, losses = 8, draws = 3, averageScore = 87.5, likes = 142) }

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
        MainMenuAnimatedBackground()

        // Main Content with page transitions
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) + 
                slideInHorizontally(animationSpec = tween(300)) { it / 3 } togetherWith
                fadeOut(animationSpec = tween(300)) +
                slideOutHorizontally(animationSpec = tween(300)) { -it / 3 }
            },
            label = "page_transition"
        ) { page ->
            when (page) {
                BottomNavPage.HOME -> HomeScreen(
                    userProfile = userProfile,
                    userStats = userStats,
                    onModeSelected = onModeSelected
                )
                BottomNavPage.PROFILE -> ProfileScreen(
                    userProfile = userProfile,
                    userStats = userStats,
                    onLogout = onLogout,
                    onDebug = onDebug,
                    settingsPage = settingsPage,
                    onSettingsPageChange = { settingsPage = it }
                )
            }
        }

        // Glassmorphism Bottom Navigation Bar
        GlassmorphismBottomNav(
            currentPage = currentPage,
            onPageSelected = { currentPage = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ==================== HOME SCREEN ====================
@Composable
fun HomeScreen(
    userProfile: UserProfile,
    userStats: UserStats,
    onModeSelected: (GameMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Retrorix",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyanPrimary
                )
                Text(
                    text = "Welcome back, ${userProfile.name.split(" ").firstOrNull() ?: "Player"}!",
                    fontSize = 16.sp,
                    color = TextGray
                )
            }
        }

        // Stats Overview Card
        StatsOverviewCard(userStats)

        Spacer(modifier = Modifier.height(48.dp))

        // Game Modes
        Text(
            text = "Choose Your Battle",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextWhite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Practice VS AI Button
        GameModeCard(
            title = "Practice VS AI",
            icon = Icons.Filled.Phone,
            gradient = listOf(CyanPrimary, CyanLight),
            description = "Train with AI opponents",
            onClick = { onModeSelected(GameMode.AI_INTERMEDIATE) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // P2P Mode Button
        GameModeCard(
            title = "P2P Mode",
            icon = Icons.Filled.AccountCircle,
            gradient = listOf(PurpleAccent, Color(0xFFD946EF)),
            description = "Challenge real players",
            onClick = { onModeSelected(GameMode.PVP) }
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Quick Stats Section
        Text(
            text = "Recent Activity",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        RecentMatchesCard()
    }
}

@Composable
fun StatsOverviewCard(stats: UserStats) {
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
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("W: ${stats.wins}", GreenWin)
            StatItem("L: ${stats.losses}", RedLoss)
            StatItem("D: ${stats.draws}", YellowDraw)
        }
    }
}

@Composable
fun StatItem(text: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        // Progress bar below stat
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .background(color.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun GameModeCard(
    title: String,
    icon: ImageVector,
    gradient: List<Color>,
    description: String,
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
                    center = Offset(size.width * 0.8f, size.height * 0.3f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = 60f,
                    center = Offset(size.width * 0.2f, size.height * 0.7f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = description,
                        fontSize = 16.sp,
                        color = TextWhite.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = TextWhite,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
    }
}

@Composable
fun RecentMatchesCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            listOf(
                Triple("AI Advanced", "Won", GreenWin),
                Triple("Player_X42", "Lost", RedLoss),
                Triple("AI Intermediate", "Won", GreenWin)
            ).forEach { (opponent, result, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "vs $opponent",
                        fontSize = 15.sp,
                        color = TextGray
                    )
                    Text(
                        text = result,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                if (opponent != "AI Intermediate") {
                    Divider(
                        color = TextGray.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ==================== PROFILE SCREEN ====================
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    userStats: UserStats,
    onLogout: () -> Unit,
    onDebug: () -> Unit,
    settingsPage: SettingsSubPage,
    onSettingsPageChange: (SettingsSubPage) -> Unit
) {
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
                Text(
                    text = "Profile",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )

                // Profile Avatar with gradient border
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(140.dp)) {
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors = listOf(CyanPrimary, PurpleAccent, CyanLight)
                            ),
                            radius = size.width / 2,
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        CyanPrimary.copy(alpha = 0.3f),
                                        PurpleAccent.copy(alpha = 0.3f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = TextWhite,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Name and Email
                Text(
                    text = userProfile.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = userProfile.email,
                    fontSize = 16.sp,
                    color = TextGray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Player ID
                Card(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkCard.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = "ID: ${userStats.playerId}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Stats Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkCard.copy(alpha = 0.7f)
                    )
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Statistics",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        // Grid of stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatItem("Wins", userStats.wins.toString(), GreenWin)
                            ProfileStatItem("Losses", userStats.losses.toString(), RedLoss)
                            ProfileStatItem("Draws", userStats.draws.toString(), YellowDraw)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatItem("Avg Score", "${userStats.averageScore}%", CyanPrimary)
                            ProfileStatItem("Likes", userStats.likes.toString(), PurpleAccent)
                            ProfileStatItem(
                                "Total",
                                "${userStats.wins + userStats.losses + userStats.draws}",
                                TextWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Match History
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkCard.copy(alpha = 0.7f)
                    )
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Match History",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        listOf(
                            MatchHistoryItem("AI Advanced", "Won", "95%", GreenWin),
                            MatchHistoryItem("Player_X42", "Lost", "72%", RedLoss),
                            MatchHistoryItem("AI Intermediate", "Won", "88%", GreenWin),
                            MatchHistoryItem("Player_Debate", "Draw", "80%", YellowDraw)
                        ).forEach { item ->
                            MatchHistoryRow(item)
                            if (item != MatchHistoryItem(
                                    "Player_Debate",
                                    "Draw",
                                    "80%",
                                    YellowDraw
                                )
                            ) {
                                Divider(
                                    color = TextGray.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Settings Section
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Account Section
                Text(
                    text = "Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                SettingsCard {
                    SettingsItem(
                        icon = Icons.Filled.Lock,
                        title = "Change Password",
                        onClick = { onSettingsPageChange(SettingsSubPage.CHANGE_PASSWORD) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logout Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedLoss
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Logout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        SettingsSubPage.CHANGE_PASSWORD -> {
            ChangePasswordScreen(
                onBack = { onSettingsPageChange(SettingsSubPage.MAIN) }
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

data class MatchHistoryItem(
    val opponent: String,
    val result: String,
    val score: String,
    val color: Color
)

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

// ==================== SETTINGS SCREEN ====================
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp, bottom = 100.dp)
    ) {
        // Back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
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
                text = "Change Password",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Current Password Field
        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Current Password", color = TextGray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // New Password Field
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password", color = TextGray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password", color = TextGray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Save Button
        Button(
            onClick = { onBack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = DarkBackground
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Save Changes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
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

// ==================== GLASSMORPHISM BOTTOM NAV ====================
@Composable
fun GlassmorphismBottomNav(
    currentPage: BottomNavPage,
    onPageSelected: (BottomNavPage) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .shadow(16.dp, RoundedCornerShape(35.dp))
                .blur(5.dp),
            shape = RoundedCornerShape(35.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkCard.copy(alpha = 0.8f)
            )
        ) {
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

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val color by animateColorAsState(
        targetValue = if (isSelected) CyanPrimary else TextGray,
        animationSpec = tween(300),
        label = "color"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clickable { onClick() }
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

// ==================== ANIMATED BACKGROUND ====================
@Composable
fun MainMenuAnimatedBackground() {
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
            
            val x = size.width / 2 + cos(Math.toRadians(time.toDouble())).toFloat() * radius
            val y = size.height / 2 + sin(Math.toRadians(time.toDouble())).toFloat() * radius
            
            drawCircle(
                color = if (index % 2 == 0) PurpleAccent.copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.15f),
                radius = (10 + index * 3).toFloat(),
                center = Offset(x, y)
            )
        }
    }
}

// ==================== PREVIEWS ====================
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainMenuScreenPreview() {
    MainMenuScreen(
        userProfile = UserProfile(
            name = "John Debater",
            email = "john@rhetorix.com",
            dateOfBirth = "15/08/1995"
        ),
        onModeSelected = {},
        onLogout = {},
        onDebug = {}
    )
}
