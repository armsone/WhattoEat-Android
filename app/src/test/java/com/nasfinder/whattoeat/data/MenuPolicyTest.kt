package com.nasfinder.whattoeat.data

import com.nasfinder.whattoeat.model.Restaurant
import org.junit.Assert.assertEquals
import org.junit.Test

class MenuPolicyTest {

    private fun restaurant(
        name: String = "테스트 식당",
        category: String = "한식",
        curatedMenus: List<String> = emptyList()
    ) = Restaurant(
        id = "1",
        name = name,
        category = category,
        lat = 37.5,
        lng = 127.0,
        curatedMenus = curatedMenus
    )

    @Test
    fun `curated menu takes priority over name and category`() {
        val r = restaurant(name = "김밥천국", category = "분식", curatedMenus = listOf("돈까스"))
        assertEquals("돈가스", MenuPolicy.resolveMenu(r))
    }

    @Test
    fun `keyword match in restaurant name resolves canonical menu`() {
        val r = restaurant(name = "명동 칼국수집", category = "한식")
        assertEquals("칼국수", MenuPolicy.resolveMenu(r))
    }

    @Test
    fun `alias normalizes to canonical form`() {
        val r = restaurant(name = "역전 자장면", category = "중식")
        assertEquals("짜장면", MenuPolicy.resolveMenu(r))
    }

    @Test
    fun `falls back to final category segment when no keyword matches`() {
        val r = restaurant(name = "이름없음", category = "음식점 > 카페")
        assertEquals("카페", MenuPolicy.resolveMenu(r))
    }

    @Test
    fun `falls back to default label when nothing resolves`() {
        val r = restaurant(name = "", category = "")
        assertEquals("오늘의 메뉴", MenuPolicy.resolveMenu(r))
    }
}
