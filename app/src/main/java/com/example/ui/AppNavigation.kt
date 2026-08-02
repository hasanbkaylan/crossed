package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        composable("nearby") {
            NearbyScreen(viewModel = viewModel, navController = navController)
        }
        composable("privacy") {
            PrivacyScreen(navController = navController)
        }
        composable("about") {
            AboutScreen(navController = navController)
        }
        composable("licenses") {
            LicensesScreen(navController = navController)
        }
        composable("settings") {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }
    }
}
