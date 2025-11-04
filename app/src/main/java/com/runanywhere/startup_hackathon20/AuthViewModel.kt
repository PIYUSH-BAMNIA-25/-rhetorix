package com.runanywhere.startup_hackathon20.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.database.RhetorixDatabase
import com.runanywhere.startup_hackathon20.database.UserEntity
import com.runanywhere.startup_hackathon20.database.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserEntity) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = RhetorixDatabase.getDatabase(application)
    private val repository = UserRepository(database.userDao(), database.debateHistoryDao())

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Check if user is already logged in
    init {
        viewModelScope.launch {
            val loggedInUser = repository.getLoggedInUser()
            if (loggedInUser != null) {
                _authState.value = AuthState.Success(loggedInUser)
            }
        }
    }

    fun signUp(name: String, email: String, password: String, dateOfBirth: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.signUp(name, email, password, dateOfBirth)

            result.onSuccess { userId ->
                // Auto-login after signup
                login(email, password)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Sign up failed")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.login(email, password)

            result.onSuccess { user ->
                _authState.value = AuthState.Success(user)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthState.Idle
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
