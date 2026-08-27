package com.nasfinder.whattoeat.data

import android.location.Location

enum class LocationFailureReason {
    PERMISSION_DENIED,
    LOCATION_SERVICES_DISABLED,
    TIMEOUT_OR_UNAVAILABLE
}

sealed class LocationFetchResult {
    data class Success(val locationResult: LocationResult) : LocationFetchResult()
    data class Failure(val reason: LocationFailureReason, val message: String) : LocationFetchResult()
}

object LocationFallbackPolicy {
    const val DEFAULT_MAX_AGE_MILLIS: Long = 5 * 60 * 1000L // 5 minutes
    const val FRESH_THRESHOLD_MILLIS: Long = DEFAULT_MAX_AGE_MILLIS
    const val LOCATION_REQUEST_TIMEOUT_MILLIS: Long = 12_000L

    /**
     * Checks whether the latitude and longitude represent valid, non-zero coordinates.
     */
    fun isValidCoordinates(lat: Double, lng: Double): Boolean {
        if (lat.isNaN() || lng.isNaN()) return false
        if (lat.isInfinite() || lng.isInfinite()) return false
        if (lat < -90.0 || lat > 90.0) return false
        if (lng < -180.0 || lng > 180.0) return false
        // Exclude default uninitialized (0.0, 0.0) null island
        if (lat == 0.0 && lng == 0.0) return false
        return true
    }

    /**
     * Checks if the given Android Location object has valid non-empty coordinates.
     */
    fun isValidLocation(location: Location?): Boolean {
        if (location == null) return false
        return isValidCoordinates(location.latitude, location.longitude)
    }

    /**
     * Prioritizes available system providers for indoor and fused location routing.
     * Order: Fused (Wi-Fi + Cell + GPS) -> Network (Wi-Fi + Cell) -> GPS -> Passive -> other.
     */
    fun getPrioritizedProviders(
        availableProviders: List<String>,
        isFineLocationGranted: Boolean = true
    ): List<String> {
        val result = mutableListOf<String>()

        // 1. Fused provider (API 31+ or Google Play Services fused if present)
        val fused = availableProviders.firstOrNull { it.equals("fused", ignoreCase = true) }
        if (fused != null) {
            result.add(fused)
        }

        // 2. Network provider (Wi-Fi / Cell positioning, ideal for indoor)
        val network = availableProviders.firstOrNull { it.equals("network", ignoreCase = true) }
        if (network != null && !result.contains(network)) {
            result.add(network)
        }

        // 3. GPS provider (Satellite, requires fine location)
        if (isFineLocationGranted) {
            val gps = availableProviders.firstOrNull { it.equals("gps", ignoreCase = true) }
            if (gps != null && !result.contains(gps)) {
                result.add(gps)
            }
        }

        // 4. Passive provider
        val passive = availableProviders.firstOrNull { it.equals("passive", ignoreCase = true) }
        if (passive != null && !result.contains(passive)) {
            result.add(passive)
        }

        // 5. Any remaining providers
        for (provider in availableProviders) {
            if (!isFineLocationGranted && provider.equals("gps", ignoreCase = true)) continue
            if (!result.contains(provider)) {
                result.add(provider)
            }
        }

        return result
    }

    /**
     * Selects the best location from a list of candidate locations based on validity, freshness, and accuracy.
     */
    fun selectBestLocation(
        candidates: List<Location>,
        maxAgeMillis: Long = DEFAULT_MAX_AGE_MILLIS,
        nowMillis: Long = System.currentTimeMillis()
    ): Location? {
        val validCandidates = candidates.filter { isValidLocation(it) }
        if (validCandidates.isEmpty()) return null

        val candidatesWithAge = validCandidates.map { loc ->
            val age = if (loc.time > 0L && nowMillis >= loc.time) nowMillis - loc.time else Long.MAX_VALUE
            Pair(loc, age)
        }

        // Filter by max age if applicable
        val acceptableCandidates = if (maxAgeMillis < Long.MAX_VALUE) {
            candidatesWithAge.filter { it.second <= maxAgeMillis }
        } else {
            candidatesWithAge
        }

        if (acceptableCandidates.isEmpty()) return null

        // Sort: fresh locations (< FRESH_THRESHOLD_MILLIS) prioritized, then best accuracy (smaller is better), then newest
        return acceptableCandidates.minWithOrNull { a, b ->
            val aIsFresh = a.second <= FRESH_THRESHOLD_MILLIS
            val bIsFresh = b.second <= FRESH_THRESHOLD_MILLIS

            when {
                aIsFresh && !bIsFresh -> -1
                !aIsFresh && bIsFresh -> 1
                else -> {
                    val aAcc = if (a.first.hasAccuracy()) a.first.accuracy else Float.MAX_VALUE
                    val bAcc = if (b.first.hasAccuracy()) b.first.accuracy else Float.MAX_VALUE

                    // If accuracies differ significantly (> 50m), prefer better accuracy
                    if (kotlin.math.abs(aAcc - bAcc) > 50f) {
                        aAcc.compareTo(bAcc)
                    } else {
                        // Otherwise prefer newer
                        a.second.compareTo(b.second)
                    }
                }
            }
        }?.first
    }

    /**
     * Resolves short Korean recovery copy for the given location failure reason.
     */
    fun resolveRecoveryMessage(reason: LocationFailureReason): String {
        return when (reason) {
            LocationFailureReason.PERMISSION_DENIED ->
                "위치 권한을 켜거나, 지역을 직접 지정해 주세요."
            LocationFailureReason.LOCATION_SERVICES_DISABLED ->
                "기기 위치 설정을 켜거나, 지역을 직접 지정해 주세요."
            LocationFailureReason.TIMEOUT_OR_UNAVAILABLE ->
                "실내에서는 위치 확인이 어려울 수 있어요. 지역을 직접 지정해 주세요."
        }
    }

    /**
     * Resolves status text for the Region screen based on the location failure reason.
     */
    fun resolveStatusText(reason: LocationFailureReason?): String {
        return when (reason) {
            LocationFailureReason.PERMISSION_DENIED ->
                "위치 권한이 꺼져 있어요"
            LocationFailureReason.LOCATION_SERVICES_DISABLED ->
                "기기 위치 서비스가 꺼져 있어요"
            LocationFailureReason.TIMEOUT_OR_UNAVAILABLE ->
                "현 위치 확인에 실패했어요. 지역을 검색해 보세요."
            null ->
                "현 위치를 다시 확인할 수 있어요"
        }
    }
}
