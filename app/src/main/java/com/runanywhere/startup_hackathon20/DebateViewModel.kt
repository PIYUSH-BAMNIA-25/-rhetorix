package com.runanywhere.startup_hackathon20

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.extensions.listAvailableModels
import com.runanywhere.sdk.models.ModelInfo
import com.runanywhere.startup_hackathon20.network.ServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

class DebateViewModel(application: Application) : AndroidViewModel(application) {

    // Server repository for saving debate results
    private val serverRepository = ServerRepository(application)

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


    // Navigation hints for MainActivity
    private val _needsModelDownload = MutableStateFlow(false)
    val needsModelDownload: StateFlow<Boolean> = _needsModelDownload

    // Score pop-up state
    private val _showScorePopup = MutableStateFlow(false)
    val showScorePopup: StateFlow<Boolean> = _showScorePopup

    private val _currentTurnScore = MutableStateFlow<TurnScore?>(null)
    val currentTurnScore: StateFlow<TurnScore?> = _currentTurnScore

    // Accumulated scores during debate
    private val _accumulatedScores = MutableStateFlow<AccumulatedScores?>(null)
    val accumulatedScores: StateFlow<AccumulatedScores?> = _accumulatedScores

    // AI typing animation state
    private val _aiTypingText = MutableStateFlow("")
    val aiTypingText: StateFlow<String> = _aiTypingText

    private val _isAITyping = MutableStateFlow(false)
    val isAITyping: StateFlow<Boolean> = _isAITyping

    // Model management
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

    // Chat history for judging context
    private val chatHistory = mutableListOf<ChatTurn>()

    // Track AI's previous arguments to prevent repetition
    private val aiArgumentHistory = mutableSetOf<String>()

