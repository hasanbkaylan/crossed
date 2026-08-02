package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import android.content.Context
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.net.wifi.WifiManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Wifi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.MatchStatus
import com.example.ui.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun NearbyScreen(viewModel: MainViewModel, navController: NavController) {
    val matchStatus by viewModel.matchStatus.collectAsState()
    val matchedPhotos by viewModel.matchedPhotos.collectAsState()

    val permissions = mutableListOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_WIFI_STATE,
        android.Manifest.permission.CHANGE_WIFI_STATE
    )
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
        permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        permissions.add(android.Manifest.permission.NEARBY_WIFI_DEVICES)
    }

    val permissionState = rememberMultiplePermissionsState(permissions)

    var statusTimer by remember { mutableStateOf(0) }
    LaunchedEffect(matchStatus) {
        statusTimer = 0
        while (true) {
            kotlinx.coroutines.delay(1000)
            statusTimer++
        }
    }


    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter = bluetoothManager?.adapter
    
    var isBluetoothEnabled by remember { mutableStateOf(bluetoothAdapter?.isEnabled == true) }
    var isWifiEnabled by remember { 
        mutableStateOf((context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.isWifiEnabled == true)
    }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isBluetoothEnabled = bluetoothAdapter?.isEnabled == true
    }
    
    val wifiSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isWifiEnabled = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.isWifiEnabled == true
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBluetoothEnabled = bluetoothAdapter?.isEnabled == true
                isWifiEnabled = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.isWifiEnabled == true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(permissionState.allPermissionsGranted, isBluetoothEnabled, isWifiEnabled) {
        if (permissionState.allPermissionsGranted && isBluetoothEnabled && isWifiEnabled) {
            viewModel.startNearbyDiscovery()
        } else {
            viewModel.clearMatches()
        }
        onDispose {
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
                Text(stringResource(R.string.nearby_permission_denied), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                    Text(stringResource(R.string.nearby_permission_grant))
                }
                return@Column
            }

            if (!isBluetoothEnabled) {
                Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Bluetooth Kapalı", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Yakındaki cihazları bulabilmek için Bluetooth'u açmanız gerekiyor.", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { 
                    enableBluetoothLauncher.launch(android.content.Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)) 
                }) {
                    Text("Bluetooth'u Aç")
                }
                return@Column
            }

            if (!isWifiEnabled) {
                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Wi-Fi Kapalı", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cihazlar arası P2P bağlantı kurabilmek için Wi-Fi'ın açık olması gerekiyor.", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { 
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        wifiSettingsLauncher.launch(android.content.Intent(android.provider.Settings.Panel.ACTION_WIFI))
                    } else {
                        wifiSettingsLauncher.launch(android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
                    }
                }) {
                    Text("Wi-Fi Ayarlarını Aç")
                }
                return@Column
            }

            when (val status = matchStatus) {
                is MatchStatus.Idle -> {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Hazırlanıyor... ($statusTimer sn)", style = MaterialTheme.typography.titleLarge)
                }
                is MatchStatus.Discovering -> {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.nearby_looking) + " ($statusTimer sn)", style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.nearby_ensure_open),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (status.devices.isEmpty()) {
                        Text("Henüz cihaz bulunamadı...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("Bulunan Cihazlar:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(status.devices) { device ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { viewModel.requestConnection(device.id) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Text(
                                        device.name,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                is MatchStatus.Connecting -> {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.nearby_connecting, status.device.name) + " ($statusTimer sn)", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(32.dp))
                    OutlinedButton(onClick = { viewModel.startNearbyDiscovery() }) {
                        Text(stringResource(R.string.nearby_btn_cancel))
                    }
                }
                is MatchStatus.ConnectionRequested -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.nearby_connection_requested, status.device.name),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
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
                                OutlinedButton(onClick = { viewModel.rejectMatch() }) {
                                    Text(stringResource(R.string.nearby_btn_cancel))
                                }
                                Button(onClick = { viewModel.approveMatch() }) {
                                    Text(stringResource(R.string.nearby_btn_connect))
                                }
                            }
                        }
                    }
                }
                is MatchStatus.ExchangingData -> {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.nearby_exchanging) + " ($statusTimer sn)", style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.nearby_intersecting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    OutlinedButton(onClick = { viewModel.startNearbyDiscovery() }) {
                        Text(stringResource(R.string.nearby_btn_cancel))
                    }
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
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(matchedPhotos) { photo ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        val photoUri = android.content.ContentUris.withAppendedId(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, photo.id)
                                        AsyncImage(
                                            model = photoUri,
                                            contentDescription = "Matched Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                stringResource(R.string.nearby_date, java.text.DateFormat.getDateTimeInstance().format(java.util.Date(photo.dateTaken))),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                stringResource(R.string.nearby_location, photo.latitude.toString(), photo.longitude.toString()),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
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
                    Text(stringResource(R.string.nearby_no_crossings_desc), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { navController.navigateUp() }) {
                        Text(stringResource(R.string.nearby_btn_back))
                    }
                }
                is MatchStatus.Error -> {
                    Text(stringResource(R.string.nearby_error, status.message), color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.startNearbyDiscovery() }) {
                        Text("Tekrar Dene")
                    }
                }
                else -> {}
            }
        }
    }
}
