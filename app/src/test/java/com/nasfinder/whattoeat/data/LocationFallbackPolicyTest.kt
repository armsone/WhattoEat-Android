package com.nasfinder.whattoeat.data

import android.location.Location
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocationFallbackPolicyTest {

    private fun createLocation(
        provider: String = "fused",
        lat: Double = 37.5665,
        lng: Double = 126.9780,
        time: Long = 1_000_000L,
        accuracy: Float? = null
    ): Location {
        return Location(provider).apply {
            latitude = lat
            longitude = lng
            this.time = time
            if (accuracy != null) {
                this.accuracy = accuracy
            }
        }
    }

    @Test
    fun `isValidCoordinates validates bounds and excludes null island`() {
        assertTrue(LocationFallbackPolicy.isValidCoordinates(37.5665, 126.9780))
        assertTrue(LocationFallbackPolicy.isValidCoordinates(-90.0, -180.0))
        assertTrue(LocationFallbackPolicy.isValidCoordinates(90.0, 180.0))
        assertTrue(LocationFallbackPolicy.isValidCoordinates(37.5, -122.0))

        // Null Island (0.0, 0.0)
        assertFalse(LocationFallbackPolicy.isValidCoordinates(0.0, 0.0))

        // Out of range
        assertFalse(LocationFallbackPolicy.isValidCoordinates(90.1, 127.0))
        assertFalse(LocationFallbackPolicy.isValidCoordinates(-90.1, 127.0))
        assertFalse(LocationFallbackPolicy.isValidCoordinates(37.0, 180.1))
        assertFalse(LocationFallbackPolicy.isValidCoordinates(37.0, -180.1))

        // NaN and Infinite
        assertFalse(LocationFallbackPolicy.isValidCoordinates(Double.NaN, 127.0))
        assertFalse(LocationFallbackPolicy.isValidCoordinates(37.0, Double.NaN))
        assertFalse(LocationFallbackPolicy.isValidCoordinates(Double.POSITIVE_INFINITY, 127.0))
        assertFalse(LocationFallbackPolicy.isValidCoordinates(37.0, Double.NEGATIVE_INFINITY))
    }

    @Test
    fun `isValidLocation handles null and coordinate validity`() {
        assertFalse(LocationFallbackPolicy.isValidLocation(null))

        val nullIsland = createLocation(lat = 0.0, lng = 0.0)
        assertFalse(LocationFallbackPolicy.isValidLocation(nullIsland))

        val validLoc = createLocation(lat = 37.5, lng = 127.0)
        assertTrue(LocationFallbackPolicy.isValidLocation(validLoc))
    }

    @Test
    fun `getPrioritizedProviders prioritizes fused and network for indoor positioning`() {
        val providers = listOf("gps", "passive", "network", "fused", "extra_provider")
        val prioritized = LocationFallbackPolicy.getPrioritizedProviders(providers, isFineLocationGranted = true)

        assertEquals(
            listOf("fused", "network", "gps", "passive", "extra_provider"),
            prioritized
        )
    }

    @Test
    fun `getPrioritizedProviders excludes gps when fine location is not granted`() {
        val providers = listOf("gps", "network", "fused", "passive")
        val prioritized = LocationFallbackPolicy.getPrioritizedProviders(providers, isFineLocationGranted = false)

        assertEquals(
            listOf("fused", "network", "passive"),
            prioritized
        )
        assertFalse(prioritized.contains("gps"))
    }

    @Test
    fun `getPrioritizedProviders works with partial providers`() {
        val providers = listOf("network", "gps")
        val prioritized = LocationFallbackPolicy.getPrioritizedProviders(providers, isFineLocationGranted = true)

        assertEquals(listOf("network", "gps"), prioritized)
    }

    @Test
    fun `selectBestLocation returns null when candidates are empty or invalid`() {
        assertNull(LocationFallbackPolicy.selectBestLocation(emptyList()))

        val invalid1 = createLocation(lat = 0.0, lng = 0.0)
        val invalid2 = createLocation(lat = Double.NaN, lng = 127.0)
        assertNull(LocationFallbackPolicy.selectBestLocation(listOf(invalid1, invalid2)))
    }

    @Test
    fun `selectBestLocation prioritizes fresh location over stale location`() {
        val now = 1_000_000L
        val freshTime = now - (5 * 60 * 1000L) // 5 minutes ago (< 15 min threshold)
        val staleTime = now - (30 * 60 * 1000L) // 30 minutes ago (> 15 min threshold)

        val freshLoc = createLocation(lat = 37.5, lng = 127.0, time = freshTime, accuracy = 100f)
        val staleLoc = createLocation(lat = 37.6, lng = 127.1, time = staleTime, accuracy = 10f)

        val selected = LocationFallbackPolicy.selectBestLocation(listOf(staleLoc, freshLoc), nowMillis = now)
        assertSame(freshLoc, selected)
    }

    @Test
    fun `selectBestLocation prefers significantly better accuracy among fresh locations`() {
        val now = 1_000_000L
        val timeA = now - (4 * 60 * 1000L) // 4 min ago
        val timeB = now - (2 * 60 * 1000L)  // 2 min ago

        // Accuracy difference > 50m (20m vs 150m) -> prefers 20m despite being slightly older
        val accurateLoc = createLocation(lat = 37.5, lng = 127.0, time = timeA, accuracy = 20f)
        val roughLoc = createLocation(lat = 37.6, lng = 127.1, time = timeB, accuracy = 150f)

        val selected = LocationFallbackPolicy.selectBestLocation(listOf(roughLoc, accurateLoc), nowMillis = now)
        assertSame(accurateLoc, selected)
    }

    @Test
    fun `selectBestLocation prefers newer location when accuracy is comparable`() {
        val now = 1_000_000L
        val olderTime = now - (4 * 60 * 1000L) // 4 min ago
        val newerTime = now - (2 * 60 * 1000L)  // 2 min ago

        // Accuracy difference <= 50m (20m vs 35m) -> prefers newerTime
        val olderLoc = createLocation(lat = 37.5, lng = 127.0, time = olderTime, accuracy = 20f)
        val newerLoc = createLocation(lat = 37.6, lng = 127.1, time = newerTime, accuracy = 35f)

        val selected = LocationFallbackPolicy.selectBestLocation(listOf(olderLoc, newerLoc), nowMillis = now)
        assertSame(newerLoc, selected)
    }

    @Test
    fun `selectBestLocation filters out locations exceeding maxAgeMillis`() {
        val now = 100_000_000L
        val maxAge = LocationFallbackPolicy.DEFAULT_MAX_AGE_MILLIS
        val tooOldTime = now - (maxAge + 1000L)

        val staleLoc = createLocation(lat = 37.5, lng = 127.0, time = tooOldTime)
        val selected = LocationFallbackPolicy.selectBestLocation(
            listOf(staleLoc),
            maxAgeMillis = maxAge,
            nowMillis = now
        )
        assertNull(selected)
    }

    @Test
    fun `resolveRecoveryMessage returns concise Korean recovery instructions`() {
        assertEquals(
            "위치 권한을 켜거나, 지역을 직접 지정해 주세요.",
            LocationFallbackPolicy.resolveRecoveryMessage(LocationFailureReason.PERMISSION_DENIED)
        )
        assertEquals(
            "기기 위치 설정을 켜거나, 지역을 직접 지정해 주세요.",
            LocationFallbackPolicy.resolveRecoveryMessage(LocationFailureReason.LOCATION_SERVICES_DISABLED)
        )
        assertEquals(
            "실내에서는 위치 확인이 어려울 수 있어요. 지역을 직접 지정해 주세요.",
            LocationFallbackPolicy.resolveRecoveryMessage(LocationFailureReason.TIMEOUT_OR_UNAVAILABLE)
        )
    }

    @Test
    fun `resolveStatusText returns appropriate region screen text`() {
        assertEquals(
            "위치 권한이 꺼져 있어요",
            LocationFallbackPolicy.resolveStatusText(LocationFailureReason.PERMISSION_DENIED)
        )
        assertEquals(
            "기기 위치 서비스가 꺼져 있어요",
            LocationFallbackPolicy.resolveStatusText(LocationFailureReason.LOCATION_SERVICES_DISABLED)
        )
        assertEquals(
            "현 위치 확인에 실패했어요. 지역을 검색해 보세요.",
            LocationFallbackPolicy.resolveStatusText(LocationFailureReason.TIMEOUT_OR_UNAVAILABLE)
        )
        assertEquals(
            "현 위치를 다시 확인할 수 있어요",
            LocationFallbackPolicy.resolveStatusText(null)
        )
    }
}
