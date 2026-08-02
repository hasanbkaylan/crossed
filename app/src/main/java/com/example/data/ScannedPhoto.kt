package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_photos")
data class ScannedPhoto(
    @PrimaryKey val id: Long, // MediaStore ID
    val dateTaken: Long,
    val latitude: Double,
    val longitude: Double,
    val locationHash: String, // Pre-computed hash of rounded location and time for secure matching
    val timestamp: Long = System.currentTimeMillis()
)
