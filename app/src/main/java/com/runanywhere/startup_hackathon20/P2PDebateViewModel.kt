package com.runanywhere.startup_hackathon20

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.startup_hackathon20.network.P2PDebateService
import com.runanywhere.startup_hackathon20.network.ServerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * P2P Debate ViewModel
 * Manages state for player-vs-player debates
 * Similar to DebateViewModel but uses Supabase for real-time sync
 * NOW WITH TIMEOUT CANCELLATION like AI mode!
 */
class P2PDebateViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "P2PDebateViewModel"
    private val p2pService = P2PDebateService()
    private val serverRepository = ServerRepository(application)

    // Session data
    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId

    private val _sessionData = MutableStateFlow<P2PDebateService.P2PSessionResponse?>(null)
    val sessionData: StateFlow<P2PDebateService.P2PSessionResponse?> = _sessionData

    // Current user
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _currentUserName = MutableStateFlow<String>("")
    val currentUserName: StateFlow<String> = _currentUserName

    // Opponent info
    private val _opponentId = MutableStateFlow<String?>(null)
    val opponentId: StateFlow<String?> = _opponentId

    private val _opponentName = MutableStateFlow<String>("")
    val opponentName: StateFlow<String> = _opponentName

    // Messages
    private val _messages = MutableStateFlow<List<DebateMessage>>(emptyList())
    val messages: StateFlow<List<DebateMessage>> = _messages

    // Turn state
    private val _isMyTurn = MutableStateFlow(false)
    val isMyTurn: StateFlow<Boolean> = _isMyTurn

    private val _turnNumber = MutableStateFlow(0)
    val turnNumber: StateFlow<Int> = _turnNumber

    // Debate status
    private val _debateStatus = MutableStateFlow("PREP")
    val debateStatus: StateFlow<String> = _debateStatus

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Scoring (same as AI mode)
    private val _showScorePopup = MutableStateFlow(false)
    val showScorePopup: StateFlow<Boolean> = _showScorePopup

    private val _currentTurnScore = MutableStateFlow<TurnScore?>(null)
    val currentTurnScore: StateFlow<TurnScore?> = _currentTurnScore

    private val _accumulatedScores = MutableStateFlow<AccumulatedScores?>(null)
    val accumulatedScores: StateFlow<AccumulatedScores?> = _accumulatedScores

    private val _finalScores = MutableStateFlow<Pair<Int, Int>?>(null) // (myScore, opponentScore)
    val finalScores: StateFlow<Pair<Int, Int>?> = _finalScores

    private val _myStrengths = MutableStateFlow<List<String>>(emptyList())
    val myStrengths: StateFlow<List<String>> = _myStrengths

    private val _myWeaknesses = MutableStateFlow<List<String>>(emptyList())
    val myWeaknesses: StateFlow<List<String>> = _myWeaknesses

    // Polling jobs
    private var messagePollJob: Job? = null
    private var turnPollJob: Job? = null
    private var statusPollJob: Job? = null
    private var timerJob: Job? = null
    private var forfeitDetectionJob: Job? = null

    // 🔥 NEW: AI generation jobs (for cancellation on timeout)
    private var aiJudgingJob: Job? = null
    private var isDebateTimedOut = false

    // Track current model ID for reloading
    private val _currentModelId = MutableStateFlow<String?>(null)

    // NEW: Client-side timer for countdown
    private val _clientTimeRemaining = MutableStateFlow(900000L) // 15 minutes
    val clientTimeRemaining: StateFlow<Long> = _clientTimeRemaining

    // NEW: Track session start time for PREP transition
    private var sessionStartTime = 0L

    // NEW: Track last opponent activity for forfeit detection
    private var lastOpponentActivityTime = System.currentTimeMillis()

    /**
     * CRITICAL FIX: Reload model to clear KV cache before generation
     * This prevents sequence position mismatches in llama-android
     * NOW WITH VERIFICATION like AI mode!
     */
    private suspend fun reloadModelForFreshGeneration(): Boolean {
        // Try to get model ID from DebateViewModel's loaded model
        // or use a known model name
        return try {
            Log.d(TAG, "🔄 Reloading model to clear KV cache...")

            // Get the Llama model ID (assuming it's loaded)
            val models = com.runanywhere.sdk.public.extensions.listAvailableModels()
            val llamaModel = models.find { it.name.contains("Llama 3.2 1B") }

            if (llamaModel == null) {
                Log.w(TAG, "⚠️ Could not find loaded model for reload")
                return false
            }

            _currentModelId.value = llamaModel.id
            val success = RunAnywhere.loadModel(llamaModel.id)

            if (success) {
                Log.d(TAG, "✅ Model reloaded successfully, KV cache cleared")
                // Small delay to ensure model is ready
                delay(500)

                // CRITICAL FIX: Verify model works with test generation (like AI mode)
                Log.d(TAG, "🧪 Testing model with simple prompt...")
                var testSuccess = false
                var attempts = 0
                val maxAttempts = 3

                while (!testSuccess && attempts < maxAttempts) {
                    attempts++
                    try {
                        Log.d(TAG, "🧪 Test attempt $attempts/$maxAttempts...")
                        val testResponse = StringBuilder()
                        RunAnywhere.generateStream("Say 'ready'").collect { token ->
                            testResponse.append(token)
                        }
                        Log.d(TAG, "✅ Model test successful: $testResponse")
                        testSuccess = true
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Model test attempt $attempts failed", e)
                        if (attempts < maxAttempts) {
                            delay(2000) // Wait 2 seconds between retries
                        }
                    }
                }

                if (!testSuccess) {
                    Log.w(TAG, "⚠️ Model test failed after $maxAttempts attempts")
                    return false
                }
            } else {
                Log.e(TAG, "❌ Failed to reload model")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reloading model", e)
            false
        }
    }

    /**
     * Initialize P2P debate session
     * Called after match is found
     */
    fun initializeSession(
        sessionId: String,
        currentUserId: String,
        currentUserName: String
    ) {
        _sessionId.value = sessionId
        _currentUserId.value = currentUserId
        _currentUserName.value = currentUserName

        Log.d(TAG, "🎮 Initializing P2P session: $sessionId")

        viewModelScope.launch {
            try {
                // Load session data
                val result = p2pService.getSession(sessionId)
                result.onSuccess { session ->
                    _sessionData.value = session
                    _debateStatus.value = session.status
                    _turnNumber.value = session.turn_number

                    // Determine opponent
                    val (oppId, oppName) = p2pService.getOpponentInfo(sessionId, currentUserId)
                        .getOrNull() ?: Pair("", "Unknown")
                    _opponentId.value = oppId
                    _opponentName.value = oppName

                    // Check if it's my turn
                    _isMyTurn.value = session.current_turn == currentUserId

                    Log.d(TAG, "✅ Session loaded: ${session.topic_title}")
                    Log.d(TAG, "Opponent: $oppName, My turn: ${_isMyTurn.value}")

                    // Load existing messages
                    loadMessages()

                    // Start polling for updates
                    startPolling()

                    // Initialize scores
                    _accumulatedScores.value = AccumulatedScores(0, 0, 0, 0)

                    // Start client-side timer
                    sessionStartTime = System.currentTimeMillis()

                    // ✅ CRITICAL FIX: Reset opponent activity time when session starts!
                    lastOpponentActivityTime = System.currentTimeMillis()

                    _clientTimeRemaining.value =
                        session.debate_time_remaining // Initialize with DB value

                }.onFailure { error ->
                    Log.e(TAG, "❌ Failed to load session", error)
                    _errorMessage.value = "Failed to load debate: ${error.message}"
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error initializing session", e)
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    /**
     * Load all messages for the session
     */
    private suspend fun loadMessages() {
        val sessionId = _sessionId.value ?: return

        p2pService.getMessages(sessionId).onSuccess { messages ->
            _messages.value = messages
            Log.d(TAG, "📬 Loaded ${messages.size} messages")
        }.onFailure { error ->
            Log.e(TAG, "❌ Failed to load messages", error)
        }
    }

    /**
     * Start polling for updates (messages, turns, status)
     */
    private fun startPolling() {
        // Poll for new messages every 2 seconds
        messagePollJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                pollNewMessages()
            }
        }

        // Poll for turn changes every 1 second
        turnPollJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                pollTurnChanges()
            }
        }

        // Poll for status changes every 2 seconds (PREP → IN_PROGRESS → FINISHED)
        statusPollJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                pollStatusChanges()
            }
        }

        Log.d(TAG, "🔄 Polling started")
    }

    /**
     * Poll for new messages
     */
    private suspend fun pollNewMessages() {
        val sessionId = _sessionId.value ?: return
        val lastTurn = _messages.value.maxOfOrNull { it.turnNumber } ?: -1

        p2pService.pollNewMessages(sessionId, lastTurn).onSuccess { newMessages ->
            if (newMessages.isNotEmpty()) {
                _messages.value = _messages.value + newMessages
                Log.d(TAG, "📬 Received ${newMessages.size} new messages")
                lastOpponentActivityTime = System.currentTimeMillis()
            }
        }
    }

    /**
     * Poll for turn changes
     */
    private suspend fun pollTurnChanges() {
        val sessionId = _sessionId.value ?: return
        val userId = _currentUserId.value ?: return

        p2pService.pollCurrentTurn(sessionId).onSuccess { (currentTurn, turnNum) ->
            val oldTurn = _isMyTurn.value
            val newTurn = currentTurn == userId

            if (oldTurn != newTurn) {
                _isMyTurn.value = newTurn
                _turnNumber.value = turnNum
                Log.d(TAG, "🔄 Turn changed! My turn: $newTurn")

                if (newTurn) {
                    _statusMessage.value = "Your turn!"
                } else {
                    _statusMessage.value = "Opponent's turn..."
                }
            }
        }
    }

    /**
     * Poll for status changes (PREP → IN_PROGRESS)
     */
    private suspend fun pollStatusChanges() {
        val sessionId = _sessionId.value ?: return

        p2pService.pollSessionStatus(sessionId).onSuccess { newStatus ->
            if (newStatus != _debateStatus.value) {
                val oldStatus = _debateStatus.value
                _debateStatus.value = newStatus
                Log.d(TAG, "🔄 Status changed from $oldStatus to: $newStatus")

                when (newStatus) {
                    "IN_PROGRESS" -> {
                        _statusMessage.value = "Debate started!"

                        // ✅ FIX: Start countdown timer when debate begins!
                        if (timerJob == null || timerJob?.isActive == false) {
                            Log.d(TAG, "⏱️ Starting countdown timer")
                            timerJob = viewModelScope.launch {
                                while (_debateStatus.value == "IN_PROGRESS") {
                                    delay(1000)

                                    // Decrement by 1 second (1000ms)
                                    val newTime = _clientTimeRemaining.value - 1000
                                    _clientTimeRemaining.value = newTime.coerceAtLeast(0)

                                    if (_clientTimeRemaining.value <= 0) {
                                        Log.d(TAG, "⏱️ Timer reached 0, ending debate")
                                        endDebate()
                                        break
                                    }
                                }
                            }
                        }
                    }

                    "FINISHED" -> {
                        _statusMessage.value = "Debate finished!"
                        stopPolling()
                    }

                    "ABANDONED" -> {
                        _statusMessage.value = "Opponent left!"
                        handleOpponentForfeit()
                    }
                }
            }
        }

        // Auto-transition from PREP to IN_PROGRESS after 30 seconds
        if (_debateStatus.value == "PREP") {
            val prepElapsed = System.currentTimeMillis() - sessionStartTime
            if (prepElapsed > 30000) {
                Log.d(TAG, "⏱️ Prep time over, transitioning to IN_PROGRESS")
                p2pService.updateSessionStatus(sessionId, "IN_PROGRESS")
                _debateStatus.value = "IN_PROGRESS"
                _statusMessage.value = "Debate started!"

                // ✅ FIX: Start timer here too (backup)
                if (timerJob == null || timerJob?.isActive == false) {
                    Log.d(TAG, "⏱️ Starting countdown timer (after prep)")
                    timerJob = viewModelScope.launch {
                        while (_debateStatus.value == "IN_PROGRESS") {
                            delay(1000)

                            val newTime = _clientTimeRemaining.value - 1000
                            _clientTimeRemaining.value = newTime.coerceAtLeast(0)

                            if (_clientTimeRemaining.value <= 0) {
                                Log.d(TAG, "⏱️ Timer reached 0, ending debate")
                                endDebate()
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle opponent forfeit (they left the match)
     */
    private fun handleOpponentForfeit() {
        viewModelScope.launch {
            Log.d(TAG, "🏆 Opponent forfeited! You win by default")

            // Stop all polling
            stopPolling()

            // 🔥 Cancel AI operations
            isDebateTimedOut = true
            aiJudgingJob?.cancel()
            aiJudgingJob = null

            // Award automatic win (100 for me, 0 for opponent)
            _finalScores.value = Pair(100, 0)
            _myStrengths.value = listOf(
                "Victory by opponent forfeit",
                "Showed up and participated fully",
                "Demonstrated commitment to the debate"
            )
            _myWeaknesses.value = listOf(
                "Opponent left before completion"
            )

            _debateStatus.value = "FINISHED"

            // Save results
            val accumulated = _accumulatedScores.value ?: AccumulatedScores(0, 0, 0, 0)
            _accumulatedScores.value = accumulated.copy(
                playerTotalScore = 100,
                aiTotalScore = 0
            )

            saveDebateResults()
        }
    }

    /**
     * User forfeits the match (leaves early)
     */
    fun forfeitMatch() {
        viewModelScope.launch {
            val sessionId = _sessionId.value ?: return@launch

            Log.d(TAG, "❌ User forfeiting match")

            try {
                // Mark session as ABANDONED
                p2pService.updateSessionStatus(sessionId, "ABANDONED")

                // Stop all operations
                stopPolling()
                isDebateTimedOut = true
                aiJudgingJob?.cancel()
                aiJudgingJob = null

                // Set scores (0 for me, 100 for opponent)
                _finalScores.value = Pair(0, 100)
                _myWeaknesses.value = listOf(
                    "Left the match early",
                    "Did not complete the debate",
                    "Forfeited"
                )
                _myStrengths.value = emptyList()

                // Update accumulated scores
                val accumulated = _accumulatedScores.value ?: AccumulatedScores(0, 0, 0, 0)
                _accumulatedScores.value = accumulated.copy(
                    playerTotalScore = 0,
                    aiTotalScore = 100
                )

                // Save forfeit result
                saveDebateResults()

                // Navigate to results
                _debateStatus.value = "FINISHED"

            } catch (e: Exception) {
                Log.e(TAG, "Error forfeiting match", e)
            }
        }
    }

    /**
     * Send a message in the debate
     */
    fun sendMessage(text: String) {
        Log.d(TAG, "📤 === SEND MESSAGE CALLED ===")
        Log.d(TAG, "📤 Text: $text")

        val sessionId = _sessionId.value
        val userId = _currentUserId.value
        val userName = _currentUserName.value

        Log.d(TAG, "📤 Session ID: $sessionId")
        Log.d(TAG, "📤 User ID: $userId")
        Log.d(TAG, "📤 User Name: $userName")
        Log.d(TAG, "📤 Is my turn: ${_isMyTurn.value}")

        if (sessionId == null || userId == null) {
            Log.e(TAG, "❌ Cannot send message: session or user not set")
            return
        }

        if (!_isMyTurn.value) {
            _statusMessage.value = "Not your turn!"
            Log.w(TAG, "⚠️ Not user's turn")
            return
        }

        if (text.isBlank()) {
            Log.w(TAG, "⚠️ Message is blank")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentTurn = _turnNumber.value + 1

                Log.d(TAG, "📤 Sending message: $text")
                Log.d(TAG, "📤 Current turn: $currentTurn")

                // Send message to database
                p2pService.sendMessage(
                    sessionId = sessionId,
                    playerId = userId,
                    playerName = userName,
                    message = text,
                    turnNumber = currentTurn
                ).onSuccess {
                    Log.d(TAG, "✅ Message sent successfully")

                    // Add message to local state immediately
                    val newMessage = DebateMessage(
                        id = it,
                        playerId = userId,
                        playerName = userName,
                        message = text,
                        timestamp = System.currentTimeMillis(),
                        turnNumber = currentTurn
                    )
                    _messages.value = _messages.value + newMessage
                    Log.d(TAG, "✅ Message added to local state")

                    // Judge my message (same AI judge as AI mode!)
                    Log.d(TAG, "⚖️ Calling judgeMessage...")
                    judgeMessage(text, userName, currentTurn)
                    Log.d(TAG, "⚖️ judgeMessage call completed")

                    // Switch turn to opponent
                    val opponentId = _opponentId.value ?: return@onSuccess
                    Log.d(TAG, "🔄 Switching turn to opponent: $opponentId")
                    p2pService.updateTurn(sessionId, opponentId, currentTurn)

                    _isMyTurn.value = false
                    _turnNumber.value = currentTurn
                    _statusMessage.value = "Opponent's turn..."
                    Log.d(TAG, "✅ Turn switched successfully")

                }.onFailure { error ->
                    Log.e(TAG, "❌ Failed to send message", error)
                    _errorMessage.value = "Failed to send: ${error.message}"
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending message", e)
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Judge a message using AI (same as AI mode!)
     * Uses the same LLM judge that judges AI vs player debates
     * NOW WITH TIMEOUT PROTECTION!
     */
    private suspend fun judgeMessage(message: String, speaker: String, turnNumber: Int) {
        Log.d(TAG, "⚖️ === JUDGE MESSAGE CALLED ===")
        Log.d(TAG, "⚖️ Message: $message")
        Log.d(TAG, "⚖️ Speaker: $speaker")
        Log.d(TAG, "⚖️ Turn: $turnNumber")
        Log.d(TAG, "⚖️ Debate status: ${_debateStatus.value}")
        Log.d(TAG, "⚖️ Timed out: $isDebateTimedOut")

        // 🔥 Block all new AI judging operations if debate has timed out
        if (isDebateTimedOut || _debateStatus.value == "FINISHED") {
            Log.w(TAG, "🛑 AI judging aborted due to timeout/debate end")
            return
        }

        try {
            _statusMessage.value = "⚖️ Judging your argument..."
            Log.d(TAG, "⚖️ Status message set")

            // Get opponent's last message for context
            val opponentLastMessage = _messages.value
                .filter { it.playerId == _opponentId.value }
                .lastOrNull()
                ?.message ?: ""

            Log.d(TAG, "⚖️ Opponent last message: ${opponentLastMessage.take(50)}...")

            // Build judging prompt (same as DebateViewModel)
            val judgingPrompt = buildJudgingPrompt(
                currentResponse = message,
                previousOpponentResponse = opponentLastMessage,
                speaker = speaker,
                turnNumber = turnNumber
            )

            Log.d(TAG, "⚖️ Judging prompt built: ${judgingPrompt.take(100)}...")

            // Reload model before judging to clear KV cache
            Log.d(TAG, "⚖️ Attempting to reload model...")
            if (!reloadModelForFreshGeneration()) {
                Log.e(TAG, "❌ Model reload failed, judging with existing model...")
            } else {
                Log.d(TAG, "✅ Model reloaded successfully")
            }

            // Use AI to judge (streaming API)
            val judgeResponse = StringBuilder()
            Log.d(TAG, "⚖️ Starting AI generation...")
            try {
                aiJudgingJob = viewModelScope.launch {
                    RunAnywhere.generateStream(judgingPrompt).collect { token ->
                        judgeResponse.append(token)
                        Log.d(TAG, "⚖️ Token received: $token")
                    }
                }
                aiJudgingJob?.join()
                Log.d(TAG, "⚖️ AI generation complete: $judgeResponse")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Judging generation failed", e)
                return
            }

            // Parse score
            val score = parseJudgeScore(judgeResponse.toString(), speaker)

            Log.d(TAG, "⚖️ Score for $speaker: ${score.score}/10")

            // Show score popup
            _currentTurnScore.value = score
            _showScorePopup.value = true
            Log.d(TAG, "⚖️ Score popup shown")
            delay(3000)
            _showScorePopup.value = false
            Log.d(TAG, "⚖️ Score popup hidden")

            // Update accumulated scores
            val accumulated = _accumulatedScores.value ?: AccumulatedScores(0, 0, 0, 0)
            _accumulatedScores.value = if (speaker == _currentUserName.value) {
                accumulated.copy(
                    playerTotalScore = accumulated.playerTotalScore + score.score,
                    playerTurnCount = accumulated.playerTurnCount + 1
                )
            } else {
                accumulated.copy(
                    aiTotalScore = accumulated.aiTotalScore + score.score,
                    aiTurnCount = accumulated.aiTurnCount + 1
                )
            }

            Log.d(TAG, "⚖️ Scores updated: ${_accumulatedScores.value}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error judging message", e)
        }
    }

    /**
     * Build judging prompt (same as DebateViewModel)
     * More reliable than the old verbose format
     */
    private fun buildJudgingPrompt(
        currentResponse: String,
        previousOpponentResponse: String,
        speaker: String,
        turnNumber: Int
    ): String {
        // ULTRA SIMPLE - just the facts (like improved AI mode)
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
     * Parse judge score from text (same as DebateViewModel)
     */
    private fun parseJudgeScore(response: String, speaker: String): TurnScore {
        return try {
            val scoreRegex = """Score:\s*(\d+)""".toRegex(RegexOption.IGNORE_CASE)
            val scoreMatch = scoreRegex.find(response)
            val score = scoreMatch?.groupValues?.get(1)?.toIntOrNull() ?: 5

            val reasoningRegex = """Reasoning:\s*([^\n]+)""".toRegex(RegexOption.IGNORE_CASE)
            val reasoningMatch = reasoningRegex.find(response)
            val reasoning = reasoningMatch?.groupValues?.get(1)?.trim() ?: "No specific feedback"

            val profanityRegex = """Profanity:\s*(yes|no)""".toRegex(RegexOption.IGNORE_CASE)
            val profanityMatch = profanityRegex.find(response)
            val hasProfanity =
                profanityMatch?.groupValues?.get(1)?.equals("yes", ignoreCase = true) ?: false

            val factsRegex = """Facts:\s*([^\n]+)""".toRegex(RegexOption.IGNORE_CASE)
            val factsMatch = factsRegex.find(response)
            val factCheck = factsMatch?.groupValues?.get(1)?.trim() ?: "Not evaluated"

            TurnScore(
                speaker = speaker,
                score = score.coerceIn(0, 10),
                reasoning = reasoning,
                hasProfanity = hasProfanity,
                factCheck = factCheck
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing judge score", e)
            TurnScore(
                speaker = speaker,
                score = 5,
                reasoning = "Unable to parse evaluation",
                hasProfanity = false,
                factCheck = "Not evaluated"
            )
        }
    }

    /**
     * Stop polling when debate ends
     */
    private fun stopPolling() {
        messagePollJob?.cancel()
        turnPollJob?.cancel()
        statusPollJob?.cancel()
        timerJob?.cancel()
        forfeitDetectionJob?.cancel()
        Log.d(TAG, "🛑 Polling stopped")
    }

    /**
     * End the debate
     * NOW WITH TIMEOUT CANCELLATION like AI mode!
     */
    fun endDebate() {
        viewModelScope.launch {
            val sessionId = _sessionId.value ?: return@launch

            Log.d(TAG, "🏁 Time's up! Ending debate...")

            // 🔥 CRITICAL: Prevent any new AI operations
            isDebateTimedOut = true

            // 🔥 CRITICAL: Cancel all ongoing AI judging jobs
            aiJudgingJob?.cancel()
            aiJudgingJob = null

            _debateStatus.value = "JUDGING"

            delay(2000) // Short delay for "judging" animation

            // Mark session as finished
            p2pService.endSession(sessionId).onSuccess {
                Log.d(TAG, "✅ Session ended")
                _debateStatus.value = "FINISHED"
                stopPolling()

                // Generate final scores and feedback
                generateFinalScores()

                // Save results to debate_history
                saveDebateResults()
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to end debate", error)
                _debateStatus.value = "FINISHED" // Still finish even if save fails
            }
        }
    }

    /**
     * Forfeit the debate
     * NOW WITH TIMEOUT CANCELLATION like AI mode!
     */
    fun forfeitDebate() {
        viewModelScope.launch {
            val sessionId = _sessionId.value ?: return@launch
            val userId = _currentUserId.value ?: return@launch

            Log.d(TAG, "🏳️ Player forfeited the debate")

            // 🔥 Prevent any new AI operations
            isDebateTimedOut = true

            // 🔥 Cancel all ongoing AI judging jobs
            aiJudgingJob?.cancel()
            aiJudgingJob = null

            // Mark session as finished
            p2pService.endSession(sessionId).onSuccess {
                Log.d(TAG, "✅ Session ended due to forfeit")
                _debateStatus.value = "FINISHED"
                stopPolling()

                // Save as loss (0 points for me, 50 for opponent)
                val accumulated = _accumulatedScores.value ?: AccumulatedScores(0, 0, 0, 0)
                _accumulatedScores.value = accumulated.copy(
                    playerTotalScore = 0,
                    aiTotalScore = 50
                )

                // Save results to debate_history
                saveDebateResults()
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to forfeit debate", error)
            }
        }
    }

    /**
     * Generate final scores
     */
    private fun generateFinalScores() {
        val accumulated = _accumulatedScores.value ?: return
        val myScore = accumulated.playerTotalScore
        val opponentScore = accumulated.aiTotalScore

        _finalScores.value = Pair(myScore, opponentScore)

        // Generate strengths and weaknesses based on performance
        val avgScore = if (accumulated.playerTurnCount > 0) {
            myScore.toFloat() / accumulated.playerTurnCount
        } else {
            5f
        }

        // Strengths (positive feedback)
        val strengths = mutableListOf<String>()
        if (avgScore >= 7) {
            strengths.add("Strong logical reasoning throughout the debate")
        } else {
            strengths.add("Maintained respectful and professional tone")
        }

        if (accumulated.playerTurnCount >= 5) {
            strengths.add("Consistent engagement with multiple well-formed arguments")
        } else {
            strengths.add("Clear communication style")
        }

        if (myScore > opponentScore) {
            strengths.add("Successfully defended your position with conviction")
        } else {
            strengths.add("Demonstrated good understanding of the topic")
        }

        _myStrengths.value = strengths.take(3)

        // Weaknesses (areas to improve)
        val weaknesses = mutableListOf<String>()
        if (avgScore < 6) {
            weaknesses.add("Strengthen arguments with more specific examples and evidence")
        } else {
            weaknesses.add("Consider addressing counter-arguments more directly")
        }

        if (accumulated.playerTurnCount < 5) {
            weaknesses.add("Increase participation and response frequency")
        } else {
            weaknesses.add("Vary argument structure to maintain opponent's interest")
        }

        if (myScore <= opponentScore) {
            weaknesses.add("Work on building more persuasive and compelling arguments")
        } else {
            weaknesses.add("Continue refining debating skills for even better performance")
        }

        _myWeaknesses.value = weaknesses.take(3)

        Log.d(TAG, "✅ Final scores generated: Me=$myScore, Opponent=$opponentScore")
    }

    /**
     * Save debate results to existing debate_history table
     */
    private suspend fun saveDebateResults() {
        try {
            val session = _sessionData.value ?: return
            val accumulated = _accumulatedScores.value ?: return
            val userId = _currentUserId.value ?: return

            // Determine winner
            val won = accumulated.playerTotalScore > accumulated.aiTotalScore

            // Get my side
            val mySide = if (session.player1_id == userId) {
                session.player1_side
            } else {
                session.player2_side
            }

            // Save to existing debate_history table
            serverRepository.saveDebateResult(
                topic = session.topic_title,
                userSide = mySide,
                opponentType = "PLAYER", // P2P marker!
                userScore = accumulated.playerTotalScore,
                opponentScore = accumulated.aiTotalScore,
                feedback = if (won) "You won the P2P debate!" else "Your opponent won the debate."
            ).onSuccess {
                Log.d(TAG, "✅ Results saved to debate_history")
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to save results", error)
            }

            // Generate final scores
            generateFinalScores()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving results", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()

        // 🔥 Cancel AI jobs on cleanup
        aiJudgingJob?.cancel()
        aiJudgingJob = null
    }
}
