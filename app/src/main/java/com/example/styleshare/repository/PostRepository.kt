
package com.example.styleshare.repository

import androidx.lifecycle.LiveData
import com.example.styleshare.model.dao.PostDao
import com.example.styleshare.model.entities.Post

class PostRepository(private val postDao: PostDao) {

    suspend fun insertPost(post: Post) {
        postDao.insertPost(post)
    }

    fun getAllPosts(): LiveData<List<Post>> {
        return postDao.getAllPosts()
    }

    fun getPostsByUser(userId: String): LiveData<List<Post>> {
        return postDao.getPostsByUser(userId)
    }

    fun getPostById(postId: String): LiveData<Post> {
        return postDao.getPostById(postId)
    }

    suspend fun updatePost(
        postId: String,
        caption: String,
        category: String,
        editedTimestamp: Long = System.currentTimeMillis()
    ) {
        postDao.updatePost(postId, caption, category, editedTimestamp)
    }

    suspend fun updateLikes(postId: String, increment: Int) {
        postDao.updateLikes(postId, increment)
    }

    suspend fun updateCommentsCount(postId: String, increment: Int) {
        postDao.updateCommentsCount(postId, increment)
    }

    suspend fun deletePost(postId: String) {
        postDao.deletePost(postId)
    }
    fun getFollowingPosts(followedUserIds: List<String>): LiveData<List<Post>> {
        return postDao.getPostsByUsers(followedUserIds)
    }
    fun searchPosts(query: String): LiveData<List<Post>> {
        return postDao.searchPosts(query)
    }


}