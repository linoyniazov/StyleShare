package com.example.styleshare.model

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.styleshare.utils.Converters
import com.example.styleshare.model.dao.PostDao
import com.example.styleshare.model.dao.UserDao
import com.example.styleshare.model.entities.Post
import com.example.styleshare.model.entities.User

@Database(
    entities = [User::class, Post::class],
    version = 2, // ✅ עדכון מספר גרסה
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao

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
                    .fallbackToDestructiveMigration() // ✅ פעולת עדכון גרסה קיצונית
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
