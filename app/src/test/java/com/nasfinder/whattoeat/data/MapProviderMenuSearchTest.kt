package com.nasfinder.whattoeat.data

import com.nasfinder.whattoeat.model.MapProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.URLEncoder

class MapProviderMenuSearchTest {

    @Test
    fun `buildMenuSearchQuery includes valid region when available`() {
        assertEquals("칼국수 강남", MapProviderHelper.buildMenuSearchQuery("칼국수", "강남"))
        assertEquals("김밥 초월읍", MapProviderHelper.buildMenuSearchQuery("김밥", "초월읍"))
        assertEquals("돈가스 판교", MapProviderHelper.buildMenuSearchQuery("돈가스", "판교"))
    }

    @Test
    fun `buildMenuSearchQuery ignores placeholders and blanks`() {
        assertEquals("칼국수", MapProviderHelper.buildMenuSearchQuery("칼국수", "현 위치"))
        assertEquals("칼국수", MapProviderHelper.buildMenuSearchQuery("칼국수", "지정 지역"))
        assertEquals("칼국수", MapProviderHelper.buildMenuSearchQuery("칼국수", "지역 다시 선택"))
        assertEquals("칼국수", MapProviderHelper.buildMenuSearchQuery("칼국수", null))
        assertEquals("칼국수", MapProviderHelper.buildMenuSearchQuery("칼국수", "   "))
    }

    @Test
    fun `buildMenuSearchQuery provides fallback when menu is empty`() {
        assertEquals("맛집", MapProviderHelper.buildMenuSearchQuery("", null))
        assertEquals("맛집 강남", MapProviderHelper.buildMenuSearchQuery("", "강남"))
    }

    @Test
    fun `native search URI follows each supported provider contract`() {
        val query = URLEncoder.encode("칼국수 강남", "UTF-8")

        assertEquals(
            "nmap://search?query=$query&appname=com.nasfinder.whattoeat",
            MapProviderHelper.buildMenuSearchUri(MapProvider.NAVER, "칼국수", "강남")
        )
        assertEquals(
            "kakaomap://search?q=$query",
            MapProviderHelper.buildMenuSearchUri(MapProvider.KAKAO, "칼국수", "강남")
        )
        assertEquals(
            "geo:37.5,127.0?q=$query",
            MapProviderHelper.buildMenuSearchUri(
                MapProvider.GOOGLE,
                "칼국수",
                "강남",
                lat = 37.5,
                lng = 127.0
            )
        )
        assertNull(MapProviderHelper.buildMenuSearchUri(MapProvider.APPLE, "칼국수", "강남"))
    }
}
