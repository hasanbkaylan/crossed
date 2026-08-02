package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.domain.CrossedRepository
import com.example.domain.PhotoScanner
import com.example.domain.RealNearbyManager
import com.example.domain.SettingsManager
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val photoScanner by lazy { PhotoScanner(this) }
    private val repository by lazy { CrossedRepository(database.photoDao(), photoScanner) }
    private val nearbyManager by lazy { RealNearbyManager(this) }
    private val settingsManager by lazy { SettingsManager(this) }
    
    private val factory by lazy { MainViewModelFactory(repository, nearbyManager, settingsManager) }
    private val viewModel: MainViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}
