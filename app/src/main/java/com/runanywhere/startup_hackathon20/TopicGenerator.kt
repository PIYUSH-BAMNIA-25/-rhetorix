package com.runanywhere.startup_hackathon20

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Generates debate topics from a curated database
 * NO AI GENERATION - Uses predefined topics to avoid repetition
 *
 * Database Structure:
 * - 3 Difficulty Levels (Beginner, Intermediate, Advanced)
 * - 8 Sectors per Level
 * - 30 Topics per Sector
 * - Total: 720 unique debate topics
 */
object TopicGenerator {

    /**
     * Generate a debate topic from database based on difficulty level
     *
     * For AI Mode:
     * 1. Get the difficulty level (Beginner/Intermediate/Advanced)
     * 2. Randomly select one sector (out of 8)
     * 3. Randomly select one topic from that sector (out of 30)
     *
     * @param category IGNORED - kept for compatibility
     * @param difficulty The difficulty level (affects topic selection)
     * @return Pair of (DebateTopic, playerSide)
     */
    suspend fun generateDynamicTopic(
        category: TopicCategory = TopicCategory.RANDOM,
        difficulty: SkillLevel = SkillLevel.INTERMEDIATE
    ): Pair<DebateTopic, DebateSide> = withContext(Dispatchers.IO) {
        try {
            // Get random topic from database based on difficulty
            val topicEntry = DebateTopicsDatabase.getRandomTopicForAI(difficulty)

            Log.d("TopicGenerator", "Selected topic: ${topicEntry.title}")
            Log.d("TopicGenerator", "Sector: ${topicEntry.sector}")
            Log.d("TopicGenerator", "Difficulty: ${topicEntry.difficulty}")

            // Create DebateTopic object
            val debateTopic = DebateTopic(
                id = System.currentTimeMillis().toString(),
                title = topicEntry.title,
                description = "Debate this statement: ${topicEntry.title}",
                category = topicEntry.sector,
                difficulty = topicEntry.difficulty
            )

            // Randomly assign player side (FOR or AGAINST)
            val playerSide = listOf(DebateSide.FOR, DebateSide.AGAINST).random()

            Log.d("TopicGenerator", "Player assigned: ${playerSide.name} side")

            debateTopic to playerSide

        } catch (e: Exception) {
            Log.e("TopicGenerator", "Error selecting topic: ${e.message}", e)

            // Fallback to static topic from old database if somehow fails
            val fallbackTopic = DebateTopics.getRandomTopic(difficulty)
            val fallbackSide = listOf(DebateSide.FOR, DebateSide.AGAINST).random()

            Log.d("TopicGenerator", "Using fallback topic: ${fallbackTopic.title}")

            fallbackTopic to fallbackSide
        }
    }

    /**
     * Generate a topic for P2P mode
     * Can come from ANY level, ANY sector, ANY topic
     *
     * @return Pair of (DebateTopic, playerSide)
     */
    suspend fun generateTopicForP2P(): Pair<DebateTopic, DebateSide> = withContext(Dispatchers.IO) {
        try {
            // Get completely random topic from entire database
            val topicEntry = DebateTopicsDatabase.getRandomTopicForP2P()

            Log.d("TopicGenerator", "P2P topic selected: ${topicEntry.title}")
            Log.d("TopicGenerator", "Sector: ${topicEntry.sector}")
            Log.d("TopicGenerator", "Difficulty: ${topicEntry.difficulty}")

            // Create DebateTopic object
            val debateTopic = DebateTopic(
                id = System.currentTimeMillis().toString(),
                title = topicEntry.title,
                description = "Debate this statement: ${topicEntry.title}",
                category = topicEntry.sector,
                difficulty = topicEntry.difficulty
            )

            // Randomly assign player side
            val playerSide = listOf(DebateSide.FOR, DebateSide.AGAINST).random()

            Log.d("TopicGenerator", "Player assigned: ${playerSide.name} side")

            debateTopic to playerSide

        } catch (e: Exception) {
            Log.e("TopicGenerator", "Error selecting P2P topic: ${e.message}", e)

            // Fallback
            val fallbackTopic = DebateTopics.getAllTopics().random()
            val fallbackSide = listOf(DebateSide.FOR, DebateSide.AGAINST).random()

            fallbackTopic to fallbackSide
        }
    }

    /**
     * Get total count of topics in database
     */
    fun getTotalTopicsCount(): Int {
        return DebateTopicsDatabase.getTotalTopicsCount()
    }

    /**
     * Get available sectors for a difficulty level
     */
    fun getSectorNames(difficulty: SkillLevel): List<String> {
        return DebateTopicsDatabase.getSectorNames(difficulty)
    }
}

/**
 * Categories for topic generation (DEPRECATED - kept for compatibility)
 * Topics are now organized by Sectors in the database
 */
enum class TopicCategory {
    AI_TECHNOLOGY,
    SOCIAL_ISSUES,
    AGRICULTURE,
    ENVIRONMENT,
    POLITICS,
    HEALTHCARE,
    EDUCATION,
    ECONOMICS,
    RANDOM
}
