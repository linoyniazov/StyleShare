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
    suspend fun getUser(id: String): User?

    @Query("SELECT COUNT(*) FROM users WHERE userId = :id")
    suspend fun doesUserExist(id: String): Int

    @Query("""
        UPDATE users 
        SET username = :username,
            email = :email,
            fullName = :fullName,
            profileImageUrl = :profileImageUrl,
            profileImageCloudinaryId = :profileImageCloudinaryId,
            bio = :bio,
            followersCount = :followersCount,
            followingCount = :followingCount,
            postsCount = :postsCount
        WHERE userId = :id
    """)
    suspend fun updateUser(
        id: String,
        username: String,
        email: String,
        fullName: String,
        profileImageUrl: String,
        profileImageCloudinaryId: String,
        bio: String,
        followersCount: Int,
        followingCount: Int,
        postsCount: Int
    )

    @Query("UPDATE users SET followersCount = followersCount + :increment WHERE userId = :userId")
    suspend fun updateFollowersCount(userId: String, increment: Int)

    @Query("UPDATE users SET followingCount = followingCount + :increment WHERE userId = :userId")
    suspend fun updateFollowingCount(userId: String, increment: Int)

    @Query("UPDATE users SET postsCount = postsCount + :increment WHERE userId = :userId")
    suspend fun updatePostsCount(userId: String, increment: Int)

    @Query("DELETE FROM users WHERE userId = :id")
    suspend fun deleteUser(id: String)

    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<User>>
}