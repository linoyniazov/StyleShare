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

    fun getPostById(postId: Int): LiveData<Post> {
        return postDao.getPostById(postId)
    }

    suspend fun updatePostCaption(postId: Int, caption: String) {
        postDao.updatePostCaption(postId, caption)
    }

    suspend fun deletePost(postId: Int) {
        postDao.deletePost(postId)
    }
}
