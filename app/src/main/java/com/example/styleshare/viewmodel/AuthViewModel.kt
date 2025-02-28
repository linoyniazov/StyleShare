package com.example.styleshare.viewmodel

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage

    fun register(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.registerUser(email, password)
            result.onSuccess { user ->
                _authState.value = user
                _authMessage.value = "Registration successful"
            }.onFailure { exception ->
                _authError.value = exception.message
                _authMessage.value = "Registration failed: ${exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.loginUser(email, password)
            result.onSuccess { user ->
                _authState.value = user
                _authMessage.value = "Login successful"
            }.onFailure { exception ->
                _authError.value = exception.message
                _authMessage.value = "Login failed: ${exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logoutUser()
        _authState.value = null
        _authMessage.value = "Logged out successfully"
    }
}