package com.example.styleshare.viewmodel

import androidx.lifecycle.*
import com.example.styleshare.model.entities.Post
import com.example.styleshare.repository.PostRepository
import kotlinx.coroutines.launch

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    val allPosts: LiveData<List<Post>> = repository.getAllPosts()
    val followingPosts: LiveData<List<Post>> = repository.getFollowingPosts(listOf()) // Pass an empty list or appropriate value

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

    fun updateLikes(postId: String, increment: Int) = viewModelScope.launch {
        repository.updateLikes(postId, increment)
    }

    fun updateCommentsCount(postId: String, increment: Int) = viewModelScope.launch {
        repository.updateCommentsCount(postId, increment)
    }

    fun deletePost(postId: String) = viewModelScope.launch {
        repository.deletePost(postId)
    }

    fun getFollowingPosts(followedUserIds: List<String>): LiveData<List<Post>> {
        return repository.getPostsByUser(followedUserIds.toString())
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