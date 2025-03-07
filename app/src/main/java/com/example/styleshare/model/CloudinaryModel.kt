package com.example.styleshare.model

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object CloudinaryModel {

    private const val CLOUD_NAME = "drwkwfox9"
    private const val API_KEY = "484696912493753"
    private const val API_SECRET = "3qLlq1fWFewCP9T-ky2IC3Sev9A"

    private val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", CLOUD_NAME,
            "api_key", API_KEY,
            "api_secret", API_SECRET
        )
    )

    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to CLOUD_NAME,
            "api_key" to API_KEY,
            "api_secret" to API_SECRET
        )
        try {
            MediaManager.get()
        } catch (e: Exception) {
            MediaManager.init(context, config)
        }
    }
    /** ✅ העלאת תמונה מ- Uri עם תיקייה מותאמת **/
    suspend fun uploadImageFromUri(context: Context, imageUri: Uri, folder: String = "uploads"): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = uriToFile(context, imageUri)
                val options = mapOf(
                    "folder" to folder // ✅ שמירת התמונה בתיקייה מותאמת אישית
                )
                val result = cloudinary.uploader().upload(file, options)
                result["secure_url"] as? String
            } catch (e: Exception) {
                Log.e("CloudinaryManager", "Error uploading image", e)
                null
            }
        }
    }

    /** ✅ העלאת תמונה מ- Bitmap עם שמירת תיקייה **/
    fun uploadImageFromBitmap(
        bitmap: Bitmap, context: Context, folder: String = "uploads",
        onSuccess: (String) -> Unit, onError: (String) -> Unit
    ) {
        val file = bitmapToFile(bitmap, context)

        MediaManager.get().upload(file.path)
            .option("folder", folder)  // ✅ שמירת תמונה בתיקייה "uploads"
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val publicUrl = resultData["secure_url"] as? String ?: ""
                    onSuccess(publicUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError(error?.description ?: "Unknown error")
                }

                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    /** ✅ המרת Uri לקובץ זמני **/
    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("upload", ".jpg", context.cacheDir)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    /** ✅ המרת Bitmap לקובץ זמני **/
    private fun bitmapToFile(bitmap: Bitmap, context: Context): File {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return file
    }
}