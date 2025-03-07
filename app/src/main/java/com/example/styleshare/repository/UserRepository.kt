package com.example.styleshare.repository

import androidx.lifecycle.LiveData
import com.example.styleshare.model.dao.UserDao
import com.example.styleshare.model.entities.User

class UserRepository(private val userDao: UserDao) {

    suspend fun getUserById(userId: String): User? {
        return userDao.getUser(userId)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun doesUserExist(userId: String): Boolean {
        return userDao.doesUserExist(userId) > 0
    }

    suspend fun updateUser(
        id: String,
        username: String,
        email: String,
        fullName: String,
        profileImageUrl: String,
        bio: String,
        followersCount: String,
        followingCount: String,
        postsCount: Int
    ) {
        userDao.updateUser(
            id,
            username,
            email,
            fullName,
            profileImageUrl,
            bio,
            followersCount,
            followingCount,
            postsCount,
        )
    }

    suspend fun updateFollowersCount(userId: String, increment: Int) {
        userDao.updateFollowersCount(userId, increment)
    }

    suspend fun updateFollowingCount(userId: String, increment: Int) {
        userDao.updateFollowingCount(userId, increment)
    }

    suspend fun updatePostsCount(userId: String, increment: Int) {
        userDao.updatePostsCount(userId, increment)
    }

    suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    fun getAllUsers(): LiveData<List<User>> {
        return userDao.getAllUsers()
    }
}