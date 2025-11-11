package com.runanywhere.startup_hackathon20

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.network.MatchmakingService
import com.runanywhere.startup_hackathon20.network.MatchResult
import com.runanywhere.startup_hackathon20.network.P2PDebateService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Matchmaking State
 * Represents the current state of matchmaking
 */
data class MatchmakingState(
    val isSearching: Boolean = false,
    val matchResult: MatchResult? = null,
    val error: String? = null,
    val playerId: String = "",
    val playerName: String = ""
)

/**
 * Matchmaking ViewModel
 * Manages matchmaking logic and state
 */
class MatchmakingViewModel(application: Application) : AndroidViewModel(application) {
    private val matchmakingService = MatchmakingService()
    private val p2pService = P2PDebateService()

    private val _matchState = MutableStateFlow(MatchmakingState())
    val matchState: StateFlow<MatchmakingState> = _matchState.asStateFlow()

    private val _createdSessionId = MutableStateFlow<String?>(null)
    val createdSessionId: StateFlow<String?> = _createdSessionId

    private var isPolling = false

    /**
     * Start matchmaking
     * Joins queue and starts polling for matches
     */
    fun startMatchmaking(playerId: String, playerName: String) {
        viewModelScope.launch {
            try {
                _matchState.update {
                    it.copy(
                        isSearching = true,
                        error = null,
                        playerId = playerId,
                        playerName = playerName,
                        matchResult = null
                    )
                }

                // Join queue
                matchmakingService.joinQueue(playerId, playerName).onSuccess {
                    Log.d("Matchmaking", "Successfully joined queue")

                    // Start polling for matches
                    startPolling(playerId)
                }.onFailure { error ->
                    Log.e("Matchmaking", "Failed to join queue: ${error.message}")
                    _matchState.update {
                        it.copy(
                            isSearching = false,
                            error = "Failed to join queue: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("Matchmaking", "Error starting matchmaking: ${e.message}")
                _matchState.update {
                    it.copy(
                        isSearching = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    /**
     * Start polling for matches
     * Checks every 2 seconds if a match is found
     * Uses two strategies: actively finding matches and checking if matched by others
     */
    private fun startPolling(playerId: String) {
        isPolling = true
        viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 60 // 2 minutes (60 * 2 seconds)

            while (isPolling && _matchState.value.matchResult == null && attempts < maxAttempts) {
                delay(2000) // Check every 2 seconds
                attempts++

                Log.d("Matchmaking", "Polling attempt $attempts/$maxAttempts")

                // Strategy 1: Try to find and match with someone
                matchmakingService.findMatch(playerId).onSuccess { matchResult ->
                    if (matchResult != null && _matchState.value.matchResult == null) {
                        Log.d("Matchmaking", "Match found (active): ${matchResult.opponentName}")
                        handleMatchFound(matchResult)
                        isPolling = false
                        return@launch
                    }
                }

                // Strategy 2: Check if someone else matched with me
                matchmakingService.checkIfMatched(playerId).onSuccess { matchResult ->
                    if (matchResult != null && _matchState.value.matchResult == null) {
                        Log.d("Matchmaking", "Match found (passive): ${matchResult.opponentName}")
                        handleMatchFound(matchResult)
                        isPolling = false
                        return@launch
                    }
                }
            }

            // Timeout after 2 minutes
            if (attempts >= maxAttempts && _matchState.value.matchResult == null) {
                Log.w("Matchmaking", "Matchmaking timeout - no opponent found")
                _matchState.update {
                    it.copy(
                        isSearching = false,
                        error = "No opponent found. Please try again."
                    )
                }
                cancelMatchmaking()
            }
        }
    }

    /**
     * Handle successful match - create P2P session
     */
    private suspend fun handleMatchFound(match: MatchResult) {
        Log.d("Matchmaking", "Match found! Opponent: ${match.opponentName}")
        _matchState.update {
            it.copy(
                isSearching = false,
                matchResult = match
            )
        }

        // Create P2P debate session
        val currentPlayerId = _matchState.value.playerId
        val currentPlayerName = _matchState.value.playerName

        p2pService.createSession(
            player1Id = currentPlayerId,
            player1Name = currentPlayerName,
            player2Id = match.opponentId,
            player2Name = match.opponentName
        ).onSuccess { sessionId ->
            Log.d("Matchmaking", "P2P session created: $sessionId")
            _createdSessionId.value = sessionId
        }.onFailure { error ->
            Log.e("Matchmaking", "Failed to create P2P session", error)
            _matchState.update {
                it.copy(
                    error = "Failed to create session: ${error.message}"
                )
            }
        }
    }

    /**
     * Cancel matchmaking
     * Removes player from queue and stops searching
     */
    fun cancelMatchmaking() {
        viewModelScope.launch {
            isPolling = false

            val playerId = _matchState.value.playerId
            if (playerId.isNotEmpty()) {
                matchmakingService.cancelQueue(playerId).onSuccess {
                    Log.d("Matchmaking", "Successfully left queue")
                }.onFailure { error ->
                    Log.e("Matchmaking", "Failed to leave queue: ${error.message}")
                }
            }

            _matchState.update {
                MatchmakingState() // Reset to initial state
            }
        }
    }

    /**
     * Reset state
     * Clears error and match result
     */
    fun resetState() {
        _matchState.update { MatchmakingState() }
    }

    /**
     * Cleanup when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        cancelMatchmaking()
    }
}