package com.nasfinder.whattoeat.data

import android.content.Context
import android.content.SharedPreferences
import com.nasfinder.whattoeat.model.ChoiceRecord
import com.nasfinder.whattoeat.model.FavoriteRecord
import com.nasfinder.whattoeat.model.LocationMode
import com.nasfinder.whattoeat.model.MapProvider
import com.nasfinder.whattoeat.model.RegionUsage
import com.nasfinder.whattoeat.model.ReminderLeadTime
import org.json.JSONArray
import org.json.JSONObject

class ChoiceStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("whattoeat_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CHOICE_RECORDS = "choiceRecords.v1"
        private const val KEY_FAVORITE_RECORDS = "favoriteRecords.v1"
        private const val KEY_REGION_USAGE = "regionUsage.v1"
        private const val KEY_LOCATION_MODE = "locationMode"
        private const val KEY_MANUAL_REGION_TEXT = "manualRegionText"
        private const val KEY_MANUAL_RESOLVED_NAME = "manualResolvedName"
        private const val KEY_MANUAL_LATITUDE = "manualLatitude"
        private const val KEY_MANUAL_LONGITUDE = "manualLongitude"
        private const val KEY_LAST_TOP_MENU = "lastTopMenu"
        private const val KEY_LUNCH_NOTIFY_ENABLED = "lunchNotifyEnabled"
        private const val KEY_LUNCH_HOUR = "lunchHour"
        private const val KEY_LUNCH_MINUTE = "lunchMinute"
        private const val KEY_LUNCH_LEAD_MINUTES = "lunchLeadMinutes"
        private const val KEY_MAP_PROVIDER = "mapProvider"
    }

    // --- Settings & Location Preferences ---

    var locationMode: LocationMode
        get() {
            val modeStr = prefs.getString(KEY_LOCATION_MODE, "auto")
            return if (modeStr == "manual") LocationMode.MANUAL else LocationMode.AUTO
        }
        set(value) {
            prefs.edit().putString(KEY_LOCATION_MODE, if (value == LocationMode.MANUAL) "manual" else "auto").apply()
        }

    var manualRegionText: String
        get() = prefs.getString(KEY_MANUAL_REGION_TEXT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MANUAL_REGION_TEXT, value).apply()

    var manualResolvedName: String
        get() = prefs.getString(KEY_MANUAL_RESOLVED_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MANUAL_RESOLVED_NAME, value).apply()

    var manualLatitude: Double?
        get() {
            val bits = prefs.getLong(KEY_MANUAL_LATITUDE, java.lang.Double.doubleToRawLongBits(Double.NaN))
            val value = java.lang.Double.longBitsToDouble(bits)
            return if (value.isNaN()) null else value
        }
        set(value) {
            val editor = prefs.edit()
            if (value == null) {
                editor.remove(KEY_MANUAL_LATITUDE)
            } else {
                editor.putLong(KEY_MANUAL_LATITUDE, java.lang.Double.doubleToRawLongBits(value))
            }
            editor.apply()
        }

    var manualLongitude: Double?
        get() {
            val bits = prefs.getLong(KEY_MANUAL_LONGITUDE, java.lang.Double.doubleToRawLongBits(Double.NaN))
            val value = java.lang.Double.longBitsToDouble(bits)
            return if (value.isNaN()) null else value
        }
        set(value) {
            val editor = prefs.edit()
            if (value == null) {
                editor.remove(KEY_MANUAL_LONGITUDE)
            } else {
                editor.putLong(KEY_MANUAL_LONGITUDE, java.lang.Double.doubleToRawLongBits(value))
            }
            editor.apply()
        }

    var lastTopMenu: String
        get() = prefs.getString(KEY_LAST_TOP_MENU, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_TOP_MENU, value).apply()

    var lunchNotifyEnabled: Boolean
        get() = prefs.getBoolean(KEY_LUNCH_NOTIFY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_LUNCH_NOTIFY_ENABLED, value).apply()

    var lunchHour: Int
        get() = prefs.getInt(KEY_LUNCH_HOUR, 12)
        set(value) = prefs.edit().putInt(KEY_LUNCH_HOUR, value).apply()

    var lunchMinute: Int
        get() = prefs.getInt(KEY_LUNCH_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_LUNCH_MINUTE, value).apply()

    var lunchLeadMinutes: Int
        get() = prefs.getInt(KEY_LUNCH_LEAD_MINUTES, 5)
        set(value) = prefs.edit().putInt(KEY_LUNCH_LEAD_MINUTES, value).apply()

    var mapProvider: MapProvider
        get() {
            val name = prefs.getString(KEY_MAP_PROVIDER, "naver")?.lowercase()
            return when (name) {
                "naver" -> MapProvider.NAVER
                "kakao" -> MapProvider.KAKAO
                "google" -> MapProvider.GOOGLE
                "apple" -> MapProvider.APPLE
                else -> MapProvider.NAVER
            }
        }
        set(value) {
            val str = when (value) {
                MapProvider.NAVER -> "naver"
                MapProvider.KAKAO -> "kakao"
                MapProvider.GOOGLE -> "google"
                MapProvider.APPLE -> "apple"
            }
            prefs.edit().putString(KEY_MAP_PROVIDER, str).apply()
        }

    // --- Choice Records ---

    fun getChoiceRecords(): List<ChoiceRecord> {
        val jsonStr = prefs.getString(KEY_CHOICE_RECORDS, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<ChoiceRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChoiceRecord(
                        menu = obj.getString("menu"),
                        restaurantName = obj.getString("restaurantName"),
                        date = obj.getLong("date"),
                        region = obj.optString("region").takeIf { it.isNotEmpty() },
                        imageUrl = obj.optString("imageUrl").takeIf { it.isNotEmpty() },
                        category = obj.optString("category").takeIf { it.isNotEmpty() },
                        photoKind = photoValue(obj, "photoKind", "isCategoryExample")?.let {
                            if (it == "true") "categoryExample" else it
                        },
                        photoProvider = photoValue(obj, "photoProvider", "provider"),
                        photoSourceUrl = photoValue(obj, "photoSourceURL", "sourceUrl"),
                        photoAttribution = photoValue(obj, "photoAttribution", null),
                        photoCreator = photoValue(obj, "photoCreator", null),
                        photoCreatorUrl = photoValue(obj, "photoCreatorURL", "authorUrl"),
                        photoLicense = photoValue(obj, "photoLicense", "terms"),
                        photoLicenseUrl = photoValue(obj, "photoLicenseURL", null),
                        photoTitle = photoValue(obj, "photoTitle", "title"),
                        restaurantId = obj.optString("restaurantId").takeIf { it.isNotEmpty() },
                        lat = if (obj.has("lat")) obj.getDouble("lat") else null,
                        lng = if (obj.has("lng")) obj.getDouble("lng") else null,
                        address = obj.optString("address").takeIf { it.isNotEmpty() }
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addChoiceRecord(record: ChoiceRecord) {
        val current = getChoiceRecords().toMutableList()
        current.add(0, record) // newest first
        saveChoiceRecords(current)
    }

    fun deleteChoiceRecord(record: ChoiceRecord) {
        val current = getChoiceRecords().filterNot {
            it.menu == record.menu && it.restaurantName == record.restaurantName && it.date == record.date
        }
        saveChoiceRecords(current)
    }

    private fun saveChoiceRecords(list: List<ChoiceRecord>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("menu", item.menu)
                put("restaurantName", item.restaurantName)
                put("date", item.date)
                put("region", item.region ?: "")
                put("imageUrl", item.imageUrl ?: "")
                put("category", item.category ?: "")
                item.restaurantId?.let { put("restaurantId", it) }
                item.lat?.let { put("lat", it) }
                item.lng?.let { put("lng", it) }
                item.address?.let { put("address", it) }
                putPhotoInformation(item.photoProvider, item.photoKind, item.photoSourceUrl,
                    item.photoAttribution, item.photoCreator, item.photoCreatorUrl,
                    item.photoLicense, item.photoLicenseUrl, item.photoTitle)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CHOICE_RECORDS, array.toString()).apply()
    }

    // --- Favorite Records ---

    fun getFavoriteRecords(): List<FavoriteRecord> {
        val jsonStr = prefs.getString(KEY_FAVORITE_RECORDS, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<FavoriteRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val menus = mutableListOf<String>()
                val menusArray = obj.optJSONArray("curatedMenus")
                if (menusArray != null) {
                    for (j in 0 until menusArray.length()) {
                        menus.add(menusArray.getString(j))
                    }
                }
                list.add(
                    FavoriteRecord(
                        restaurantId = obj.getString("restaurantId"),
                        restaurantName = obj.getString("restaurantName"),
                        category = obj.getString("category"),
                        region = obj.optString("region").takeIf { it.isNotEmpty() },
                        imageUrl = obj.optString("imageUrl").takeIf { it.isNotEmpty() },
                        photoKind = photoValue(obj, "photoKind", "isCategoryExample")?.let {
                            if (it == "true") "categoryExample" else it
                        },
                        photoProvider = photoValue(obj, "photoProvider", "provider"),
                        photoSourceUrl = photoValue(obj, "photoSourceURL", "sourceUrl"),
                        photoAttribution = photoValue(obj, "photoAttribution", null),
                        photoCreator = photoValue(obj, "photoCreator", null),
                        photoCreatorUrl = photoValue(obj, "photoCreatorURL", "authorUrl"),
                        photoLicense = photoValue(obj, "photoLicense", "terms"),
                        photoLicenseUrl = photoValue(obj, "photoLicenseURL", null),
                        photoTitle = photoValue(obj, "photoTitle", "title"),
                        date = obj.getLong("date"),
                        lat = if (obj.has("lat")) obj.getDouble("lat") else null,
                        lng = if (obj.has("lng")) obj.getDouble("lng") else null,
                        address = obj.optString("address").takeIf { it.isNotEmpty() },
                        curatedMenus = menus,
                        phone = obj.optString("phone").takeIf { it.isNotEmpty() },
                        placeUrl = obj.optString("placeUrl").takeIf { it.isNotEmpty() }
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isFavorite(restaurantId: String): Boolean {
        return getFavoriteRecords().any { it.restaurantId == restaurantId }
    }

    fun toggleFavorite(record: FavoriteRecord): Boolean {
        val current = getFavoriteRecords().toMutableList()
        val exists = current.any { it.restaurantId == record.restaurantId }
        if (exists) {
            current.removeAll { it.restaurantId == record.restaurantId }
            saveFavoriteRecords(current)
            return false
        } else {
            current.add(0, record)
            saveFavoriteRecords(current)
            return true
        }
    }

    fun deleteFavorite(restaurantId: String) {
        val current = getFavoriteRecords().filterNot { it.restaurantId == restaurantId }
        saveFavoriteRecords(current)
    }

    private fun saveFavoriteRecords(list: List<FavoriteRecord>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("restaurantId", item.restaurantId)
                put("restaurantName", item.restaurantName)
                put("category", item.category)
                put("region", item.region ?: "")
                put("imageUrl", item.imageUrl ?: "")
                put("date", item.date)
                item.lat?.let { put("lat", it) }
                item.lng?.let { put("lng", it) }
                item.address?.let { put("address", it) }
                item.phone?.let { put("phone", it) }
                item.placeUrl?.let { put("placeUrl", it) }
                if (item.curatedMenus.isNotEmpty()) {
                    val mArray = JSONArray()
                    item.curatedMenus.forEach { mArray.put(it) }
                    put("curatedMenus", mArray)
                }
                putPhotoInformation(item.photoProvider, item.photoKind, item.photoSourceUrl,
                    item.photoAttribution, item.photoCreator, item.photoCreatorUrl,
                    item.photoLicense, item.photoLicenseUrl, item.photoTitle)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_FAVORITE_RECORDS, array.toString()).apply()
    }

    // --- Region Usage ---

    fun getRegionUsages(): List<RegionUsage> {
        val jsonStr = prefs.getString(KEY_REGION_USAGE, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<RegionUsage>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RegionUsage(
                        name = obj.getString("name"),
                        count = obj.getInt("count"),
                        lastUsed = obj.getLong("lastUsed"),
                        lat = obj.getDouble("lat"),
                        lng = obj.getDouble("lng")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun recordRegionUsage(name: String, lat: Double, lng: Double) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed == "현재 위치" || trimmed == "지정 지역") {
            return
        }
        val current = getRegionUsages().toMutableList()
        val index = current.indexOfFirst { it.name == trimmed }
        val now = System.currentTimeMillis()
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(
                count = existing.count + 1,
                lastUsed = now,
                lat = lat,
                lng = lng
            )
        } else {
            current.add(
                RegionUsage(
                    name = trimmed,
                    count = 1,
                    lastUsed = now,
                    lat = lat,
                    lng = lng
                )
            )
        }
        saveRegionUsages(current)
    }

    fun getTopFrequentRegions(limit: Int = 3): List<RegionUsage> {
        return getRegionUsages()
            .sortedWith(compareByDescending<RegionUsage> { it.count }.thenByDescending { it.lastUsed })
            .take(limit)
    }

    private fun saveRegionUsages(list: List<RegionUsage>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("name", item.name)
                put("count", item.count)
                put("lastUsed", item.lastUsed)
                put("lat", item.lat)
                put("lng", item.lng)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_REGION_USAGE, array.toString()).apply()
    }

    private fun photoValue(obj: JSONObject, directKey: String, legacyKey: String?): String? {
        obj.optString(directKey).takeIf { it.isNotEmpty() }?.let { return it }
        val legacy = obj.optJSONObject("photoEvidence") ?: return null
        if (legacyKey == null || !legacy.has(legacyKey) || legacy.isNull(legacyKey)) return null
        return legacy.get(legacyKey).toString().takeIf { it.isNotEmpty() }
    }

    private fun JSONObject.putPhotoInformation(
        provider: String?, kind: String?, sourceUrl: String?, attribution: String?,
        creator: String?, creatorUrl: String?, license: String?, licenseUrl: String?, title: String?
    ) {
        val persist = provider?.lowercase() != "foursquare"
        if (!persist) {
            put("imageUrl", "")
            return
        }
        kind?.let { put("photoKind", it) }
        provider?.let { put("photoProvider", it) }
        sourceUrl?.let { put("photoSourceURL", it) }
        attribution?.let { put("photoAttribution", it) }
        creator?.let { put("photoCreator", it) }
        creatorUrl?.let { put("photoCreatorURL", it) }
        license?.let { put("photoLicense", it) }
        licenseUrl?.let { put("photoLicenseURL", it) }
        title?.let { put("photoTitle", it) }
    }
}
