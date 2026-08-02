package com.example.domain

import java.security.MessageDigest
import kotlin.math.round

object HashUtil {
    /**
     * Converts latitude, longitude, and timestamp into an irreversible hash.
     * To find intersections, we group coordinates into grids (e.g., ~100 meters).
     * 3 decimal places in lat/lon is roughly 110 meters.
     * We group time into 1-hour windows.
     */
    fun createLocationHash(latitude: Double, longitude: Double, dateTakenMs: Long): String {
        // Round to 3 decimal places (~100m precision)
        val latRounded = round(latitude * 1000) / 1000.0
        val lonRounded = round(longitude * 1000) / 1000.0
        
        // Time window: divide timestamp by (1000 * 60 * 60) to get hour blocks
        val timeWindow = dateTakenMs / 3600000L

        val rawString = "lat:${latRounded}_lon:${lonRounded}_time:${timeWindow}"
        return hashString(rawString)
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
