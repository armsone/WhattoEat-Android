package com.nasfinder.whattoeat.data

import com.nasfinder.whattoeat.model.Restaurant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class PhotoRefreshPolicyTest {
    private fun restaurant(id: String, photoUrl: String? = null) = Restaurant(
        id = id, name = "식당$id", category = "한식", lat = 37.5, lng = 127.0,
        photoUrl = photoUrl
    )

    @Test
    fun `uses iOS retry delays`() {
        assertEquals(listOf(900L, 1_800L), PhotoRefreshPolicy.retryDelaysMillis)
    }

    @Test
    fun `merge preserves current order and replaces only newly photographed ids`() {
        val a = restaurant("a")
        val b = restaurant("b", "https://img.test/old-b.jpg")
        val c = restaurant("c")
        val latestA = restaurant("a", "https://img.test/new-a.jpg")
        val latestBWithoutPhoto = restaurant("b")

        val merged = PhotoRefreshPolicy.mergePreservingOrder(
            current = listOf(b, a, c),
            refreshed = listOf(latestA, latestBWithoutPhoto)
        )

        assertEquals(listOf("b", "a", "c"), merged.map { it.id })
        assertSame(b, merged[0])
        assertSame(latestA, merged[1])
        assertSame(c, merged[2])
    }
}
