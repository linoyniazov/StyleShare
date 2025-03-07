package com.example.styleshare.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.model.entities.Comment
import com.example.styleshare.repository.CommentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentViewModel(private val repository: CommentRepository) : ViewModel() {

    fun insertComment(comment: Comment, callback: (Long) -> Unit = {}) = viewModelScope.launch {
        val commentId = withContext(Dispatchers.IO) {
            repository.insertComment(comment)
        }
        callback(commentId)
    }

    fun getCommentsForPost(postId: String): LiveData<List<Comment>> {
        return repository.getCommentsForPost(postId)
    }

    fun getCommentsByUser(userId: String): LiveData<List<Comment>> {
        return repository.getCommentsByUser(userId)
    }

    suspend fun getCommentCountForPost(postId: String): Int = withContext(Dispatchers.IO) {
        return@withContext repository.getCommentCountForPost(postId)
    }

    fun deleteComment(commentId: Long) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteComment(commentId)
    }

    fun updateComment(
        commentId: Long,
        content: String,
        editedTimestamp: Long = System.currentTimeMillis()
    ) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateComment(commentId, content, editedTimestamp)
    }

    class CommentViewModelFactory(private val repository: CommentRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CommentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CommentViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

fun createCommentViewModel(application: Application): CommentViewModel {
    val repository = CommentRepository(AppDatabase.getDatabase(application).commentDao())
    return CommentViewModel(repository)
}