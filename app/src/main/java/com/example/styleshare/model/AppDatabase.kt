package com.example.styleshare.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.styleshare.utils.Converters
import com.example.styleshare.model.dao.CommentDao
import com.example.styleshare.model.dao.FollowDao
import com.example.styleshare.model.dao.PostDao
import com.example.styleshare.model.dao.UserDao
import com.example.styleshare.model.entities.Comment
import com.example.styleshare.model.entities.Follow
import com.example.styleshare.model.entities.Post
import com.example.styleshare.model.entities.User

@Database(
    entities = [User::class, Post::class, Comment::class, Follow::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun followDao(): FollowDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS follows (" +
                            "userId TEXT NOT NULL, " +
                            "followingId TEXT NOT NULL, " +
                            "PRIMARY KEY(userId, followingId))"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "styleshare_database"
                )
                    .addMigrations(MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}