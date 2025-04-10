package org.moodapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SillyDao {
    @Insert
    fun insertSilly(silly: SillyEntity)

    @Query("SELECT * FROM sillies")
    fun getAllSillies(): List<SillyEntity>

    @Query("SELECT silly FROM sillies GROUP BY silly ORDER BY COUNT(*) DESC LIMIT 1")
    fun getMostFrequentSilly(): String?
}