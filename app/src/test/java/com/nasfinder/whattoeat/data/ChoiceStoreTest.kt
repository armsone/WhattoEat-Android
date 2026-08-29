package com.nasfinder.whattoeat.data

import androidx.test.core.app.ApplicationProvider
import com.nasfinder.whattoeat.model.ChoiceRecord
import com.nasfinder.whattoeat.model.FavoriteRecord
import com.nasfinder.whattoeat.model.SituationFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ChoiceStoreTest {

    private lateinit var store: ChoiceStore

    @Before
    fun setUp() {
        store = ChoiceStore(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `choice records are stored newest first and survive round trip`() {
        store.addChoiceRecord(ChoiceRecord(menu = "김밥", restaurantName = "김밥천국", date = 1000L, region = "강남"))
        store.addChoiceRecord(ChoiceRecord(menu = "라면", restaurantName = "라면집", date = 2000L, region = "강남"))

        val records = store.getChoiceRecords()
        assertEquals(2, records.size)
        assertEquals("라면집", records.first().restaurantName)
    }

    @Test
    fun `deleting a choice record removes only the matching entry`() {
        val a = ChoiceRecord(menu = "김밥", restaurantName = "김밥천국", date = 1000L)
        val b = ChoiceRecord(menu = "라면", restaurantName = "라면집", date = 2000L)
        store.addChoiceRecord(a)
        store.addChoiceRecord(b)

        store.deleteChoiceRecord(a)

        val records = store.getChoiceRecords()
        assertEquals(1, records.size)
        assertEquals("라면집", records.first().restaurantName)
    }

    @Test
    fun `toggling favorite twice restores original absent state`() {
        val record = FavoriteRecord(restaurantId = "r1", restaurantName = "식당", category = "한식", date = 1000L)

        val addedResult = store.toggleFavorite(record)
        assertTrue(addedResult)
        assertTrue(store.isFavorite("r1"))

        val removedResult = store.toggleFavorite(record)
        assertFalse(removedResult)
        assertFalse(store.isFavorite("r1"))
    }

    @Test
    fun `region usage ranks by count desc then recency`() {
        store.recordRegionUsage("강남", 37.1, 127.1)
        store.recordRegionUsage("강남", 37.1, 127.1)
        store.recordRegionUsage("판교", 37.2, 127.2)

        val top = store.getTopFrequentRegions(3)
        assertEquals("강남", top.first().name)
        assertEquals(2, top.first().count)
    }

    @Test
    fun `region usage ignores placeholder and blank names`() {
        store.recordRegionUsage("현재 위치", 0.0, 0.0)
        store.recordRegionUsage("지정 지역", 0.0, 0.0)
        store.recordRegionUsage("   ", 0.0, 0.0)

        assertTrue(store.getRegionUsages().isEmpty())
    }

    @Test
    fun `manual location coordinates persist across store instances`() {
        store.manualLatitude = 37.5665
        store.manualLongitude = 126.9780

        val reopened = ChoiceStore(ApplicationProvider.getApplicationContext())
        assertEquals(37.5665, reopened.manualLatitude!!, 0.0001)
        assertEquals(126.9780, reopened.manualLongitude!!, 0.0001)
    }

    @Test
    fun `situation filter persists across store instances`() {
        store.situationFilter = SituationFilter.DESSERT_CAFE

        val reopened = ChoiceStore(ApplicationProvider.getApplicationContext())

        assertEquals(SituationFilter.DESSERT_CAFE, reopened.situationFilter)
    }

    @Test
    fun `current photo metadata survives choice and favorite round trips`() {
        val choice = ChoiceRecord(
            menu = "국밥", restaurantName = "테스트식당", date = 9_001L,
            imageUrl = "https://img.test/a.jpg", photoKind = "categoryExample",
            photoProvider = "openverse", photoSourceUrl = "https://source.test/a",
            photoAttribution = "CC0 · Openverse", photoCreator = "작가",
            photoCreatorUrl = "https://creator.test/a", photoLicense = "cc0",
            photoLicenseUrl = "https://license.test/cc0", photoTitle = "국밥 사진"
        )
        store.addChoiceRecord(choice)
        val loadedChoice = store.getChoiceRecords().first { it.date == 9_001L }
        assertEquals(choice.imageUrl, loadedChoice.imageUrl)
        assertEquals(choice.photoKind, loadedChoice.photoKind)
        assertEquals(choice.photoProvider, loadedChoice.photoProvider)
        assertEquals(choice.photoSourceUrl, loadedChoice.photoSourceUrl)
        assertEquals(choice.photoAttribution, loadedChoice.photoAttribution)
        assertEquals(choice.photoCreator, loadedChoice.photoCreator)
        assertEquals(choice.photoCreatorUrl, loadedChoice.photoCreatorUrl)
        assertEquals(choice.photoLicense, loadedChoice.photoLicense)
        assertEquals(choice.photoLicenseUrl, loadedChoice.photoLicenseUrl)
        assertEquals(choice.photoTitle, loadedChoice.photoTitle)

        val favorite = FavoriteRecord(
            restaurantId = "photo-r1", restaurantName = "테스트식당", category = "한식", date = 9_002L,
            imageUrl = choice.imageUrl, photoKind = choice.photoKind, photoProvider = choice.photoProvider,
            photoSourceUrl = choice.photoSourceUrl, photoAttribution = choice.photoAttribution,
            photoCreator = choice.photoCreator, photoCreatorUrl = choice.photoCreatorUrl,
            photoLicense = choice.photoLicense, photoLicenseUrl = choice.photoLicenseUrl,
            photoTitle = choice.photoTitle
        )
        store.toggleFavorite(favorite)
        val loadedFavorite = store.getFavoriteRecords().first { it.restaurantId == "photo-r1" }
        assertEquals(favorite.imageUrl, loadedFavorite.imageUrl)
        assertEquals(favorite.photoKind, loadedFavorite.photoKind)
        assertEquals(favorite.photoProvider, loadedFavorite.photoProvider)
        assertEquals(favorite.photoSourceUrl, loadedFavorite.photoSourceUrl)
        assertEquals(favorite.photoAttribution, loadedFavorite.photoAttribution)
        assertEquals(favorite.photoCreator, loadedFavorite.photoCreator)
        assertEquals(favorite.photoCreatorUrl, loadedFavorite.photoCreatorUrl)
        assertEquals(favorite.photoLicense, loadedFavorite.photoLicense)
        assertEquals(favorite.photoLicenseUrl, loadedFavorite.photoLicenseUrl)
        assertEquals(favorite.photoTitle, loadedFavorite.photoTitle)
    }

    @Test
    fun `foursquare photo url and metadata are not persisted`() {
        store.addChoiceRecord(
            ChoiceRecord(
                menu = "한식", restaurantName = "구형식당", date = 9_003L,
                imageUrl = "https://img.test/foursquare.jpg", photoProvider = "foursquare",
                photoKind = "restaurantVerified", photoAttribution = "Foursquare"
            )
        )
        val loaded = store.getChoiceRecords().first { it.date == 9_003L }
        assertNull(loaded.imageUrl)
        assertNull(loaded.photoProvider)
        assertNull(loaded.photoKind)
    }

    @Test
    fun `hasRequestedLocationPermission persists across store instances`() {
        assertFalse(store.hasRequestedLocationPermission)
        store.hasRequestedLocationPermission = true
        assertTrue(store.hasRequestedLocationPermission)

        val reopened = ChoiceStore(ApplicationProvider.getApplicationContext())
        assertTrue(reopened.hasRequestedLocationPermission)
    }
}
