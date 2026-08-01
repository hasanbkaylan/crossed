package com.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.domain.CrossedRepository
import com.example.domain.PhotoScanner
import com.example.domain.SimulatedNearbyManager
import com.example.ui.AppNavigation
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val database = AppDatabase.getDatabase(this)
    val photoScanner = PhotoScanner(this)
    val repository = CrossedRepository(database.photoDao(), photoScanner)
    val nearbyManager = SimulatedNearbyManager()
    
    val factory = MainViewModelFactory(repository, nearbyManager)
    val viewModel: MainViewModel by viewModels { factory }
    
    setContent {
      MyApplicationTheme {
         AppNavigation(viewModel = viewModel)
      }
    }
  }
}

