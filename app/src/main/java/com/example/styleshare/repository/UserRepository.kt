package com.example.styleshare.repository

import androidx.lifecycle.LiveData
import com.example.styleshare.model.dao.UserDao
import com.example.styleshare.model.entities.User

class UserRepository(private val userDao: UserDao) {

    // שליפת משתמש לפי ID
    suspend fun getUserById(userId: String): User? {
        return userDao.getUser(userId)
    }

    // הכנסת משתמש חדש למסד הנתונים
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    // בדיקה אם המשתמש קיים במסד הנתונים
    suspend fun doesUserExist(userId: String): Boolean {
        return userDao.doesUserExist(userId) > 0
    }

    // עדכון פרטי משתמש
    suspend fun updateUser(
        userId: String,
        username: String,
        fullName: String,
        profileImageUrl: String,
        bio: String,
        followers: Int,
        following: Int
    ) {
        userDao.updateUser(userId, username, fullName, profileImageUrl, bio, followers, following)
    }

    // מחיקת משתמש
    suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    // שליפת כל המשתמשים
    fun getAllUsers(): LiveData<List<User>> {
        return userDao.getAllUsers()
    }
}
