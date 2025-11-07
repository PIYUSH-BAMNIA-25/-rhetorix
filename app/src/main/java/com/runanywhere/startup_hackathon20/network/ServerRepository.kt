package com.runanywhere.startup_hackathon20.network

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.runanywhere.startup_hackathon20.network.models.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.random.Random

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class ServerRepository(private val context: Context) {
    
    private val supabase = SupabaseConfig.client
    private val dataStore = context.dataStore
    
    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private const val TAG = "ServerRepository"
    }
    
    // DataStore operations
    private suspend fun saveSession(userId: String, accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }
    
    private suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
    
    private fun getStoredUserId(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[USER_ID_KEY]
        }
    }
    
    // Generate unique Player ID
    private suspend fun generatePlayerId(): String {
        var playerId: String
        var isUnique: Boolean
        
        do {
            val randomNumber = Random.nextInt(100000, 999999)
            playerId = "R$randomNumber"
            
            // Check if this ID already exists
            try {
                val existing = supabase.from("users")
                    .select(columns = Columns.list("player_id")) {
                        filter {
                            eq("player_id", playerId)
                        }
                    }
                    .decodeSingle<ServerUser>()
                isUnique = existing == null
            } catch (e: Exception) {
                isUnique = true // If not found, it's unique
            }
        } while (!isUnique)
        
        return playerId
    }
    
    // Authentication
    suspend fun signUp(
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        dateOfBirth: String
    ): Result<ServerUser> {
        return try {
            Log.d(TAG, "Starting sign up for email: $email")
            
            // Sign up with Supabase Auth
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            // IMPORTANT: If email confirmation is required, we need to handle it
            // For now, we'll try to login immediately after signup
            // If your Supabase has email confirmation enabled, disable it in:
            // Dashboard → Authentication → Providers → Email → Confirm email (OFF)
            
            try {
                // Attempt auto-login after signup
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
            } catch (e: Exception) {
                // If login fails due to email confirmation requirement
                Log.e(TAG, "Auto-login after signup failed: ${e.message}")
                
                // Return a more helpful error message
                return Result.failure(
                    Exception(
                        "Account created! Please check your email to confirm your account, " +
                        "or disable email confirmation in Supabase Dashboard: " +
                        "Authentication → Providers → Email → Confirm email (OFF)"
                    )
                )
            }

            val userId =
                supabase.auth.currentUserOrNull()?.id ?: throw Exception("User ID not found")
            Log.d(TAG, "Auth sign up successful, userId: $userId")
            
            // Generate player ID
            val playerId = generatePlayerId()
            Log.d(TAG, "Generated player ID: $playerId")
            
            // Create user profile in database
            val user = ServerUser(
                id = userId,
                playerId = playerId,
                username = username,
                firstName = firstName,
                lastName = lastName,
                email = email,
                dateOfBirth = dateOfBirth
            )
            
            supabase.from("users").insert(user)
            Log.d(TAG, "User profile created successfully")
            
            // Save session tokens
            val accessToken = supabase.auth.currentAccessTokenOrNull() ?: ""
            val refreshToken = supabase.auth.currentSessionOrNull()?.refreshToken ?: ""
            saveSession(userId, accessToken, refreshToken)
            
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign up error: ${e.message}", e)
            
            // Provide user-friendly error messages
            val errorMessage = when {
                e.message?.contains("Email not confirmed", ignoreCase = true) == true -> {
                    "Email confirmation required. Please disable it in Supabase Dashboard: " +
                    "Authentication → Providers → Email → Confirm email (OFF)"
                }
                e.message?.contains("already registered", ignoreCase = true) == true -> {
                    "This email is already registered. Please login instead."
                }
                e.message?.contains("Invalid email", ignoreCase = true) == true -> {
                    "Invalid email format. Please check your email address."
                }
                e.message?.contains("password", ignoreCase = true) == true -> {
                    "Password must be at least 6 characters long."
                }
                else -> e.message ?: "Sign up failed. Please try again."
            }
            
            Result.failure(Exception(errorMessage))
        }
    }
    
    suspend fun login(email: String, password: String): Result<ServerUser> {
        return try {
            Log.d(TAG, "Starting login for email: $email")
            
            // Login with Supabase Auth
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val userId =
                supabase.auth.currentUserOrNull()?.id ?: throw Exception("User ID not found")
            Log.d(TAG, "Auth login successful, userId: $userId")
            
            // Get user profile from database
            val user = supabase.from("users")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<ServerUser>()
            
            Log.d(TAG, "User profile fetched: ${user.playerId}")
            
            // Save session tokens
            val accessToken = supabase.auth.currentAccessTokenOrNull() ?: ""
            val refreshToken = supabase.auth.currentSessionOrNull()?.refreshToken ?: ""
            saveSession(userId, accessToken, refreshToken)
            
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        try {
            supabase.auth.signOut()
            clearSession()
            Log.d(TAG, "Logout successful")
        } catch (e: Exception) {
            Log.e(TAG, "Logout error: ${e.message}", e)
        }
    }
    
    suspend fun getCurrentUser(): ServerUser? {
        return try {
            val userId = getStoredUserId().firstOrNull() ?: return null
            
            supabase.from("users")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<ServerUser>()
        } catch (e: Exception) {
            Log.e(TAG, "Get current user error: ${e.message}", e)
            null
        }
    }
    
    // User Stats
    suspend fun updateUserStats(userId: String): Result<Unit> {
        return try {
            // Get all debates for user
            val debates = supabase.from("debate_history")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<ServerDebateHistory>()
            
            val totalGames = debates.size
            val wins = debates.count { it.won }
            val losses = totalGames - wins
            val averageScore = if (debates.isNotEmpty()) {
                debates.map { it.userScore }.average().toFloat()
            } else {
                0f
            }
            
            // Update user stats
            supabase.from("users")
                .update({
                    set("total_games", totalGames)
                    set("wins", wins)
                    set("losses", losses)
                    set("average_score", averageScore)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
            
            Log.d(TAG, "Stats updated: $totalGames games, $wins wins, avg: $averageScore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Update stats error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Debate History
    suspend fun saveDebateResult(
        topic: String,
        userSide: String,
        opponentType: String,
        userScore: Int,
        opponentScore: Int,
        feedback: String
    ): Result<ServerDebateHistory> {
        return try {
            val userId = getStoredUserId().firstOrNull() 
                ?: return Result.failure(Exception("User not logged in"))
            
            val debate = ServerDebateHistory(
                userId = userId,
                topic = topic,
                userSide = userSide,
                opponentType = opponentType,
                userScore = userScore,
                opponentScore = opponentScore,
                won = userScore > opponentScore,
                feedback = feedback
            )
            
            supabase.from("debate_history").insert(debate)
            
            // Update user stats
            updateUserStats(userId)
            
            Log.d(TAG, "Debate saved: $topic, score: $userScore vs $opponentScore")
            Result.success(debate)
        } catch (e: Exception) {
            Log.e(TAG, "Save debate error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUserDebateHistory(limit: Int = 50): Result<List<ServerDebateHistory>> {
        return try {
            val userId = getStoredUserId().firstOrNull() 
                ?: return Result.failure(Exception("User not logged in"))
            
            val debates = supabase.from("debate_history")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(count = limit.toLong())
                }
                .decodeList<ServerDebateHistory>()
            
            Log.d(TAG, "Fetched ${debates.size} debate history records")
            Result.success(debates)
        } catch (e: Exception) {
            Log.e(TAG, "Get debate history error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAllUsers(): Result<List<ServerUser>> {
        return try {
            val users = supabase.from("users")
                .select {
                    order(column = "total_games", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<ServerUser>()
            
            Log.d(TAG, "Fetched ${users.size} users for leaderboard")
            Result.success(users)
        } catch (e: Exception) {
            Log.e(TAG, "Get all users error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Check if server is reachable
    suspend fun checkServerConnection(): Boolean {
        return try {
            supabase.from("users").select().decodeList<ServerUser>()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Server connection check failed: ${e.message}", e)
            false
        }
    }
}
