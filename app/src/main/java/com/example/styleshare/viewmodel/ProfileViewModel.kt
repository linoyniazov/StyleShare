package com.example.styleshare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.styleshare.model.entities.Post

class ProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _followersCount = MutableLiveData<Int>()
    val followersCount: LiveData<Int> get() = _followersCount

    private val _followingCount = MutableLiveData<Int>()
    val followingCount: LiveData<Int> get() = _followingCount

    private val _profileImage = MutableLiveData<String>()
    val profileImage: LiveData<String> get() = _profileImage

    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> get() = _userPosts

    fun loadUserProfile(userId: String) {
        db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("ProfileViewModel", "❌ Error loading user profile", error)
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    _username.value = document.getString("username") ?: "Unknown"
                    _followersCount.value = document.getLong("followersCount")?.toInt() ?: 0
                    _followingCount.value = document.getLong("followingCount")?.toInt() ?: 0
                    _profileImage.value = document.getString("profileImageUrl") ?: ""
                }
            }
    }

    fun loadUserPosts(userId: String) {
        db.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProfileViewModel", "❌ Error loading posts", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _userPosts.value = snapshot.toObjects(Post::class.java)
                }
            }
    }
}
