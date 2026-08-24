package com.nasfinder.whattoeat.data

import com.nasfinder.whattoeat.model.Restaurant

/**
 * Pure, order-preserving pool builder used by MainViewModel before shuffling.
 * Kept separate so the `isOpenNow == false` exclusion rule is unit-testable
 * without needing a shuffled/random result.
 */
object RecommendationPool {
    private const val MAX_POOL_SIZE = 13

    fun buildPool(restaurants: List<Restaurant>): List<Restaurant> =
        restaurants.filterNot { it.isOpenNow == false }
            .sortedBy { it.distanceMeters ?: Int.MAX_VALUE }
            .take(MAX_POOL_SIZE)
}

object MenuPolicy {
    private val canonicalMenus = listOf(
        "김밥", "냉면", "돈가스", "초밥", "국밥", "설렁탕", "칼국수",
        "햄버거", "피자", "치킨", "떡볶이", "샤브샤브", "갈비탕",
        "짜장면", "쌀국수", "마라탕", "파스타", "곱창", "삼계탕", "보쌈"
    )

    private val aliasMap = mapOf(
        "돈까스" to "돈가스",
        "자장면" to "짜장면"
    )

    fun resolveMenu(restaurant: Restaurant): String {
        // 1. Curated menus from server
        if (restaurant.curatedMenus.isNotEmpty()) {
            val first = restaurant.curatedMenus.first().trim()
            if (first.isNotEmpty()) {
                return normalizeMenuName(first)
            }
        }

        // 2. Keyword match in name or category
        val combinedText = "${restaurant.name} ${restaurant.category}"
        for (alias in aliasMap.keys) {
            if (combinedText.contains(alias, ignoreCase = true)) {
                return aliasMap[alias]!!
            }
        }
        for (canonical in canonicalMenus) {
            if (combinedText.contains(canonical, ignoreCase = true)) {
                return canonical
            }
        }

        // 3. Final category segment
        val categorySegments = restaurant.category.split(">", ",", "/", " ")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != "음식점" }

        if (categorySegments.isNotEmpty()) {
            val lastSegment = categorySegments.last()
            return normalizeMenuName(lastSegment)
        }

        // 4. Default fallback
        return "오늘의 메뉴"
    }

    private fun normalizeMenuName(name: String): String {
        val trimmed = name.trim()
        return aliasMap[trimmed] ?: trimmed
    }
}
