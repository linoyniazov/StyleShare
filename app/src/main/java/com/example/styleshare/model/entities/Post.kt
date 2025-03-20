package com.example.styleshare.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.styleshare.utils.Converters

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
    indices = [Index("userId")]
)
@TypeConverters(Converters::class)
data class Post(
    @PrimaryKey val postId: String = "",
    val userId: String = "",
    val username: String = "", // ✅ חדש
    val imageUrl: String = "",
    val caption: String = "",
    val category: String = "",
    val timestamp: Long = 0L,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val edited: Boolean = false,
    val editedTimestamp: Long? = null,
    val isDraft: Boolean = false,
    val items: List<String> = emptyList()
) {
    constructor() : this(
        postId = "",
        userId = "",
        username = "", // ✅ גם כאן
        imageUrl = "",
        caption = "",
        category = "",
        timestamp = 0L,
        imageWidth = null,
        imageHeight = null,
        edited = false,
        editedTimestamp = null,
        isDraft = false,
        items = emptyList()
    )
}


