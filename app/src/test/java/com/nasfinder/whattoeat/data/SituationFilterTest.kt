package com.nasfinder.whattoeat.data

import com.nasfinder.whattoeat.model.Restaurant
import com.nasfinder.whattoeat.model.SituationFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SituationFilterTest {

    private fun restaurant(
        id: String,
        name: String,
        category: String,
        curatedMenus: List<String> = emptyList(),
        isOpenNow: Boolean? = true,
        distanceMeters: Int? = 100
    ) = Restaurant(
        id = id,
        name = name,
        category = category,
        lat = 37.5,
        lng = 127.0,
        distanceMeters = distanceMeters,
        curatedMenus = curatedMenus,
        isOpenNow = isOpenNow
    )

    @Test
    fun `SituationFilter fromKey resolves English and Korean keys properly`() {
        assertEquals(SituationFilter.ALL, SituationFilter.fromKey("all"))
        assertEquals(SituationFilter.ALL, SituationFilter.fromKey("전체"))
        assertEquals(SituationFilter.ALL, SituationFilter.fromKey(null))
        assertEquals(SituationFilter.ALL, SituationFilter.fromKey("invalid_key"))

        assertEquals(SituationFilter.HEARTY_MEAL, SituationFilter.fromKey("hearty_meal"))
        assertEquals(SituationFilter.HEARTY_MEAL, SituationFilter.fromKey("hearty"))
        assertEquals(SituationFilter.HEARTY_MEAL, SituationFilter.fromKey("든든한 식사"))

        assertEquals(SituationFilter.LIGHT_MEAL, SituationFilter.fromKey("light_meal"))
        assertEquals(SituationFilter.LIGHT_MEAL, SituationFilter.fromKey("simple"))
        assertEquals(SituationFilter.LIGHT_MEAL, SituationFilter.fromKey("간단하게"))

        assertEquals(SituationFilter.FAST_FOOD, SituationFilter.fromKey("fast_food"))
        assertEquals(SituationFilter.FAST_FOOD, SituationFilter.fromKey("fastfood"))
        assertEquals(SituationFilter.FAST_FOOD, SituationFilter.fromKey("패스트푸드"))

        assertEquals(SituationFilter.DESSERT_CAFE, SituationFilter.fromKey("dessert_cafe"))
        assertEquals(SituationFilter.DESSERT_CAFE, SituationFilter.fromKey("cafe"))
        assertEquals(SituationFilter.DESSERT_CAFE, SituationFilter.fromKey("디저트·카페"))

        assertEquals(SituationFilter.GATHERING_DINING, SituationFilter.fromKey("gathering_dining"))
        assertEquals(SituationFilter.GATHERING_DINING, SituationFilter.fromKey("gathering"))
        assertEquals(SituationFilter.GATHERING_DINING, SituationFilter.fromKey("회식·모임"))

        assertEquals(SituationFilter.LATE_NIGHT, SituationFilter.fromKey("late_night"))
        assertEquals(SituationFilter.LATE_NIGHT, SituationFilter.fromKey("night"))
        assertEquals(SituationFilter.LATE_NIGHT, SituationFilter.fromKey("야식"))
    }

    @Test
    fun `ALL matches every restaurant`() {
        val r1 = restaurant("1", "바람난김밥카페", "음식점 > 분식")
        val r2 = restaurant("2", "설월식당", "음식점 > 한식")
        val r3 = restaurant("3", "스타벅스", "음식점 > 카페")

        assertTrue(SituationFilterPolicy.matches(r1, SituationFilter.ALL))
        assertTrue(SituationFilterPolicy.matches(r2, SituationFilter.ALL))
        assertTrue(SituationFilterPolicy.matches(r3, SituationFilter.ALL))
    }

    @Test
    fun `HEARTY_MEAL matches hearty Korean, Japanese, Chinese, Western, Meat dishes`() {
        val soup = restaurant("1", "원조 설렁탕", "음식점 > 한식")
        val stew = restaurant("2", "맛있는 찌개마을", "음식점 > 한식", listOf("찌개"))
        val riceBowl = restaurant("3", "일식 덮밥집", "음식점 > 일식", listOf("덮밥"))
        val pasta = restaurant("4", "이탈리안 파스타", "음식점 > 양식")
        val cafe = restaurant("5", "달콤한 디저트", "음식점 > 카페")

        assertTrue(SituationFilterPolicy.matches(soup, SituationFilter.HEARTY_MEAL))
        assertTrue(SituationFilterPolicy.matches(stew, SituationFilter.HEARTY_MEAL))
        assertTrue(SituationFilterPolicy.matches(riceBowl, SituationFilter.HEARTY_MEAL))
        assertTrue(SituationFilterPolicy.matches(pasta, SituationFilter.HEARTY_MEAL))
        assertFalse(SituationFilterPolicy.matches(cafe, SituationFilter.HEARTY_MEAL))
    }

    @Test
    fun `LIGHT_MEAL matches snacks, kimbap, noodles, toast`() {
        val kimbap = restaurant("1", "김밥천국", "음식점 > 분식", listOf("김밥"))
        val noodle = restaurant("2", "명동 우동", "음식점 > 일식", listOf("우동"))
        val toast = restaurant("3", "이삭토스트", "음식점 > 간식", listOf("토스트"))
        val bbq = restaurant("4", "마장동 한우 삼겹살", "음식점 > 고기구이")

        assertTrue(SituationFilterPolicy.matches(kimbap, SituationFilter.LIGHT_MEAL))
        assertTrue(SituationFilterPolicy.matches(noodle, SituationFilter.LIGHT_MEAL))
        assertTrue(SituationFilterPolicy.matches(toast, SituationFilter.LIGHT_MEAL))
        assertFalse(SituationFilterPolicy.matches(bbq, SituationFilter.LIGHT_MEAL))
    }

    @Test
    fun `FAST_FOOD matches burger, pizza, chicken`() {
        val burger = restaurant("1", "수제버거 웍스", "음식점 > 패스트푸드", listOf("햄버거"))
        val pizza = restaurant("2", "도미노피자", "음식점 > 피자")
        val chicken = restaurant("3", "교촌치킨", "음식점 > 치킨")
        val soup = restaurant("4", "나주곰탕", "음식점 > 한식")

        assertTrue(SituationFilterPolicy.matches(burger, SituationFilter.FAST_FOOD))
        assertTrue(SituationFilterPolicy.matches(pizza, SituationFilter.FAST_FOOD))
        assertTrue(SituationFilterPolicy.matches(chicken, SituationFilter.FAST_FOOD))
        assertFalse(SituationFilterPolicy.matches(soup, SituationFilter.FAST_FOOD))
    }

    @Test
    fun `DESSERT_CAFE matches cafe, bakery, dessert`() {
        val cafe = restaurant("1", "블루보틀 커피", "음식점 > 카페", listOf("커피"))
        val bakery = restaurant("2", "성심당 베이커리", "음식점 > 제과", listOf("베이커리"))
        val meat = restaurant("3", "신촌 갈비탕", "음식점 > 한식")

        assertTrue(SituationFilterPolicy.matches(cafe, SituationFilter.DESSERT_CAFE))
        assertTrue(SituationFilterPolicy.matches(bakery, SituationFilter.DESSERT_CAFE))
        assertFalse(SituationFilterPolicy.matches(meat, SituationFilter.DESSERT_CAFE))
    }

    @Test
    fun `GATHERING_DINING matches meat BBQ, hot pot, seafood, pub`() {
        val bbq = restaurant("1", "하남돼지집 삼겹살", "음식점 > 고기구이", listOf("삼겹살"))
        val gopchang = restaurant("2", "원조 곱창구이", "음식점 > 곱창")
        val sharedPlate = restaurant("3", "청담 모임식당", "음식점 > 한식", listOf("보쌈"))
        val toast = restaurant("4", "토스트하우스", "음식점 > 간식")

        assertTrue(SituationFilterPolicy.matches(bbq, SituationFilter.GATHERING_DINING))
        assertTrue(SituationFilterPolicy.matches(gopchang, SituationFilter.GATHERING_DINING))
        assertTrue(SituationFilterPolicy.matches(sharedPlate, SituationFilter.GATHERING_DINING))
        assertFalse(SituationFilterPolicy.matches(toast, SituationFilter.GATHERING_DINING))
    }

    @Test
    fun `LATE_NIGHT matches chicken, feet, jokbal, late night snacks`() {
        val chicken = restaurant("1", "황금올리브 치킨", "음식점 > 치킨")
        val jokbal = restaurant("2", "장충동 족발보쌈", "음식점 > 족발")
        val ramen = restaurant("3", "야간 포차 라면", "음식점 > 포차", listOf("라면"))

        assertTrue(SituationFilterPolicy.matches(chicken, SituationFilter.LATE_NIGHT))
        assertTrue(SituationFilterPolicy.matches(jokbal, SituationFilter.LATE_NIGHT))
        assertTrue(SituationFilterPolicy.matches(ramen, SituationFilter.LATE_NIGHT))
    }

    @Test
    fun `RecommendationPool buildPool filters deterministically and excludes closed restaurants`() {
        val list = listOf(
            restaurant("1", "원조설렁탕", "음식점 > 한식", isOpenNow = true, distanceMeters = 100),
            restaurant("2", "명동칼국수", "음식점 > 분식", isOpenNow = false, distanceMeters = 50),
            restaurant("3", "스타벅스", "음식점 > 카페", isOpenNow = true, distanceMeters = 200),
            restaurant("4", "김밥천국", "음식점 > 분식", isOpenNow = true, distanceMeters = 150)
        )

        val heartyPool = RecommendationPool.buildPool(list, SituationFilter.HEARTY_MEAL)
        assertEquals(1, heartyPool.size)
        assertEquals("1", heartyPool.first().id)

        val lightPool = RecommendationPool.buildPool(list, SituationFilter.LIGHT_MEAL)
        assertEquals(1, lightPool.size)
        assertEquals("4", lightPool.first().id) // "2" is closed, so only "4" remains

        val allPool = RecommendationPool.buildPool(list, SituationFilter.ALL)
        assertEquals(3, allPool.size) // 3 open restaurants
    }

    @Test
    fun `RecommendationPool returns empty list when no candidate matches category`() {
        val list = listOf(
            restaurant("1", "원조설렁탕", "음식점 > 한식", isOpenNow = true),
            restaurant("2", "김밥천국", "음식점 > 분식", isOpenNow = true)
        )

        val fastFoodPool = RecommendationPool.buildPool(list, SituationFilter.FAST_FOOD)
        assertTrue(fastFoodPool.isEmpty())
    }
}
