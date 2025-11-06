package com.runanywhere.startup_hackathon20

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.extensions.listAvailableModels
import com.runanywhere.sdk.models.ModelInfo
import com.runanywhere.startup_hackathon20.database.RhetorixDatabase
import com.runanywhere.startup_hackathon20.database.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID

class DebateViewModel(application: Application) : AndroidViewModel(application) {

    // Database
    private val database = RhetorixDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao(), database.debateHistoryDao())

    // Current debate session
    private val _currentSession = MutableStateFlow<DebateSession?>(null)
    val currentSession: StateFlow<DebateSession?> = _currentSession

    // Current user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // UI State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _statusMessage = MutableStateFlow("Welcome to Debate Arena!")
    val statusMessage: StateFlow<String> = _statusMessage

    // Model management (keeping from original)
    private val _availableModels = MutableStateFlow<List<ModelInfo>>(emptyList())
    val availableModels: StateFlow<List<ModelInfo>> = _availableModels

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress

    private val _currentModelId = MutableStateFlow<String?>(null)
    val currentModelId: StateFlow<String?> = _currentModelId

    // Timer for debates
    private val _timeRemaining = MutableStateFlow(0L)
    val timeRemaining: StateFlow<Long> = _timeRemaining

    // Screen navigation
    private val _currentScreen = MutableStateFlow(DebateScreen.LOGIN)
    val currentScreen: StateFlow<DebateScreen> = _currentScreen

    init {
        loadAvailableModels()
    }

    // === USER AUTHENTICATION ===
    fun loginUser(userId: Long, name: String, email: String, dateOfBirth: String) {
        val user = User(
            id = userId.toString(), // Use database ID
            name = name,
            email = email,
            dateOfBirth = dateOfBirth
        )
        _currentUser.value = user
        _currentScreen.value = DebateScreen.MAIN_MENU
        _statusMessage.value = "Welcome, $name!"
    }

    // === GAME START ===
    fun startDebate(gameMode: GameMode) {
        val user = _currentUser.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Loading AI model and generating topic..."

            try {
                // Step 1: Load appropriate model based on difficulty
                val modelToUse = when (gameMode) {
                    GameMode.AI_BEGINNER -> "Llama 3.2 1B Instruct Q6_K"
                    GameMode.AI_INTERMEDIATE,
                    GameMode.AI_ADVANCED,
                    GameMode.PVP -> "Qwen 2.5 3B Instruct Q6_K"
                }

                // Check if model is ready
                if (_currentModelId.value != modelToUse) {
                    _statusMessage.value = "Loading AI model..."
                    val success = RunAnywhere.loadModel(modelToUse)
                    if (success) {
                        _currentModelId.value = modelToUse
                    } else {
                        _statusMessage.value =
                            "Model not ready. Please wait for download to complete."
                        _isLoading.value = false
                        return@launch
                    }
                }

                // Step 2: Generate dynamic topic using AI
                _statusMessage.value = "Generating debate topic from current events..."

                val skillLevel = when (gameMode) {
                    GameMode.AI_BEGINNER -> SkillLevel.BEGINNER
                    GameMode.AI_INTERMEDIATE -> SkillLevel.INTERMEDIATE
                    GameMode.AI_ADVANCED -> SkillLevel.ADVANCED
                    GameMode.PVP -> SkillLevel.INTERMEDIATE
                }

                // Use dynamic topic generation
                val (topic, playerSide) = TopicGenerator.generateDynamicTopic(
                    category = TopicCategory.RANDOM,
                    difficulty = skillLevel
                )

                val aiSide = if (playerSide == DebateSide.FOR) DebateSide.AGAINST else DebateSide.FOR

                // Step 3: Create AI opponent
                val aiOpponent = User(
                    id = "ai_opponent",
                    name = "AI Debater",
                    email = "ai@debate.com",
                    skillLevel = skillLevel
                )

                // Step 4: Create debate session
                val session = DebateSession(
                    id = UUID.randomUUID().toString(),
                    topic = topic,
                    player1 = user,
                    player2 = aiOpponent,
                    player1Side = playerSide,
                    player2Side = aiSide,
                    gameMode = gameMode,
                    status = DebateStatus.PREP_TIME,
                    currentTurn = user.id, // Player starts
                    timeRemaining = 600000, // 10 minutes
                    startTime = System.currentTimeMillis()
                )

                _currentSession.value = session
                _currentScreen.value = DebateScreen.DEBATE_PREP
                
                // Start prep timer (30 seconds)
                startPrepTimer()

            } catch (e: Exception) {
                _statusMessage.value = "Error starting debate: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun startPrepTimer() {
        _statusMessage.value = "Study your topic and side! Debate starts in 30 seconds..."
        
        for (i in 30 downTo 1) {
            _timeRemaining.value = i * 1000L
            delay(1000)
        }
        
        // Start actual debate
        val session = _currentSession.value ?: return
        _currentSession.value = session.copy(
            status = DebateStatus.IN_PROGRESS,
            timeRemaining = 600000 // Reset to 10 minutes
        )
        _currentScreen.value = DebateScreen.DEBATE_ACTIVE
        _statusMessage.value = "Debate started! You have 10 minutes total."
        
        startDebateTimer()
    }

    private suspend fun startDebateTimer() {
        val session = _currentSession.value ?: return
        var timeLeft = session.timeRemaining

        while (timeLeft > 0 && _currentSession.value?.status == DebateStatus.IN_PROGRESS) {
            delay(1000)
            timeLeft -= 1000
            _timeRemaining.value = timeLeft
            
            // Update session
            _currentSession.value = _currentSession.value?.copy(timeRemaining = timeLeft)
        }

        // Time's up!
        if (timeLeft <= 0) {
            endDebate()
        }
    }

    // === MESSAGING ===
    fun sendDebateMessage(message: String) {
        val session = _currentSession.value ?: return
        val user = _currentUser.value ?: return

        if (session.currentTurn != user.id) {
            _statusMessage.value = "Not your turn!"
            return
        }

        if (message.isBlank()) return

        viewModelScope.launch {
            // Add user message
            val userMessage = DebateMessage(
                id = UUID.randomUUID().toString(),
                playerId = user.id,
                playerName = user.name,
                message = message,
                timestamp = System.currentTimeMillis(),
                turnNumber = session.messages.size + 1
            )

            val updatedMessages = session.messages + userMessage
            _currentSession.value = session.copy(
                messages = updatedMessages,
                currentTurn = "ai_opponent" // Switch to AI
            )

            _statusMessage.value = "AI is thinking..."
            
            // Generate AI response
            generateAIResponse(session.copy(messages = updatedMessages))
        }
    }

    private suspend fun generateAIResponse(session: DebateSession) {
        try {
            val aiPrompt = buildAIPrompt(session)
            
            var aiResponse = ""
            RunAnywhere.generateStream(aiPrompt).collect { token ->
                aiResponse += token
            }

            // Add AI message
            val aiMessage = DebateMessage(
                id = UUID.randomUUID().toString(),
                playerId = "ai_opponent",
                playerName = "AI Debater",
                message = aiResponse,
                timestamp = System.currentTimeMillis(),
                turnNumber = session.messages.size + 1
            )

            val finalMessages = session.messages + aiMessage
            _currentSession.value = session.copy(
                messages = finalMessages,
                currentTurn = _currentUser.value?.id ?: "" // Back to player
            )

            _statusMessage.value = "Your turn! Respond to the AI's argument."

        } catch (e: Exception) {
            _statusMessage.value = "AI response failed: ${e.message}"
            // Switch back to player anyway
            _currentSession.value = session.copy(currentTurn = _currentUser.value?.id ?: "")
        }
    }

    private fun buildAIPrompt(session: DebateSession): String {
        val topic = session.topic
        val aiSide = session.player2Side
        val sideText = if (aiSide == DebateSide.FOR) "in favor of" else "against"
        
        val conversationHistory = session.messages.joinToString("\n") { msg ->
            "${msg.playerName}: ${msg.message}"
        }

        val difficultyInstruction = when (session.gameMode) {
            GameMode.AI_BEGINNER -> "Keep arguments simple and easy to understand. Use basic examples."
            GameMode.AI_INTERMEDIATE -> "Use moderate complexity. Include some evidence and logical reasoning."
            GameMode.AI_ADVANCED -> "Use sophisticated arguments, complex reasoning, and detailed evidence."
            GameMode.PVP -> "Use intermediate level arguments."
        }

        return """
            You are participating in a formal debate about: "${topic.title}"
            
            Your position: You are arguing $sideText this topic.
            Your difficulty level: $difficultyInstruction
            
            Debate Context: ${topic.description}
            
            Conversation so far:
            $conversationHistory
            
            Instructions:
            - Stay focused on the debate topic
            - Argue your assigned side consistently
            - Keep responses to 2-3 sentences for good debate flow
            - Be respectful but persuasive
            - Use logical reasoning and examples
            - Don't repeat previous arguments
            
            Your response:
        """.trimIndent()
    }

    // === DEBATE ENDING ===
    private suspend fun endDebate() {
        val session = _currentSession.value ?: return
        
        _currentSession.value = session.copy(status = DebateStatus.JUDGING)
        _statusMessage.value = "Time's up! AI is judging the debate..."
        _currentScreen.value = DebateScreen.DEBATE_RESULTS
        
        // Generate scores
        generateDebateScores(session)
    }

    private suspend fun generateDebateScores(session: DebateSession) {
        try {
            val judgingPrompt = buildJudgingPrompt(session)
            val judgingResponse = RunAnywhere.generate(judgingPrompt)
            
            // Parse AI response and create scores
            val scores = parseJudgingResponse(judgingResponse, session)
            
            _currentSession.value = session.copy(
                status = DebateStatus.FINISHED,
                scores = scores
            )
            
            val winner = if (scores.player1Score.totalScore > scores.player2Score.totalScore) 
                session.player1.name else "AI Debater"
            
            _statusMessage.value = "Debate complete! Winner: $winner"

            // Save debate results to database
            saveDebateResults(session, scores)

        } catch (e: Exception) {
            _statusMessage.value = "Error judging debate: ${e.message}"
        }
    }

    private fun buildJudgingPrompt(session: DebateSession): String {
        val topic = session.topic
        val player1Side = if (session.player1Side == DebateSide.FOR) "FOR" else "AGAINST"
        val player2Side = if (session.player2Side == DebateSide.FOR) "FOR" else "AGAINST"
        
        val conversation = session.messages.joinToString("\n") { msg ->
            "${msg.playerName} (${ if (msg.playerId == session.player1.id) player1Side else player2Side }): ${msg.message}"
        }

        return """
            You are a professional debate judge. Score this debate on the topic: "${topic.title}"
            
            Participants:
            - ${session.player1.name} (arguing $player1Side)
            - AI Debater (arguing $player2Side)
            
            Debate transcript:
            $conversation
            
            Score each participant (1-10) on:
            1. Logic & Reasoning: Clear, logical arguments
            2. Evidence Quality: Use of facts and examples
            3. Tone & Respect: Professional, respectful communication
            4. Counter Arguments: Addressing opponent's points effectively
            5. Factual Accuracy: Truthfulness of claims
            
            Format your response as:
            PLAYER1_LOGIC: [score]
            PLAYER1_EVIDENCE: [score]
            PLAYER1_TONE: [score]
            PLAYER1_COUNTER: [score]
            PLAYER1_ACCURACY: [score]
            PLAYER1_FEEDBACK: [specific feedback]
            
            PLAYER2_LOGIC: [score]
            PLAYER2_EVIDENCE: [score]
            PLAYER2_TONE: [score]
            PLAYER2_COUNTER: [score]
            PLAYER2_ACCURACY: [score]
            PLAYER2_FEEDBACK: [specific feedback]
            
            WINNER: [player name]
            ANALYSIS: [overall analysis of debate quality]
        """.trimIndent()
    }

    private fun parseJudgingResponse(response: String, session: DebateSession): DebateScores {
        // Simple parsing - in production, you'd want more robust parsing
        val lines = response.lines()
        
        fun extractScore(prefix: String): Int {
            return lines.find { it.startsWith(prefix) }
                ?.substringAfter(":")
                ?.trim()
                ?.toIntOrNull() ?: 5
        }
        
        fun extractText(prefix: String): String {
            return lines.find { it.startsWith(prefix) }
                ?.substringAfter(":")
                ?.trim() ?: ""
        }

        val player1Logic = extractScore("PLAYER1_LOGIC")
        val player1Evidence = extractScore("PLAYER1_EVIDENCE")
        val player1Tone = extractScore("PLAYER1_TONE")
        val player1Counter = extractScore("PLAYER1_COUNTER")
        val player1Accuracy = extractScore("PLAYER1_ACCURACY")
        val player1Total = player1Logic + player1Evidence + player1Tone + player1Counter + player1Accuracy
        val player1Feedback = extractText("PLAYER1_FEEDBACK")

        val player2Logic = extractScore("PLAYER2_LOGIC")
        val player2Evidence = extractScore("PLAYER2_EVIDENCE")
        val player2Tone = extractScore("PLAYER2_TONE")
        val player2Counter = extractScore("PLAYER2_COUNTER")
        val player2Accuracy = extractScore("PLAYER2_ACCURACY")
        val player2Total = player2Logic + player2Evidence + player2Tone + player2Counter + player2Accuracy
        val player2Feedback = extractText("PLAYER2_FEEDBACK")

        val winner = if (player1Total > player2Total) session.player1.id else "ai_opponent"
        val analysis = extractText("ANALYSIS")

        return DebateScores(
            player1Score = PlayerScore(
                playerId = session.player1.id,
                playerName = session.player1.name,
                logicReasoning = player1Logic,
                evidenceQuality = player1Evidence,
                toneRespect = player1Tone,
                counterArguments = player1Counter,
                factualAccuracy = player1Accuracy,
                totalScore = player1Total,
                feedback = player1Feedback
            ),
            player2Score = PlayerScore(
                playerId = "ai_opponent",
                playerName = "AI Debater",
                logicReasoning = player2Logic,
                evidenceQuality = player2Evidence,
                toneRespect = player2Tone,
                counterArguments = player2Counter,
                factualAccuracy = player2Accuracy,
                totalScore = player2Total,
                feedback = player2Feedback
            ),
            winner = winner,
            feedback = analysis,
            detailedAnalysis = response
        )
    }

    /**
     * Save debate results to database and update user stats
     */
    private suspend fun saveDebateResults(session: DebateSession, scores: DebateScores) {
        try {
            val userWon = scores.winner == session.player1.id

            // Convert user ID from String to Long (assuming format)
            val userId = session.player1.id.toLongOrNull() ?: return

            // Save debate history
            userRepository.saveDebateResult(
                userId = userId,
                topic = session.topic.title,
                userSide = session.player1Side.toString(),
                opponentType = session.gameMode.toString(),
                userScore = scores.player1Score.totalScore,
                opponentScore = scores.player2Score.totalScore,
                feedback = scores.feedback
            )

            // User stats are automatically updated in repository.saveDebateResult()

        } catch (e: Exception) {
            // Log error but don't stop the flow
            _statusMessage.value = "Warning: Could not save debate results"
        }
    }

    // === NAVIGATION ===
    fun navigateToScreen(screen: DebateScreen) {
        _currentScreen.value = screen
    }

    fun startNewDebate() {
        _currentSession.value = null
        _currentScreen.value = DebateScreen.MAIN_MENU
        _statusMessage.value = "Ready for another debate!"
    }

    // === MODEL MANAGEMENT (from original ChatViewModel) ===
    private fun loadAvailableModels() {
        viewModelScope.launch {
            try {
                val models = listAvailableModels()
                _availableModels.value = models
                if (models.isNotEmpty()) {
                    _statusMessage.value = "AI models loaded. Please download and load a model to start debating!"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error loading models: ${e.message}"
            }
        }
    }

    fun downloadModel(modelId: String) {
        viewModelScope.launch {
            try {
                _statusMessage.value = "Downloading AI model..."
                RunAnywhere.downloadModel(modelId).collect { progress ->
                    _downloadProgress.value = progress
                    _statusMessage.value = "Downloading: ${(progress * 100).toInt()}%"
                }
                _downloadProgress.value = null
                _statusMessage.value = "Download complete! Please load the model."
            } catch (e: Exception) {
                _statusMessage.value = "Download failed: ${e.message}"
                _downloadProgress.value = null
            }
        }
    }

    fun loadModel(modelId: String) {
        viewModelScope.launch {
            try {
                _statusMessage.value = "Loading AI model..."
                val success = RunAnywhere.loadModel(modelId)
                if (success) {
                    _currentModelId.value = modelId
                    _statusMessage.value = "AI model loaded! Ready to debate."
                } else {
                    _statusMessage.value = "Failed to load model"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error loading model: ${e.message}"
            }
        }
    }

    fun refreshModels() {
        loadAvailableModels()
    }
}

enum class DebateScreen {
    LOGIN, MAIN_MENU, MODEL_SETUP, DEBATE_PREP, DEBATE_ACTIVE, DEBATE_RESULTS
}