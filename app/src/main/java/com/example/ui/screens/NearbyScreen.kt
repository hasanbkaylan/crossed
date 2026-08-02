package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.navigation.NavController
import com.example.R
import com.example.domain.MatchStatus
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun NearbyScreen(viewModel: MainViewModel, navController: NavController) {
    val matchStatus by viewModel.matchStatus.collectAsState()
    val matchedPhotos by viewModel.matchedPhotos.collectAsState()

    val permissions = mutableListOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
        permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
    }

    val permissionState = com.google.accompanist.permissions.rememberMultiplePermissionsState(
        permissions = permissions
    )

    DisposableEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            viewModel.startNearbyDiscovery()
        }
        onDispose {
            viewModel.stopNearbyDiscovery()
            viewModel.clearMatches()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nearby_title)) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!permissionState.allPermissionsGranted) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.nearby_permission_denied),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                    Text(stringResource(R.string.nearby_permission_grant))
                }
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedButton(onClick = { navController.navigateUp() }) {
                    Text(stringResource(R.string.nearby_btn_back))
                }
            } else {
                when (val status = matchStatus) {
                is MatchStatus.Idle, is MatchStatus.Discovering -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 6.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        stringResource(R.string.nearby_looking),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.nearby_ensure_open),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is MatchStatus.DeviceFound -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.nearby_found, status.device.name),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.nearby_compare_prompt),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(onClick = { viewModel.rejectMatch(); navController.navigateUp() }) {
                                    Text(stringResource(R.string.nearby_btn_cancel))
                                }
                                Button(onClick = { viewModel.approveMatch() }) {
                                    Text(stringResource(R.string.nearby_btn_compare))
                                }
                            }
                        }
                    }
                }

                is MatchStatus.ExchangingData -> {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.nearby_exchanging), style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.nearby_intersecting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is MatchStatus.MatchComplete -> {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.nearby_paths_crossed),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.nearby_match_count, status.matchedHashes.size),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (matchedPhotos.isEmpty()) {
                        LaunchedEffect(Unit) {
                            viewModel.processMatches(status.matchedHashes)
                        }
                    } else {
                        // Display minimal location info (since we don't have a map api in this simple app)
                        matchedPhotos.forEach { photo ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(stringResource(R.string.nearby_date, java.text.DateFormat.getDateTimeInstance().format(java.util.Date(photo.dateTaken))))
                                    Text(stringResource(R.string.nearby_location, photo.latitude.toString(), photo.longitude.toString()))
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { navController.navigateUp() }) {
                        Text(stringResource(R.string.nearby_btn_done))
                    }
                }

                is MatchStatus.NoMatch -> {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.nearby_no_crossings),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.nearby_no_crossings_desc))
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { navController.navigateUp() }) {
                        Text(stringResource(R.string.nearby_btn_back))
                    }
                }

                is MatchStatus.NoDeviceFound -> {
                    Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.nearby_no_device_found),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.nearby_no_device_found_desc), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { navController.navigateUp() }) {
                        Text(stringResource(R.string.nearby_btn_back))
                    }
                }

                is MatchStatus.Error -> {
                    Text(stringResource(R.string.nearby_error, status.message), color = MaterialTheme.colorScheme.error)
                }
                
                else -> {}
            }
        }
    }
}
            }
