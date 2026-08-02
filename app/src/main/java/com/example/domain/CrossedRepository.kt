package com.example.domain

import com.example.data.PhotoDao
import com.example.data.ScannedPhoto
import kotlinx.coroutines.flow.Flow

class CrossedRepository(
    private val photoDao: PhotoDao,
    private val photoScanner: PhotoScanner
) {
    val totalPhotosCount: Flow<Int> = photoDao.getPhotoCount()
    val allPhotos: Flow<List<ScannedPhoto>> = photoDao.getAllPhotos()

    suspend fun scanAndSavePhotos(onProgress: suspend (Int, Int) -> Unit): Pair<Int, Int> {
        val (totalScanned, photos) = photoScanner.scanPhotos(onProgress)
        if (photos.isNotEmpty()) {
            photoDao.insertPhotos(photos)
        }
        return Pair(totalScanned, photos.size)
    }

    suspend fun getMyHashes(): List<String> {
        return photoDao.getAllHashes()
    }

    suspend fun getPhotosByHashes(hashes: List<String>): List<ScannedPhoto> {
        return photoDao.getPhotosByHashes(hashes)
    }

    suspend fun deleteAllData() {
        photoDao.deleteAll()
    }
}
