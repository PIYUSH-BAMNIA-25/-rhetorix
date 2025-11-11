package com.runanywhere.startup_hackathon20.network

import android.util.Log
import com.runanywhere.startup_hackathon20.DebateMessage
import com.runanywhere.startup_hackathon20.TopicGenerator
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * P2P Debate Service
 * Handles all database operations for player-vs-player debates
 */
class P2PDebateService {

    private val supabase = SupabaseConfig.client
    private val TAG = "P2PDebateService"

    /**
     * Data classes for Supabase interaction
     */
    data class P2PSessionResponse(
        val session_id: String,
        val topic_id: String,
        val topic_title: String,
        val topic_description: String? = null,
        val player1_id: String,
        val player1_name: String,
        val player1_side: String,
        val player2_id: String,
        val player2_name: String,
        val player2_side: String,
        val status: String,
        val current_turn: String,
        val turn_number: Int,
        val prep_time_remaining: Int,
        val debate_time_remaining: Long,
        val start_time: String? = null,
        val end_time: String? = null,
        val created_at: String? = null,
        val updated_at: String? = null
    )

    data class P2PMessageResponse(
        val id: String,
        val session_id: String,
        val player_id: String,
        val player_name: String,
        val message: String,
        val turn_number: Int,
        val timestamp: String,
        val word_count: Int? = null
    )

