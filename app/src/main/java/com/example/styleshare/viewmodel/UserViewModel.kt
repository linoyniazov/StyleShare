package com.example.styleshare.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.model.entities.User
import com.example.styleshare.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user
    val allUsers: LiveData<List<User>> = repository.getAllUsers()

    fun fetchUserById(userId: String) {
        viewModelScope.launch {
            val fetchedUser = repository.getUserById(userId)
            _user.postValue(fetchedUser)
        }
    }

    fun insertUser(user: User) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertUser(user)
    }

    suspend fun doesUserExist(userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext repository.doesUserExist(userId)
    }

    fun checkUserExists(userId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = repository.doesUserExist(userId)
            callback(exists)
        }
    }

    fun updateUser(
        id: String,
        username: String,
        email: String,
        fullName: String,
        profileImageUrl: String,
        bio: String,
        followersCount: String,
        followingCount: String,
        postsCount: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateUser(
            id,
            username,
            email,
            fullName,
            profileImageUrl,
            bio,
            followersCount,
            followingCount,
            postsCount
        )
    }

    fun updateFollowersCount(userId: String, increment: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateFollowersCount(userId, increment)
    }

    fun updateFollowingCount(userId: String, increment: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateFollowingCount(userId, increment)
    }

    fun updatePostsCount(userId: String, increment: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.updatePostsCount(userId, increment)
    }

    fun deleteUser(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteUser(userId)
    }

    class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

fun createUserViewModel(application: Application): UserViewModel {
    val repository = UserRepository(AppDatabase.getDatabase(application).userDao())
    return UserViewModel(repository)
}