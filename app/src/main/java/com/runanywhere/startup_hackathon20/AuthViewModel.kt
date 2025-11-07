package com.runanywhere.startup_hackathon20

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.network.ServerRepository
import com.runanywhere.startup_hackathon20.network.models.ServerUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserData) : AuthState()
    data class Error(val message: String) : AuthState()
}

// Unified user data class
data class UserData(
    val id: String,
    val playerId: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val dateOfBirth: String,
    val totalGames: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val likes: Int = 0,
    val averageScore: Float = 0f
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val serverRepository = ServerRepository(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    companion object {
        private const val TAG = "AuthViewModel"
    }

    // Check if user is already logged in
    init {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Checking for logged in user from server...")
                val serverUser = serverRepository.getCurrentUser()
                if (serverUser != null) {
                    _authState.value = AuthState.Success(serverUser.toUserData())
                    Log.d(TAG, "Found logged in user: ${serverUser.playerId}")
                } else {
                    Log.d(TAG, "No logged in user found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking logged in user: ${e.message}", e)
                _authState.value =
                    AuthState.Error("Failed to connect to server. Please check your internet connection.")
            }
        }
    }

    fun signUp(
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        dateOfBirth: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val serverResult = serverRepository.signUp(
                username = username,
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                dateOfBirth = dateOfBirth
            )

            serverResult.onSuccess { serverUser ->
                Log.d(TAG, "Server signup successful: ${serverUser.playerId}")
                // Auto-login after signup
                login(email, password)
            }.onFailure { error ->
                Log.e(TAG, "Server signup failed: ${error.message}")
                _authState.value =
                    AuthState.Error(error.message ?: "Sign up failed. Please try again.")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val serverResult = serverRepository.login(email, password)

            serverResult.onSuccess { serverUser ->
                Log.d(TAG, "Server login successful: ${serverUser.playerId}")
                _authState.value = AuthState.Success(serverUser.toUserData())
            }.onFailure { error ->
                Log.e(TAG, "Server login failed: ${error.message}")
                _authState.value =
                    AuthState.Error(error.message ?: "Login failed. Please check your credentials.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            serverRepository.logout()
            _authState.value = AuthState.Idle
            Log.d(TAG, "Logout successful")
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // Extension function to convert ServerUser to UserData
    private fun ServerUser.toUserData() = UserData(
        id = id ?: "",
        playerId = playerId,
        username = username,
        firstName = firstName,
        lastName = lastName,
        email = email,
        dateOfBirth = dateOfBirth,
        totalGames = totalGames,
        wins = wins,
        losses = losses,
        likes = likes,
        averageScore = averageScore
    )
}
