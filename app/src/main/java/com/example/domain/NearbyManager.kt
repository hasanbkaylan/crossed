package com.example.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job

data class NearbyDevice(
    val id: String,
    val name: String
)

sealed class MatchStatus {
    object Idle : MatchStatus()
    object Discovering : MatchStatus()
    data class DeviceFound(val device: NearbyDevice) : MatchStatus()
    object AwaitingApproval : MatchStatus()
    object ExchangingData : MatchStatus()
    data class MatchComplete(val matchedHashes: List<String>) : MatchStatus()
    object NoMatch : MatchStatus()
    data class Error(val message: String) : MatchStatus()
}

interface NearbyManager {
    val matchStatus: StateFlow<MatchStatus>
    fun startDiscovery()
    fun stopDiscovery()
    fun approveMatch(myHashes: List<String>)
    fun rejectMatch()
    fun setMockPeerHashes(hashes: List<String>) // For Simulation mode
}

class SimulatedNearbyManager : NearbyManager {
    private val _matchStatus = MutableStateFlow<MatchStatus>(MatchStatus.Idle)
    override val matchStatus: StateFlow<MatchStatus> = _matchStatus
    
    private var simulatedJob: Job? = null
    
    private var mockPeerHashes: List<String> = emptyList()

    override fun startDiscovery() {
        _matchStatus.value = MatchStatus.Discovering
        
        simulatedJob = GlobalScope.launch {
            delay(3000)
            _matchStatus.value = MatchStatus.DeviceFound(NearbyDevice("peer_123", "Alice's Phone"))
        }
    }

    override fun stopDiscovery() {
        simulatedJob?.cancel()
        _matchStatus.value = MatchStatus.Idle
    }

    override fun approveMatch(myHashes: List<String>) {
        _matchStatus.value = MatchStatus.ExchangingData
        
        simulatedJob = GlobalScope.launch {
            delay(2000)
            
            val matches = myHashes.intersect(mockPeerHashes.toSet()).toList()
            
            if (matches.isNotEmpty()) {
                _matchStatus.value = MatchStatus.MatchComplete(matches)
            } else {
                _matchStatus.value = MatchStatus.NoMatch
            }
        }
    }

    override fun rejectMatch() {
        stopDiscovery()
    }
    
    override fun setMockPeerHashes(hashes: List<String>) {
        val fakeMatches = mutableListOf<String>()
        if (hashes.isNotEmpty()) {
             fakeMatches.add(hashes.random()) // Peer crossed paths here!
        }
        fakeMatches.add(HashUtil.createLocationHash(40.0, -73.0, System.currentTimeMillis()))
        mockPeerHashes = fakeMatches
    }
}

