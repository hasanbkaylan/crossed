package com.example.domain

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.example.data.ScannedPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoScanner(private val context: Context) {
    suspend fun scanPhotos(existingIds: Set<Long>, onProgress: suspend (Int, Int) -> Unit): Pair<Int, List<ScannedPhoto>> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<ScannedPhoto>()
        var newPhotosFound = 0
        var processedCount = 0

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val totalCount = cursor.count

                while (cursor.moveToNext()) {
                    processedCount++
                    val id = cursor.getLong(idColumn)
                    
                    if (existingIds.contains(id)) {
                        if (processedCount % 1000 == 0 || processedCount == totalCount) {
                            onProgress(processedCount, totalCount)
                        }
                        continue
                    }
                    
                    newPhotosFound++
                    var dateTaken = cursor.getLong(dateColumn)
                    
                    val contentUri = Uri.withAppendedPath(collection, id.toString())
                    
                    var lat: Double? = null
                    var lon: Double? = null
                    
                    try {
                        val photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            MediaStore.setRequireOriginal(contentUri)
                        } else {
                            contentUri
                        }
                        
                        context.contentResolver.openInputStream(photoUri)?.use { inputStream ->
                            val exif = ExifInterface(inputStream)
                            val latLong = exif.latLong
                            if (latLong != null) {
                                lat = latLong[0]
                                lon = latLong[1]
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PhotoScanner", "Error reading EXIF for $contentUri: ${e.message}")
                    }

                    if (lat != null && lon != null) {
                        val safeLat: Double = lat!!
                        val safeLon: Double = lon!!
                        if (safeLat != 0.0 || safeLon != 0.0) {
                            if (dateTaken > 0) {
                                photos.add(ScannedPhoto(id, dateTaken, safeLat, safeLon, ""))
                            }
                        }
                    }
                    
                    if (processedCount % 10 == 0 || processedCount == totalCount) {
                        onProgress(processedCount, totalCount)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PhotoScanner", "Error scanning photos: ${e.message}")
            throw e
        }

        Pair(newPhotosFound, photos)
    }
}
