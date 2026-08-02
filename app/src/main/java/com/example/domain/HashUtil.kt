package com.example.domain

import java.security.MessageDigest
import kotlin.math.round

object HashUtil {
    /**
     * Converts latitude, longitude, and timestamp into an irreversible hash.
     * @param radiusMeters The approximate grid size in meters.
     */
    fun createLocationHash(latitude: Double, longitude: Double, dateTakenMs: Long, radiusMeters: Int = 100): String {
        // 1 degree is roughly 111,000 meters.
        // step size in degrees = radiusMeters / 111000.0
        val step = radiusMeters / 111000.0
        
        val latRounded = if (step > 0) round(latitude / step) * step else latitude
        val lonRounded = if (step > 0) round(longitude / step) * step else longitude
        
        // Time window: divide timestamp by (1000 * 60 * 60) to get 1-hour blocks
        val timeWindow = dateTakenMs / 3600000L
        
        val rawString = "lat:${latRounded}_lon:${lonRounded}_time:${timeWindow}"
        return hashString(rawString)
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
