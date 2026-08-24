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

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null

        val location = suspendCancellableCoroutine<Location?> { cont ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val cancellationSignal = CancellationSignal()
                cont.invokeOnCancellation { cancellationSignal.cancel() }
                try {
                    val provider = when {
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                        else -> LocationManager.PASSIVE_PROVIDER
                    }
                    locationManager.getCurrentLocation(
                        provider,
                        cancellationSignal,
                        context.mainExecutor
                    ) { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                } catch (e: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            } else {
                // Fallback for older API levels
                var bestLocation: Location? = null
                val providers = locationManager.getProviders(true)
                for (p in providers) {
                    val l = try { locationManager.getLastKnownLocation(p) } catch (e: Exception) { null }
                    if (l != null && (bestLocation == null || l.accuracy < bestLocation.accuracy)) {
                        bestLocation = l
                    }
                }
                if (bestLocation != null) {
                    cont.resume(bestLocation)
                } else {
                    val listener = object : LocationListener {
                        override fun onLocationChanged(loc: Location) {
                            try { locationManager.removeUpdates(this) } catch (e: Exception) {}
                            if (cont.isActive) cont.resume(loc)
                        }
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }
                    cont.invokeOnCancellation {
                        try { locationManager.removeUpdates(listener) } catch (e: Exception) {}
                    }
                    try {
                        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, null)
                    } catch (e: Exception) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
        }

        if (location == null) {
            // Last known fallback
            val lastKnown = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            } catch (e: Exception) {
                null
            } ?: return@withContext null

            val name = resolveAddress(lastKnown.latitude, lastKnown.longitude)
            return@withContext LocationResult(lastKnown.latitude, lastKnown.longitude, name)
        }

        val resolved = resolveAddress(location.latitude, location.longitude)
        LocationResult(location.latitude, location.longitude, resolved)
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
