package com.example.styleshare.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.styleshare.model.entities.Comment

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp DESC")
    fun getCommentsForPost(postId: String): LiveData<List<Comment>>

    @Query("SELECT * FROM comments WHERE userId = :userId ORDER BY timestamp DESC")
    fun getCommentsByUser(userId: String): LiveData<List<Comment>>

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun getCommentCountForPost(postId: String): Int

    @Query("DELETE FROM comments WHERE commentId = :commentId")
    suspend fun deleteComment(commentId: Long)

    @Query("UPDATE comments SET content = :content, edited = true, editedTimestamp = :editedTimestamp WHERE commentId = :commentId")
    suspend fun updateComment(commentId: Long, content: String, editedTimestamp: Long = System.currentTimeMillis())
}