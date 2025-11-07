package com.runanywhere.startup_hackathon20.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerUser(
    @SerialName("id")
    val id: String? = null,

    @SerialName("player_id")
    val playerId: String,

    @SerialName("username")
    val username: String,

    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    @SerialName("email")
    val email: String,

    @SerialName("date_of_birth")
    val dateOfBirth: String,

    @SerialName("total_games")
    val totalGames: Int = 0,

    @SerialName("wins")
    val wins: Int = 0,

    @SerialName("losses")
    val losses: Int = 0,

    @SerialName("likes")
    val likes: Int = 0,

    @SerialName("average_score")
    val averageScore: Float = 0f,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class ServerDebateHistory(
    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("topic")
    val topic: String,

    @SerialName("user_side")
    val userSide: String,

    @SerialName("opponent_type")
    val opponentType: String,

    @SerialName("user_score")
    val userScore: Int,

    @SerialName("opponent_score")
    val opponentScore: Int,

    @SerialName("won")
    val won: Boolean,

    @SerialName("feedback")
    val feedback: String,

    @SerialName("debate_date")
    val debateDate: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class AuthRequest(
    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String
)

@Serializable
data class SignUpRequest(
    @SerialName("name")
    val name: String,

    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String,

    @SerialName("date_of_birth")
    val dateOfBirth: String
)

@Serializable
data class AuthResponse(
    @SerialName("user")
    val user: ServerUser,

    @SerialName("session")
    val session: SessionData
)

@Serializable
data class SessionData(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String,

    @SerialName("expires_in")
    val expiresIn: Long
)
