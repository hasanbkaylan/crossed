with open('app/src/main/java/com/example/domain/RealNearbyManager.kt', 'r') as f:
    content = f.read()

# Add autoAcceptConnection flag
content = content.replace("private var peerDataReceived = false", "private var autoAcceptConnection = false\n    private var peerDataReceived = false")

# Update onConnectionInitiated
old_init = """            if (info.isIncomingConnection) {
                _matchStatus.value = MatchStatus.ConnectionRequested(NearbyDevice(endpointId, info.endpointName))
            } else {
                _matchStatus.value = MatchStatus.Connecting(NearbyDevice(endpointId, info.endpointName))
            }"""
new_init = """            if (info.isIncomingConnection) {
                _matchStatus.value = MatchStatus.ConnectionRequested(NearbyDevice(endpointId, info.endpointName))
            } else {
                _matchStatus.value = MatchStatus.Connecting(NearbyDevice(endpointId, info.endpointName))
                if (autoAcceptConnection) {
                    Log.d("CROSSED_NEARBY", "Auto-accepting connection as initiator")
                    connectionsClient.acceptConnection(endpointId, payloadCallback)
                        .addOnFailureListener { e ->
                            Log.e("CROSSED_NEARBY", "acceptConnection failed", e)
                            _matchStatus.value = MatchStatus.Error("Otomatik kabul başarısız: ${e.message}")
                        }
                }
            }"""
content = content.replace(old_init, new_init)

# Update requestConnection
old_req = """    override fun requestConnection(deviceId: String) {
        val device = discoveredDevices[deviceId] ?: return
        if (_matchStatus.value is MatchStatus.Connecting || _matchStatus.value is MatchStatus.ConnectionRequested) return
        
        Log.d("CROSSED_NEARBY", "requestConnection to $deviceId")
        _matchStatus.value = MatchStatus.Connecting(device)
        startTimeout(60000, "Bağlantı isteği zaman aşımına uğradı.")"""
new_req = """    override fun requestConnection(deviceId: String, myHashes: List<String>) {
        val device = discoveredDevices[deviceId] ?: return
        if (_matchStatus.value is MatchStatus.Connecting || _matchStatus.value is MatchStatus.ConnectionRequested) return
        
        Log.d("CROSSED_NEARBY", "requestConnection to $deviceId")
        _matchStatus.value = MatchStatus.Connecting(device)
        myHashesToSend = myHashes
        autoAcceptConnection = true
        startTimeout(60000, "Bağlantı isteği zaman aşımına uğradı.")"""
content = content.replace(old_req, new_req)

# Update stopDiscoveryInternal
content = content.replace("discoveredDevices.clear()", "discoveredDevices.clear()\n        autoAcceptConnection = false")

# Update approveMatch
content = content.replace("myHashesToSend = myHashes", "myHashesToSend = myHashes\n        autoAcceptConnection = false")

with open('app/src/main/java/com/example/domain/RealNearbyManager.kt', 'w') as f:
    f.write(content)
