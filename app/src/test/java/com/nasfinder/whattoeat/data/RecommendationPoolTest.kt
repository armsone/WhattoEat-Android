package com.nasfinder.whattoeat.data

import com.nasfinder.whattoeat.model.Restaurant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationPoolTest {

    private fun restaurant(id: String, isOpenNow: Boolean?) = Restaurant(
        id = id,
        name = "식당$id",
        category = "한식",
        lat = 0.0,
        lng = 0.0,
        isOpenNow = isOpenNow
    )

    @Test
    fun `excludes only restaurants explicitly closed`() {
        val restaurants = listOf(
            restaurant("open", true),
            restaurant("closed", false),
            restaurant("unknown", null)
        )

        val pool = RecommendationPool.buildPool(restaurants)

        assertEquals(2, pool.size)
        assertTrue(pool.none { it.id == "closed" })
        assertTrue(pool.any { it.id == "open" })
        assertTrue(pool.any { it.id == "unknown" })
    }

    @Test
    fun `caps pool at 13 restaurants`() {
        val restaurants = (1..20).map { restaurant(it.toString(), true) }

        val pool = RecommendationPool.buildPool(restaurants)

        assertEquals(13, pool.size)
    }

    @Test
    fun `empty input yields empty pool`() {
        assertTrue(RecommendationPool.buildPool(emptyList()).isEmpty())
    }
}
