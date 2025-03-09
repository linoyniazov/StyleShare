package com.example.styleshare.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.styleshare.model.entities.Post
import com.example.styleshare.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    fun checkDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.checkDatabase() // ✅ קריאה תקינה בתוך Coroutine
        }
    }


    private val _followingUsers = MutableLiveData<List<String>>(emptyList()) // ✅ הגדרה מפורשת
    val followingUsers: LiveData<List<String>> get() = _followingUsers

    val allPosts: LiveData<List<Post>> = repository.getAllPosts()
    val forYouPosts: LiveData<List<Post>> = repository.getPopularPosts()

    // ✅ טעינת פוסטים של משתמשים שאחריהם המשתמש עוקב
    val followingPosts: LiveData<List<Post>> = _followingUsers.switchMap { users ->
        val posts = repository.getFollowingPosts(users)
        Log.d("HomeViewModel", "מספר הפוסטים שהתקבלו: ${posts.value?.size ?: 0}")
        posts
    }

    // ✅ פונקציה לטעינת המשתמשים שאחריהם המשתמש עוקב
    fun loadFollowingUsers(userId: String) {
        viewModelScope.launch {
            val users = repository.getFollowingUserIds(userId)
            Log.d("HomeViewModel", "משתמשים שאחריהם עוקבים: $users")
            _followingUsers.postValue(users)
        }
    }

    class HomeViewModelFactory(private val repository: HomeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


}