package com.nasfinder.whattoeat.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ApiClientTest {
    @Test
    fun `decodes current iOS direct photo contract without losing metadata`() {
        val response = ApiClient.parseRestaurantsResponse(
            """
            {
              "restaurants": [{
                "id": "k1", "name": "테스트식당", "category": "음식점 > 한식",
                "latitude": 37.5, "longitude": 127.0, "distanceMeters": 42,
                "address": "지번", "roadAddress": "도로명", "phone": "02-123-4567",
                "placeURL": "https://place.test/k1", "isOpenNow": true,
                "curatedMenus": ["국밥"],
                "photoURL": "https://img.test/a.jpg", "photoKind": "categoryExample",
                "photoProvider": "openverse", "photoSourceURL": "https://source.test/a",
                "photoAttribution": "사진: 작가 · CC0 · Openverse", "photoCreator": "작가",
                "photoCreatorURL": "https://creator.test/a", "photoLicense": "cc0",
                "photoLicenseURL": "https://license.test/cc0", "photoTitle": "국밥 사진",
                "photoMatchEvidence": {
                  "exactNormalizedName": true, "addressMatch": false, "distanceMeters": 31,
                  "phoneMatch": true, "previouslyVerifiedPlaceId": false
                }
              }],
              "source": "kakao-local-category-FD6"
            }
            """.trimIndent()
        )

        val restaurant = response.restaurants.single()
        assertEquals(42, restaurant.distanceMeters)
        assertEquals("https://img.test/a.jpg", restaurant.photoUrl)
        assertEquals("categoryExample", restaurant.photoKind)
        assertEquals("openverse", restaurant.photoProvider)
        assertEquals("https://source.test/a", restaurant.photoSourceUrl)
        assertEquals("작가", restaurant.photoCreator)
        assertEquals("cc0", restaurant.photoLicense)
        assertEquals("국밥 사진", restaurant.photoTitle)
        assertTrue(restaurant.photoMatchEvidence?.exactNormalizedName == true)
        assertFalse(restaurant.photoMatchEvidence?.addressMatch ?: true)
        assertEquals(31, restaurant.photoMatchEvidence?.distanceMeters)
    }

    @Test
    fun `does not accept legacy photo aliases that current iOS decoder rejects`() {
        val response = ApiClient.parseRestaurantsResponse(
            """
            {"restaurants":[{
              "id":"k1","name":"식당","category":"음식점 > 한식",
              "latitude":37.5,"longitude":127.0,
              "photoUrl":"https://img.test/legacy.jpg",
              "photo":{"url":"https://img.test/nested.jpg"},
              "distance":99
            }]}
            """.trimIndent()
        )

        val restaurant = response.restaurants.single()
        assertNull(restaurant.photoUrl)
        assertNull(restaurant.distanceMeters)
    }
}
