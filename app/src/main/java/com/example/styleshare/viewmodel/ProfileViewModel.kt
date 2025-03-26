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

    private val _profileImage = MutableLiveData<String>()
    val profileImage: LiveData<String> get() = _profileImage

    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> get() = _userPosts

    private val _bio = MutableLiveData<String>()
    val bio: LiveData<String> get() = _bio


    fun loadUserProfile(userId: String) {
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    _username.value = document.getString("username") ?: ""
                    _profileImage.value = document.getString("profileImageUrl") ?: ""
                    _bio.value = document.getString("bio") ?: "" // ✅ הוספת טעינת הביו
                }
            }
            .addOnFailureListener { exception -> // ✅ שינוי `it` לשם מפורש
                Log.e("ProfileViewModel", "Failed to load user profile", exception)
            }
    }
    fun deletePost(postId: String) {
        val currentPosts = _userPosts.value?.toMutableList() ?: return
        val updatedPosts = currentPosts.filter { it.postId != postId }
        _userPosts.value = updatedPosts
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
