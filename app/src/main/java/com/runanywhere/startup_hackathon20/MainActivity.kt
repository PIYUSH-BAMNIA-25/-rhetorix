package com.runanywhere.startup_hackathon20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon20.ui.theme.Startup_hackathon20Theme
import com.runanywhere.startup_hackathon20.AuthViewModel
import com.runanywhere.startup_hackathon20.DebateViewModel
import com.runanywhere.startup_hackathon20.UserProfile
import kotlinx.coroutines.launch
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Startup_hackathon20Theme {
                AppNavigation()
            }
        }
    }
}

// Navigation Screens
sealed class Screen {
    object Auth : Screen()
    object ModelSetup : Screen()
    object Home : Screen()
    object AIModeSelection : Screen()
    object DebatePreparation : Screen()
    object DebateActive : Screen()
    object DebateResults : Screen()
    object ChangePassword : Screen()

    // P2P Screens
    object Matchmaking : Screen()
    object P2PDebatePreparation : Screen()
    object P2PDebateActive : Screen()
    object P2PDebateResults : Screen()
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Auth) }
    var currentUser by remember { mutableStateOf<UserData?>(null) }
    var userId by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showDebateExitDialog by remember { mutableStateOf(false) }

    // P2P state
    var p2pSessionId by remember { mutableStateOf<String?>(null) }

    // Add loading state
    var isInitializing by remember { mutableStateOf(true) }

    // Initialize ViewModels
    val authViewModel: AuthViewModel = viewModel()
    val debateViewModel: DebateViewModel = viewModel()
    val matchmakingViewModel: MatchmakingViewModel = viewModel()
    val p2pViewModel: P2PDebateViewModel = viewModel()
    
    // Add coroutine scope
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    // Handle system back button
    BackHandler(enabled = true) {
        when (currentScreen) {
            Screen.Auth -> {
                // On auth screen, show exit dialog
                showExitDialog = true
            }

            Screen.Home -> {
                // On home screen, show exit dialog
                showExitDialog = true
            }

            Screen.ModelSetup -> {
                // Go back to home
                debateViewModel.resetModelDownloadFlag()
                currentScreen = Screen.Home
            }

            Screen.AIModeSelection -> {
                // Go back to home
                currentScreen = Screen.Home
            }

            Screen.DebatePreparation -> {
                // Ask for confirmation before leaving debate
                showDebateExitDialog = true
            }

            Screen.DebateActive -> {
                // Ask for confirmation before leaving active debate
                showDebateExitDialog = true
            }

            Screen.DebateResults -> {
                // Go back to home from results
                currentScreen = Screen.Home
            }

            Screen.ChangePassword -> {
                // Go back to home (profile page)
                currentScreen = Screen.Home
            }

            // P2P Screens
            Screen.Matchmaking -> {
                // Ask for confirmation before leaving matchmaking
                showDebateExitDialog = true
            }

            Screen.P2PDebatePreparation -> {
                // Ask for confirmation before leaving P2P prep
                showDebateExitDialog = true
            }

            Screen.P2PDebateActive -> {
                // Ask for confirmation before leaving P2P debate
                showDebateExitDialog = true
            }

            Screen.P2PDebateResults -> {
                // Go back to home from P2P results
                currentScreen = Screen.Home
            }
        }
    }

    // Observe auth state from AuthViewModel (handles server auto-login)
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                // User is logged in (from server)
                currentUser = state.user
                currentScreen = Screen.Home

                // Login user in DebateViewModel
                debateViewModel.loginUser(
                    userId = state.user.id.toLongOrNull() ?: 0L,
                    name = "${state.user.firstName} ${state.user.lastName}",
                    email = state.user.email,
                    dateOfBirth = state.user.dateOfBirth
                )
                userId = state.user.id
                isInitializing = false
                android.util.Log.d("MainActivity", "✅ User logged in: ${state.user.firstName}")
            }

            is AuthState.Idle -> {
                // No user logged in
                currentScreen = Screen.Auth
                isInitializing = false
                android.util.Log.d("MainActivity", "ℹ️ No logged in user, showing auth screen")
            }

            is AuthState.SignUpSuccess -> {
                // Sign up successful, stay on auth screen (will show login)
                currentScreen = Screen.Auth
                isInitializing = false
                android.util.Log.d("MainActivity", "✅ Sign up successful")
            }

            is AuthState.Error -> {
                // Error checking login
                currentScreen = Screen.Auth
                isInitializing = false
                android.util.Log.e("MainActivity", "❌ Auth error: ${state.message}")
            }

            is AuthState.Loading -> {
                // Still checking...
                isInitializing = true
            }
        }
    }

    // Auto-navigate to DebatePreparation when session is created
    val currentSession by debateViewModel.currentSession.collectAsState()
    LaunchedEffect(currentSession) {
        android.util.Log.d(
            "MainActivity",
            " LaunchedEffect triggered - Session: ${currentSession?.status}, CurrentScreen: $currentScreen"
        )
        currentSession?.let { session ->
            // If a new session was created and we're not already on a debate screen
            when {
                session.status == DebateStatus.PREP_TIME &&
                        (currentScreen == Screen.AIModeSelection || currentScreen == Screen.Home) -> {
                    // Just started a new debate from AI mode selection or Home (PVP)
                    android.util.Log.d("MainActivity", " Navigating to DebatePreparation")
                    currentScreen = Screen.DebatePreparation
                }
                session.status == DebateStatus.IN_PROGRESS && currentScreen == Screen.DebatePreparation -> {
                    // Prep time is over, move to active debate
                    android.util.Log.d("MainActivity", " Navigating to DebateActive")
                    currentScreen = Screen.DebateActive
                }

                session.status == DebateStatus.FINISHED && currentScreen == Screen.DebateActive -> {
                    // Debate finished, show results
                    android.util.Log.d("MainActivity", " Navigating to DebateResults")
                    currentScreen = Screen.DebateResults
                }
                else -> {
                    android.util.Log.d(
                        "MainActivity",
                        " No navigation - Status: ${session.status}, Screen: $currentScreen"
                    )
                }
            }
        }
    }

    // Auto-navigate to Model Setup if models need to be downloaded
    val needsModelDownload by debateViewModel.needsModelDownload.collectAsState()
    LaunchedEffect(needsModelDownload) {
        if (needsModelDownload) {
            android.util.Log.d("MainActivity", "⚠️ Model not downloaded, navigating to Model Setup")
            currentScreen = Screen.ModelSetup
            // Reset the flag
            debateViewModel.resetModelDownloadFlag()
        }
    }

    // Exit App Dialog
    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                activity?.finish()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }

    // Exit Debate Dialog
    if (showDebateExitDialog) {
        ExitDebateDialog(
            onConfirm = {
                showDebateExitDialog = false
                // Record forfeit before leaving
                debateViewModel.forfeitDebate()
                currentScreen = Screen.Home
            },
            onDismiss = {
                showDebateExitDialog = false
            }
        )
    }

    // Show loading screen while initializing
    if (isInitializing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0F)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF00D9FF))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading Retrorix...",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
        return
    }

    when (currentScreen) {
        // 1. AUTH SCREEN (Sign In / Login)
        Screen.Auth -> {
            AuthScreen(
                onAuthSuccess = { user ->
                    currentUser = user
                    // Skip ModelSetup, go directly to Home since models auto-register
                    currentScreen = Screen.Home
                },
                viewModel = authViewModel
            )
        }

        // 2. MODEL SETUP SCREEN
        Screen.ModelSetup -> {
            ModelSetupScreen(
                onSetupComplete = {
                    // Reset flag if it was set
                    debateViewModel.resetModelDownloadFlag()
                    currentScreen = Screen.Home
                },
                onBack = {
                    // Reset flag and go back to home
                    debateViewModel.resetModelDownloadFlag()
                    currentScreen = Screen.Home
                }
            )
        }

        // 3. HOME SCREEN (Home & Profile tabs)
        Screen.Home -> {
            currentUser?.let { user ->
                MainMenuScreen(
                    userProfile = UserProfile(
                        name = "${user.firstName} ${user.lastName}",
                        email = user.email,
                        dateOfBirth = user.dateOfBirth
                    ),
                    userWins = user.wins,
                    userLosses = user.losses,
                    userTotalGames = user.totalGames,
                    userAverageScore = user.averageScore,
                    userPlayerId = user.playerId,
                    onModeSelected = { mode ->
                        when (mode) {
                            GameMode.PVP -> {
                                // P2P: Go to matchmaking
                                currentScreen = Screen.Matchmaking
                            }
                            else -> {
                                // AI: Go to mode selection first
                                currentScreen = Screen.AIModeSelection
                            }
                        }
                    },
                    onLogout = {
                        authViewModel.logout()
                        currentUser = null
                        currentScreen = Screen.Auth
                    },
                    onDebug = {
                        // Debug screen removed - view data in Supabase dashboard instead
                    }
                )
            }
        }

        // 4. AI MODE SELECTION (Beginner / Intermediate / Advanced)
        Screen.AIModeSelection -> {
            AIPracticeModeScreen(
                userWins = currentUser?.wins ?: 0,
                onDifficultySelected = { selectedMode ->
                    // Start debate with selected difficulty
                    android.util.Log.d(
                        "MainActivity",
                        "🎯 onDifficultySelected called with mode: $selectedMode"
                    )
                    debateViewModel.startDebate(selectedMode)
                    android.util.Log.d(
                        "MainActivity",
                        "🎯 startDebate() called, waiting for session to be created..."
                    )
                },
                onBack = {
                    currentScreen = Screen.Home
                }
            )
        }

        // 5. DEBATE PREPARATION (VS animation, topic reveal, coin toss)
        Screen.DebatePreparation -> {
            val session by debateViewModel.currentSession.collectAsState()

            session?.let { debateSession ->
                DebatePreparationScreen(
                    playerName = debateSession.player1.name,
                    aiName = debateSession.player2?.name ?: "AI Debater",
                    topic = debateSession.topic.title,
                    topicDescription = debateSession.topic.description,
                    playerSide = if (debateSession.player1Side == DebateSide.FOR) "FOR" else "AGAINST",
                    aiSide = if (debateSession.player2Side == DebateSide.FOR) "FOR" else "AGAINST",
                    gameMode = debateSession.gameMode,
                    onPreparationComplete = { playerStarts ->
                        // Set who starts first based on coin toss result
                        debateViewModel.setFirstTurn(playerStarts)
                        currentScreen = Screen.DebateActive
                    }
                )
            }
        }

        // 6. DEBATE ACTIVE (Chat screen with AI)
        Screen.DebateActive -> {
            val session by debateViewModel.currentSession.collectAsState()

            DebateActiveScreen(
                gameMode = session?.gameMode
                    ?: GameMode.AI_INTERMEDIATE, // Get actual mode from session
                aiViewModel = debateViewModel
            )

            // Listen for debate completion
            LaunchedEffect(session?.status) {
                if (session?.status == DebateStatus.FINISHED) {
                    currentScreen = Screen.DebateResults
                }
            }
        }

        // 7. DEBATE RESULTS (Winner, scores, feedback)
        Screen.DebateResults -> {
            val session by debateViewModel.currentSession.collectAsState()

            session?.let { debateSession ->
                debateSession.scores?.let { scores ->
                    // Extract feedback points
                    val feedbackParts = extractFeedbackPoints(scores.detailedAnalysis)

                    DebateResultsScreen(
                        playerName = debateSession.player1.name,
                        aiName = debateSession.player2?.name ?: "AI Debater",
                        playerScore = scores.player1Score.totalScore,
                        aiScore = scores.player2Score.totalScore,
                        playerWon = scores.winner == debateSession.player1.id,
                        shiningPoints = feedbackParts.first,
                        lackingPoints = feedbackParts.second,
                        topic = debateSession.topic.title,
                        gameMode = debateSession.gameMode,
                        comprehensiveFeedback = scores.detailedAnalysis, // Pass full AI feedback
                        onPlayAgain = {
                            // Play again with same mode
                            debateViewModel.startDebate(debateSession.gameMode)
                            // Navigation will happen automatically via LaunchedEffect
                        },
                        onBackToMenu = {
                            // Back to home screen
                            currentScreen = Screen.Home
                        }
                    )
                }
            }
        }

        // 8. CHANGE PASSWORD (from profile)
        Screen.ChangePassword -> {
            ChangePasswordScreen(
                onBack = {
                    currentScreen = Screen.Home
                }
            )
        }

        // 9. P2P Screens
        Screen.Matchmaking -> {
            currentUser?.let { user ->
                val createdSessionId by matchmakingViewModel.createdSessionId.collectAsState()

                // Auto-navigate when session is created
                LaunchedEffect(createdSessionId) {
                    createdSessionId?.let { sessionId ->
                        p2pSessionId = sessionId
                        currentScreen = Screen.P2PDebatePreparation
                    }
                }

                MatchmakingScreen(
                    playerName = "${user.firstName} ${user.lastName}",
                    onMatchFound = { _, _ -> /* Handled by LaunchedEffect */ },
                    onCancel = {
                        currentScreen = Screen.Home
                    },
                    viewModel = matchmakingViewModel
                )

                // Start matchmaking automatically
                LaunchedEffect(Unit) {
                    matchmakingViewModel.startMatchmaking(
                        playerId = user.playerId.toString(),
                        playerName = "${user.firstName} ${user.lastName}"
                    )
                }
            }
        }

        Screen.P2PDebatePreparation -> {
            currentUser?.let { user ->
                p2pSessionId?.let { sessionId ->
                    P2PDebatePreparationScreen(
                        sessionId = sessionId,
                        currentUserId = user.playerId.toString(),
                        currentUserName = "${user.firstName} ${user.lastName}",
                        onPreparationComplete = {
                            currentScreen = Screen.P2PDebateActive
                        },
                        p2pViewModel = p2pViewModel
                    )
                }
            }
        }

        Screen.P2PDebateActive -> {
            currentUser?.let { user ->
                p2pSessionId?.let { sessionId ->
                    P2PDebateActiveScreen(
                        sessionId = sessionId,
                        currentUserId = user.playerId.toString(),
                        currentUserName = "${user.firstName} ${user.lastName}",
                        onDebateFinished = {
                            currentScreen = Screen.P2PDebateResults
                        },
                        p2pViewModel = p2pViewModel
                    )
                }
            }
        }

        Screen.P2PDebateResults -> {
            currentUser?.let { user ->
                val finalScores by p2pViewModel.finalScores.collectAsState()
                val myStrengths by p2pViewModel.myStrengths.collectAsState()
                val myWeaknesses by p2pViewModel.myWeaknesses.collectAsState()
                val sessionData by p2pViewModel.sessionData.collectAsState()
                val opponentName by p2pViewModel.opponentName.collectAsState()

                if (finalScores != null && sessionData != null) {
                    P2PDebateResultsScreen(
                        myName = "${user.firstName} ${user.lastName}",
                        opponentName = opponentName ?: "Opponent",
                        myScore = finalScores!!.first,
                        opponentScore = finalScores!!.second,
                        iWon = finalScores!!.first > finalScores!!.second,
                        myStrengths = myStrengths,
                        myWeaknesses = myWeaknesses,
                        topic = sessionData!!.topic_title,
                        onRematch = {
                            // Reset and go back to matchmaking
                            p2pSessionId = null
                            currentScreen = Screen.Matchmaking
                        },
                        onBackToMenu = {
                            p2pSessionId = null
                            currentScreen = Screen.Home
                        }
                    )
                }
            }
        }
    }
}

