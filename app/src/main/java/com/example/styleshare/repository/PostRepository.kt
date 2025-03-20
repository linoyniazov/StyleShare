package com.example.styleshare.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.styleshare.model.dao.PostDao
import com.example.styleshare.model.entities.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PostRepository(private val postDao: PostDao) {

    suspend fun insertPost(post: Post) {
        postDao.insertPost(post)
    }

    /**
     * Syncs posts from Firestore to local Room database.
     */
    suspend fun syncPostsFromFirestore() {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("posts")
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { doc ->
                try {
                    val map = doc.data ?: return@mapNotNull null
                    Post(
                        postId = map["postId"] as? String ?: "",
                        userId = map["userId"] as? String ?: "",
                        username = map["username"] as? String ?: "", // ✅ חדש
                        imageUrl = map["imageUrl"] as? String ?: "",
                        caption = map["caption"] as? String ?: "",
                        category = map["category"] as? String ?: "",
                        timestamp = (map["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time
                            ?: System.currentTimeMillis(),
                        items = map["items"] as? List<String> ?: emptyList()
                    )
                } catch (e: Exception) {
                    Log.e("PostRepository", "❌ Failed to parse post: ${e.message}")
                    null
                }
            }

            posts.forEach { post ->
                postDao.insertPost(post)
            }

            Log.d("PostRepository", "✅ Synced ${posts.size} posts to Room.")

        } catch (e: Exception) {
            Log.e("PostRepository", "❌ Firestore sync failed: ${e.message}", e)
        }
    }

    /**
     * Pulls all posts from Firestore (for displaying on Home screen).
     */
    fun getAllPostsFromFirestore(): LiveData<List<Post>> {
        val liveData = MutableLiveData<List<Post>>()

        FirebaseFirestore.getInstance()
            .collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PostRepository", "❌ Firestore error: ${error.message}", error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val map = doc.data ?: return@mapNotNull null
                        Post(
                            postId = map["postId"] as? String ?: "",
                            userId = map["userId"] as? String ?: "",
                            username = map["username"] as? String ?: "", // ✅ חדש
                            imageUrl = map["imageUrl"] as? String ?: "",
                            caption = map["caption"] as? String ?: "",
                            category = map["category"] as? String ?: "",
                            timestamp = (map["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time
                                ?: System.currentTimeMillis(),
                            items = map["items"] as? List<String> ?: emptyList()
                        )
                    } catch (e: Exception) {
                        Log.e("PostRepository", "❌ Parse error: ${e.message}")
                        null
                    }
                } ?: emptyList()

                liveData.value = posts
            }

        return liveData
    }

    // Room access
    fun getAllPosts(): LiveData<List<Post>> = postDao.getAllPosts()
    fun getPostsByUser(userId: String): LiveData<List<Post>> = postDao.getPostsByUser(userId)
    fun getPostById(postId: String): LiveData<Post> = postDao.getPostById(postId)

    suspend fun updatePost(
        postId: String,
        caption: String,
        category: String,
        editedTimestamp: Long = System.currentTimeMillis()
    ) {
        postDao.updatePost(postId, caption, category, editedTimestamp)
    }

    suspend fun deletePost(postId: String) {
        postDao.deletePost(postId)
    }

    fun getFollowingPosts(followedUserIds: List<String>): LiveData<List<Post>> {
        return postDao.getPostsByUsers(followedUserIds)
    }
}
