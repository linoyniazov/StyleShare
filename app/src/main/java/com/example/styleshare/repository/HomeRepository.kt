package com.example.styleshare.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.styleshare.model.dao.PostDao
import com.example.styleshare.model.entities.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class HomeRepository(private val postDao: PostDao) {

    fun getAllPosts(): LiveData<List<Post>> = postDao.getAllPosts()

    suspend fun checkDatabase() {
        val posts = postDao.getAllPostsSync() // ✅ קריאה תקינה בתוך suspend function
        Log.d("DatabaseCheck", "📌 מספר הפוסטים במסד הנתונים: ${posts.size}")
    }

}
