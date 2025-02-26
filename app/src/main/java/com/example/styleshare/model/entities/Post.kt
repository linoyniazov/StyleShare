package com.example.styleshare.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val postId: String,
    val userId: String,
    val imageUrl: String,
    val caption: String,
    val category: String,
    val timestamp: Long,
    val likes: Int,
    val commentsCount: Int
)
