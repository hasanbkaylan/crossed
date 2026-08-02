package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ScannedPhoto
import com.example.domain.CrossedRepository
import com.example.domain.MatchStatus
import com.example.domain.NearbyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.R

class MainViewModel(
    private val repository: CrossedRepository,
    private val nearbyManager: NearbyManager
) : ViewModel() {

    val scannedPhotoCount: StateFlow<Int> = repository.totalPhotosCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    private val _lastScanResult = MutableStateFlow<UiText?>(null)
    val lastScanResult = _lastScanResult.asStateFlow()

    val matchStatus = nearbyManager.matchStatus

    private val _matchedPhotos = MutableStateFlow<List<ScannedPhoto>>(emptyList())
    val matchedPhotos: StateFlow<List<ScannedPhoto>> = _matchedPhotos.asStateFlow()

    private val _scanProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val scanProgress: StateFlow<Pair<Int, Int>?> = _scanProgress.asStateFlow()

    fun scanPhotos() {
        if (_isScanning.value) return
        _isScanning.value = true
        _lastScanResult.value = null
        _scanProgress.value = null
        
        viewModelScope.launch {
            try {
                val (total, locationCount) = repository.scanAndSavePhotos { current, max ->
                    _scanProgress.value = Pair(current, max)
                }
                _lastScanResult.value = UiText.StringResource(R.string.home_scanned_result, total, locationCount)
            } catch (e: Exception) {
                _lastScanResult.value = UiText.StringResource(R.string.home_scan_error, e.message ?: "Unknown Error")
            } finally {
                _isScanning.value = false
                _scanProgress.value = null
            }
        }
    }

    fun startNearbyDiscovery() {
        viewModelScope.launch {
            nearbyManager.startDiscovery()
        }
    }

    fun stopNearbyDiscovery() {
        nearbyManager.stopDiscovery()
    }

    fun approveMatch() {
        viewModelScope.launch {
            val hashes = repository.getMyHashes()
            nearbyManager.approveMatch(hashes)
        }
    }

    fun rejectMatch() {
        nearbyManager.rejectMatch()
    }
    
    fun processMatches(hashes: List<String>) {
        viewModelScope.launch {
            val photos = repository.getPhotosByHashes(hashes)
            _matchedPhotos.value = photos
        }
    }
    
    fun clearMatches() {
        _matchedPhotos.value = emptyList()
        stopNearbyDiscovery()
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
            _lastScanResult.value = UiText.StringResource(R.string.settings_deleted_result)
        }
    }
}
