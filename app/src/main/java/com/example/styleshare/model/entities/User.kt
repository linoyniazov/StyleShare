// User.kt
package com.example.styleshare.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val username: String,
    val email: String,
    val fullName: String,
    val profileImageUrl: String,
    val bio: String,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0
)
