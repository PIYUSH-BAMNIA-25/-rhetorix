package com.runanywhere.startup_hackathon20.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DebateHistoryDao {

    @Insert
    suspend fun insertDebateHistory(history: DebateHistoryEntity): Long

    @Query("SELECT * FROM debate_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserDebateHistory(userId: Long): Flow<List<DebateHistoryEntity>>

    @Query("SELECT * FROM debate_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentDebates(userId: Long, limit: Int = 10): List<DebateHistoryEntity>

    @Query("SELECT COUNT(*) FROM debate_history WHERE userId = :userId AND won = 1")
    suspend fun getUserWinCount(userId: Long): Int

    @Query("SELECT COUNT(*) FROM debate_history WHERE userId = :userId")
    suspend fun getUserTotalDebates(userId: Long): Int

    @Query("SELECT AVG(userScore) FROM debate_history WHERE userId = :userId")
    suspend fun getUserAverageScore(userId: Long): Float?

    @Query("DELETE FROM debate_history WHERE userId = :userId")
    suspend fun deleteUserHistory(userId: Long)

    @Delete
    suspend fun deleteDebateHistory(history: DebateHistoryEntity)
}
