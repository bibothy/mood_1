package org.moodapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DesireDao {
    @Insert
    fun insertDesire(desire: DesireEntity)

    @Query("SELECT * FROM desires")
    fun getAllDesires(): List<DesireEntity>

    @Query("SELECT desire FROM desires GROUP BY desire ORDER BY COUNT(*) DESC LIMIT 1")
    fun getMostFrequentDesire(): String?
}