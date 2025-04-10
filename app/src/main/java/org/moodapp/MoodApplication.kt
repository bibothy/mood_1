package org.moodapp

import android.app.Application
import org.moodapp.database.AppDatabase

class MoodApplication : Application() {

    // Ленивая инициализация базы данных (создается при первом обращении)
    // Убедитесь, что путь org.moodapp.database.AppDatabase правильный
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }


    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            CrashHandler.handleCrash(applicationContext, throwable)
        }
    }
}