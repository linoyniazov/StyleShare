package com.example.styleshare.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.styleshare.model.dao.FollowDao
import com.example.styleshare.model.dao.PostDao
import com.example.styleshare.model.entities.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class HomeRepository(private val postDao: PostDao, private val followDao: FollowDao) {

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
    suspend fun checkDatabase() {
        val posts = postDao.getAllPostsSync() // ✅ קריאה תקינה בתוך suspend function
        Log.d("DatabaseCheck", "📌 מספר הפוסטים במסד הנתונים: ${posts.size}")
    }

}
