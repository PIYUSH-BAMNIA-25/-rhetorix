package com.runanywhere.startup_hackathon20

import kotlinx.serialization.Serializable

// User Data
@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val totalGames: Int = 0,
    val wins: Int = 0,
    val averageScore: Float = 0f,
    val skillLevel: SkillLevel = SkillLevel.BEGINNER
)

enum class SkillLevel {
    BEGINNER, INTERMEDIATE, ADVANCED
}

// Debate Topics
@Serializable
data class DebateTopic(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: SkillLevel
)

// Debate Session
@Serializable
data class DebateSession(
    val id: String,
    val topic: DebateTopic,
    val player1: User,
    val player2: User? = null, // null for AI mode
    val player1Side: DebateSide,
    val player2Side: DebateSide,
    val gameMode: GameMode,
    val status: DebateStatus = DebateStatus.WAITING,
    val currentTurn: String = "", // player ID whose turn it is
    val timeRemaining: Long = 600000, // 10 minutes in milliseconds
    val startTime: Long = 0,
    val messages: List<DebateMessage> = emptyList(),
    val scores: DebateScores? = null
)

enum class GameMode {
    AI_BEGINNER, AI_INTERMEDIATE, AI_ADVANCED, PVP
}

enum class DebateSide {
    FOR, AGAINST
}

enum class DebateStatus {
    WAITING, PREP_TIME, IN_PROGRESS, FINISHED, JUDGING
}

// Debate Messages
@Serializable
data class DebateMessage(
    val id: String,
    val playerId: String,
    val playerName: String,
    val message: String,
    val timestamp: Long,
    val turnNumber: Int
)

// Scoring System
@Serializable
data class DebateScores(
    val player1Score: PlayerScore,
    val player2Score: PlayerScore,
    val winner: String, // player ID
    val feedback: String,
    val detailedAnalysis: String
)

@Serializable
data class PlayerScore(
    val playerId: String,
    val playerName: String,
    val logicReasoning: Int, // 1-10
    val evidenceQuality: Int, // 1-10
    val toneRespect: Int, // 1-10
    val counterArguments: Int, // 1-10
    val factualAccuracy: Int, // 1-10
    val totalScore: Int,
    val feedback: String
)

// Turn-by-Turn Scoring
@Serializable
data class TurnScore(
    val speaker: String, // "Player" or "AI"
    val score: Int, // 0-10
    val reasoning: String,
    val hasProfanity: Boolean,
    val factCheck: String
)

// Accumulated Scores During Debate
@Serializable
data class AccumulatedScores(
    val playerTotalScore: Int,
    val aiTotalScore: Int,
    val playerTurnCount: Int,
    val aiTurnCount: Int
)

// Chat History for Judging Context
@Serializable
data class ChatTurn(
    val speaker: String, // "player" or "ai"
    val message: String,
    val timestamp: Long
)

// Pre-defined Topics Database
object DebateTopics {
    val BEGINNER_TOPICS = listOf(
        DebateTopic(
            "1", "Social media does more harm than good",
            "Discuss the impact of social media on society", "Technology", SkillLevel.BEGINNER
        ),
        DebateTopic(
            "2", "Students should wear school uniforms",
            "Debate about uniform policies in schools", "Education", SkillLevel.BEGINNER
        ),
        DebateTopic(
            "3", "Video games cause violence",
            "Examine the relationship between gaming and aggression", "Entertainment", SkillLevel.BEGINNER
        ),
        DebateTopic(
            "4", "Fast food should be banned",
            "Discuss health implications of fast food", "Health", SkillLevel.BEGINNER
        ),
        DebateTopic(
            "5", "Homework should be abolished",
            "Debate the effectiveness of homework", "Education", SkillLevel.BEGINNER
        )
    )

    val INTERMEDIATE_TOPICS = listOf(
        DebateTopic(
            "6", "Artificial Intelligence will replace human jobs",
            "Analyze AI's impact on employment", "Technology", SkillLevel.INTERMEDIATE
        ),
        DebateTopic(
            "7", "Climate change is the biggest threat to humanity",
            "Discuss environmental priorities", "Environment", SkillLevel.INTERMEDIATE
        ),
        DebateTopic(
            "8", "Universal basic income should be implemented",
            "Examine economic policy proposals", "Economics", SkillLevel.INTERMEDIATE
        ),
        DebateTopic(
            "9", "Genetic engineering should be allowed in humans",
            "Debate bioethical considerations", "Science", SkillLevel.INTERMEDIATE
        ),
        DebateTopic(
            "10", "Democracy is the best form of government",
            "Compare different political systems", "Politics", SkillLevel.INTERMEDIATE
        )
    )

    val ADVANCED_TOPICS = listOf(
        DebateTopic(
            "11", "Cryptocurrency will replace traditional currency",
            "Analyze the future of monetary systems", "Economics", SkillLevel.ADVANCED
        ),
        DebateTopic(
            "12", "Space exploration funding should be redirected to Earth problems",
            "Debate resource allocation priorities", "Science", SkillLevel.ADVANCED
        ),
        DebateTopic(
            "13", "Philosophical zombies prove consciousness is non-physical",
            "Examine theories of consciousness", "Philosophy", SkillLevel.ADVANCED
        ),
        DebateTopic(
            "14", "Quantum computing threatens global cybersecurity",
            "Discuss implications of quantum technology", "Technology", SkillLevel.ADVANCED
        ),
        DebateTopic(
            "15", "Transhumanism is the next step in human evolution",
            "Debate the future of human enhancement", "Philosophy", SkillLevel.ADVANCED
        )
    )

    fun getRandomTopic(skillLevel: SkillLevel): DebateTopic {
        val topics = when (skillLevel) {
            SkillLevel.BEGINNER -> BEGINNER_TOPICS
            SkillLevel.INTERMEDIATE -> INTERMEDIATE_TOPICS
            SkillLevel.ADVANCED -> ADVANCED_TOPICS
        }
        return topics.random()
    }

    fun getAllTopics(): List<DebateTopic> {
        return BEGINNER_TOPICS + INTERMEDIATE_TOPICS + ADVANCED_TOPICS
    }
}