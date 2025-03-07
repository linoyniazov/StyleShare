// Comment.kt
package com.example.styleshare.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = ["postId"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("postId"),
        Index("userId")
    ]
)
data class Comment(
    @PrimaryKey(autoGenerate = true) val commentId: Long = 0,
    val postId: String,
    val userId: String,
    val content: String,
    val timestamp: Long,
    val edited: Boolean = false,
    val editedTimestamp: Long? = null
)
