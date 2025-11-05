package com.runanywhere.startup_hackathon20.data

/**
 * Difficulty levels with progressive unlock system
 *
 * Strategy:
 * - Beginner: Uses Llama 1B (downloads during signup)
 * - Intermediate: Uses Qwen 3B (downloads during beginner matches)
 * - Advanced: Uses Qwen 3B (same model, higher difficulty)
 * - P2P: Uses Qwen 3B (ensures fairness)
 */
enum class DifficultyLevel(
    val displayName: String,
    val requiredWins: Int,
    val modelName: String,
    val unlockMessage: String,
    val description: String,
    val colorHex: String
) {
    BEGINNER(
        displayName = "Beginner",
        requiredWins = 0,
        modelName = "Llama 3.2 1B Instruct Q6_K",
        unlockMessage = "🎓 Start your debate journey!",
        description = "Learn the basics of debate with a friendly AI",
        colorHex = "#4CAF50" // Green
    ),

    INTERMEDIATE(
        displayName = "Intermediate",
        requiredWins = 2,
        modelName = "Qwen 2.5 3B Instruct Q6_K",
        unlockMessage = "🎉 Intermediate AI Unlocked! The AI has evolved!",
        description = "Face a smarter AI with better reasoning",
        colorHex = "#FF9800" // Orange
    ),

    ADVANCED(
        displayName = "Advanced",
        requiredWins = 5,
        modelName = "Qwen 2.5 3B Instruct Q6_K",
        unlockMessage = "🔥 Advanced AI Unlocked! Ultimate challenge awaits!",
        description = "Challenge the most sophisticated AI opponent",
        colorHex = "#F44336" // Red
    ),

    PVP(
        displayName = "P2P Mode",
        requiredWins = 3,
        modelName = "Qwen 2.5 3B Instruct Q6_K",
        unlockMessage = "⚔️ P2P Mode Unlocked! Challenge real players!",
        description = "Compete against real players with fair AI judging",
        colorHex = "#9C27B0" // Purple
    );

    /**
     * Check if this difficulty level is unlocked based on user's win count
     */
    fun isUnlocked(totalWins: Int): Boolean {
        return totalWins >= requiredWins
    }

    /**
     * Get the model type for this difficulty
     */
    fun getModelType(): ModelType {
        return when (this) {
            BEGINNER -> ModelType.LLAMA_1B
            INTERMEDIATE, ADVANCED, PVP -> ModelType.QWEN_3B
        }
    }
}

/**
 * Model types used in the app
 */
enum class ModelType(
    val modelName: String,
    val sizeBytes: Long,
    val sizeMB: Int,
    val downloadTrigger: DownloadTrigger
) {
    LLAMA_1B(
        modelName = "Llama 3.2 1B Instruct Q6_K",
        sizeBytes = 815_000_000,
        sizeMB = 815,
        downloadTrigger = DownloadTrigger.ON_SIGNUP
    ),

    QWEN_3B(
        modelName = "Qwen 2.5 3B Instruct Q6_K",
        sizeBytes = 2_300_000_000,
        sizeMB = 2300,
        downloadTrigger = DownloadTrigger.ON_FIRST_MATCH
    )
}

/**
 * When to trigger model downloads
 */
enum class DownloadTrigger {
    ON_SIGNUP,           // Download immediately during signup
    ON_FIRST_MATCH,      // Download in background during first match
    ALREADY_DOWNLOADED   // Model already available
}

/**
 * Model download state
 */
sealed class ModelDownloadState {
    object NotStarted : ModelDownloadState()
    data class Downloading(val progress: Float) : ModelDownloadState() // 0.0 to 1.0
    object Downloaded : ModelDownloadState()
    data class Error(val message: String) : ModelDownloadState()
}
