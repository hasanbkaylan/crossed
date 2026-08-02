package com.example.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class NearbyDevice(
    val id: String,
    val name: String
)

sealed class MatchStatus {
    object Idle : MatchStatus()
    data class Discovering(val devices: List<NearbyDevice>) : MatchStatus()
    data class Connecting(val device: NearbyDevice) : MatchStatus()
    data class ConnectionRequested(val device: NearbyDevice) : MatchStatus()
    object ExchangingData : MatchStatus()
    data class MatchComplete(val matchedHashes: List<String>) : MatchStatus()
    object NoMatch : MatchStatus()
    data class Error(val message: String) : MatchStatus()
}

interface NearbyManager {
    val matchStatus: StateFlow<MatchStatus>
    fun startDiscovery(myName: String)
    fun stopDiscovery()
    fun requestConnection(deviceId: String, myHashes: List<String>)
    fun approveMatch(myHashes: List<String>)
    fun rejectMatch()
}
