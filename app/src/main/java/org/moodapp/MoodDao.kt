package org.moodapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MoodDao {
    @Insert
    fun insertMood(mood: MoodEntity)

    @Query("SELECT * FROM moods")
    fun getAllMoods(): List<MoodEntity>
    
    @Query("SELECT mood FROM moods GROUP BY mood ORDER BY COUNT(*) DESC LIMIT 1")
    fun getMostFrequentMood(): String?
}