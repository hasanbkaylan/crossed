package com.example.domain

import com.example.data.PhotoDao
import com.example.data.ScannedPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CrossedRepository(
    private val photoDao: PhotoDao,
    private val photoScanner: PhotoScanner
) {
    val totalPhotosCount: Flow<Int> = photoDao.getPhotoCount()
    val allPhotos: Flow<List<ScannedPhoto>> = photoDao.getAllPhotos()

    suspend fun scanAndSavePhotos(onProgress: suspend (Int, Int) -> Unit): Pair<Int, Int> {
        val existingIds = photoDao.getAllPhotoIds().toSet()
        val (totalScanned, photos) = photoScanner.scanPhotos(existingIds, onProgress)
        if (photos.isNotEmpty()) {
            photoDao.insertPhotos(photos)
        }
        val finalCount = photoDao.getPhotoCount().first()
        return Pair(totalScanned, finalCount)
    }

    suspend fun getMyHashes(radiusMeters: Int): List<String> {
        val allPhotos = photoDao.getAllPhotos().first()
        return allPhotos.map { photo ->
            HashUtil.createLocationHash(photo.latitude, photo.longitude, photo.dateTaken, radiusMeters)
        }.distinct()
    }
    
    suspend fun getPhotosByHashes(hashes: List<String>, radiusMeters: Int): List<ScannedPhoto> {
        val allPhotos = photoDao.getAllPhotos().first()
        
        // Match photos by calculating their dynamic hash and checking if it's in the requested hashes
        // We only want 1 photo per hash to avoid duplicates on the UI side.
        val hashSet = hashes.toSet()
        val result = mutableMapOf<String, ScannedPhoto>()
        
        for (photo in allPhotos) {
            val h = HashUtil.createLocationHash(photo.latitude, photo.longitude, photo.dateTaken, radiusMeters)
            if (hashSet.contains(h)) {
                if (!result.containsKey(h)) {
                    result[h] = photo
                }
            }
        }
        
        return result.values.toList()
    }

    suspend fun deleteAllData() {
        photoDao.deleteAll()
    }
}
