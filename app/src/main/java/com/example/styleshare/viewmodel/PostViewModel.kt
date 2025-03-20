package com.example.styleshare.viewmodel

import androidx.lifecycle.*
import com.example.styleshare.model.entities.Post
import com.example.styleshare.repository.PostRepository
import kotlinx.coroutines.launch

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    // 🔁 שמור רק למקומות שצריכים ROOM
    val allPosts: LiveData<List<Post>> = repository.getAllPosts()

    fun insertPost(post: Post) = viewModelScope.launch {
        repository.insertPost(post)
    }

    fun getPostsByUser(userId: String): LiveData<List<Post>> {
        return repository.getPostsByUser(userId)
    }

    fun getPostById(postId: String): LiveData<Post> {
        return repository.getPostById(postId)
    }

    fun updatePost(postId: String, caption: String, category: String) = viewModelScope.launch {
        repository.updatePost(postId, caption, category)
    }

    fun deletePost(postId: String) = viewModelScope.launch {
        repository.deletePost(postId)
    }

    // ✅ לשימוש במסך הבית (מ-Firestore)
    fun getAllPostsFromFirestore(): LiveData<List<Post>> {
        return repository.getAllPostsFromFirestore()
    }

    // (אופציונלי) אם רוצים גם לשמור ב-ROOM
    fun syncPostsFromFirestore() = viewModelScope.launch {
        repository.syncPostsFromFirestore()
    }

    class PostViewModelFactory(private val repository: PostRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PostViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
