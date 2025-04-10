package org.moodapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sillies")
data class SillyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val silly: String,
    val timestamp: Long
)