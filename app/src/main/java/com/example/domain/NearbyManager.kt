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
    object NoDeviceFound : MatchStatus()
}

interface NearbyManager {
    val matchStatus: StateFlow<MatchStatus>
    fun startDiscovery()
    fun stopDiscovery()
    fun approveMatch(myHashes: List<String>)
    fun rejectMatch()
}

class SimulatedNearbyManager : NearbyManager {
    private val _matchStatus = MutableStateFlow<MatchStatus>(MatchStatus.Idle)
    override val matchStatus: StateFlow<MatchStatus> = _matchStatus
    
    private var simulatedJob: Job? = null

    override fun startDiscovery() {
        _matchStatus.value = MatchStatus.Discovering
        
        simulatedJob = GlobalScope.launch {
            delay(5000)
            _matchStatus.value = MatchStatus.NoDeviceFound
        }
    }

    override fun stopDiscovery() {
        simulatedJob?.cancel()
        _matchStatus.value = MatchStatus.Idle
    }

    override fun approveMatch(myHashes: List<String>) {
        // Not reachable in this state
    }

    override fun rejectMatch() {
        stopDiscovery()
    }
}
