package com.example.styleshare.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.styleshare.model.entities.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE userId = :id")
    suspend fun getUser(id: String): User? // החזרת User במקום LiveData<User>

    @Query("SELECT COUNT(*) FROM users WHERE userId = :id")
    suspend fun doesUserExist(id: String): Int

    @Query("UPDATE users SET username = :username, fullName = :fullName, profileImageUrl = :profileImage, bio = :bio, followersCount = :followers, followingCount = :following WHERE userId = :id")
    suspend fun updateUser(
        id: String,
        username: String,
        fullName: String,
        profileImage: String,
        bio: String,
        followers: Int,
        following: Int
    )

    @Query("DELETE FROM users WHERE userId = :id")
    suspend fun deleteUser(id: String)

    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<User>> // החזרת כל המשתמשים
}

