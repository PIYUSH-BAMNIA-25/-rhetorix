package com.runanywhere.startup_hackathon20

import com.runanywhere.sdk.public.RunAnywhere
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import android.util.Log

/**
 * Generates debate topics dynamically using AI based on current world events
 */
object TopicGenerator {

    /**
     * Generate a debate topic using AI based on current world issues
     *
     * @param category The category for topic (AI, Social, Agriculture, etc.)
     * @param difficulty The difficulty level (affects topic complexity)
     * @return Pair of (DebateTopic, playerSide)
     */
    suspend fun generateDynamicTopic(
        category: TopicCategory = TopicCategory.RANDOM,
        difficulty: SkillLevel = SkillLevel.INTERMEDIATE
    ): Pair<DebateTopic, DebateSide> = withContext(Dispatchers.IO) {
        try {
            // Build the JSON prompt
            val prompt = buildTopicGenerationPrompt(category, difficulty)

            // Get AI response
            val response = RunAnywhere.generate(prompt)

            Log.d("TopicGenerator", "AI Response: $response")

            // Parse JSON response
            val topicData = parseTopicResponse(response, difficulty)

            // Randomly assign player side
            val playerSide = listOf(DebateSide.FOR, DebateSide.AGAINST).random()

            topicData to playerSide

        } catch (e: Exception) {
            Log.e("TopicGenerator", "Error generating topic: ${e.message}", e)
            // Fallback to static topic
            val fallbackTopic = DebateTopics.getRandomTopic(difficulty)
            val fallbackSide = listOf(DebateSide.FOR, DebateSide.AGAINST).random()
            fallbackTopic to fallbackSide
        }
    }

    /**
     * Build the JSON prompt for topic generation
     */
    private fun buildTopicGenerationPrompt(
        category: TopicCategory,
        difficulty: SkillLevel
    ): String {
        val categoryInstruction = when (category) {
            TopicCategory.AI_TECHNOLOGY -> "Focus on Artificial Intelligence, Machine Learning, Robotics, or emerging technologies."
            TopicCategory.SOCIAL_ISSUES -> "Focus on social justice, equality, human rights, or community issues."
            TopicCategory.AGRICULTURE -> "Focus on farming, food security, sustainable agriculture, or rural development."
            TopicCategory.ENVIRONMENT -> "Focus on climate change, pollution, conservation, or environmental policies."
            TopicCategory.POLITICS -> "Focus on governance, democracy, international relations, or political reforms."
            TopicCategory.HEALTHCARE -> "Focus on medical ethics, public health, healthcare access, or medical research."
            TopicCategory.EDUCATION -> "Focus on learning systems, education access, curriculum, or academic policies."
            TopicCategory.ECONOMICS -> "Focus on economic policies, trade, employment, or financial systems."
            TopicCategory.RANDOM -> "Choose any relevant contemporary topic from current world issues."
        }

        val complexityInstruction = when (difficulty) {
            SkillLevel.BEGINNER -> "Keep the topic simple and easy to understand. Use everyday language."
            SkillLevel.INTERMEDIATE -> "Make the topic moderately complex with balanced arguments."
            SkillLevel.ADVANCED -> "Create a sophisticated, nuanced topic requiring deep analysis."
        }

        return """
{
  "role": "Professional Debate Topic Generator",
  "task": "Generate a relevant, thought-provoking debate topic based on current world issues or trends from the year 2024-2025.",
  "instructions": {
    "context": "Consider recent news, global events, technology trends, ethical dilemmas, social issues, or political developments from around the world. ${categoryInstruction}",
    "difficulty": "${complexityInstruction}",
    "requirements": [
      "Topic must be specific, contemporary, and suitable for debate.",
      "Topic should be from current events or ongoing discussions in 2024-2025.",
      "Provide a concise, clear statement of the debate topic.",
      "Include brief context explaining why this topic is relevant now.",
      "Provide 2-3 key points supporting the 'FOR' side.",
      "Provide 2-3 key points supporting the 'AGAINST' side.",
      "Maintain neutrality - both sides should be equally strong.",
      "Use clear and professional language suitable for competitive debate."
    ]
  },
  "output_format": {
    "topic": "<Clear debate statement>",
    "description": "<2-3 sentences explaining why this topic is relevant in 2024-2025>",
    "for_arguments": [
      "<Strong point 1 supporting the topic>",
      "<Strong point 2 supporting the topic>",
      "<Strong point 3 supporting the topic>"
    ],
    "against_arguments": [
      "<Strong point 1 opposing the topic>",
      "<Strong point 2 opposing the topic>",
      "<Strong point 3 opposing the topic>"
    ]
  },
  "example_output": {
    "topic": "Governments should mandate AI safety testing before public deployment",
    "description": "With rapid AI advancement in 2024-2025, including ChatGPT-5, Claude, and autonomous systems, the question of mandatory safety regulations has become urgent. Recent AI incidents and calls from tech leaders make this a critical contemporary debate.",
    "for_arguments": [
      "Prevents potential harm from untested AI systems like misinformation or bias",
      "Ensures public safety similar to pharmaceutical or automotive regulations",
      "Builds public trust in AI technology and encourages responsible innovation"
    ],
    "against_arguments": [
      "Slows innovation and gives advantage to countries with less regulation",
      "Difficult to define universal safety standards for diverse AI applications",
      "Stifles smaller companies who cannot afford extensive testing procedures"
    ]
  },
  "note": "Generate ONLY the JSON output in the exact format above. Do not include any additional text, explanations, or formatting outside the JSON structure."
}

Please generate a debate topic now in the exact JSON format specified:
""".trimIndent()
    }

    /**
     * Parse the AI response to extract topic data
     */
    private fun parseTopicResponse(response: String, difficulty: SkillLevel): DebateTopic {
        return try {
            // Extract JSON from response (AI might include extra text)
            val jsonStart = response.indexOf("{")
            val jsonEnd = response.lastIndexOf("}") + 1

            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                throw Exception("No valid JSON found in response")
            }

            val jsonString = response.substring(jsonStart, jsonEnd)
            val json = JSONObject(jsonString)

            // Extract fields
            val topic = json.optString("topic", "")
            val description = json.optString("description", "")

            // Extract arguments (optional, for future use)
            val forArgs = mutableListOf<String>()
            val againstArgs = mutableListOf<String>()

            if (json.has("for_arguments")) {
                val forArray = json.getJSONArray("for_arguments")
                for (i in 0 until forArray.length()) {
                    forArgs.add(forArray.getString(i))
                }
            }

            if (json.has("against_arguments")) {
                val againstArray = json.getJSONArray("against_arguments")
                for (i in 0 until againstArray.length()) {
                    againstArgs.add(againstArray.getString(i))
                }
            }

            Log.d("TopicGenerator", "Parsed topic: $topic")
            Log.d("TopicGenerator", "FOR arguments: $forArgs")
            Log.d("TopicGenerator", "AGAINST arguments: $againstArgs")

            DebateTopic(
                id = System.currentTimeMillis().toString(),
                title = topic.ifEmpty { "The benefits of technology outweigh its risks" },
                description = description.ifEmpty { "A contemporary debate on modern technology." },
                category = "Current Events",
                difficulty = difficulty
            )

        } catch (e: Exception) {
            Log.e("TopicGenerator", "Error parsing topic: ${e.message}", e)
            // Return fallback topic
            DebateTopic(
                id = "fallback_${System.currentTimeMillis()}",
                title = "Technology is making society more connected",
                description = "A debate about the impact of modern technology on social connections.",
                category = "Technology",
                difficulty = difficulty
            )
        }
    }
}

/**
 * Categories for topic generation
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
