package com.nasfinder.whattoeat.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class LocationResult(
    val lat: Double,
    val lng: Double,
    val resolvedName: String
)

data class NearbyRegion(
    val name: String,
    val lat: Double,
    val lng: Double
)

class LocationService(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isFineLocationGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                locationManager.isLocationEnabled
            } catch (e: Exception) {
                false
            }
        } else {
            try {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (e: Exception) {
                false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null
        val candidates = mutableListOf<Location>()
        val enabledProviders = try {
            locationManager.getProviders(true)
        } catch (e: Exception) {
            emptyList<String>()
        }
        for (provider in enabledProviders) {
            try {
                locationManager.getLastKnownLocation(provider)?.let { candidates.add(it) }
            } catch (e: Exception) {
                // ignore provider error
            }
        }
        val standardProviders = listOf(
            "fused",
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        for (p in standardProviders) {
            if (!enabledProviders.contains(p)) {
                try {
                    locationManager.getLastKnownLocation(p)?.let { candidates.add(it) }
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
        return LocationFallbackPolicy.selectBestLocation(candidates)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationDetailed(): LocationFetchResult = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext LocationFetchResult.Failure(
                LocationFailureReason.PERMISSION_DENIED,
                LocationFallbackPolicy.resolveRecoveryMessage(LocationFailureReason.PERMISSION_DENIED)
            )
        }

        if (!isLocationEnabled()) {
            val lastKnown = getLastKnownLocation()
            if (lastKnown != null && LocationFallbackPolicy.isValidLocation(lastKnown)) {
                val name = resolveAddress(lastKnown.latitude, lastKnown.longitude)
                return@withContext LocationFetchResult.Success(
                    LocationResult(lastKnown.latitude, lastKnown.longitude, name)
                )
            }
            return@withContext LocationFetchResult.Failure(
                LocationFailureReason.LOCATION_SERVICES_DISABLED,
                LocationFallbackPolicy.resolveRecoveryMessage(LocationFailureReason.LOCATION_SERVICES_DISABLED)
            )
        }

        val enabledProviders = try {
            locationManager.getProviders(true)
        } catch (e: Exception) {
            emptyList<String>()
        }

        val isFine = isFineLocationGranted()
        val prioritizedProviders = LocationFallbackPolicy.getPrioritizedProviders(enabledProviders, isFine)

        val freshLocation = withTimeoutOrNull(LocationFallbackPolicy.LOCATION_REQUEST_TIMEOUT_MILLIS) {
            requestFreshLocation(prioritizedProviders)
        }

        val chosenLocation = if (freshLocation != null && LocationFallbackPolicy.isValidLocation(freshLocation)) {
            freshLocation
        } else {
            getLastKnownLocation()
        }

        if (chosenLocation == null || !LocationFallbackPolicy.isValidLocation(chosenLocation)) {
            return@withContext LocationFetchResult.Failure(
                LocationFailureReason.TIMEOUT_OR_UNAVAILABLE,
                LocationFallbackPolicy.resolveRecoveryMessage(LocationFailureReason.TIMEOUT_OR_UNAVAILABLE)
            )
        }

        val resolvedName = resolveAddress(chosenLocation.latitude, chosenLocation.longitude)
        LocationFetchResult.Success(
            LocationResult(chosenLocation.latitude, chosenLocation.longitude, resolvedName)
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(providers: List<String>): Location? {
        if (providers.isEmpty()) return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            for (provider in providers) {
                val loc = suspendCancellableCoroutine<Location?> { cont ->
                    val cancellationSignal = CancellationSignal()
                    cont.invokeOnCancellation { cancellationSignal.cancel() }
                    try {
                        locationManager.getCurrentLocation(
                            provider,
                            cancellationSignal,
                            context.mainExecutor
                        ) { result ->
                            if (cont.isActive) cont.resume(result)
                        }
                    } catch (e: Exception) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
                if (loc != null && LocationFallbackPolicy.isValidLocation(loc)) {
                    return loc
                }
            }
            return null
        } else {
            for (provider in providers) {
                val loc = suspendCancellableCoroutine<Location?> { cont ->
                    val listener = object : LocationListener {
                        override fun onLocationChanged(l: Location) {
                            try { locationManager.removeUpdates(this) } catch (e: Exception) {}
                            if (cont.isActive) cont.resume(l)
                        }
                        override fun onStatusChanged(p: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(p: String) {}
                        override fun onProviderDisabled(p: String) {}
                    }
                    cont.invokeOnCancellation {
                        try { locationManager.removeUpdates(listener) } catch (e: Exception) {}
                    }
                    try {
                        locationManager.requestSingleUpdate(provider, listener, null)
                    } catch (e: Exception) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
                if (loc != null && LocationFallbackPolicy.isValidLocation(loc)) {
                    return loc
                }
            }
            return null
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult? {
        return when (val result = getCurrentLocationDetailed()) {
            is LocationFetchResult.Success -> result.locationResult
            is LocationFetchResult.Failure -> null
        }
    }

    suspend fun resolveAddress(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.KOREA)
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                return@withContext formatAddress(addr)
            }
        } catch (e: Exception) {
            // fallback
        }
        "현 위치"
    }

    suspend fun resolveNearbyRegions(lat: Double, lng: Double): List<NearbyRegion> = withContext(Dispatchers.IO) {
        val list = mutableListOf<NearbyRegion>()
        val mainName = resolveAddress(lat, lng)
        list.add(NearbyRegion(mainName, lat, lng))

        // Radial geocoding at offsets (~4km and ~8km)
        // 1 deg lat ~ 111km -> 0.036 deg ~ 4km
        val offsets = listOf(
            Pair(0.036, 0.0),
            Pair(-0.036, 0.0),
            Pair(0.0, 0.045),
            Pair(0.0, -0.045)
        )
        for ((dLat, dLng) in offsets) {
            if (list.size >= 3) break
            val nLat = lat + dLat
            val nLng = lng + dLng
            val name = resolveAddress(nLat, nLng)
            if (name != "현 위치" && list.none { it.name == name }) {
                list.add(NearbyRegion(name, nLat, nLng))
            }
        }

        while (list.size < 3) {
            // Default nearby candidates if geocoder returns duplicates
            val idx = list.size
            val fallbackName = when (idx) {
                1 -> "${mainName} 동쪽"
                else -> "${mainName} 서쪽"
            }
            list.add(NearbyRegion(fallbackName, lat + 0.01 * idx, lng + 0.01 * idx))
        }

        list.take(3)
    }

    suspend fun searchLocation(query: String): LocationResult? = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext null
        try {
            val geocoder = Geocoder(context, Locale.KOREA)
            val addresses = geocoder.getFromLocationName(trimmed, 5)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val name = formatAddress(addr).ifEmpty { trimmed }
                return@withContext LocationResult(addr.latitude, addr.longitude, name)
            }
        } catch (e: Exception) {
            // Geocoder search error
        }
        null
    }

    private fun formatAddress(addr: Address): String {
        // Priority: subLocality / thoroughfare / locality / adminArea
        val admin = addr.adminArea ?: ""
        val locality = addr.locality ?: addr.subAdminArea ?: ""
        val subLocality = addr.subLocality ?: ""
        val thoroughfare = addr.thoroughfare ?: ""
        val feature = addr.featureName ?: ""

        val parts = mutableListOf<String>()
        if (admin.isNotEmpty() && !locality.contains(admin)) parts.add(admin)
        if (locality.isNotEmpty()) parts.add(locality)
        if (subLocality.isNotEmpty() && !locality.contains(subLocality)) parts.add(subLocality)
        if (thoroughfare.isNotEmpty() && !subLocality.contains(thoroughfare) && !parts.contains(thoroughfare)) {
            parts.add(thoroughfare)
        } else if (feature.isNotEmpty() && !parts.contains(feature) && feature != thoroughfare) {
            parts.add(feature)
        }

        return if (parts.isNotEmpty()) {
            parts.joinToString(" ")
        } else {
            addr.getAddressLine(0) ?: ""
        }
    }
}
