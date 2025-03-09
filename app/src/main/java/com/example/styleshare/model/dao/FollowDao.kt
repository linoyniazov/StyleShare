package com.example.styleshare.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.styleshare.model.entities.Follow

@Dao
interface FollowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followUser(follow: Follow)

    @Query("DELETE FROM follows WHERE userId = :userId AND followingId = :followingId")
    suspend fun unfollowUser(userId: String, followingId: String)

    @Query("SELECT followingId FROM follows WHERE userId = :userId")
    suspend fun getFollowingUserIds(userId: String): List<String>
}
