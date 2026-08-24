package com.nasfinder.whattoeat.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageFallbackParityTest {
    @Test
    fun `current five matchup fixtures match iOS fallback sequence`() {
        val fixtures = listOf(
            Triple("matchup-1", "음식점 > 분식", "김밥"),
            Triple("matchup-2", "음식점 > 한식", "한식"),
            Triple("matchup-3", "음식점 > 한식", "불고기"),
            Triple("matchup-4", "음식점 > 냉면", "냉면"),
            Triple("matchup-5", "음식점 > 순대", "순대")
        )
        val used = mutableSetOf<ImageLoader.FallbackType>()
        val actual = fixtures.map { (id, category, menu) ->
            ImageLoader.resolveFallbackType(category, menu, id, used).also(used::add)
        }

        assertEquals(
            listOf(
                ImageLoader.FallbackType.FOOD_JJAMPPONG,
                ImageLoader.FallbackType.FOOD_SUSHI,
                ImageLoader.FallbackType.FOOD_MAIN,
                ImageLoader.FallbackType.FOOD_BIBIMBAP,
                ImageLoader.FallbackType.FOOD_SIDE1
            ),
            actual
        )
    }
}
