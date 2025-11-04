package com.runanywhere.startup_hackathon20.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val dateOfBirth: String,
    val password: String, // In production, this should be hashed!
    val totalGames: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val averageScore: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = false
)
