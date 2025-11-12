package com.runanywhere.startup_hackathon20.network

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Matchmaking Service for P2P Mode
 * Handles finding opponents and creating matches
 */
class MatchmakingService {
    private val supabase = SupabaseConfig.client

    companion object {
        private const val TAG = "MatchmakingService"
    }

    /**
     * Join matchmaking queue
     * Removes player if already in queue, then adds them
     */
    suspend fun joinQueue(playerId: String, playerName: String): Result<String> {
        return try {
            Log.d(TAG, "=== JOIN QUEUE CALLED ===")
            Log.d(TAG, "Player ID: $playerId")
            Log.d(TAG, "Player Name: $playerName")

            // Remove player from queue if already there (cleanup)
            Log.d(TAG, "Step 1: Removing existing entry (if any)...")
            try {
                supabase.from("matchmaking_queue")
                    .delete {
                        filter { eq("player_id", playerId) }
                    }
                Log.d(TAG, "✅ Cleanup successful (or no entry to remove)")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Cleanup failed (might be okay): ${e.message}")
            }

            // Insert player into queue
            Log.d(TAG, "Step 2: Inserting new entry...")
            val queueEntry = MatchmakingQueueEntry(
                playerId = playerId,
                playerName = playerName,
                status = "WAITING"
            )
            Log.d(TAG, "Entry to insert: $queueEntry")

            try {
                supabase.from("matchmaking_queue")
                    .insert(queueEntry)

                Log.d(TAG, "✅✅✅ INSERT SUCCESSFUL!")
                Log.d(TAG, "Player $playerName is now in matchmaking queue")

                Result.success("Joined queue successfully")
            } catch (insertError: Exception) {
                Log.e(TAG, "❌❌❌ INSERT FAILED!")
                Log.e(TAG, "Error type: ${insertError::class.simpleName}")
                Log.e(TAG, "Error message: ${insertError.message}")
                Log.e(TAG, "Full error:", insertError)
                throw insertError
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ joinQueue() failed completely")
            Log.e(TAG, "Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Find a match (matchmaking logic)
     * Uses FIFO (First In, First Out) - oldest waiting player gets matched
     */
    suspend fun findMatch(playerId: String): Result<MatchResult?> {
        return try {
            // Get all waiting players (excluding current player)
            val waitingPlayers = supabase.from("matchmaking_queue")
                .select {
                    filter {
                        eq("status", "WAITING")
                        neq("player_id", playerId)
                    }
                }
                .decodeList<MatchmakingQueueEntry>()

            // Find oldest waiting player (FIFO)
            val opponent = waitingPlayers
                .sortedBy { it.joinedAt ?: "" }
                .firstOrNull()
                ?: return Result.success(null) // No opponent found

            // Create session ID
            val sessionId = UUID.randomUUID().toString()

            // Mark both players as matched
            supabase.from("matchmaking_queue")
                .update(
                    mapOf(
                        "status" to "MATCHED",
                        "session_id" to sessionId
                    )
                ) {
                    filter {
                        eq("player_id", playerId)
                    }
                }

            supabase.from("matchmaking_queue")
                .update(
                    mapOf(
                        "status" to "MATCHED",
                        "session_id" to sessionId
                    )
                ) {
                    filter {
                        eq("player_id", opponent.playerId)
                    }
                }

            val matchResult = MatchResult(
                sessionId = sessionId,
                opponentId = opponent.playerId,
                opponentName = opponent.playerName
            )

            Result.success(matchResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if player was matched by another player
     * Polls the database to see if status changed to MATCHED
     */
    suspend fun checkIfMatched(playerId: String): Result<MatchResult?> {
        return try {
            val entries = supabase.from("matchmaking_queue")
                .select {
                    filter {
                        eq("player_id", playerId)
                        eq("status", "MATCHED")
                    }
                }
                .decodeList<MatchmakingQueueEntry>()

            val myEntry = entries.firstOrNull()
            if (myEntry != null && myEntry.sessionId != null) {
                // I was matched! Get opponent details
                val matchResult = getMatchDetails(myEntry.sessionId, playerId)
                Result.success(matchResult)
            } else {
                Result.success(null) // Not matched yet
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get match details after being matched
     * Fetches opponent info from the same session
     */
    private suspend fun getMatchDetails(sessionId: String, playerId: String): MatchResult? {
        return try {
            val entries = supabase.from("matchmaking_queue")
                .select {
                    filter {
                        eq("session_id", sessionId)
                    }
                }
                .decodeList<MatchmakingQueueEntry>()

            val opponent = entries.find { it.playerId != playerId } ?: return null

            MatchResult(
                sessionId = sessionId,
                opponentId = opponent.playerId,
                opponentName = opponent.playerName
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cancel matchmaking
     * Removes player from queue
     */
    suspend fun cancelQueue(playerId: String): Result<Unit> {
        return try {
            supabase.from("matchmaking_queue")
                .delete {
                    filter { eq("player_id", playerId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clean up old queue entries (maintenance)
     * Removes entries older than 5 minutes
     */
    suspend fun cleanupOldEntries(): Result<Unit> {
        return try {
            // Call the cleanup function we created in database
            supabase.postgrest.rpc("cleanup_old_queue_entries")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Matchmaking Queue Entry Model
 * Matches the database schema (snake_case columns)
 */
@Serializable
data class MatchmakingQueueEntry(
    @SerialName("id")
    val id: String? = null,  // UUID primary key (auto-generated by DB)

    @SerialName("player_id")
    val playerId: String,

    @SerialName("player_name")
    val playerName: String,

    @SerialName("joined_at")
    val joinedAt: String? = null,  // Auto-generated by DB (default: now())

    @SerialName("status")
    val status: String,

    @SerialName("session_id")
    val sessionId: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null  // Auto-generated by DB (default: now())
)

/**
 * Match Result Model
 * Contains opponent info and session ID
 */
data class MatchResult(
    val sessionId: String,
    val opponentId: String,
    val opponentName: String
)