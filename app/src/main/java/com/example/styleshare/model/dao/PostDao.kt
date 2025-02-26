package com.example.styleshare.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.styleshare.model.entities.Post


@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPostsByUser(userId: String): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE postId = :postId")
    fun getPostById(postId: Int): LiveData<Post>  // Changed String to Int

    @Query("UPDATE posts SET caption = :caption WHERE postId = :postId")
    suspend fun updatePostCaption(postId: Int, caption: String) // Renamed 'content' to 'caption'

    @Query("DELETE FROM posts WHERE postId = :postId")
    suspend fun deletePost(postId: Int) // Changed String to Int
}
