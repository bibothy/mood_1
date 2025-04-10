package org.moodapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "desires")
data class DesireEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val desire: String,
    val timestamp: Long
)