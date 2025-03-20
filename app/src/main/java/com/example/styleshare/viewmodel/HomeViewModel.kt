package com.example.styleshare.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.styleshare.model.entities.Post
import com.example.styleshare.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    val allPosts: LiveData<List<Post>> = repository.getAllPosts()

    fun checkDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.checkDatabase() // ✅ קריאה תקינה בתוך Coroutine
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