package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.domain.CrossedRepository
import com.example.domain.NearbyManager

class MainViewModelFactory(
    private val repository: CrossedRepository,
    private val nearbyManager: NearbyManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, nearbyManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
