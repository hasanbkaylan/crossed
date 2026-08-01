package com.example.domain

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.data.ScannedPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoScanner(private val context: Context) {

    suspend fun scanPhotos(): List<ScannedPhoto> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<ScannedPhoto>()
        
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            // We'll read latitude and longitude if available. 
            // Note: In newer Androids, this might require ACCESS_MEDIA_LOCATION and MediaStore.setRequireOriginal()
            // but we'll read standard columns to be safe.
        )
        // Only get photos that have lat/lon columns (though on modern Androids they are often redacted without permission)
        // We will just read all and filter manually for this implementation.

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        try {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                
                // Location columns might not exist or be redacted, so we check safely.
                val latColumn = cursor.getColumnIndex("latitude")
                val lonColumn = cursor.getColumnIndex("longitude")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateColumn)
                    
                    var lat: Double? = null
                    var lon: Double? = null
                    
                    if (latColumn != -1 && lonColumn != -1) {
                        lat = cursor.getDouble(latColumn)
                        lon = cursor.getDouble(lonColumn)
                    }

                    // If lat/lon are 0.0 or null, it's useless for proximity.
                    // For safety in this test, if there are no coordinates, we'll skip them.
                    if (lat != null && lon != null && (lat != 0.0 || lon != 0.0)) {
                        val hash = HashUtil.createLocationHash(lat, lon, dateTaken)
                        photos.add(ScannedPhoto(id, dateTaken, lat, lon, hash))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PhotoScanner", "Error scanning photos: ${e.message}")
        }
        
        photos
    }
}
