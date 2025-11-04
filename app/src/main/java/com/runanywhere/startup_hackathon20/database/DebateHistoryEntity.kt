package com.runanywhere.startup_hackathon20.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debate_history")
data class DebateHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val topic: String,
    val userSide: String, // "FOR" or "AGAINST"
    val opponentType: String, // "AI_BEGINNER", "AI_INTERMEDIATE", "AI_ADVANCED", "PVP"
    val userScore: Int,
    val opponentScore: Int,
    val won: Boolean,
    val feedback: String,
    val timestamp: Long = System.currentTimeMillis()
)
