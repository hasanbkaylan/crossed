package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                LicenseItem("Android Jetpack", "Apache License 2.0")
                LicenseItem("Kotlin", "Apache License 2.0")
                LicenseItem("Material Components", "Apache License 2.0")
                LicenseItem("Coroutines", "Apache License 2.0")
                LicenseItem("Room Database", "Apache License 2.0")
            }
        }
    }
}

@Composable
fun LicenseItem(name: String, license: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(name, style = MaterialTheme.typography.titleMedium)
        Text(license, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider()
}
