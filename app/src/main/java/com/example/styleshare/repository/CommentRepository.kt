package com.example.styleshare.repository

import androidx.lifecycle.LiveData
import com.example.styleshare.model.dao.CommentDao
import com.example.styleshare.model.entities.Comment

class CommentRepository(private val commentDao: CommentDao) {

    suspend fun insertComment(comment: Comment): Long {
        return commentDao.insertComment(comment)
    }

    fun getCommentsForPost(postId: String): LiveData<List<Comment>> {
        return commentDao.getCommentsForPost(postId)
    }

    fun getCommentsByUser(userId: String): LiveData<List<Comment>> {
        return commentDao.getCommentsByUser(userId)
    }

    suspend fun getCommentCountForPost(postId: String): Int {
        return commentDao.getCommentCountForPost(postId)
    }

    suspend fun deleteComment(commentId: Long) {
        commentDao.deleteComment(commentId)
    }

    suspend fun updateComment(commentId: Long, content: String, editedTimestamp: Long = System.currentTimeMillis()) {
        commentDao.updateComment(commentId, content, editedTimestamp)
    }
}