package com.runanywhere.startup_hackathon20.network

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Matchmaking Service for P2P Mode
 * Handles finding opponents and creating matches
 */
class MatchmakingService {
    private val supabase = SupabaseConfig.client

    /**
     * Join matchmaking queue
     * Removes player if already in queue, then adds them
     */
    suspend fun joinQueue(playerId: String, playerName: String): Result<String> {
        return try {
            // Remove player from queue if already there (cleanup)
            supabase.from("matchmaking_queue")
                .delete {
                    filter { eq("player_id", playerId) }
                }

            // Insert player into queue
            val queueEntry = MatchmakingQueueEntry(
                playerId = playerId,
                playerName = playerName,
                status = "WAITING"
            )

            supabase.from("matchmaking_queue")
                .insert(queueEntry)

            Result.success("Joined queue successfully")
        } catch (e: Exception) {
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
                .sortedBy { it.joinedAt }
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
 * Matches the database schema
 */
@Serializable
data class MatchmakingQueueEntry(
    val playerId: String,
    val playerName: String,
    val status: String,
    val joinedAt: String = "",
    val sessionId: String? = null
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