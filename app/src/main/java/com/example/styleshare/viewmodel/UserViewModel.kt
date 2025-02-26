package com.example.styleshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styleshare.model.entities.User
import com.example.styleshare.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    // פונקציה לשליפת משתמש לפי ID ועדכון ה-LiveData
    fun fetchUserById(userId: String) {
        viewModelScope.launch {
            val fetchedUser = userRepository.getUserById(userId)
            _user.postValue(fetchedUser)
        }
    }

    // פונקציה להכנסת משתמש חדש
    fun insertUser(user: User) {
        viewModelScope.launch {
            userRepository.insertUser(user)
        }
    }

    // פונקציה לבדיקה אם משתמש קיים
    fun doesUserExist(userId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = userRepository.doesUserExist(userId)
            callback(exists)
        }
    }

    // פונקציה לעדכון משתמש
    fun updateUser(
        userId: String,
        username: String,
        fullName: String,
        profileImageUrl: String,
        bio: String,
        followers: Int,
        following: Int
    ) {
        viewModelScope.launch {
            userRepository.updateUser(userId, username, fullName, profileImageUrl, bio, followers, following)
        }
    }

    // פונקציה למחיקת משתמש
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            userRepository.deleteUser(userId)
        }
    }

    // פונקציה לשליפת כל המשתמשים
    fun getAllUsers(): LiveData<List<User>> {
        return userRepository.getAllUsers()
    }
}
