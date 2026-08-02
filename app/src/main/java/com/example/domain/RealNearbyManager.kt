package com.example.domain

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timeoutJob: Job? = null

    // Handshake state
    private var peerDataReceived = false
    private var peerAckReceived = false
    private var matchesResult: List<String>? = null

    private fun startTimeout(ms: Long, timeoutMessage: String) {
        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(ms)
            Log.e("CROSSED_NEARBY", "Timeout reached: $timeoutMessage")
            _matchStatus.value = MatchStatus.Error(timeoutMessage)
            stopDiscoveryInternal(preserveStatus = true)
        }
    }

    private fun cancelTimeout() {
        timeoutJob?.cancel()
        timeoutJob = null
    }

    private fun checkHandshakeComplete() {
        if (peerDataReceived && peerAckReceived) {
            cancelTimeout()
            Log.d("CROSSED_NEARBY", "Handshake complete. Matches: ${matchesResult?.size}")
            val matches = matchesResult ?: emptyList()
            if (matches.isNotEmpty()) {
                _matchStatus.value = MatchStatus.MatchComplete(matches)
            } else {
                _matchStatus.value = MatchStatus.NoMatch
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d("CROSSED_NEARBY", "onConnectionInitiated with $endpointId, incoming: ${info.isIncomingConnection}")
            currentEndpointId = endpointId
            peerDataReceived = false
            peerAckReceived = false
            matchesResult = null
            startTimeout(60000, "Bağlantı onayı zaman aşımına uğradı.")
            
            if (info.isIncomingConnection) {
                _matchStatus.value = MatchStatus.ConnectionRequested(NearbyDevice(endpointId, info.endpointName))
            } else {
                _matchStatus.value = MatchStatus.Connecting(NearbyDevice(endpointId, info.endpointName))
            }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Log.d("CROSSED_NEARBY", "onConnectionResult for $endpointId: ${result.status.statusCode}")
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    _matchStatus.value = MatchStatus.ExchangingData
                    startTimeout(30000, "Veri değiş tokuşu zaman aşımına uğradı.")
                    sendData()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    cancelTimeout()
                    _matchStatus.value = MatchStatus.Error("Bağlantı reddedildi.")
                    stopDiscoveryInternal(preserveStatus = true)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    cancelTimeout()
                    _matchStatus.value = MatchStatus.Error("Bağlantı hatası oluştu.")
                    stopDiscoveryInternal(preserveStatus = true)
                }
                else -> {
                    cancelTimeout()
                    _matchStatus.value = MatchStatus.Error("Bağlantı kurulamadı (Hata Kodu: ${result.status.statusCode})")
                    stopDiscoveryInternal(preserveStatus = true)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d("CROSSED_NEARBY", "onDisconnected from $endpointId")
            if (currentEndpointId == endpointId) {
                currentEndpointId = null
                cancelTimeout()
                if (_matchStatus.value is MatchStatus.ExchangingData) {
                    _matchStatus.value = MatchStatus.Error("Bağlantı koptu.")
                } else if (_matchStatus.value !is MatchStatus.MatchComplete && _matchStatus.value !is MatchStatus.NoMatch && _matchStatus.value !is MatchStatus.Error) {
                    _matchStatus.value = MatchStatus.Discovering(discoveredDevices.values.toList())
                }
            }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val msgStr = String(payload.asBytes()!!, StandardCharsets.UTF_8)
                Log.d("CROSSED_NEARBY", "onPayloadReceived from $endpointId: ${msgStr.take(20)}...")
                
                if (msgStr.startsWith("DATA:")) {
                    val peerHashes = msgStr.substringAfter("DATA:").split(",").filter { it.isNotBlank() }
                    val myHashes = myHashesToSend ?: emptyList()
                    matchesResult = myHashes.intersect(peerHashes.toSet()).toList()
                    peerDataReceived = true
                    
                    // Send ACK back
                    Log.d("CROSSED_NEARBY", "Sending ACK to $endpointId")
                    val ackPayload = Payload.fromBytes("ACK:OK".toByteArray(StandardCharsets.UTF_8))
                    connectionsClient.sendPayload(endpointId, ackPayload)
                    
                    checkHandshakeComplete()
                } else if (msgStr.startsWith("ACK:")) {
                    Log.d("CROSSED_NEARBY", "Received ACK from $endpointId")
                    peerAckReceived = true
                    checkHandshakeComplete()
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d("CROSSED_NEARBY", "onEndpointFound: $endpointId, name: ${info.endpointName}")
            val device = NearbyDevice(endpointId, info.endpointName)
            discoveredDevices[endpointId] = device
            if (_matchStatus.value is MatchStatus.Discovering) {
                _matchStatus.value = MatchStatus.Discovering(discoveredDevices.values.toList())
            }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d("CROSSED_NEARBY", "onEndpointLost: $endpointId")
            discoveredDevices.remove(endpointId)
            if (_matchStatus.value is MatchStatus.Discovering) {
                _matchStatus.value = MatchStatus.Discovering(discoveredDevices.values.toList())
            }
        }
    }

    override fun startDiscovery(myName: String) {
        Log.d("CROSSED_NEARBY", "startDiscovery called")
        stopDiscoveryInternal(preserveStatus = false)
        this.myName = myName.ifBlank { android.os.Build.MODEL }
        discoveredDevices.clear()
        _matchStatus.value = MatchStatus.Discovering(emptyList())
        
        startTimeout(60000, "Cihaz arama zaman aşımına uğradı. (Lütfen tekrar deneyin)")

        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        val advOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()

        connectionsClient.startAdvertising(this.myName, SERVICE_ID, connectionLifecycleCallback, advOptions)
            .addOnFailureListener { e -> 
                Log.e("CROSSED_NEARBY", "Advertising failed", e) 
                _matchStatus.value = MatchStatus.Error("Reklam başlatılamadı: ${e.message}")
            }

        connectionsClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
            .addOnFailureListener { e ->
                Log.e("CROSSED_NEARBY", "Discovery failed", e)
                _matchStatus.value = MatchStatus.Error("Keşif başlatılamadı: ${e.message}")
            }
    }
    
    override fun requestConnection(deviceId: String) {
        val device = discoveredDevices[deviceId] ?: return
        if (_matchStatus.value is MatchStatus.Connecting || _matchStatus.value is MatchStatus.ConnectionRequested) return
        
        Log.d("CROSSED_NEARBY", "requestConnection to $deviceId")
        _matchStatus.value = MatchStatus.Connecting(device)
        startTimeout(60000, "Bağlantı isteği zaman aşımına uğradı.")
        
        connectionsClient.requestConnection(myName, deviceId, connectionLifecycleCallback)
            .addOnFailureListener { e ->
                Log.e("CROSSED_NEARBY", "requestConnection failed", e)
                _matchStatus.value = MatchStatus.Error("Bağlantı isteği başarısız: ${e.message}")
            }
    }

    private fun stopDiscoveryInternal(preserveStatus: Boolean = false) {
        Log.d("CROSSED_NEARBY", "stopDiscoveryInternal, preserveStatus: $preserveStatus")
        cancelTimeout()
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        currentEndpointId = null
        discoveredDevices.clear()
        peerDataReceived = false
        peerAckReceived = false
        matchesResult = null
        if (!preserveStatus) {
            _matchStatus.value = MatchStatus.Idle
        }
    }

    override fun stopDiscovery() {
        Log.d("CROSSED_NEARBY", "stopDiscovery called (from UI)")
        stopDiscoveryInternal(preserveStatus = false)
    }

    override fun approveMatch(myHashes: List<String>) {
        if (_matchStatus.value !is MatchStatus.ConnectionRequested && _matchStatus.value !is MatchStatus.Connecting) {
             return
        }
        val endpointId = currentEndpointId
        if (endpointId == null) {
            _matchStatus.value = MatchStatus.Error("No device bağlı değil.")
            return
        }
        
        Log.d("CROSSED_NEARBY", "approveMatch for $endpointId")
        myHashesToSend = myHashes
        _matchStatus.value = MatchStatus.ExchangingData
        startTimeout(30000, "Veri değiş tokuşu zaman aşımına uğradı.")
        
        connectionsClient.acceptConnection(endpointId, payloadCallback)
            .addOnFailureListener { e ->
                Log.e("CROSSED_NEARBY", "acceptConnection failed", e)
                _matchStatus.value = MatchStatus.Error("Kabul işlemi başarısız: ${e.message}")
            }
    }

    override fun rejectMatch() {
        if (_matchStatus.value !is MatchStatus.ConnectionRequested && _matchStatus.value !is MatchStatus.Connecting) {
             return
        }
        Log.d("CROSSED_NEARBY", "rejectMatch called")
        currentEndpointId?.let { endpointId ->
            connectionsClient.rejectConnection(endpointId)
        }
        _matchStatus.value = MatchStatus.Discovering(discoveredDevices.values.toList())
        startTimeout(60000, "Cihaz arama zaman aşımına uğradı.")
    }
    
    private fun sendData() {
        currentEndpointId?.let { endpointId ->
            Log.d("CROSSED_NEARBY", "sendData to $endpointId")
            val dataStr = "DATA:" + (myHashesToSend ?: emptyList()).joinToString(",")
            val payload = Payload.fromBytes(dataStr.toByteArray(StandardCharsets.UTF_8))
            connectionsClient.sendPayload(endpointId, payload)
        }
    }
}