    // Model heartbeat job to keep it alive
    private var modelHeartbeatJob: kotlinx.coroutines.Job? = null

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
        // DO NOT set _currentScreen - MainActivity handles all navigation
        _statusMessage.value = "Welcome, $name!"
    }

    /**
     * Determine debate phase based on turn number
     * Phases: OPENING (1-2) → DEVELOPMENT (3-5) → CLASH (6-8) → CLOSING (9+)
     */
    private fun getDebatePhase(turnNumber: Int): String {
        return when {
            turnNumber <= 2 -> "OPENING"
            turnNumber <= 5 -> "DEVELOPMENT"
            turnNumber <= 8 -> "CLASH"
            else -> "CLOSING"
        }
    }

    /**
     * Update AI argument history to prevent repetition
     */
    private fun updateAIArgumentHistory(aiResponse: String) {
        // Extract key claims (sentences longer than 20 chars)
        val keyClaims = aiResponse
            .split(".", "!", "?")
            .map { it.trim() }
            .filter { it.length > 20 }
            .take(2) // Up to 2 main claims per turn
        
        aiArgumentHistory.addAll(keyClaims)
        
        // Keep only last 6 arguments to prevent prompt bloat
        if (aiArgumentHistory.size > 6) {
            val toRemove = aiArgumentHistory.take(aiArgumentHistory.size - 6)
            aiArgumentHistory.removeAll(toRemove.toSet())
        }
    }

    /**
     * Set who starts the debate first based on coin toss result
     */
    fun setFirstTurn(playerStarts: Boolean) {
        val session = _currentSession.value ?: return
        val firstPlayerId = if (playerStarts)
            session.player1.id
        else
            session.player2?.id ?: "ai_opponent"

        _currentSession.value = session.copy(
            currentTurn = firstPlayerId
        )

        Log.d(
            "DebateViewModel",
            "🎲 First turn set to: $firstPlayerId (playerStarts: $playerStarts)"
        )

        // If AI starts first AND we're in prep time, do nothing yet
        // The AI will start when debate transitions to IN_PROGRESS
        if (!playerStarts && session.status == DebateStatus.PREP_TIME) {
            Log.d("DebateViewModel", "🤖 AI will start when debate begins (after prep time)")
        } else if (!playerStarts && session.status == DebateStatus.IN_PROGRESS) {
            // If somehow we're already in progress and AI should start, generate now
            Log.d("DebateViewModel", "🤖 AI starts first, generating opening statement...")
            viewModelScope.launch {
                delay(1000) // Small delay for UX
                generateAIResponseWithDelay(session.copy(currentTurn = firstPlayerId))
            }
        }
    }

    // === GAME START ===
    fun startDebate(gameMode: GameMode) {
        val user = _currentUser.value ?: return
        Log.d("DebateViewModel", "🚀 startDebate called with mode: $gameMode, user: ${user.name}")

        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Preparing debate..."
            Log.d("DebateViewModel", "📝 Status set to: Preparing debate...")

            try {
                // Step 0: Check if models exist first
                _statusMessage.value = "Checking available models..."
                Log.d("DebateViewModel", "📝 Checking available models...")
                val models = try {
                    listAvailableModels()
                } catch (e: Exception) {
                    Log.e("DebateViewModel", "Error listing models", e)
                    emptyList()
                }

                if (models.isEmpty()) {
                    Log.e("DebateViewModel", "❌ No models found!")
                    _statusMessage.value =
                        "⚠️ No models registered! Please wait for app to initialize, then try again."
                    _isLoading.value = false
                    delay(3000)
                    return@launch
                }

                Log.d("DebateViewModel", "Found ${models.size} models: ${models.map { it.name }}")

                // Step 1: Determine which model to use based on difficulty
                val modelToUse = when (gameMode) {
                    GameMode.AI_BEGINNER -> "Qwen 2.5 3B Instruct Q6_K"  // Single model for all
                    GameMode.AI_INTERMEDIATE,
                    GameMode.AI_ADVANCED,
                    GameMode.PVP -> "Qwen 2.5 3B Instruct Q6_K"  // Same model
                }

                _statusMessage.value = "Looking for model: $modelToUse..."
                Log.d("DebateViewModel", "🔍 Looking for model: $modelToUse")

                // Step 2: Check if model exists and is downloaded
                val targetModel = models.find { it.name == modelToUse }

                if (targetModel == null) {
                    Log.e("DebateViewModel", "❌ Model not found: $modelToUse")
                    _statusMessage.value =
                        "⚠️ Model '$modelToUse' not found!\n\nPlease restart the app to allow models to register properly."
                    _isLoading.value = false
                    delay(5000)
                    return@launch
                }

                Log.d(
                    "DebateViewModel",
                    "✅ Model found: ${targetModel.name}, isDownloaded: ${targetModel.isDownloaded}"
                )

                if (!targetModel.isDownloaded) {
                    Log.e("DebateViewModel", "❌ Model not downloaded: $modelToUse")
                    _needsModelDownload.value = true
                    _statusMessage.value =
                        "⚠️ Model not downloaded yet!\n\nPlease download '$modelToUse' from the Model Setup screen first."
                    _isLoading.value = false
                    delay(5000)
                    return@launch
                }

                // Step 3: Load model if not already loaded
                _statusMessage.value = "Loading AI model..."
                Log.d("DebateViewModel", "🔄 Loading model: $modelToUse")
                if (_currentModelId.value != targetModel.id) {
                    Log.d("DebateViewModel", "Loading model with ID: ${targetModel.id}")
                    val success = try {
                        RunAnywhere.loadModel(targetModel.id)
                    } catch (e: Exception) {
                        Log.e("DebateViewModel", "Error loading model", e)
                        false
                    }

                    if (success) {
                        _currentModelId.value = targetModel.id
                        Log.d("DebateViewModel", "✅ Model loaded successfully!")

                        // CRITICAL: Wait for model to fully initialize
                        _statusMessage.value = "Initializing AI model..."
                        delay(8000) // Increased from 5 seconds to 8 seconds for better stability
                        Log.d("DebateViewModel", "✅ Model initialization complete")
                    } else {
                        Log.e("DebateViewModel", "❌ Failed to load model!")
                        _statusMessage.value =
                            "❌ Failed to load model.\n\nPlease try again or restart the app."
                        _isLoading.value = false
                        delay(5000)
                        return@launch
                    }
                } else {
                    Log.d("DebateViewModel", "✅ Model already loaded with ID: ${targetModel.id}")
                    // Still wait a bit to ensure it's ready
                    delay(3000) // Increased from 2 seconds to 3 seconds for better stability
                }

                // Step 3.5: Verify model is actually ready by testing generation
                _statusMessage.value = "Testing AI connection..."
                Log.d("DebateViewModel", "🧪 Testing model with simple prompt...")

                // WORKAROUND: Force SDK to re-scan models (might wake up GenerationService)
                try {
                    RunAnywhere.scanForDownloadedModels()
                    delay(1000)
                    Log.d("DebateViewModel", "🔄 Re-scanned models as workaround")
                } catch (e: Exception) {
                    Log.e("DebateViewModel", "Failed to re-scan models", e)
                }

                var testSuccess = false
                var attempts = 0
                val maxAttempts = 3

                while (!testSuccess && attempts < maxAttempts) {
                    attempts++
                    try {
                        Log.d("DebateViewModel", "🧪 Test attempt $attempts/$maxAttempts...")
                        // Try streaming API first (might work even if generate() doesn't)
                        val testResponse = StringBuilder()
                        RunAnywhere.generateStream("Say 'ready'").collect { token ->
                            testResponse.append(token)
                        }
                        Log.d(
                            "DebateViewModel",
                            "✅ Model test successful (streaming): $testResponse"
                        )
                        testSuccess = true
                    } catch (e: Exception) {
                        Log.e("DebateViewModel", "❌ Model test attempt $attempts failed", e)
                        if (attempts < maxAttempts) {
                            _statusMessage.value = "Retrying AI test... ($attempts/$maxAttempts)"
                            delay(2000) // Wait 2 seconds between retries
                        }
                    }
                }

                // If test still fails, show warning but continue anyway
                // The model might work during actual debate after more time
                if (!testSuccess) {
                    Log.w(
                        "DebateViewModel",
                        "⚠️ Model test failed after $maxAttempts attempts, but continuing anyway..."
                    )
                    _statusMessage.value = "⚠️ AI test incomplete, but proceeding..."
                    delay(1000)
                }

                // Step 4: Generate dynamic topic using AI
                _statusMessage.value = "Generating debate topic from current events..."
                Log.d("DebateViewModel", "🎲 Generating topic...")

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
                Log.d("DebateViewModel", "✅ Topic generated: ${topic.title}")

                val aiSide = if (playerSide == DebateSide.FOR) DebateSide.AGAINST else DebateSide.FOR

                // Step 5: Create AI opponent with balanced IQ
                val aiIQ = getBalancedAIIQ(gameMode)
                val aiOpponent = User(
                    id = "ai_opponent",
                    name = "AI Debater (IQ $aiIQ)",
                    email = "ai@debate.com",
                    skillLevel = skillLevel
                )
                Log.d("DebateViewModel", "✅ AI opponent created with IQ: $aiIQ")

                // Step 6: Create debate session
                val session = DebateSession(
                    id = UUID.randomUUID().toString(),
                    topic = topic,
                    player1 = user,
                    player2 = aiOpponent,
                    player1Side = playerSide,
                    player2Side = aiSide,
                    gameMode = gameMode,
                    status = DebateStatus.PREP_TIME,
                    currentTurn = user.id, // Will be set by coin toss
                    timeRemaining = 600000, // 10 minutes
                    startTime = System.currentTimeMillis()
                )

                Log.d("DebateViewModel", "🎮 Session created with ID: ${session.id}")
                _currentSession.value = session
                Log.d("DebateViewModel", "✅ Session set to StateFlow, status: ${session.status}")
                // DO NOT set _currentScreen - MainActivity will observe currentSession and navigate
                // _currentScreen.value = DebateScreen.DEBATE_PREP

                // Clear chat history for new debate
                chatHistory.clear()

                // Clear AI argument history for fresh debate
                aiArgumentHistory.clear()

                // Start prep timer (30 seconds)
                _statusMessage.value = "Everything ready! Starting preparation phase..."
                Log.d("DebateViewModel", " Starting prep timer...")
                startPrepTimer()

            } catch (e: Exception) {
                Log.e("DebateViewModel", "❌ CRITICAL ERROR in startDebate", e)
                _statusMessage.value =
                    "❌ Error starting debate: ${e.message}\n\nPlease try again or restart the app."
                Log.e("DebateViewModel", "Error starting debate", e)
                delay(5000)
            } finally {
                _isLoading.value = false
                Log.d("DebateViewModel", "🏁 startDebate finally block reached")
            }
        }
    }

    /**
     * Get balanced AI IQ based on difficulty
     * Player should be able to win with good arguments!
     */
    private fun getBalancedAIIQ(gameMode: GameMode): Int {
        return when (gameMode) {
            GameMode.AI_BEGINNER -> Random.nextInt(55, 65)      // 55-65 IQ (Easy to beat)
            GameMode.AI_INTERMEDIATE -> Random.nextInt(70, 80)  // 70-80 IQ (Balanced)
            GameMode.AI_ADVANCED -> Random.nextInt(
                85,
                95
            )      // 85-95 IQ (Challenging but beatable)
            GameMode.PVP -> Random.nextInt(70, 80)              // Similar to intermediate
        }
    }

    private suspend fun startPrepTimer() {
        _statusMessage.value = "Study your topic and side! Debate starts soon..."

        // Countdown from 30 to 1
        for (i in 30 downTo 1) {
            _timeRemaining.value = i * 1000L
            // Update status message to show countdown
            _statusMessage.value = "Preparation time: ${i}s remaining..."
            delay(1000)
        }

        // Initialize accumulated scores when debate starts
        _accumulatedScores.value = AccumulatedScores(
            playerTotalScore = 0,
            aiTotalScore = 0,
            playerTurnCount = 0,
            aiTurnCount = 0
        )

        // Start actual debate
        val session = _currentSession.value ?: return
        val updatedSession = session.copy(
            status = DebateStatus.IN_PROGRESS,
            timeRemaining = 600000 // Reset to 10 minutes
        )
        _currentSession.value = updatedSession

        // START MODEL HEARTBEAT to keep it alive during debate
        startModelHeartbeat()

        // Check who starts first and set appropriate status message
        if (updatedSession.currentTurn == "ai_opponent") {
            Log.d(
                "DebateViewModel",
                "🤖 AI starts first, generating opening statement after prep..."
            )
            _statusMessage.value = "AI is preparing opening statement..."
            viewModelScope.launch {
                delay(1000)
                generateAIResponseWithDelay(updatedSession)
            }
        } else {
            Log.d("DebateViewModel", "👤 Player starts first")
            _statusMessage.value = "Your turn! Start the debate with your opening argument."
        }

        startDebateTimer()
    }

    private suspend fun startDebateTimer() {
        val session = _currentSession.value ?: return
        var timeLeft = session.timeRemaining

        while (timeLeft > 0 && _currentSession.value?.status == DebateStatus.IN_PROGRESS) {
            delay(1000)
            timeLeft -= 1000
            _timeRemaining.value = timeLeft

            // Update session with new time - preserve currentTurn!
            val currentSess = _currentSession.value
            if (currentSess != null && currentSess.status == DebateStatus.IN_PROGRESS) {
                _currentSession.value = currentSess.copy(timeRemaining = timeLeft)
            }

            // Update status message based on time remaining (only if not already showing other message)
            when {
                timeLeft <= 60000 && !_statusMessage.value.contains("turn") ->
                    _statusMessage.value = "⏰ 1 minute remaining!"

                timeLeft <= 120000 && !_statusMessage.value.contains("turn") ->
                    _statusMessage.value = "⏰ 2 minutes remaining!"

                timeLeft <= 300000 && timeLeft % 60000L == 0L && !_statusMessage.value.contains("turn") ->
                    _statusMessage.value = "⏰ ${timeLeft / 60000} minutes remaining!"
            }
        }

        // Time's up!
        if (timeLeft <= 0) {
            endDebate()
        }
    }

    // === MESSAGING WITH JUDGING ===
    fun sendDebateMessage(message: String) {
        val session = _currentSession.value ?: return
        val user = _currentUser.value ?: return

        Log.d("DebateViewModel", "📤 sendDebateMessage called: '$message'")

        if (session.currentTurn != user.id) {
            _statusMessage.value = "Not your turn!"
            Log.d(
                "DebateViewModel",
                "❌ Not player's turn. Current turn: ${session.currentTurn}, Player ID: ${user.id}"
            )
            return
        }

        if (message.isBlank()) {
            Log.d("DebateViewModel", "❌ Message is blank, ignoring")
            return
        }

        Log.d("DebateViewModel", "✅ Valid message from player, processing...")

        viewModelScope.launch {
            try {
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
                _currentSession.value = session.copy(messages = updatedMessages)

                Log.d(
                    "DebateViewModel",
                    "📝 Player message added to session (Turn ${userMessage.turnNumber})"
                )

                // Store in chat history for judging
                chatHistory.add(
                    ChatTurn(
                        speaker = "player",
                        message = message,
                        timestamp = System.currentTimeMillis()
                    )
                )

                // JUDGE PLAYER'S RESPONSE
                _statusMessage.value = "⚖️ Judging your argument..."
                Log.d("DebateViewModel", "⚖️ Judging player's argument...")
                val playerScore = judgeResponse(
                    currentResponse = message,
                    previousOpponentResponse = chatHistory.findLast { it.speaker == "ai" }?.message
                        ?: "",
                    speaker = "Player",
                    difficulty = session.gameMode,
                    turnNumber = userMessage.turnNumber
                )

                Log.d("DebateViewModel", "Player Score: ${playerScore.score}/10")

                // Show score pop-up
                _currentTurnScore.value = playerScore
                _showScorePopup.value = true
                delay(3000) // Show for 3 seconds
                _showScorePopup.value = false

                // Update accumulated scores
                val accumulated = _accumulatedScores.value ?: AccumulatedScores(0, 0, 0, 0)
                _accumulatedScores.value = accumulated.copy(
                    playerTotalScore = accumulated.playerTotalScore + playerScore.score,
                    playerTurnCount = accumulated.playerTurnCount + 1
                )

                // Switch to AI turn
                _currentSession.value = session.copy(
                    messages = updatedMessages,
                    currentTurn = "ai_opponent"
                )

                Log.d(
                    "DebateViewModel",
                    "🔄 Switching to AI turn, calling generateAIResponseWithDelay..."
                )

                // Generate AI response with realistic delay
                generateAIResponseWithDelay(session.copy(messages = updatedMessages))

            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
                Log.e("DebateViewModel", "Error sending message", e)
                // Switch back to player anyway
                _currentSession.value = session.copy(currentTurn = _currentUser.value?.id ?: "")
            }
        }
    }

    /**
     * Generate AI response with realistic timing and typing animation
     */
    private suspend fun generateAIResponseWithDelay(session: DebateSession) {
        try {
            // Always use the latest session from state to avoid race conditions and get fresh messages
            val currentSession = _currentSession.value ?: return

            Log.d("DebateViewModel", "🤖 Generating AI response for turn #${currentSession.messages.size + 1}")
            
            _statusMessage.value = "🧠 AI is thinking..."
            _isAITyping.value = false

            // Generate response (this may take time)
            val aiPrompt = buildAIPrompt(currentSession)
            Log.d("DebateViewModel", "🤖 Generating AI response...")
            Log.d("DebateViewModel", "AI Prompt:\n$aiPrompt")

            val aiResponseText = try {
                // Try using streaming API - which works!
                val response = StringBuilder()
                try {
                    Log.d("DebateViewModel", "📡 Using streaming API for AI response...")

                    // Longer timeout: 60 seconds (model needs time to wake up)
                    kotlinx.coroutines.withTimeout(60000) {
                        RunAnywhere.generateStream(aiPrompt).collect { token ->
                            response.append(token)
                        }
                    }
                    val result = response.toString()
                    Log.d("DebateViewModel", "✅ Streaming succeeded, got ${result.length} chars")
                    result
                } catch (streamError: Exception) {
                    Log.e(
                        "DebateViewModel",
                        "❌ Streaming failed or timeout",
                        streamError
                    )
                    // Try regular generate as fallback
                    RunAnywhere.generate(aiPrompt)
                }
            } catch (e: Exception) {
                Log.e("DebateViewModel", "❌ AI generation failed completely", e)

                // Check if it's the LLM not initialized error
                if (e.message?.contains("LLM component not initialized") == true) {
                    // Try waiting longer and retrying with streaming
                    Log.w(
                        "DebateViewModel",
                        "⚠️ LLM not initialized, waiting 5 seconds and retrying with streaming..."
                    )
                    _statusMessage.value = "⏳ AI model warming up, please wait..."
                    delay(5000)

                    try {
                        Log.d("DebateViewModel", "🔄 Retry attempt with streaming...")
                        val retryResponse = StringBuilder()
                        RunAnywhere.generateStream(aiPrompt).collect { token ->
                            retryResponse.append(token)
                        }
                        val result = retryResponse.toString()
                        Log.d("DebateViewModel", "✅ Retry succeeded!")
                        result
                    } catch (e2: Exception) {
                        Log.e("DebateViewModel", "❌ Retry also failed", e2)
                        // Don't end debate, just give simple response
                        "I understand your point. Let me respond: ${listOf(
                            "That's an interesting perspective, but have you considered the other side?",
                            "I see your argument, though I believe there are other factors to consider.",
                            "You make a fair point, however there's another way to look at this.",
                            "Interesting take, but let me counter with this perspective.",
                            "I hear what you're saying, though I'd argue differently."
                        ).random()}"
                    }
                } else {
                    // For other errors, also provide fallback response
                    Log.w("DebateViewModel", "⚠️ Using fallback response due to error: ${e.message}")
                    "I appreciate your argument. Let me respond: ${listOf(
                        "That's worth considering, but there are other aspects to this debate.",
                        "You've raised some good points, though I see it differently.",
                        "Interesting perspective, but I'd like to challenge that view.",
                        "I understand where you're coming from, but let me counter.",
                        "Fair point, however there's another angle to explore here."
                    ).random()}"
                }
            }

            Log.d("DebateViewModel", "✅ AI raw response (${aiResponseText.length} chars): $aiResponseText")

            // Check for repetitive gibberish (same word repeated)
            val words = aiResponseText.split(" ").filter { it.length > 3 }
            val uniqueWords = words.toSet()
            val isGibberish =
                words.size > 5 && uniqueWords.size < words.size * 0.3 // Less than 30% unique words

            // ALSO check for single-word loops (e.g., "human human human")
            val wordCounts = words.groupingBy { it.lowercase() }.eachCount()
            val maxRepeat = wordCounts.values.maxOrNull() ?: 0
            val hasLoops = maxRepeat > 3 // Same word more than 3 times = loop

            if (isGibberish || hasLoops) {
                Log.e(
                    "DebateViewModel",
                    "⚠️ Detected ${if (hasLoops) "LOOP" else "gibberish"} response (${uniqueWords.size} unique out of ${words.size} words, max repeat: $maxRepeat)"
                )
                // Use a contextual fallback instead
                val topic = currentSession.topic.title
                val aiSide =
                    if (currentSession.player2Side == DebateSide.FOR) "support" else "oppose"
                val fallbackResponses = listOf(
                    "I $aiSide this because the evidence clearly shows long-term benefits that outweigh any concerns.",
                    "While I understand your point, I believe the practical implications support the opposite conclusion.",
                    "Let me counter that: research and real-world examples suggest a different perspective is more valid.",
                    "That's an interesting argument, but I'd emphasize that fundamental principles point in another direction.",
                    "I hear your reasoning, however the broader context indicates we should $aiSide this position."
                )
                val fallbackResponse = fallbackResponses.random()

                // Add fallback message
                val aiMessage = DebateMessage(
                    id = UUID.randomUUID().toString(),
                    playerId = "ai_opponent",
                    playerName = "AI Debater",
                    message = fallbackResponse,
                    timestamp = System.currentTimeMillis(),
                    turnNumber = currentSession.messages.size + 1
                )

                _currentSession.value = currentSession.copy(
                    messages = currentSession.messages + aiMessage,
                    currentTurn = _currentUser.value?.id ?: ""
                )
                _statusMessage.value = "Your turn!"
                Log.w(
                    "DebateViewModel",
                    "Used fallback due to ${if (hasLoops) "repetition loop" else "gibberish"}"
                )
                return
            }

            // Trim to reasonable length (3-4 sentences max, 250 chars limit)
            val trimmedResponse = aiResponseText.lines()
                .filter { it.isNotBlank() }
                .take(4)
                .joinToString("\n")
                .take(250) // Max 250 chars
                .trim()

            if (trimmedResponse.isEmpty()) {
                Log.e("DebateViewModel", "❌ AI response was empty after trimming!")
                // Provide a generic counter-argument
                val fallbackResponse = "I understand your position, but I'd like to present an alternative viewpoint on this matter."
                Log.d("DebateViewModel", "Using fallback response")

                // Add the fallback message
                val aiMessage = DebateMessage(
                    id = UUID.randomUUID().toString(),
                    playerId = "ai_opponent",
                    playerName = "AI Debater",
                    message = fallbackResponse,
                    timestamp = System.currentTimeMillis(),
                    turnNumber = currentSession.messages.size + 1
                )

                _currentSession.value = currentSession.copy(
                    messages = currentSession.messages + aiMessage,
                    currentTurn = _currentUser.value?.id ?: ""
                )
                _statusMessage.value = "Your turn!"
                return
            }

            Log.d("DebateViewModel", "✅ Trimmed response: $trimmedResponse")

            // Show typing animation
            _isAITyping.value = true
            _aiTypingText.value = ""
            _statusMessage.value = "💭 AI is responding..."

            // Type word by word (slower: 60ms per word)
            val typingWords = trimmedResponse.split(" ")
            for (word in typingWords) {
                _aiTypingText.value += "$word "
                delay(60) // 60ms per word (slower for quality)
            }

            _isAITyping.value = false

            // Add AI message TO CURRENT SESSION (not old session!)
            val aiMessage = DebateMessage(
                id = UUID.randomUUID().toString(),
                playerId = "ai_opponent",
                playerName = "AI Debater",
                message = trimmedResponse,
                timestamp = System.currentTimeMillis(),
                turnNumber = currentSession.messages.size + 1
            )

            val finalMessages = currentSession.messages + aiMessage

            // Store in chat history
            chatHistory.add(
                ChatTurn(
                    speaker = "ai",
                    message = trimmedResponse,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Update argument history to prevent repetition in next turn
            updateAIArgumentHistory(trimmedResponse)

            Log.d("DebateViewModel", "⚖️ Judging AI response...")
            // JUDGE AI'S RESPONSE
            _statusMessage.value = "⚖️ Judging AI's argument..."
            val aiScore = judgeResponse(
                currentResponse = trimmedResponse,
                previousOpponentResponse = chatHistory.findLast { it.speaker == "player" }?.message
                    ?: "",
                speaker = "AI",
                difficulty = currentSession.gameMode,
                turnNumber = aiMessage.turnNumber
            )

            Log.d("DebateViewModel", "AI Score: ${aiScore.score}/10")

            // Show AI score pop-up
            _currentTurnScore.value = aiScore
            _showScorePopup.value = true
            delay(3000) // Show for 3 seconds
            _showScorePopup.value = false

            // Update accumulated scores
            val accumulated = _accumulatedScores.value ?: AccumulatedScores(0, 0, 0, 0)
            _accumulatedScores.value = accumulated.copy(
                aiTotalScore = accumulated.aiTotalScore + aiScore.score,
                aiTurnCount = accumulated.aiTurnCount + 1
            )

            // Back to player turn - use CURRENT session
            _currentSession.value = currentSession.copy(
                messages = finalMessages,
                currentTurn = _currentUser.value?.id ?: ""
            )

            _statusMessage.value = "Your turn! Respond to the AI's argument."
            Log.d("DebateViewModel", "✅ AI turn complete, back to player")

        } catch (e: Exception) {
            Log.e("DebateViewModel", "❌ CRITICAL ERROR in generateAIResponseWithDelay", e)
            _statusMessage.value = "AI encountered an error: ${e.message}"
            // Get fresh session and switch back to player
            val currentSess = _currentSession.value
            if (currentSess != null) {
                _currentSession.value = currentSess.copy(currentTurn = _currentUser.value?.id ?: "")
                _statusMessage.value = "Your turn! (AI had an error)"
            }
        }
    }

    /**
     * Judge a response based on previous opponent's argument
     * Uses streaming (which works) and parses simple text format (not JSON)
     */
    private suspend fun judgeResponse(
        currentResponse: String,
        previousOpponentResponse: String,
        speaker: String,
        difficulty: GameMode,
        turnNumber: Int = 0
    ): TurnScore {
        return try {
            val judgingPrompt = buildJudgingPromptTurnBased(
                currentResponse = currentResponse,
                previousOpponentResponse = previousOpponentResponse,
                speaker = speaker,
                turnNumber = turnNumber
            )

            Log.d("DebateViewModel", "⚖️ Judging prompt:\n$judgingPrompt")

            // Use streaming API (which actually works) and parse text output
            val judgeResponse = StringBuilder()
            try {
                // Longer timeout: 30 seconds for judging
                kotlinx.coroutines.withTimeout(30000) {
                    RunAnywhere.generateStream(judgingPrompt).collect { token ->
                        judgeResponse.append(token)
                    }
                }
            } catch (e: Exception) {
                Log.e("DebateViewModel", "❌ Streaming judging failed", e)
                // If streaming fails, wait and retry once
                if (e.message?.contains("LLM component not initialized") == true) {
                    Log.w("DebateViewModel", "⚠️ LLM not ready, waiting 2s and retrying...")
                    delay(2000)
                    try {
                        RunAnywhere.generateStream(judgingPrompt).collect { token ->
                            judgeResponse.append(token)
                        }
                    } catch (e2: Exception) {
                        Log.e("DebateViewModel", "❌ Retry also failed", e2)
                    }
                }
            }

            val responseText = judgeResponse.toString()
            Log.d("DebateViewModel", "Judge response for $speaker: $responseText")

            if (responseText.isBlank()) {
                Log.w("DebateViewModel", "⚠️ Empty judge response, using smart default score")

                // Give a smarter default score based on response length
                val messageLength = currentResponse.length
                val defaultScore = when {
                    messageLength < 20 -> 4  // Very short = probably weak
                    messageLength < 50 -> 5  // Short = mediocre
                    messageLength < 100 -> 6 // Medium = decent
                    messageLength < 150 -> 7 // Good length = good
                    else -> 8                // Long, detailed = very good
                }.coerceIn(4, 8)

                return TurnScore(
                    speaker = speaker,
                    score = defaultScore,
                    reasoning = "Judging unavailable - scored ${messageLength} chars: ${if (messageLength < 50) "brief" else if (messageLength < 100) "moderate" else "detailed"}",
                    hasProfanity = false,
                    factCheck = "Not evaluated"
                )
            }

            // Parse the simple text format
            parseJudgeScoreFromText(responseText, speaker)

        } catch (e: Exception) {
            Log.e("DebateViewModel", "Error judging response", e)
            // Return default score on error
            TurnScore(
                speaker = speaker,
                score = 5,
                reasoning = "Unable to score this turn due to technical error",
                hasProfanity = false,
                factCheck = "Not evaluated"
            )
        }
    }

    /**
     * Build judging prompt for simple text output (not JSON)
     * EMERGENCY FIX: Ultra-simple format for reliable parsing
     */
    private fun buildJudgingPromptTurnBased(
        currentResponse: String,
        previousOpponentResponse: String,
        speaker: String,
        turnNumber: Int = 0
    ): String {
        // ULTRA SIMPLE - just the facts
        return buildString {
            if (previousOpponentResponse.isNotEmpty()) {
                append("Opponent said: \"$previousOpponentResponse\"\n\n")
            }
            append("$speaker said: \"$currentResponse\"\n\n")
            append("Judge this argument (0-10):\n")
            append("- Does it make logical sense? (0-5 pts)\n")
            append("- Is it persuasive? (0-5 pts)\n\n")
            append("Your score:\n")
            append("Score: ")
        }.trim()
    }

    /**
     * Parse simple text format instead of JSON
     * Looks for "Score: X/10" pattern and extracts information
     */
    private fun parseJudgeScoreFromText(response: String, speaker: String): TurnScore {
        return try {
            Log.d("DebateViewModel", "Parsing judge response: $response")

            // Extract score - look for any number 0-10
            val scoreRegex = """(\d+)""".toRegex()
            val scoreMatch = scoreRegex.find(response)
            val score = scoreMatch?.groupValues?.get(1)?.toIntOrNull()?.coerceIn(0, 10) ?: 6

            // Get first sentence as reasoning
            val reasoning = response
                .split("\n")
                .firstOrNull { it.length > 10 && !it.contains("Score:", ignoreCase = true) }
                ?.take(100)
                ?: "Evaluated based on logic and persuasion"

            Log.d("DebateViewModel", "✅ Parsed: score=$score, reasoning=$reasoning")

            TurnScore(
                speaker = speaker,
                score = score,
                reasoning = reasoning.trim(),
                hasProfanity = false,
                factCheck = "reasonable"
            )
        } catch (e: Exception) {
            Log.e("DebateViewModel", "Error parsing judge score", e)
            // Default to middle score
            TurnScore(
                speaker = speaker,
                score = 6,
                reasoning = "Argument evaluated",
                hasProfanity = false,
                factCheck = "reasonable"
            )
        }
    }

    /**
     * Build AI debate prompt with IQ-based difficulty
     * Enhanced: Context-aware, phase-based, and anti-repetition
     * SIMPLIFIED: Only varies word count for IQ/difficulty.
     */
    private fun buildAIPrompt(session: DebateSession): String {
        val topic = session.topic
        val aiSide = session.player2Side
        val sideText = if (aiSide == DebateSide.FOR) "in favor of" else "against"

        // Get AI IQ from session
        val aiIQ =
            session.player2?.name?.substringAfter("IQ ")?.substringBefore(")")?.toIntOrNull() ?: 75

        val currentTurn = session.messages.size + 1
        val phase = getDebatePhase(currentTurn)

        // Get recent context (last 4 messages = last 2 full exchanges)
        val recentContext = session.messages.takeLast(4).joinToString("\n") { msg ->
            val speaker = if (msg.playerId == session.player1.id)
                session.player1.name else "AI"
            "$speaker: ${msg.message}"
        }

        // Get opponent's LAST message for direct rebuttal
        val opponentLast = session.messages
            .lastOrNull { it.playerId == session.player1.id }
            ?.message ?: ""

        // Phase-specific instruction
        val instruction = when (phase) {
            "OPENING" -> "Make your opening argument with one strong reason"
            "DEVELOPMENT" -> "Add new evidence or examples to support your position"
            "CLASH" -> "Counter the opponent's argument: \"$opponentLast\""
            else -> "Give your final summary of the strongest point"
        }

        // Difficulty affects word count only
        val wordCount = when {
            aiIQ < 65 -> "30-40 words, keep it simple"
            aiIQ < 80 -> "40-50 words"
            else -> "50-60 words, be sophisticated"
        }

        // CRITICAL: Clear, direct format
        return buildString {
            append("You are arguing $sideText the topic: \"${topic.title}\"\n\n")
            append("Your task: $instruction\n\n")
            if (recentContext.isNotEmpty()) {
                append("Recent debate:\n$recentContext\n\n")
            }
            append("Write your complete response now ($wordCount):")
        }.trim()
    }

    // === DEBATE ENDING ===
    private suspend fun endDebate() {
        val session = _currentSession.value ?: return

        // Stop heartbeat when debate ends
        stopModelHeartbeat()
        
        _currentSession.value = session.copy(status = DebateStatus.JUDGING)
        _statusMessage.value = "Time's up! AI is judging the debate..."
        // DO NOT set _currentScreen - MainActivity will observe currentSession and navigate
        // _currentScreen.value = DebateScreen.DEBATE_RESULTS

        // Generate comprehensive scores and feedback
        generateDebateScores(session)
    }

    private suspend fun generateDebateScores(session: DebateSession) {
        try {
            _statusMessage.value = "Calculating final scores..."

            // Use accumulated turn scores as base
            val accumulated = _accumulatedScores.value ?: AccumulatedScores(0, 0, 0, 0)

            val playerAvgScore = if (accumulated.playerTurnCount > 0)
                accumulated.playerTotalScore.toFloat() / accumulated.playerTurnCount
            else 5f

            val aiAvgScore = if (accumulated.aiTurnCount > 0)
                accumulated.aiTotalScore.toFloat() / accumulated.aiTurnCount
            else 5f

            // WINNER DETERMINATION: Based on accumulated scores (objective)
            val winner = if (accumulated.playerTotalScore > accumulated.aiTotalScore) {
                session.player1.id
            } else if (accumulated.playerTotalScore < accumulated.aiTotalScore) {
                "ai_opponent"
            } else {
                // Tie: Use LLM as tiebreaker
                val tiebreaker = judgeFinalDebate(session, playerAvgScore, aiAvgScore)
                tiebreaker.winner
            }

            Log.d(
                "DebateViewModel", """
                📊 Final Score Calculation:
                Player: ${accumulated.playerTotalScore} pts (${accumulated.playerTurnCount} turns, avg ${
                    String.format(
                        "%.1f",
                        playerAvgScore
                    )
                })
                AI: ${accumulated.aiTotalScore} pts (${accumulated.aiTurnCount} turns, avg ${
                    String.format(
                        "%.1f",
                        aiAvgScore
                    )
                })
                Winner: ${if (winner == session.player1.id) session.player1.name else "AI"} ${if (accumulated.playerTotalScore == accumulated.aiTotalScore) "(TIEBREAKER)" else "(SCORE)"}
            """.trimIndent()
            )

            // Get LLM feedback for strengths/weaknesses (but not winner decision)
            _statusMessage.value = "Generating performance feedback..."
            val feedback = generatePerformanceFeedback(session, playerAvgScore, aiAvgScore)

            // Combine turn-by-turn scores with feedback
            val finalScores = DebateScores(
                player1Score = PlayerScore(
                    playerId = session.player1.id,
                    playerName = session.player1.name,
                    logicReasoning = (playerAvgScore * 0.5).toInt().coerceIn(0, 10),
                    evidenceQuality = (playerAvgScore * 0.5).toInt().coerceIn(0, 10),
                    toneRespect = 8, // Assume good unless profanity detected
                    counterArguments = (playerAvgScore * 0.6).toInt().coerceIn(0, 10),
                    factualAccuracy = 7, // Default unless flagged
                    totalScore = accumulated.playerTotalScore,
                    feedback = feedback.playerFeedback
                ),
                player2Score = PlayerScore(
                    playerId = "ai_opponent",
                    playerName = "AI Debater",
                    logicReasoning = (aiAvgScore * 0.5).toInt().coerceIn(0, 10),
                    evidenceQuality = (aiAvgScore * 0.5).toInt().coerceIn(0, 10),
                    toneRespect = 8,
                    counterArguments = (aiAvgScore * 0.6).toInt().coerceIn(0, 10),
                    factualAccuracy = 7,
                    totalScore = accumulated.aiTotalScore,
                    feedback = feedback.aiFeedback
                ),
                winner = winner,
                feedback = feedback.overallAnalysis,
                detailedAnalysis = feedback.detailedAnalysis
            )

            _currentSession.value = session.copy(
                status = DebateStatus.FINISHED,
                scores = finalScores
            )

            val winnerName = if (finalScores.winner == session.player1.id)
                session.player1.name else "AI Debater"

            _statusMessage.value = "Debate complete! Winner: $winnerName"

            saveDebateResults(session, finalScores)

        } catch (e: Exception) {
            Log.e("DebateViewModel", "Error in final judging", e)
            _statusMessage.value = "Error judging debate: ${e.message}"
        }
    }


    /**
     * Final judgment with simplified pairwise comparison
     * Used ONLY for tiebreaker or for generating feedback (not winner decision unless tied)
     * FIXED: Now uses Qwen chat template format
     */
    private suspend fun judgeFinalDebate(
        session: DebateSession,
        playerAvgScore: Float,
        aiAvgScore: Float
    ): FinalVerdict {

        val topic = session.topic
        val playerSide = if (session.player1Side == DebateSide.FOR) "FOR" else "AGAINST"
        val aiSide = if (session.player2Side == DebateSide.FOR) "FOR" else "AGAINST"

        // Create condensed transcript (only key messages, truncated for token efficiency)
        val transcript = session.messages
            .mapIndexed { index, msg ->
                val speaker = if (msg.playerId == session.player1.id)
                    "${session.player1.name} ($playerSide)"
                else
                    "AI ($aiSide)"
                "Turn ${index + 1} - $speaker: ${msg.message.take(150)}..."
            }
            .joinToString("\n\n")

        val judgmentContent = buildString {
            append("You are the final judge for this debate.\n\n")
            append("TOPIC: \"${topic.title}\"\n")
            append("${session.player1.name} argues: $playerSide\n")
            append("AI argues: $aiSide\n\n")
            append("TURN-BY-TURN SCORES:\n")
            append(
                "- ${session.player1.name}: Average ${
                    String.format(
                        "%.1f",
                        playerAvgScore
                    )
                }/10 per turn\n"
            )
            append("- AI: Average ${String.format("%.1f", aiAvgScore)}/10 per turn\n\n")
            append("DEBATE TRANSCRIPT:\n")
            append("$transcript\n\n")
            append("YOUR TASK: Decide the winner based on:\n")
            append("1. Who made better arguments overall?\n")
            append("2. Who addressed opponent's points better?\n")
            append("3. Who was more persuasive?\n")
            append("4. Who stayed consistent and logical?\n\n")
            append("Respond in this EXACT format:\n\n")
            append("Winner: [${session.player1.name} or AI]\n")
            append("Reason: [one sentence why they won]\n")
            append("${session.player1.name} Strength: [main strength in one sentence]\n")
            append("${session.player1.name} Weakness: [main weakness in one sentence]\n")
            append("AI Strength: [main strength in one sentence]\n")
            append("AI Weakness: [main weakness in one sentence]")
        }

        // Use Qwen chat template format - CRITICAL FIX
        val prompt = """<|im_start|>system
You are an expert debate judge. Analyze arguments objectively and provide clear, concise feedback.<|im_end|>
<|im_start|>user
$judgmentContent<|im_end|>
<|im_start|>assistant
""".trimIndent()

        val response = try {
            val result = StringBuilder()
            RunAnywhere.generateStream(prompt).collect { token ->
                result.append(token)
            }
            result.toString()
        } catch (e: Exception) {
            Log.e("DebateViewModel", "Final judging failed", e)
            // Fallback to turn scores
            if (playerAvgScore > aiAvgScore) {
                """
                Winner: ${session.player1.name}
                Reason: Better average turn scores
                ${session.player1.name} Strength: Consistent performance
                ${session.player1.name} Weakness: Could use more evidence
                AI Strength: Logical arguments
                AI Weakness: Less persuasive overall
                """.trimIndent()
            } else {
                """
                Winner: AI
                Reason: Better average turn scores
                ${session.player1.name} Strength: Good engagement
                ${session.player1.name} Weakness: Arguments less developed
                AI Strength: Strong logical reasoning
                AI Weakness: Could be more varied
                """.trimIndent()
            }
        }

        return parseFinalVerdict(response, session)
    }

    /**
     * Generate performance feedback ONLY (strengths/weaknesses), not winner/loser
     * FIXED: Now uses Qwen chat template format
     */
    private suspend fun generatePerformanceFeedback(
        session: DebateSession,
        playerAvgScore: Float,
        aiAvgScore: Float
    ): FinalVerdict {

        val topic = session.topic
        val playerSide = if (session.player1Side == DebateSide.FOR) "FOR" else "AGAINST"
        val aiSide = if (session.player2Side == DebateSide.FOR) "FOR" else "AGAINST"

        val transcript = session.messages
            .mapIndexed { index, msg ->
                val speaker = if (msg.playerId == session.player1.id)
                    "${session.player1.name} ($playerSide)"
                else
                    "AI ($aiSide)"
                "Turn ${index + 1} - $speaker: ${msg.message.take(150)}..."
            }
            .joinToString("\n\n")

        val feedbackContent = buildString {
            append("You are a debate coach. Please give performance feedback on the two participants.\n\n")
            append("Focus on:\n")
            append("- Three strengths for the PLAYER (${session.player1.name})\n")
            append("- Three areas for improvement for PLAYER\n")
            append("- One summary sentence of PLAYER's overall performance and learning points\n")
            append("- Three strengths for AI\n")
            append("- Three areas for improvement for AI\n")
            append("- One summary sentence about AI's performance\n\n")
            append("Do NOT declare any winner or loser. Just focus on constructive feedback.\n\n")
            append("TOPIC: \"${topic.title}\"\n")
            append("${session.player1.name} argues: $playerSide\n")
            append("AI argues: $aiSide\n\n")
            append("TURN-BY-TURN SCORES:\n")
            append(
                "- ${session.player1.name}: Average ${
                    String.format(
                        "%.1f",
                        playerAvgScore
                    )
                }/10 per turn\n"
            )
            append("- AI: Average ${String.format("%.1f", aiAvgScore)}/10 per turn\n\n")
            append("DEBATE TRANSCRIPT:\n")
            append("$transcript\n\n")
            append("FORMAT:\n")
            append("PLAYER Strengths: [list, comma-separated]\n")
            append("PLAYER Areas to Improve: [list, comma-separated]\n")
            append("PLAYER Summary: [one sentence]\n")
            append("AI Strengths: [list, comma-separated]\n")
            append("AI Areas to Improve: [list, comma-separated]\n")
            append("AI Summary: [one sentence]")
        }

        // Use Qwen chat template format - CRITICAL FIX
        val prompt = """<|im_start|>system
You are a constructive debate coach. Provide detailed, actionable feedback to help debaters improve.<|im_end|>
<|im_start|>user
$feedbackContent<|im_end|>
<|im_start|>assistant
""".trimIndent()

        val response = try {
            val result = StringBuilder()
            RunAnywhere.generateStream(prompt).collect { token ->
                result.append(token)
            }
            result.toString()
        } catch (e: Exception) {
            Log.e("DebateViewModel", "Performance feedback failed", e)
            """
PLAYER Strengths: Good engagement, logical reasoning, clear arguments
PLAYER Areas to Improve: Use more examples, address more rebuttals, improve clarity
PLAYER Summary: Solid performance overall but could improve in some areas.
AI Strengths: Consistent logic, varied arguments, strong counterpoints
AI Areas to Improve: More persuasive language, less repetition, clearer structure
AI Summary: The AI gave well-structured arguments but could connect better with the opponent.
            """.trimIndent()
        }

        // Parse feedback structure
        val lines = response.lines().map { it.trim() }
        fun extract(prefix: String): String {
            return lines.find { it.startsWith(prefix, ignoreCase = true) }
                ?.substringAfter(":")
                ?.trim()
                ?: ""
        }

        val playerStrength = extract("PLAYER Strengths")
        val playerWeakness = extract("PLAYER Areas to Improve")
        val playerSummary = extract("PLAYER Summary")
        val aiStrength = extract("AI Strengths")
        val aiWeakness = extract("AI Areas to Improve")
        val aiSummary = extract("AI Summary")
        val overallAnalysis = playerSummary
        val detailedAnalysis = response

        return FinalVerdict(
            winner = "", // not used -- feedback only
            overallAnalysis = overallAnalysis,
            playerFeedback = "Strengths: $playerStrength. Areas to improve: $playerWeakness. $playerSummary",
            aiFeedback = "Strengths: $aiStrength. Areas to improve: $aiWeakness. $aiSummary",
            detailedAnalysis = detailedAnalysis
        )
    }

    /**
     * Parse the simplified final verdict format
     */
    private fun parseFinalVerdict(response: String, session: DebateSession): FinalVerdict {
        val lines = response.lines().map { it.trim() }

        fun extract(prefix: String): String {
            return lines.find { it.startsWith(prefix, ignoreCase = true) }
                ?.substringAfter(":")
                ?.trim()
                ?: ""
        }

        val winnerText = extract("Winner")
        val winner = if (winnerText.contains(session.player1.name, ignoreCase = true))
            session.player1.id
        else
            "ai_opponent"

        val reason = extract("Reason")
        val playerStrength = extract("${session.player1.name} Strength")
        val playerWeakness = extract("${session.player1.name} Weakness")
        val aiStrength = extract("AI Strength")
        val aiWeakness = extract("AI Weakness")

        return FinalVerdict(
            winner = winner,
            overallAnalysis = reason,
            playerFeedback = "Strength: $playerStrength. Weakness: $playerWeakness",
            aiFeedback = "Strength: $aiStrength. Weakness: $aiWeakness",
            detailedAnalysis = response
        )
    }

    /**
     * Save debate results to server and update user stats
     */
    private suspend fun saveDebateResults(session: DebateSession, scores: DebateScores) {
        try {
            // Save debate history to server
            val result = serverRepository.saveDebateResult(
                topic = session.topic.title,
                userSide = session.player1Side.toString(),
                opponentType = session.gameMode.toString(),
                userScore = scores.player1Score.totalScore,
                opponentScore = scores.player2Score.totalScore,
                feedback = scores.feedback
            )

            result.onSuccess {
                Log.d("DebateViewModel", "✅ Debate results saved to server successfully")
            }.onFailure { error ->
                Log.e("DebateViewModel", "❌ Failed to save debate results: ${error.message}")
                _statusMessage.value = "Warning: Could not save debate results to server"
            }

        } catch (e: Exception) {
            // Log error but don't stop the flow
            Log.e("DebateViewModel", "Error saving debate results", e)
            _statusMessage.value = "Warning: Could not save debate results"
        }
    }

    // === NAVIGATION ===
    fun navigateToScreen(screen: DebateScreen) {
        _currentScreen.value = screen
    }

    fun startNewDebate() {
        _currentSession.value = null
        // DO NOT set _currentScreen - MainActivity will observe currentSession and navigate
        // _currentScreen.value = DebateScreen.MAIN_MENU
        _statusMessage.value = "Ready for another debate!"
    }

    /**
     * Handle match forfeit/abandonment
     * Saves the debate as a loss in history when player leaves during active debate
     */
    fun forfeitDebate() {
        val session = _currentSession.value ?: return
        val user = _currentUser.value ?: return

        // Only forfeit if debate was in progress or prep
        if (session.status != DebateStatus.IN_PROGRESS && session.status != DebateStatus.PREP_TIME) {
            _currentSession.value = null
            return
        }

        Log.d("DebateViewModel", "🏳️ Player forfeited the debate")

        // Stop heartbeat when forfeiting
        stopModelHeartbeat()

        viewModelScope.launch {
            try {
                // Create forfeit result with minimum scores
                val forfeitScores = DebateScores(
                    player1Score = PlayerScore(
                        playerId = user.id,
                        playerName = user.name,
                        logicReasoning = 0,
                        evidenceQuality = 0,
                        toneRespect = 0,
                        counterArguments = 0,
                        factualAccuracy = 0,
                        totalScore = 0,
                        feedback = "Match forfeited"
                    ),
                    player2Score = PlayerScore(
                        playerId = "ai_opponent",
                        playerName = session.player2?.name ?: "AI",
                        logicReasoning = 10,
                        evidenceQuality = 10,
                        toneRespect = 10,
                        counterArguments = 10,
                        factualAccuracy = 10,
                        totalScore = 50,
                        feedback = "Won by forfeit"
                    ),
                    winner = "ai_opponent",
                    feedback = "Player forfeited the debate",
                    detailedAnalysis = "Match was forfeited by the player. No score was recorded."
                )

                // Save to server as a loss
                val result = serverRepository.saveDebateResult(
                    topic = session.topic.title,
                    userSide = session.player1Side.toString(),
                    opponentType = session.gameMode.toString(),
                    userScore = 0,
                    opponentScore = 50,
                    feedback = "Match forfeited - counted as loss"
                )

                result.onSuccess {
                    Log.d("DebateViewModel", "✅ Forfeit recorded in history as loss")
                }.onFailure { error ->
                    Log.e("DebateViewModel", "❌ Failed to record forfeit: ${error.message}")
                }

                // Clear current session
                _currentSession.value = null
                _statusMessage.value = "Debate forfeited"

            } catch (e: Exception) {
                Log.e("DebateViewModel", "Error recording forfeit", e)
                _currentSession.value = null
            }
        }
    }

    // === MODEL HEARTBEAT ===

    /**
     * Keep model alive during debate with periodic pings
     * DISABLED: Causing "LLM component not initialized" errors
     */
    private fun startModelHeartbeat() {
        // DISABLED - heartbeat was causing model crashes
        Log.d("DebateViewModel", "💔 Heartbeat disabled (was causing crashes)")

        /* OLD CODE - CAUSING CRASHES:
        modelHeartbeatJob?.cancel() // Cancel any existing heartbeat
        modelHeartbeatJob = viewModelScope.launch {
            while (_currentSession.value?.status == DebateStatus.IN_PROGRESS) {
                try {
                    delay(30000) // Every 30 seconds
                    // Send tiny generation request to keep model alive
                    val heartbeat = StringBuilder()
                    kotlinx.coroutines.withTimeout(5000) {
                        RunAnywhere.generateStream("ok").collect { token ->
                            heartbeat.append(token)
                        }
                    }
                    Log.d("DebateViewModel", "💓 Model heartbeat successful")
                } catch (e: Exception) {
                    Log.w("DebateViewModel", "⚠️ Model heartbeat failed: ${e.message}")
                }
            }
        }
        */
    }

    /**
     * Stop model heartbeat when debate ends
     */
    private fun stopModelHeartbeat() {
        modelHeartbeatJob?.cancel()
        modelHeartbeatJob = null
        Log.d("DebateViewModel", "💔 Model heartbeat stopped")
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
                val success = try {
                    RunAnywhere.loadModel(modelId)
                } catch (e: Exception) {
                    Log.e("DebateViewModel", "Error loading model", e)
                    false
                }

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

    fun resetModelDownloadFlag() {
        _needsModelDownload.value = false
    }

    enum class DebateScreen {
        LOGIN, MAIN_MENU, MODEL_SETUP, DEBATE_PREP, DEBATE_ACTIVE, DEBATE_RESULTS
    }
}