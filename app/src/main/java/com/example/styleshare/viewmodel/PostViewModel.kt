package com.example.styleshare.viewmodel

import androidx.lifecycle.*
import com.example.styleshare.model.entities.Post
import com.example.styleshare.repository.PostRepository
import kotlinx.coroutines.launch

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    fun insertPost(post: Post) = viewModelScope.launch {
        repository.insertPost(post)
    }

    fun getAllPosts(): LiveData<List<Post>> {
        return repository.getAllPosts()
    }

    fun getPostsByUser(userId: String): LiveData<List<Post>> {
        return repository.getPostsByUser(userId)
    }

    fun getPostById(postId: Int): LiveData<Post> {
        return repository.getPostById(postId)
    }

    fun updatePostCaption(postId: Int, caption: String) = viewModelScope.launch {
        repository.updatePostCaption(postId, caption)
    }

    fun deletePost(postId: Int) = viewModelScope.launch {
        repository.deletePost(postId)
    }
}
