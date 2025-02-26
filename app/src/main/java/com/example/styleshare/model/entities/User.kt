package com.example.styleshare.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val username: String,
    val fullName: String,
    val profileImageUrl: String,
    val bio: String,
    val followersCount: Int,
    val followingCount: Int
)
