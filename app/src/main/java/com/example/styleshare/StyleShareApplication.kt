package com.example.styleshare

import android.app.Application
import com.example.styleshare.model.AppDatabase

class StyleShareApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: StyleShareApplication
            private set
    }
}