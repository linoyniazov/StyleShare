package com.example.styleshare.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        Log.d(
            "AuthRepository",
            "👤 Checking current user: ${firebaseAuth.currentUser?.email ?: "No user logged in"}"
        )
        return firebaseAuth.currentUser
    }

    suspend fun registerUser(email: String, password: String): Result<FirebaseUser> {
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(Exception("Registration failed. Please check your email and try again."))
        }
    }


    //    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
//        Log.d("AuthRepository", "🔄 loginUser() called with email: $email")
//        return try {
//            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
//            Log.d("AuthRepository", "✅ User logged in: ${result.user?.email}")
//            Result.success(result.user!!)
//        } catch (e: Exception) {
//            Log.e("AuthRepository", "❌ Login failed: ${e.message}", e)
//            Result.failure(e)
//        }
//    }
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        Log.d("AuthRepository", "🔄 loginUser() called with email: $email")
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()

            // ✅ בדיקת reCAPTCHA לאחר התחברות
            result.user?.getIdToken(true)?.await()

            Log.d("AuthRepository", "✅ User logged in: ${result.user?.email}")
            Result.success(result.user!!)
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Login failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun logoutUser() {
        Log.d("AuthRepository", "🔴 Logging out user: ${firebaseAuth.currentUser?.email}")
        firebaseAuth.signOut()
    }
}