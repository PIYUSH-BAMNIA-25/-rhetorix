package com.runanywhere.startup_hackathon20

import android.app.Application
import android.util.Log
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
import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

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

    // Score pop-up state
    private val _showScorePopup = MutableStateFlow(false)
    val showScorePopup: StateFlow<Boolean> = _showScorePopup

    private val _currentTurnScore = MutableStateFlow<TurnScore?>(null)
    val currentTurnScore: StateFlow<TurnScore?> = _currentTurnScore

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

                // Step 3: Create AI opponent with balanced IQ
                val aiIQ = getBalancedAIIQ(gameMode)
                val aiOpponent = User(
                    id = "ai_opponent",
                    name = "AI Debater (IQ $aiIQ)",
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
                    currentTurn = user.id, // Will be set by coin toss
                    timeRemaining = 600000, // 10 minutes
                    startTime = System.currentTimeMillis()
                )

                _currentSession.value = session
                _currentScreen.value = DebateScreen.DEBATE_PREP

                // Clear chat history for new debate
                chatHistory.clear()

                // Start prep timer (30 seconds)
                startPrepTimer()

            } catch (e: Exception) {
                _statusMessage.value = "Error starting debate: ${e.message}"
                Log.e("DebateViewModel", "Error starting debate", e)
            } finally {
                _isLoading.value = false
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

    // === MESSAGING WITH JUDGING ===
    fun sendDebateMessage(message: String) {
        val session = _currentSession.value ?: return
        val user = _currentUser.value ?: return

        if (session.currentTurn != user.id) {
            _statusMessage.value = "Not your turn!"
            return
        }

        if (message.isBlank()) return

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
                val playerScore = judgeResponse(
                    currentResponse = message,
                    previousOpponentResponse = chatHistory.findLast { it.speaker == "ai" }?.message
                        ?: "",
                    speaker = "Player",
                    difficulty = session.gameMode
                )

                // Show score pop-up
                _currentTurnScore.value = playerScore
                _showScorePopup.value = true
                delay(3000) // Show for 3 seconds
                _showScorePopup.value = false

                // Switch to AI turn
                _currentSession.value = session.copy(
                    messages = updatedMessages,
                    currentTurn = "ai_opponent"
                )

                // Generate AI response with realistic delay
                generateAIResponseWithDelay(session.copy(messages = updatedMessages))

            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
                Log.e("DebateViewModel", "Error sending message", e)
            }
        }
    }

    /**
     * Generate AI response with realistic timing and typing animation
     */
    private suspend fun generateAIResponseWithDelay(session: DebateSession) {
        try {
            // AI "thinking" time (20-40 seconds like a human)
            val thinkingTime = Random.nextLong(20000, 40000)
            _statusMessage.value = "🧠 AI is thinking..."
            _isAITyping.value = false

            // Generate response during thinking time
            val aiPrompt = buildAIPrompt(session)
            var aiResponseText = ""

            // Start generation (asynchronous)
            viewModelScope.launch {
                aiResponseText = RunAnywhere.generate(aiPrompt)
            }

            // Wait for thinking time
            delay(thinkingTime)

            // Wait for response to be ready (if not already)
            while (aiResponseText.isEmpty()) {
                delay(100)
            }

            // Trim to 3-4 lines max
            val lines = aiResponseText.lines().take(4)
            aiResponseText = lines.joinToString("\n")

            // Show typing animation
            _isAITyping.value = true
            _aiTypingText.value = ""
            _statusMessage.value = "💭 AI is responding..."

            // Type word by word
            val words = aiResponseText.split(" ")
            for (word in words) {
                _aiTypingText.value += "$word "
                delay(100) // 100ms per word
            }

            _isAITyping.value = false

            // Add AI message
            val aiMessage = DebateMessage(
                id = UUID.randomUUID().toString(),
                playerId = "ai_opponent",
                playerName = "AI Debater",
                message = aiResponseText.trim(),
                timestamp = System.currentTimeMillis(),
                turnNumber = session.messages.size + 1
            )

            val finalMessages = session.messages + aiMessage

            // Store in chat history
            chatHistory.add(
                ChatTurn(
                    speaker = "ai",
                    message = aiResponseText.trim(),
                    timestamp = System.currentTimeMillis()
                )
            )

            // JUDGE AI'S RESPONSE
            _statusMessage.value = "⚖️ Judging AI's argument..."
            val aiScore = judgeResponse(
                currentResponse = aiResponseText.trim(),
                previousOpponentResponse = chatHistory.findLast { it.speaker == "player" }?.message
                    ?: "",
                speaker = "AI",
                difficulty = session.gameMode
            )

            // Show AI score pop-up
            _currentTurnScore.value = aiScore
            _showScorePopup.value = true
            delay(3000) // Show for 3 seconds
            _showScorePopup.value = false

            // Back to player turn
            _currentSession.value = session.copy(
                messages = finalMessages,
                currentTurn = _currentUser.value?.id ?: ""
            )

            _statusMessage.value = "Your turn! Respond to the AI's argument."

        } catch (e: Exception) {
            _statusMessage.value = "AI response failed: ${e.message}"
            Log.e("DebateViewModel", "Error generating AI response", e)
            // Switch back to player anyway
            _currentSession.value = session.copy(currentTurn = _currentUser.value?.id ?: "")
        }
    }

    /**
     * Judge a response based on previous opponent's argument
     * Checks: counter-argument, logic, evidence, facts, tone, profanity
     */
    private suspend fun judgeResponse(
        currentResponse: String,
        previousOpponentResponse: String,
        speaker: String,
        difficulty: GameMode
    ): TurnScore {
        return try {
            val judgingPrompt = buildJudgingPromptTurnBased(
                currentResponse = currentResponse,
                previousOpponentResponse = previousOpponentResponse,
                speaker = speaker
            )

            val judgeResponse = RunAnywhere.generate(judgingPrompt)
            Log.d("DebateViewModel", "Judge response for $speaker: $judgeResponse")

            parseJudgeScore(judgeResponse, speaker)

        } catch (e: Exception) {
            Log.e("DebateViewModel", "Error judging response", e)
            // Return default score on error
            TurnScore(
                speaker = speaker,
                score = 5,
                reasoning = "Unable to score this turn",
                hasProfanity = false,
                factCheck = "Not evaluated"
            )
        }
    }

    /**
     * Build judging prompt for turn-based evaluation
     */
    private fun buildJudgingPromptTurnBased(
        currentResponse: String,
        previousOpponentResponse: String,
        speaker: String
    ): String {
        return """
You are a professional debate judge. Score this response fairly and objectively.

${
            if (previousOpponentResponse.isNotEmpty()) {
                "OPPONENT'S PREVIOUS ARGUMENT:\n\"$previousOpponentResponse\"\n"
            } else {
                "This is an opening statement (no previous opponent argument).\n"
            }
        }

$speaker'S CURRENT RESPONSE:
"$currentResponse"

Score this response (0-10) based on:
1. **Counter-Argument**: Did they address the opponent's point effectively?
2. **Logic & Structure**: Is the argument well-reasoned and clear?
3. **Evidence & Facts**: Did they use examples, data, or credible sources?
4. **Fact Accuracy**: Are their claims plausible and reasonable?
5. **Tone & Respect**: Is the language professional and respectful?
6. **Language**: Any profanity, insults, or inappropriate content?

Respond in EXACT JSON format:
{
  "score": 8,
  "reasoning": "Strong counter-argument with good evidence, but tone could be better",
  "hasProfanity": false,
  "factCheck": "Claims seem plausible"
}

Generate ONLY the JSON, no other text.
""".trimIndent()
    }

    /**
     * Parse judge's JSON response into TurnScore
     */
    private fun parseJudgeScore(response: String, speaker: String): TurnScore {
        return try {
            // Extract JSON from response
            val jsonStart = response.indexOf("{")
            val jsonEnd = response.lastIndexOf("}") + 1

            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                throw Exception("No valid JSON in response")
            }

            val jsonString = response.substring(jsonStart, jsonEnd)
            val json = JSONObject(jsonString)

            TurnScore(
                speaker = speaker,
                score = json.optInt("score", 5),
                reasoning = json.optString("reasoning", "No feedback provided"),
                hasProfanity = json.optBoolean("hasProfanity", false),
                factCheck = json.optString("factCheck", "Not evaluated")
            )
        } catch (e: Exception) {
            Log.e("DebateViewModel", "Error parsing judge score", e)
            TurnScore(
                speaker = speaker,
                score = 5,
                reasoning = "Unable to parse score",
                hasProfanity = false,
                factCheck = "Not evaluated"
            )
        }
    }

    /**
     * Build AI debate prompt with IQ-based difficulty
     */
    private fun buildAIPrompt(session: DebateSession): String {
        val topic = session.topic
        val aiSide = session.player2Side
        val sideText = if (aiSide == DebateSide.FOR) "in favor of" else "against"

        // Get AI IQ from session
        val aiIQ =
            session.player2?.name?.substringAfter("IQ ")?.substringBefore(")")?.toIntOrNull() ?: 75

        val conversationHistory = session.messages.takeLast(6).joinToString("\n") { msg ->
            "${msg.playerName}: ${msg.message}"
        }

        val iqBehavior = when {
            aiIQ < 65 -> """
                IQ Level: BEGINNER ($aiIQ)
                - Use VERY simple arguments
                - Basic logic only
                - Short sentences
                - Everyday examples
                - Occasionally miss opponent's points
                - Sometimes make small logical errors
            """.trimIndent()

            aiIQ < 80 -> """
                IQ Level: INTERMEDIATE ($aiIQ)
                - Use moderate complexity
                - Clear logical structure
                - Some evidence and examples
                - Address main opponent points
                - Balanced reasoning
            """.trimIndent()

            else -> """
                IQ Level: ADVANCED ($aiIQ)
                - Use sophisticated reasoning
                - Strong counter-arguments
                - Good use of evidence
                - Address nuances
                - Persuasive language
            """.trimIndent()
        }

        return """
You are a debate participant arguing $sideText: "${topic.title}"

$iqBehavior

DEBATE CONTEXT: ${topic.description}

CONVERSATION SO FAR:
$conversationHistory

CRITICAL RULES:
- Keep response VERY SHORT (3-4 lines maximum, about 50-60 words)
- Make ONE clear point per response
- No long explanations or essays
- Stay in character with your IQ level
- ${if (aiIQ < 70) "Occasionally show weakness (easier for player to counter)" else "Be challenging but fair"}

Your SHORT response (3-4 lines only):
""".trimIndent()
    }

    // === DEBATE ENDING ===
    private suspend fun endDebate() {
        val session = _currentSession.value ?: return
        
        _currentSession.value = session.copy(status = DebateStatus.JUDGING)
        _statusMessage.value = "Time's up! AI is judging the debate..."
        _currentScreen.value = DebateScreen.DEBATE_RESULTS

        // Generate comprehensive scores and feedback
        generateDebateScores(session)
    }

    private suspend fun generateDebateScores(session: DebateSession) {
        try {
            // First: Basic scoring
            _statusMessage.value = "Analyzing arguments..."
            val judgingPrompt = buildJudgingPrompt(session)
            val judgingResponse = RunAnywhere.generate(judgingPrompt)
            
            // Parse AI response and create scores
            val scores = parseJudgingResponse(judgingResponse, session)

            // Second: Generate comprehensive feedback
            _statusMessage.value = "Generating detailed feedback..."
            val comprehensiveFeedback = generateComprehensiveFeedback(session, scores)

            // Update scores with comprehensive feedback
            val finalScores = scores.copy(
                detailedAnalysis = comprehensiveFeedback
            )

            _currentSession.value = session.copy(
                status = DebateStatus.FINISHED,
                scores = finalScores
            )
            
            val winner = if (scores.player1Score.totalScore > scores.player2Score.totalScore) 
                session.player1.name else "AI Debater"
            
            _statusMessage.value = "Debate complete! Winner: $winner"

            // Save debate results to database
            saveDebateResults(session, finalScores)

        } catch (e: Exception) {
            _statusMessage.value = "Error judging debate: ${e.message}"
            Log.e("DebateViewModel", "Error generating scores", e)
        }
    }

    /**
     * FEEDBACK AI: Comprehensive analysis of entire debate
     * Analyzes: behavior, strengths, weaknesses, where they shined, where they lacked
     */
    private suspend fun generateComprehensiveFeedback(
        session: DebateSession,
        scores: DebateScores
    ): String {
        try {
            val feedbackPrompt = buildFeedbackAIPrompt(session, scores)
            val feedback = RunAnywhere.generate(feedbackPrompt)

            return feedback
        } catch (e: Exception) {
            Log.e("DebateViewModel", "Error generating feedback", e)
            return "Unable to generate detailed feedback at this time."
        }
    }

    /**
     * Build comprehensive feedback prompt
     */
    private fun buildFeedbackAIPrompt(session: DebateSession, scores: DebateScores): String {
        val topic = session.topic
        val player1Side = if (session.player1Side == DebateSide.FOR) "FOR" else "AGAINST"
        val player2Side = if (session.player2Side == DebateSide.FOR) "FOR" else "AGAINST"

        // Get full debate transcript
        val fullTranscript = session.messages.joinToString("\n\n") { msg ->
            val side = if (msg.playerId == session.player1.id) player1Side else player2Side
            "**${msg.playerName}** (arguing $side) - Turn ${msg.turnNumber}:\n${msg.message}"
        }

        // Player's scores
        val playerScore = scores.player1Score
        val aiScore = scores.player2Score

        return """
You are an expert debate coach analyzing a complete debate performance. Provide comprehensive, constructive feedback.

=== DEBATE INFORMATION ===
Topic: "${topic.title}"
Description: ${topic.description}

${session.player1.name}'s Side: $player1Side
AI Opponent's Side: $player2Side

Time Duration: ${(600000 - session.timeRemaining) / 1000 / 60} minutes
Total Exchanges: ${session.messages.size} messages

=== PERFORMANCE SCORES ===
${session.player1.name}:
- Logic & Reasoning: ${playerScore.logicReasoning}/10
- Evidence Quality: ${playerScore.evidenceQuality}/10
- Tone & Respect: ${playerScore.toneRespect}/10
- Counter Arguments: ${playerScore.counterArguments}/10
- Factual Accuracy: ${playerScore.factualAccuracy}/10
TOTAL: ${playerScore.totalScore}/50

AI Opponent:
- Logic & Reasoning: ${aiScore.logicReasoning}/10
- Evidence Quality: ${aiScore.evidenceQuality}/10
- Tone & Respect: ${aiScore.toneRespect}/10
- Counter Arguments: ${aiScore.counterArguments}/10
- Factual Accuracy: ${aiScore.factualAccuracy}/10
TOTAL: ${aiScore.totalScore}/50

Winner: ${if (scores.winner == session.player1.id) session.player1.name else "AI Opponent"}

=== FULL DEBATE TRANSCRIPT ===
$fullTranscript

=== YOUR TASK ===
Provide a comprehensive analysis covering:

1. **OVERALL BEHAVIOR** (2-3 sentences)
   - How did ${session.player1.name} approach the debate?
   - What was their debating style?
   - Were they aggressive, defensive, or balanced?

2. **WHERE THEY SHINED** ⭐ (3-4 specific examples)
   - What were their strongest moments?
   - Which arguments were most effective?
   - What techniques worked well?

3. **WHERE THEY LACKED** ⚠️ (3-4 specific areas)
   - What weaknesses were evident?
   - Which arguments were weak or missed?
   - What could be improved?

4. **SPECIFIC TURN ANALYSIS** (Pick 2-3 key turns)
   - Turn X: Why this was good/bad
   - What they did right/wrong
   - How they could improve

5. **COMMUNICATION STYLE**
   - Clarity of expression
   - Use of evidence
   - Logical structure
   - Tone and respect

6. **STRATEGIC ANALYSIS**
   - Did they address opponent's points effectively?
   - Did they stay on topic?
   - Did they control the narrative?

7. **AREAS FOR IMPROVEMENT** (Actionable advice)
   - Top 3 things to work on
   - Specific recommendations
   - How to practice

8. **FINAL VERDICT & ENCOURAGEMENT**
   - Overall assessment
   - Growth potential
   - Motivational closing

FORMAT: Write in a friendly, coaching tone. Be honest but encouraging. Use specific examples from the transcript.

Generate your comprehensive feedback:
""".trimIndent()
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
            1. **Logic & Reasoning**: Clear, logical arguments
            2. **Evidence Quality**: Use of facts and examples
            3. **Tone & Respect**: Professional, respectful communication
            4. **Counter Arguments**: Addressing opponent's points effectively
            5. **Factual Accuracy**: Truthfulness of claims
            
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

    enum class DebateScreen {
        LOGIN, MAIN_MENU, MODEL_SETUP, DEBATE_PREP, DEBATE_ACTIVE, DEBATE_RESULTS
    }
}