// Helper function to extract feedback points
fun extractFeedbackPoints(detailedAnalysis: String): Pair<List<String>, List<String>> {
    val shiningPoints = mutableListOf<String>()
    val lackingPoints = mutableListOf<String>()

    // Parse PLAYER1_FEEDBACK for shining points
    val player1Feedback = detailedAnalysis.lines()
        .find { it.startsWith("PLAYER1_FEEDBACK:") }
        ?.substringAfter(":")
        ?.trim() ?: ""

    // Look for positive keywords in feedback
    if (player1Feedback.isNotEmpty()) {
        when {
            player1Feedback.contains("logic", ignoreCase = true) ||
                    player1Feedback.contains("reasoning", ignoreCase = true) -> {
                shiningPoints.add("Strong logical reasoning and clear argument structure")
            }

            player1Feedback.contains("evidence", ignoreCase = true) -> {
                shiningPoints.add("Good use of evidence and supporting examples")
            }

            player1Feedback.contains("respectful", ignoreCase = true) ||
                    player1Feedback.contains("tone", ignoreCase = true) -> {
                shiningPoints.add("Maintained respectful and professional tone")
            }
        }
    }

    // Default shining points if parsing doesn't find specific ones
    if (shiningPoints.isEmpty()) {
        shiningPoints.addAll(
            listOf(
                "Demonstrated clear understanding of the topic",
                "Provided relevant arguments supporting your position",
                "Engaged constructively throughout the debate"
            )
        )
    }

    // Extract lacking points (areas to improve)
    when {
        detailedAnalysis.contains("counter", ignoreCase = true) -> {
            lackingPoints.add("Address opponent's counter-arguments more directly")
        }

        detailedAnalysis.contains("evidence", ignoreCase = true) &&
                detailedAnalysis.contains("lack", ignoreCase = true) -> {
            lackingPoints.add("Provide more concrete evidence to support claims")
        }

        detailedAnalysis.contains("repetit", ignoreCase = true) -> {
            lackingPoints.add("Avoid repetition - introduce new angles and perspectives")
        }
    }

    // Default lacking points
    if (lackingPoints.isEmpty()) {
        lackingPoints.addAll(
            listOf(
                "Could strengthen arguments with more specific examples",
                "Consider addressing counter-arguments more thoroughly",
                "Explore different perspectives to make arguments more robust"
            )
        )
    }

    // Ensure exactly 3 of each
    return Pair(
        shiningPoints.take(3),
        lackingPoints.take(3)
    )
}

