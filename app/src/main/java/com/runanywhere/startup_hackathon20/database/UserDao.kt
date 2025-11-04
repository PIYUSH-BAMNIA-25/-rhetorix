package com.runanywhere.startup_hackathon20.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    @Query("UPDATE users SET isLoggedIn = 1 WHERE id = :userId")
    suspend fun setUserLoggedIn(userId: Long)

    @Query("UPDATE users SET totalGames = :totalGames, wins = :wins, losses = :losses, averageScore = :averageScore WHERE id = :userId")
    suspend fun updateUserStats(
        userId: Long,
        totalGames: Int,
        wins: Int,
        losses: Int,
        averageScore: Float
    )

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Delete
    suspend fun deleteUser(user: UserEntity)
}
