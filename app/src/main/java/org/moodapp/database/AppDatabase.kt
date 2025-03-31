package org.moodapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ИСПРАВЛЕНО: Указана версия 1, entities = [MessageEntity::class], exportSchema = false (для простоты)
@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // ИСПРАВЛЕНО: Абстрактный метод для получения DAO
    abstract fun messageDao(): MessageDao

    // ИСПРАВЛЕНО: Реализация Companion object для синглтона
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mood_database" // Имя файла базы данных
                )
                    // ИСПОЛЬЗУЕМ fallbackToDestructiveMigration для простоты при обновлении версии.
                    // ВНИМАНИЕ: Это удалит старую базу данных при несовпадении версии!
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance // Возвращаем экземпляр
            }
        }
    }
}