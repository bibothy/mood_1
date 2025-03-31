package org.moodapp

import android.app.Application
import androidx.room.Room
import org.moodapp.database.AppDatabase

class MoodApp : Application() {
    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "moodapp_database"
        ).build()
    }
}