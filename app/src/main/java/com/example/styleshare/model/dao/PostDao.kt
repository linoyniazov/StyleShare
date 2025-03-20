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
    fun getPostById(postId: String): LiveData<Post>

    @Query("""
        UPDATE posts
        SET caption = :caption,
            edited = true,
            editedTimestamp = :editedTimestamp,
            category = :category
        WHERE postId = :postId
    """)
    suspend fun updatePost(
        postId: String,
        caption: String,
        category: String,
        editedTimestamp: Long = System.currentTimeMillis()
    )


    @Query("DELETE FROM posts WHERE postId = :postId")
    suspend fun deletePost(postId: String)

    @Query("SELECT * FROM posts WHERE userId IN (:userIds)")
    fun getPostsByUsers(userIds: List<String>): LiveData<List<Post>>


    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    suspend fun getAllPostsSync(): List<Post>




}