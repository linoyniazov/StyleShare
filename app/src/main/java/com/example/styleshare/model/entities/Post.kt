package com.example.styleshare.model.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.styleshare.utils.Converters
import kotlinx.parcelize.Parcelize

@Parcelize
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
    val username: String = "",
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
) : Parcelable