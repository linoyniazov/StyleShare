package com.example.styleshare.model

import android.content.Context
import androidx.room.*
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
    version = 7, // ✅ עדכון מספר גרסה
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

        // ✅ מיגרציה מ-5 ל-6 - יצירת טבלת `follows` אם היא לא קיימת
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

        // ✅ מיגרציה מ-6 ל-7 - הוספת FOREIGN KEYS לטבלת `follows`
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `follows_new` (
                        `userId` TEXT NOT NULL, 
                        `followingId` TEXT NOT NULL, 
                        PRIMARY KEY(`userId`, `followingId`),
                        FOREIGN KEY(`userId`) REFERENCES `users`(`userId`) ON DELETE CASCADE,
                        FOREIGN KEY(`followingId`) REFERENCES `users`(`userId`) ON DELETE CASCADE
                    )
                """.trimIndent())

                // ✅ מעתיקים נתונים מהטבלה הישנה לחדשה
                database.execSQL("INSERT INTO follows_new (userId, followingId) SELECT userId, followingId FROM follows")

                // ✅ מוחקים את הטבלה הישנה
                database.execSQL("DROP TABLE follows")

                // ✅ משנים את שם הטבלה החדשה לטבלת follows
                database.execSQL("ALTER TABLE follows_new RENAME TO follows")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "styleshare_database"
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7) // ✅ הוספת שתי המיגרציות
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
