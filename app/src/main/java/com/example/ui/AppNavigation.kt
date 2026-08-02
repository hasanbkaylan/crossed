package com.example.ui

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    
    val hasSeenOnboarding by viewModel.settingsManager.hasSeenOnboardingFlow.collectAsState(initial = null)
    
    if (hasSeenOnboarding == null) {
        // Loading state, just wait
        return
    }
    
    val startDest = if (hasSeenOnboarding == true) "home" else "onboarding"

    NavHost(navController = navController, startDestination = startDest) {
        composable("onboarding") {
            OnboardingScreen(viewModel = viewModel, onFinish = {
                navController.navigate("home") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }
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
