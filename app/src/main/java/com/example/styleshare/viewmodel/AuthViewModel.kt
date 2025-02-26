package com.example.styleshare.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styleshare.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<FirebaseUser?>(authRepository.getCurrentUser())
    val authState: StateFlow<FirebaseUser?> = _authState

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    fun register(email: String, password: String) {
        Log.d("AuthViewModel", "🔄 Calling register() with email: $email")
        viewModelScope.launch {
            val result = authRepository.registerUser(email, password)
            result.onSuccess { user ->
                Log.d("AuthViewModel", "✅ Register successful: ${user.email}")
                _authState.value = user
            }.onFailure { exception ->
                Log.e("AuthViewModel", "❌ Register failed: ${exception.message}")
                _authError.value = exception.message
            }
        }
    }

    fun login(email: String, password: String) {
        Log.d("AuthViewModel", "🔄 Calling login() with email: $email")
        viewModelScope.launch {
            val result = authRepository.loginUser(email, password)
            result.onSuccess { user ->
                Log.d("AuthViewModel", "✅ Login successful: ${user.email}")
                _authState.value = user
            }.onFailure { exception ->
                Log.e("AuthViewModel", "❌ Login failed: ${exception.message}")
                _authError.value = exception.message
            }
        }
    }

    fun logout() {
        Log.d("AuthViewModel", "🔴 Logging out user")
        authRepository.logoutUser()
        _authState.value = null
    }
}