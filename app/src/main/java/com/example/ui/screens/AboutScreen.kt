package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.about_version, "1.0.0"), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.about_developer), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(stringResource(R.string.about_foss_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.about_foss_desc),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(onClick = { 
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hasanbkaylan/crossed"))
                context.startActivity(intent)
            }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_btn_source))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { 
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hasanbkaylan/crossed/issues"))
                context.startActivity(intent)
            }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_btn_issue))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { navController.navigate("licenses") }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_btn_licenses))
            }
        }
    }
}
