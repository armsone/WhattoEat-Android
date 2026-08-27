package com.nasfinder.whattoeat.data

import com.nasfinder.whattoeat.model.Restaurant
import com.nasfinder.whattoeat.model.SituationFilter

/**
 * Pure, order-preserving pool builder used by MainViewModel before shuffling.
 * Kept separate so the `isOpenNow == false` exclusion and SituationFilter
 * rules are unit-testable without needing a shuffled/random result.
 */
object RecommendationPool {
    private const val MAX_POOL_SIZE = 13

    fun buildPool(
        restaurants: List<Restaurant>,
        filter: SituationFilter = SituationFilter.ALL
    ): List<Restaurant> {
        val openRestaurants = restaurants.filterNot { it.isOpenNow == false }
            .sortedBy { it.distanceMeters ?: Int.MAX_VALUE }

        if (filter == SituationFilter.ALL) {
            return openRestaurants.take(MAX_POOL_SIZE)
        }

        val filtered = openRestaurants.filter { SituationFilterPolicy.matches(it, filter) }
        return filtered.take(MAX_POOL_SIZE)
    }
}

object SituationFilterPolicy {
    private val heartyKeywords = listOf(
        "국밥", "설렁탕", "갈비탕", "삼계탕", "샤브샤브", "칼국수", "냉면",
        "비빔밥", "백반", "찌개", "전골", "덮밥", "쌀국수", "짜장면", "자장면",
        "마라탕", "파스타"
    )

    private val lightKeywords = listOf(
        "김밥", "떡볶이", "토스트", "샌드위치", "샐러드", "라면", "우동", "분식"
    )

    private val fastFoodKeywords = listOf(
        "햄버거", "버거", "피자", "치킨", "핫도그"
    )

    private val dessertKeywords = listOf(
        "커피", "카페", "디저트", "케이크", "아이스크림", "빙수", "빵", "베이커리",
        "와플", "도넛"
    )

    private val gatheringKeywords = listOf(
        "곱창", "보쌈", "족발", "샤브샤브", "삼겹살", "갈비", "고기", "전골",
        "회", "치킨", "피자"
    )

    private val lateNightKeywords = listOf(
        "치킨", "피자", "떡볶이", "곱창", "보쌈", "족발", "닭발", "라면"
    )

    fun matches(restaurant: Restaurant, filter: SituationFilter): Boolean {
        if (filter == SituationFilter.ALL) return true

        val supportedMenus = MenuPolicy.supportedMenus(restaurant)
        fun matchesAny(keywords: List<String>): Boolean =
            supportedMenus.any { menu -> keywords.any { menu.contains(it, ignoreCase = true) } }

        return when (filter) {
            SituationFilter.ALL -> true
            SituationFilter.HEARTY_MEAL -> matchesAny(heartyKeywords)
            SituationFilter.LIGHT_MEAL -> matchesAny(lightKeywords)
            SituationFilter.FAST_FOOD -> matchesAny(fastFoodKeywords)
            SituationFilter.DESSERT_CAFE -> matchesAny(dessertKeywords)
            SituationFilter.GATHERING_DINING -> matchesAny(gatheringKeywords)
            SituationFilter.LATE_NIGHT -> matchesAny(lateNightKeywords)
        }
    }
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

    fun supportedMenus(restaurant: Restaurant): List<String> {
        val found = restaurant.curatedMenus
            .map(::normalizeMenuName)
            .filter { it.isNotEmpty() }
            .toMutableList()
        val finalCategory = restaurant.category.substringAfterLast(">").trim()
        val evidence = listOf(restaurant.name, finalCategory)

        for ((alias, canonical) in aliasMap) {
            if (canonical !in found && evidence.any { it.contains(alias, ignoreCase = true) }) {
                found += canonical
            }
        }
        for (canonical in canonicalMenus) {
            if (canonical !in found && evidence.any { it.contains(canonical, ignoreCase = true) }) {
                found += canonical
            }
        }
        return found
    }

    fun resolveMenu(restaurant: Restaurant): String {
        supportedMenus(restaurant).firstOrNull()?.let { return it }

        // Unverified broad categories remain a display fallback, never situation-filter evidence.
        val categorySegments = restaurant.category.split(">", ",", "/", " ")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != "음식점" }

        if (categorySegments.isNotEmpty()) {
            val lastSegment = categorySegments.last()
            return normalizeMenuName(lastSegment)
        }

        // Default fallback
        return "오늘의 메뉴"
    }

    private fun normalizeMenuName(name: String): String {
        val trimmed = name.trim()
        return aliasMap[trimmed] ?: trimmed
    }
}
