package com.example.styleshare.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.styleshare.model.converters.Converters
import com.example.styleshare.model.dao.CommentDao
import com.example.styleshare.model.dao.PostDao
import com.example.styleshare.model.dao.UserDao
import com.example.styleshare.model.entities.Comment
import com.example.styleshare.model.entities.Post
import com.example.styleshare.model.entities.User

@Database(
    entities = [User::class, Post::class, Comment::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "styleshare_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}