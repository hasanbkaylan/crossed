package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM scanned_photos ORDER BY dateTaken DESC")
    fun getAllPhotos(): Flow<List<ScannedPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<ScannedPhoto>)

    @Query("DELETE FROM scanned_photos")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM scanned_photos")
    fun getPhotoCount(): Flow<Int>

    @Query("SELECT locationHash FROM scanned_photos")
    suspend fun getAllHashes(): List<String>

    @Query("SELECT * FROM scanned_photos WHERE locationHash IN (:hashes)")
    suspend fun getPhotosByHashes(hashes: List<String>): List<ScannedPhoto>
}