@Composable
fun ExitConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Exit App",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Are you sure you want to exit Retrorix?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = onConfirm) {
                        Text("Exit")
                    }
                }
            }
        }
    }
}

@Composable
fun ExitDebateDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Leave Debate",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Are you sure you want to leave this debate? You won't be able to rejoin.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Stay")
                    }
                    Button(onClick = onConfirm) {
                        Text("Leave")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val currentModelId by viewModel.currentModelId.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showModelSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Chat") },
                actions = {
                    TextButton(onClick = { showModelSelector = !showModelSelector }) {
                        Text("Models")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    downloadProgress?.let { progress ->
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            }

            // Model selector (collapsible)
            if (showModelSelector) {
                ModelSelector(
                    models = availableModels,
                    currentModelId = currentModelId,
                    onDownload = { modelId -> viewModel.downloadModel(modelId) },
                    onLoad = { modelId -> viewModel.loadModel(modelId) },
                    onRefresh = { viewModel.refreshModels() }
                )
            }

            // Messages List
            val listState = rememberLazyListState()

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message)
                }
            }

            // Auto-scroll to bottom when new messages arrive
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            // Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    enabled = !isLoading && currentModelId != null
                )

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = !isLoading && inputText.isNotBlank() && currentModelId != null
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (message.isUser)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (message.isUser) "You" else "AI",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ModelSelector(
    models: List<com.runanywhere.sdk.models.ModelInfo>,
    currentModelId: String?,
    onDownload: (String) -> Unit,
    onLoad: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Available Models",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onRefresh) {
                    Text("Refresh")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (models.isEmpty()) {
                Text(
                    text = "No models available. Initializing...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(models) { model ->
                        ModelItem(
                            model = model,
                            isLoaded = model.id == currentModelId,
                            onDownload = { onDownload(model.id) },
                            onLoad = { onLoad(model.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModelItem(
    model: com.runanywhere.sdk.models.ModelInfo,
    isLoaded: Boolean,
    onDownload: () -> Unit,
    onLoad: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLoaded)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = model.name,
                style = MaterialTheme.typography.titleSmall
            )

            if (isLoaded) {
                Text(
                    text = "✓ Currently Loaded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.weight(1f),
                        enabled = !model.isDownloaded
                    ) {
                        Text(if (model.isDownloaded) "Downloaded" else "Download")
                    }

                    Button(
                        onClick = onLoad,
                        modifier = Modifier.weight(1f),
                        enabled = model.isDownloaded
                    ) {
                        Text("Load")
                    }
                }
            }
        }
    }
}

@Composable
fun ModelSetupScreen(onSetupComplete: () -> Unit, onBack: () -> Unit) {
    val debateViewModel: DebateViewModel = viewModel()
    val availableModels by debateViewModel.availableModels.collectAsState()
    val downloadProgress by debateViewModel.downloadProgress.collectAsState()
    val currentModelId by debateViewModel.currentModelId.collectAsState()
    val statusMessage by debateViewModel.statusMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "🤖 AI Model Setup",
            style = MaterialTheme.typography.headlineMedium
        )

        // Alert banner if coming from failed debate start
        val needsDownload by debateViewModel.needsModelDownload.collectAsState()
        if (needsDownload) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ Model Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To start AI practice, you need to download the 'Llama 3.2 1B Instruct' model for Beginner mode (815 MB).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Text(
            text = if (needsDownload) "Please download the required model below:" else "Download at least one model to start debating!",
            style = MaterialTheme.typography.bodyLarge
        )

        // Status Message
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
                downloadProgress?.let { progress ->
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% downloaded",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Model List
        Text(
            text = "Available Models",
            style = MaterialTheme.typography.titleMedium
        )

        if (availableModels.isEmpty()) {
            Text(
                text = "Loading models... Please wait.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableModels) { model ->
                    ModelSetupItem(
                        model = model,
                        isLoaded = model.name == currentModelId,
                        onDownload = { debateViewModel.downloadModel(model.id) },
                        onLoad = { debateViewModel.loadModel(model.id) },
                        downloadProgress = downloadProgress
                    )
                }
            }
        }

        // Continue Button
        val hasDownloadedModel = availableModels.any { it.isDownloaded }

        Button(
            onClick = onSetupComplete,
            modifier = Modifier.fillMaxWidth(),
            enabled = hasDownloadedModel && downloadProgress == null
        ) {
            Text(
                if (hasDownloadedModel) "Continue to Debate Arena"
                else "Please download at least one model"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = { debateViewModel.refreshModels() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Refresh Models")
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
fun ModelSetupItem(
    model: com.runanywhere.sdk.models.ModelInfo,
    isLoaded: Boolean,
    onDownload: () -> Unit,
    onLoad: () -> Unit,
    downloadProgress: Float?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isLoaded -> MaterialTheme.colorScheme.tertiaryContainer
                model.isDownloaded -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Model Name
            Text(
                text = model.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status and Actions
            when {
                isLoaded -> {
                    Text(
                        text = "✓ Currently Loaded & Ready",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                model.isDownloaded -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✓ Downloaded",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .weight(1f)
                                .align(androidx.compose.ui.Alignment.CenterVertically)
                        )
                        Button(
                            onClick = onLoad,
                            enabled = downloadProgress == null
                        ) {
                            Text("Load Model")
                        }
                    }
                }

                else -> {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = downloadProgress == null
                    ) {
                        Text("Download Model")
                    }
                }
            }

            // Recommended Badge
            if (model.name.contains("Llama 3.2 1B")) {
                Text(
                    text = "⭐ Recommended for beginners",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (model.name.contains("Qwen 2.5 3B")) {
                Text(
                    text = "🎯 Best for advanced debates",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Startup_hackathon20Theme {
        ChatScreen()
    }
}