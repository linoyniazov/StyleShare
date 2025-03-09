package com.example.styleshare.repository

import androidx.lifecycle.LiveData
import com.example.styleshare.model.dao.FollowDao
import com.example.styleshare.model.dao.PostDao
import com.example.styleshare.model.entities.Post
import javax.inject.Inject

class HomeRepository @Inject constructor(private val postDao: PostDao, private val followDao: FollowDao) {

    fun getAllPosts(): LiveData<List<Post>> = postDao.getAllPosts()

    fun getFollowingPosts(followedUserIds: List<String>): LiveData<List<Post>> {
        return postDao.getPostsByUsers(followedUserIds)
    }

    fun getForYouPosts(): LiveData<List<Post>> {
        return postDao.getAllPosts() // ניתן לשפר בעתיד
    }

    // פונקציה חדשה - מחזירה רשימה של משתמשים שהמשתמש עוקב אחריהם
    suspend fun getFollowingUserIds(userId: String): List<String> {
        return followDao.getFollowingUserIds(userId) // ✅ תיקון
    }
    fun getPopularPosts(): LiveData<List<Post>> {
        return postDao.getPopularPosts()
    }


}
