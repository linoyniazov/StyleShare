package com.example.styleshare.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "follows",
    primaryKeys = ["userId", "followingId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["followingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Follow(
    val userId: String,        // המשתמש שעוקב
    val followingId: String    // המשתמש שאחריו הוא עוקב
)