    /**
     * Create a new P2P debate session
     * Called by matchmaking service when two players are matched
     */
    suspend fun createSession(
        player1Id: String,
        player1Name: String,
        player2Id: String,
        player2Name: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎮 Creating P2P session: $player1Name vs $player2Name")

            // 1. Generate random topic (same as AI mode!)
            val (topic, _) = TopicGenerator.generateDynamicTopic()
            Log.d(TAG, "📜 Generated topic: ${topic.title}")

            // 2. Random side assignment
            val player1Side = listOf("FOR", "AGAINST").random()
            val player2Side = if (player1Side == "FOR") "AGAINST" else "FOR"
            Log.d(TAG, "⚔️ Sides: $player1Name = $player1Side, $player2Name = $player2Side")

            // 3. Random first turn (replaces coin toss!)
            val firstTurn = listOf(player1Id, player2Id).random()
            val starterName = if (firstTurn == player1Id) player1Name else player2Name
            Log.d(TAG, "🎲 First turn: $starterName ($firstTurn)")

            // 4. Create session ID
            val sessionId = UUID.randomUUID().toString()

            // 5. Insert into database
            val sessionData = mapOf(
                "session_id" to sessionId,
                "topic_id" to topic.id,
                "topic_title" to topic.title,
                "topic_description" to topic.description,
                "player1_id" to player1Id,
                "player1_name" to player1Name,
                "player1_side" to player1Side,
                "player2_id" to player2Id,
                "player2_name" to player2Name,
                "player2_side" to player2Side,
                "status" to "PREP",
                "current_turn" to firstTurn,
                "turn_number" to 0,
                "prep_time_remaining" to 30, // 30 seconds prep
                "debate_time_remaining" to 600000, // 10 minutes
                "start_time" to System.currentTimeMillis().toString()
            )

            supabase.from("debate_sessions_p2p").insert(sessionData)

            Log.d(TAG, "✅ P2P session created successfully: $sessionId")
            Result.success(sessionId)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating P2P session", e)
            Result.failure(e)
        }
    }

    /**
     * Get session details by ID
     */
    suspend fun getSession(sessionId: String): Result<P2PSessionResponse> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📥 Fetching session: $sessionId")

                val response = supabase.from("debate_sessions_p2p")
                    .select {
                        filter {
                            eq("session_id", sessionId)
                        }
                    }
                    .decodeSingle<P2PSessionResponse>()

                Log.d(TAG, "✅ Session fetched: ${response.topic_title}")
                Result.success(response)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error fetching session", e)
                Result.failure(e)
            }
        }

    /**
     * Update session status (PREP → IN_PROGRESS → FINISHED)
     */
    suspend fun updateSessionStatus(
        sessionId: String,
        newStatus: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Updating session status to: $newStatus")

            supabase.from("debate_sessions_p2p")
                .update({
                    set("status", newStatus)
                    set("updated_at", System.currentTimeMillis().toString())
                }) {
                    filter {
                        eq("session_id", sessionId)
                    }
                }

            Log.d(TAG, "✅ Session status updated")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating session status", e)
            Result.failure(e)
        }
    }

    /**
     * Update current turn (switches between players)
     */
    suspend fun updateTurn(
        sessionId: String,
        nextPlayerId: String,
        turnNumber: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Switching turn to: $nextPlayerId (Turn #$turnNumber)")

            supabase.from("debate_sessions_p2p")
                .update({
                    set("current_turn", nextPlayerId)
                    set("turn_number", turnNumber)
                    set("updated_at", System.currentTimeMillis().toString())
                }) {
                    filter {
                        eq("session_id", sessionId)
                    }
                }

            Log.d(TAG, "✅ Turn updated")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating turn", e)
            Result.failure(e)
        }
    }

    /**
     * Send a message in the debate
     */
    suspend fun sendMessage(
        sessionId: String,
        playerId: String,
        playerName: String,
        message: String,
        turnNumber: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📤 Sending message from $playerName (Turn $turnNumber)")

            val messageId = UUID.randomUUID().toString()
            val messageData = mapOf(
                "id" to messageId,
                "session_id" to sessionId,
                "player_id" to playerId,
                "player_name" to playerName,
                "message" to message,
                "turn_number" to turnNumber,
                "timestamp" to System.currentTimeMillis().toString()
            )

            supabase.from("debate_messages_p2p").insert(messageData)

            Log.d(TAG, "✅ Message sent successfully")
            Result.success(messageId)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sending message", e)
            Result.failure(e)
        }
    }

    /**
     * Get all messages for a session (ordered by timestamp)
     */
    suspend fun getMessages(sessionId: String): Result<List<DebateMessage>> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📥 Fetching messages for session: $sessionId")

                val response = supabase.from("debate_messages_p2p")
                    .select {
                        filter {
                            eq("session_id", sessionId)
                        }
                    }
                    .decodeList<P2PMessageResponse>()

                // Convert to DebateMessage and sort by timestamp
                val messages = response.map { msg ->
                    DebateMessage(
                        id = msg.id,
                        playerId = msg.player_id,
                        playerName = msg.player_name,
                        message = msg.message,
                        timestamp = msg.timestamp.toLongOrNull() ?: System.currentTimeMillis(),
                        turnNumber = msg.turn_number
                    )
                }.sortedBy { it.timestamp }

                Log.d(TAG, "✅ Fetched ${messages.size} messages")
                Result.success(messages)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error fetching messages", e)
                Result.failure(e)
            }
        }

    /**
     * Poll for new messages (backup if real-time fails)
     * Returns messages with turn_number > lastSeenTurnNumber
     */
    suspend fun pollNewMessages(
        sessionId: String,
        lastSeenTurnNumber: Int
    ): Result<List<DebateMessage>> = withContext(Dispatchers.IO) {
        try {
            val response = supabase.from("debate_messages_p2p")
                .select {
                    filter {
                        eq("session_id", sessionId)
                        gt("turn_number", lastSeenTurnNumber)
                    }
                }
                .decodeList<P2PMessageResponse>()

            val messages = response.map { msg ->
                DebateMessage(
                    id = msg.id,
                    playerId = msg.player_id,
                    playerName = msg.player_name,
                    message = msg.message,
                    timestamp = msg.timestamp.toLongOrNull() ?: System.currentTimeMillis(),
                    turnNumber = msg.turn_number
                )
            }.sortedBy { it.timestamp }

            if (messages.isNotEmpty()) {
                Log.d(TAG, "📬 Polled ${messages.size} new messages")
            }

            Result.success(messages)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error polling messages", e)
            Result.failure(e)
        }
    }

    /**
     * Check if session status changed (for prep → in_progress transition)
     */
    suspend fun pollSessionStatus(sessionId: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = supabase.from("debate_sessions_p2p")
                    .select {
                        filter {
                            eq("session_id", sessionId)
                        }
                    }
                    .decodeSingle<P2PSessionResponse>()

                Result.success(response.status)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error polling session status", e)
                Result.failure(e)
            }
        }

    /**
     * Check whose turn it is (for UI updates)
     */
    suspend fun pollCurrentTurn(sessionId: String): Result<Pair<String, Int>> =
        withContext(Dispatchers.IO) {
            try {
                val response = supabase.from("debate_sessions_p2p")
                    .select {
                        filter {
                            eq("session_id", sessionId)
                        }
                    }
                    .decodeSingle<P2PSessionResponse>()

                Result.success(Pair(response.current_turn, response.turn_number))

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error polling current turn", e)
                Result.failure(e)
            }
        }

    /**
     * End debate session
     */
    suspend fun endSession(sessionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏁 Ending session: $sessionId")

            supabase.from("debate_sessions_p2p")
                .update({
                    set("status", "FINISHED")
                    set("end_time", System.currentTimeMillis().toString())
                    set("updated_at", System.currentTimeMillis().toString())
                }) {
                    filter {
                        eq("session_id", sessionId)
                    }
                }

            Log.d(TAG, "✅ Session ended")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error ending session", e)
            Result.failure(e)
        }
    }

    /**
     * Delete session and all messages (cleanup)
     */
    suspend fun deleteSession(sessionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗑️ Deleting session: $sessionId")

            // Messages will auto-delete due to CASCADE foreign key
            supabase.from("debate_sessions_p2p")
                .delete {
                    filter {
                        eq("session_id", sessionId)
                    }
                }

            Log.d(TAG, "✅ Session deleted")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting session", e)
            Result.failure(e)
        }
    }

    /**
     * Get opponent info for current player
     */
    suspend fun getOpponentInfo(
        sessionId: String,
        currentPlayerId: String
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val session = getSession(sessionId).getOrThrow()

            val (opponentId, opponentName) = if (currentPlayerId == session.player1_id) {
                Pair(session.player2_id, session.player2_name)
            } else {
                Pair(session.player1_id, session.player1_name)
            }

            Result.success(Pair(opponentId, opponentName))

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting opponent info", e)
            Result.failure(e)
        }
    }
}
