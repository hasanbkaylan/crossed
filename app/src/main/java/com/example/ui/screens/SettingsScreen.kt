package com.example.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.R
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, navController: NavController) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }
    var radiusExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val currentLocale = AppCompatDelegate.getApplicationLocales().getFirstMatch(arrayOf("en", "tr"))?.language ?: "system"
    val username by viewModel.settingsManager.usernameFlow.collectAsState(initial = "")
    val radius by viewModel.settingsManager.radiusFlow.collectAsState(initial = 100)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(stringResource(R.string.settings_username_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.settings_username_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { scope.launch { viewModel.settingsManager.setUsername(it) } },
                label = { Text(stringResource(R.string.onboarding_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(stringResource(R.string.settings_radius_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.settings_radius_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box {
                OutlinedButton(onClick = { radiusExpanded = true }) {
                    Text(
                        when (radius) {
                            50 -> stringResource(R.string.radius_50)
                            100 -> stringResource(R.string.radius_100)
                            200 -> stringResource(R.string.radius_200)
                            500 -> stringResource(R.string.radius_500)
                            else -> "$radius m"
                        }
                    )
                }
                DropdownMenu(expanded = radiusExpanded, onDismissRequest = { radiusExpanded = false }) {
                    listOf(50, 100, 200, 500).forEach { r ->
                        DropdownMenuItem(
                            text = { 
                                Text(when (r) {
                                    50 -> stringResource(R.string.radius_50)
                                    100 -> stringResource(R.string.radius_100)
                                    200 -> stringResource(R.string.radius_200)
                                    500 -> stringResource(R.string.radius_500)
                                    else -> "$r m"
                                }) 
                            },
                            onClick = {
                                scope.launch { viewModel.settingsManager.setRadius(r) }
                                radiusExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.settings_language_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box {
                OutlinedButton(onClick = { languageExpanded = true }) {
                    Icon(Icons.Default.Language, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (currentLocale) {
                            "en" -> stringResource(R.string.settings_language_en)
                            "tr" -> stringResource(R.string.settings_language_tr)
                            else -> stringResource(R.string.settings_language_system)
                        }
                    )
                }
                DropdownMenu(
                    expanded = languageExpanded,
                    onDismissRequest = { languageExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings_language_system)) },
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                            languageExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings_language_en)) },
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                            languageExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings_language_tr)) },
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr"))
                            languageExpanded = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(stringResource(R.string.settings_data_management), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_delete_all))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.settings_delete_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllData()
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.dialog_delete_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.dialog_delete_cancel))
                }
            }
        )
    }
}
