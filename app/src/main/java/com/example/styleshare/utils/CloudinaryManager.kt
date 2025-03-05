package com.example.styleshare.utils

import android.content.Context
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import java.util.*

class CloudinaryManager(context: Context) {

    private val cloudinary: Cloudinary

    init {
        val properties = Properties()
        val inputStream = context.assets.open("cloudinary.properties")
        properties.load(inputStream)

        val cloudinaryUrl = properties.getProperty("CLOUDINARY_URL")
        cloudinary = Cloudinary(cloudinaryUrl)
        Log.d("Cloudinary", "✅ Cloudinary initialized successfully")
    }

    // ✅ העלאת תמונה עם לוגים
    fun uploadImage(imagePath: String): String? {
        return try {
            Log.d("Cloudinary", "🌩 Uploading image: Path=$imagePath")

            val uploadResult = cloudinary.uploader().upload(imagePath, ObjectUtils.asMap(
                "folder", "StyleShare/Posts",
                "use_filename", true,
                "unique_filename", true,
                "overwrite", true
            ))

            val imageUrl = uploadResult["url"] as String
            val publicId = uploadResult["public_id"] as String

            Log.d("Cloudinary", "✅ Image uploaded successfully: URL=$imageUrl, Public ID=$publicId")

            imageUrl
        } catch (e: Exception) {
            Log.e("Cloudinary", "❌ Image upload failed: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // ✅ מחיקת תמונה עם לוגים
    fun deleteImage(publicId: String): Boolean {
        return try {
            Log.d("Cloudinary", "🗑 Deleting image: Public ID=$publicId")

            val result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())

            val status = result["result"] as String
            Log.d("Cloudinary", "🗑 Delete status: $status")

            status == "ok"
        } catch (e: Exception) {
            Log.e("Cloudinary", "❌ Image delete failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
//package com.example.styleshare.utils
//
//import android.content.Context
//import com.cloudinary.Cloudinary
//import com.cloudinary.utils.ObjectUtils
//import java.util.*
//
//class CloudinaryManager(context: Context) {
//
//    private val cloudinary: Cloudinary
//
//    init {
//        val properties = Properties()
//        val inputStream = context.assets.open("cloudinary.properties")
//        properties.load(inputStream)
//
//        val cloudinaryUrl = properties.getProperty("CLOUDINARY_URL")
//        cloudinary = Cloudinary(cloudinaryUrl)
//    }
//
//    // ✅ העלאת תמונה ושמירת ה-publicId
//    fun uploadImage(imagePath: String): String? {
//        return try {
//            val uploadResult = cloudinary.uploader().upload(imagePath, ObjectUtils.asMap(
//                "folder", "StyleShare/Posts",  // שמירה בתיקייה ספציפית
//                "use_filename", true,
//                "unique_filename", true,
//                "overwrite", true
//            ))
//
//            val imageUrl = uploadResult["url"] as String
//            val publicId = uploadResult["public_id"] as String // שמירת publicId למחיקה בעתיד
//
//            println("✅ תמונה הועלתה בהצלחה: $imageUrl")
//            println("📌 Cloudinary Public ID: $publicId")
//
//            imageUrl // מחזירים את ה-URL של התמונה
//        } catch (e: Exception) {
//            e.printStackTrace()
//            println("❌ שגיאה בהעלאת תמונה ל-Cloudinary: ${e.message}")
//            null
//        }
//    }
//
//    // ✅ מחיקת תמונה לפי ה-publicId
//    fun deleteImage(publicId: String): Boolean {
//        return try {
//            val result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
//            println("🗑 מחיקת תמונה - סטטוס: ${result["result"]}")
//            result["result"] == "ok"
//        } catch (e: Exception) {
//            e.printStackTrace()
//            println("❌ שגיאה במחיקת תמונה מ-Cloudinary: ${e.message}")
//            false
//        }
//    }
//}
//package com.example.styleshare.utils
//
//import android.content.Context
//import com.cloudinary.Cloudinary
//import com.cloudinary.utils.ObjectUtils
//import java.util.*
//
//class CloudinaryManager(context: Context) {
//
//    private val cloudinary: Cloudinary
//
//    init {
//        val properties = Properties()
//        val inputStream = context.assets.open("cloudinary.properties")
//        properties.load(inputStream)
//
//        val cloudinaryUrl = properties.getProperty("CLOUDINARY_URL")
//        cloudinary = Cloudinary(cloudinaryUrl)
//    }
//
//    fun uploadImage(imagePath: String): String? {
//        return try {
//            val uploadResult = cloudinary.uploader().upload(imagePath, ObjectUtils.emptyMap())
//            uploadResult["url"] as String
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    fun deleteImage(publicId: String): Boolean {
//        return try {
//            val result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
//            result["result"] == "ok"
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//    }
//}