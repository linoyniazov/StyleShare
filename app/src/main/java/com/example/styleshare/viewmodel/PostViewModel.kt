
package com.example.styleshare.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.model.entities.Post
import com.example.styleshare.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PostViewModel(private val repository: PostRepository, context: Context) : ViewModel() {

    val allPosts: LiveData<List<Post>> = repository.getAllPosts()

    private val _uploadResult = MutableLiveData<String?>()
    val uploadResult: LiveData<String?> get() = _uploadResult

    fun insertPost(post: Post) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertPost(post)
    }

    fun getPostsByUser(userId: String): LiveData<List<Post>> {
        return repository.getPostsByUser(userId)
    }

    fun getPostById(postId: String): LiveData<Post> {
        return repository.getPostById(postId)
    }

    fun updatePost(
        postId: String,
        caption: String,
        category: String,
        editedTimestamp: Long = System.currentTimeMillis()
    ) = viewModelScope.launch(Dispatchers.IO) {
        repository.updatePost(postId, caption, category, editedTimestamp)
    }

    fun updateLikes(postId: String, increment: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateLikes(postId, increment)
    }

    fun updateCommentsCount(postId: String, increment: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCommentsCount(postId, increment)
    }

    fun deletePost(postId: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deletePost(postId)
    }

    fun deletePostWithImage(postId: String, cloudinaryPublicId: String) = viewModelScope.launch(Dispatchers.IO) {

        repository.deletePost(postId)
    }


    class PostViewModelFactory(private val repository: PostRepository, private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PostViewModel(repository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

fun createPostViewModel(application: Application): PostViewModel {
    val database = AppDatabase.getDatabase(application)
    val repository = PostRepository(database.postDao()) // ✅ Correct
    return PostViewModel(repository, application.applicationContext)
}