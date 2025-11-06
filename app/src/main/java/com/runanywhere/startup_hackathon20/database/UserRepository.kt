package com.runanywhere.startup_hackathon20.database

import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class UserRepository(private val userDao: UserDao, private val debateHistoryDao: DebateHistoryDao) {

    /**
     * Generate unique Player ID in format RXXXXXX
     * Example: R123456, R987654
     */
    private suspend fun generatePlayerId(): String {
        var playerId: String
        var isUnique: Boolean

        do {
            // Generate 6-digit random number
            val randomNumber = Random.nextInt(100000, 999999)
            playerId = "R$randomNumber"

            // Check if this ID already exists
            isUnique = userDao.getUserByPlayerId(playerId) == null
        } while (!isUnique)

        return playerId
    }

    // User Authentication
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        dateOfBirth: String
    ): Result<Long> {
        return try {
            // Check if user already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                Result.failure(Exception("Email already registered"))
            } else {
                // Generate unique player ID
                val playerId = generatePlayerId()

                val user = UserEntity(
                    playerId = playerId,
                    name = name,
                    email = email,
                    password = password,
                    dateOfBirth = dateOfBirth
                )
                val userId = userDao.insertUser(user)
                Result.success(userId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            val user = userDao.login(email, password)
            if (user != null) {
                // Logout all other users and login this one
                userDao.logoutAllUsers()
                userDao.setUserLoggedIn(user.id)
                Result.success(user.copy(isLoggedIn = true))
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        userDao.logoutAllUsers()
    }

    suspend fun getLoggedInUser(): UserEntity? {
        return userDao.getLoggedInUser()
    }

    suspend fun getUserById(userId: Long): UserEntity? {
        return userDao.getUserById(userId)
    }

    // User Stats
    suspend fun updateUserStats(userId: Long) {
        val totalGames = debateHistoryDao.getUserTotalDebates(userId)
        val wins = debateHistoryDao.getUserWinCount(userId)
        val losses = totalGames - wins
        val averageScore = debateHistoryDao.getUserAverageScore(userId) ?: 0f

        userDao.updateUserStats(userId, totalGames, wins, losses, averageScore)
    }

    fun getAllUsers(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }

    // Debate History
    suspend fun saveDebateResult(
        userId: Long,
        topic: String,
        userSide: String,
        opponentType: String,
        userScore: Int,
        opponentScore: Int,
        feedback: String
    ): Long {
        val history = DebateHistoryEntity(
            userId = userId,
            topic = topic,
            userSide = userSide,
            opponentType = opponentType,
            userScore = userScore,
            opponentScore = opponentScore,
            won = userScore > opponentScore,
            feedback = feedback
        )
        val historyId = debateHistoryDao.insertDebateHistory(history)

        // Update user stats
        updateUserStats(userId)

        return historyId
    }

    fun getUserDebateHistory(userId: Long): Flow<List<DebateHistoryEntity>> {
        return debateHistoryDao.getUserDebateHistory(userId)
    }

    suspend fun getRecentDebates(userId: Long, limit: Int = 10): List<DebateHistoryEntity> {
        return debateHistoryDao.getRecentDebates(userId, limit)
    }
}
