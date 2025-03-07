// Post.kt
package com.example.styleshare.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId")
    ]
)
data class Post(
    @PrimaryKey val postId: String,
    val userId: String,
    val imageUrl: String,
    val caption: String,
    val category: String,
    val timestamp: Long,
    val likes: Int = 0,
    val commentsCount: Int = 0,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val edited: Boolean = false,
    val editedTimestamp: Long? = null,
    val items: List<String> = listOf()
    )
