package com.example.domain

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.nio.charset.StandardCharsets

class RealNearbyManager(private val context: Context) : NearbyManager {
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val SERVICE_ID = "com.example.crossed.SERVICE_ID"
    private var myName = android.os.Build.MODEL

    private val _matchStatus = MutableStateFlow<MatchStatus>(MatchStatus.Idle)
    override val matchStatus: StateFlow<MatchStatus> = _matchStatus

    private var currentEndpointId: String? = null
    private var myHashesToSend: List<String>? = null
    private val discoveredDevices = mutableMapOf<String, NearbyDevice>()

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            currentEndpointId = endpointId
            // If we initiated it, we are in Connecting. If they initiated, we are in ConnectionRequested.
            if (info.isIncomingConnection) {
                _matchStatus.value = MatchStatus.ConnectionRequested(NearbyDevice(endpointId, info.endpointName))
            } else {
                _matchStatus.value = MatchStatus.Connecting(NearbyDevice(endpointId, info.endpointName))
            }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    _matchStatus.value = MatchStatus.ExchangingData
                    sendData()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    _matchStatus.value = MatchStatus.Error("Bağlantı reddedildi.")
                    stopDiscovery()
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    _matchStatus.value = MatchStatus.Error("Bağlantı hatası oluştu.")
                    stopDiscovery()
                }
                else -> {
                    _matchStatus.value = MatchStatus.Error("Bağlantı kurulamadı (Hata Kodu: ${result.status.statusCode})")
                    stopDiscovery()
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            if (currentEndpointId == endpointId) {
                currentEndpointId = null
                if (_matchStatus.value is MatchStatus.ExchangingData) {
                    _matchStatus.value = MatchStatus.Error("Bağlantı koptu.")
                } else if (_matchStatus.value !is MatchStatus.MatchComplete && _matchStatus.value !is MatchStatus.NoMatch && _matchStatus.value !is MatchStatus.Idle) {
                    _matchStatus.value = MatchStatus.Discovering(discoveredDevices.values.toList())
                }
            }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val dataStr = String(payload.asBytes()!!, StandardCharsets.UTF_8)
                val peerHashes = dataStr.split(",").filter { it.isNotBlank() }
                
                val myHashes = myHashesToSend ?: emptyList()
                val matches = myHashes.intersect(peerHashes.toSet()).toList()
                
                if (matches.isNotEmpty()) {
                    _matchStatus.value = MatchStatus.MatchComplete(matches)
                } else {
                    _matchStatus.value = MatchStatus.NoMatch
                }
                
                // Do not disconnect immediately. 
                // Let the other side receive our payload. 
                // Connection will be closed via stopDiscovery() when the user navigates away.
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val device = NearbyDevice(endpointId, info.endpointName)
            discoveredDevices[endpointId] = device
            if (_matchStatus.value is MatchStatus.Discovering) {
                _matchStatus.value = MatchStatus.Discovering(discoveredDevices.values.toList())
            }
        }

        override fun onEndpointLost(endpointId: String) {
            discoveredDevices.remove(endpointId)
            if (_matchStatus.value is MatchStatus.Discovering) {
                _matchStatus.value = MatchStatus.Discovering(discoveredDevices.values.toList())
            }
        }
    }

    override fun startDiscovery(myName: String) {
        stopDiscovery()
        this.myName = myName.ifBlank { android.os.Build.MODEL }
        discoveredDevices.clear()
        _matchStatus.value = MatchStatus.Discovering(emptyList())
        
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        val advOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()

        connectionsClient.startAdvertising(this.myName, SERVICE_ID, connectionLifecycleCallback, advOptions)
            .addOnFailureListener { e -> 
                Log.e("Nearby", "Advertising failed", e) 
                _matchStatus.value = MatchStatus.Error("Reklam başlatılamadı: ${e.message}")
            }

        connectionsClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
            .addOnFailureListener { e ->
                Log.e("Nearby", "Discovery failed", e)
                _matchStatus.value = MatchStatus.Error("Keşif başlatılamadı: ${e.message}")
            }
    }
    
    override fun requestConnection(deviceId: String) {
        val device = discoveredDevices[deviceId] ?: return
        if (_matchStatus.value is MatchStatus.Connecting || _matchStatus.value is MatchStatus.ConnectionRequested) return
        _matchStatus.value = MatchStatus.Connecting(device)
        connectionsClient.requestConnection(myName, deviceId, connectionLifecycleCallback)
            .addOnFailureListener { e ->
                Log.e("Nearby", "requestConnection failed", e)
                _matchStatus.value = MatchStatus.Error("Bağlantı isteği başarısız: ${e.message}")
            }
    }

    override fun stopDiscovery() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        currentEndpointId = null
        discoveredDevices.clear()
        _matchStatus.value = MatchStatus.Idle
    }

    override fun approveMatch(myHashes: List<String>) {
        if (_matchStatus.value !is MatchStatus.ConnectionRequested && _matchStatus.value !is MatchStatus.Connecting) {
             return
        }
        val endpointId = currentEndpointId
        if (endpointId == null) {
            _matchStatus.value = MatchStatus.Error("No device connected.")
            return
        }
        myHashesToSend = myHashes
        _matchStatus.value = MatchStatus.ExchangingData
        connectionsClient.acceptConnection(endpointId, payloadCallback)
            .addOnFailureListener { e ->
                _matchStatus.value = MatchStatus.Error("Kabul işlemi başarısız: ${e.message}")
            }
    }

    override fun rejectMatch() {
        if (_matchStatus.value !is MatchStatus.ConnectionRequested && _matchStatus.value !is MatchStatus.Connecting) {
             return
        }
        currentEndpointId?.let { endpointId ->
            connectionsClient.rejectConnection(endpointId)
        }
        _matchStatus.value = MatchStatus.Discovering(discoveredDevices.values.toList())
    }
    
    private fun sendData() {
        currentEndpointId?.let { endpointId ->
            val dataStr = (myHashesToSend ?: emptyList()).joinToString(",")
            val payload = Payload.fromBytes(dataStr.toByteArray(StandardCharsets.UTF_8))
            connectionsClient.sendPayload(endpointId, payload)
        }
    }
}
