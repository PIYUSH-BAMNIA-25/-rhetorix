package com.runanywhere.startup_hackathon20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon20.database.UserEntity
import com.runanywhere.startup_hackathon20.ui.theme.Startup_hackathon20Theme
import com.runanywhere.startup_hackathon20.viewmodel.AuthViewModel
import com.runanywhere.startup_hackathon20.DebateViewModel

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
    object Home : Screen()
    object AIModeSelection : Screen()
    object DebatePreparation : Screen()
    object DebateActive : Screen()
    object DebateResults : Screen()
    object ChangePassword : Screen()
    object Debug : Screen()
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Auth) }
    var currentUser by remember { mutableStateOf<UserEntity?>(null) }
    var userId by remember { mutableStateOf<String?>(null) }

    // Initialize ViewModels
    val authViewModel: AuthViewModel = viewModel()
    val debateViewModel: DebateViewModel = viewModel()

    // Set current user in DebateViewModel when user logs in
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // Login user in DebateViewModel with database ID
            debateViewModel.loginUser(
                userId = user.id,
                name = user.name,
                email = user.email,
                dateOfBirth = user.dateOfBirth
            )
            userId = user.id.toString()
        }
    }

    when (currentScreen) {
        // 1. AUTH SCREEN (Sign In / Login)
        Screen.Auth -> {
            AuthScreen(
                onAuthSuccess = { user ->
                    currentUser = user
                    currentScreen = Screen.Home
                },
                viewModel = authViewModel
            )
        }

        // 2. HOME SCREEN (Home & Profile tabs)
        Screen.Home -> {
            currentUser?.let { user ->
                MainMenuScreen(
                    userProfile = UserProfile(
                        name = user.name,
                        email = user.email,
                        dateOfBirth = user.dateOfBirth
                    ),
                    userWins = user.wins,
                    onModeSelected = { mode ->
                        when (mode) {
                            GameMode.PVP -> {
                                // P2P: Skip mode selection, go direct to prep
                                debateViewModel.startDebate(GameMode.PVP)
                                currentScreen = Screen.DebatePreparation
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
                        currentScreen = Screen.Debug
                    }
                )
            }
        }

        // 3. AI MODE SELECTION (Beginner / Intermediate / Advanced)
        Screen.AIModeSelection -> {
            AIPracticeModeScreen(
                userWins = currentUser?.wins ?: 0,
                onDifficultySelected = { selectedMode ->
                    // Start debate with selected difficulty
                    debateViewModel.startDebate(selectedMode)
                    currentScreen = Screen.DebatePreparation
                },
                onBack = {
                    currentScreen = Screen.Home
                }
            )
        }

        // 4. DEBATE PREPARATION (VS animation, topic reveal, coin toss)
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

        // 5. DEBATE ACTIVE (Chat screen with AI)
        Screen.DebateActive -> {
            DebateActiveScreen(
                viewModel = debateViewModel
            )

            // Listen for debate completion
            val session by debateViewModel.currentSession.collectAsState()
            LaunchedEffect(session?.status) {
                if (session?.status == DebateStatus.FINISHED) {
                    currentScreen = Screen.DebateResults
                }
            }
        }

        // 6. DEBATE RESULTS (Winner, scores, feedback)
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
                            currentScreen = Screen.DebatePreparation
                        },
                        onBackToMenu = {
                            // Back to home screen
                            currentScreen = Screen.Home
                        }
                    )
                }
            }
        }

        // 7. CHANGE PASSWORD (from profile)
        Screen.ChangePassword -> {
            ChangePasswordScreen(
                onBack = {
                    currentScreen = Screen.Home
                }
            )
        }


        // 8. DEBUG SCREEN
        Screen.Debug -> {
            DebugScreen(onBack = { currentScreen = Screen.Home })
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Startup_hackathon20Theme {
        ChatScreen()
    }
}