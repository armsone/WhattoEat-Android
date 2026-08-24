package com.nasfinder.whattoeat.data

import com.nasfinder.whattoeat.BuildConfig
import com.nasfinder.whattoeat.model.PhotoMatchEvidence
import com.nasfinder.whattoeat.model.Restaurant
import com.nasfinder.whattoeat.model.RestaurantsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ApiException(override val message: String) : Exception(message)

object ApiClient {
    private const val TIMEOUT_MS = 15000

    suspend fun fetchRestaurants(latitude: Double, longitude: Double): RestaurantsResponse =
        withContext(Dispatchers.IO) {
            val baseUrl = BuildConfig.API_BASE_URL.trim()
            if (baseUrl.isEmpty() || baseUrl == "REPLACE-ME" || !baseUrl.startsWith("https://")) {
                throw ApiException("백엔드 주소가 설정되지 않았습니다. Info.plist의 APIBaseURL 값을 배포한 HTTPS 서버 주소로 바꿔 주세요.")
            }

            val fullUrl = "$baseUrl/api/restaurants?latitude=$latitude&longitude=$longitude"
            var connection: HttpsURLConnection? = null
            try {
                val url = URL(fullUrl)
                connection = (url.openConnection() as? HttpsURLConnection)
                    ?: throw ApiException("서버에 연결하지 못했습니다. 네트워크 상태를 확인해 주세요.")

                connection.requestMethod = "GET"
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw ApiException("서버 응답 오류($responseCode)가 발생했습니다. 잠시 후 다시 시도해 주세요.")
                }

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonString = reader.use { it.readText() }

                parseRestaurantsResponse(jsonString)
            } catch (e: ApiException) {
                throw e
            } catch (e: org.json.JSONException) {
                throw ApiException("서버 응답을 해석하지 못했습니다. 앱과 서버 버전이 맞는지 확인해 주세요.")
            } catch (e: Exception) {
                throw ApiException("서버에 연결하지 못했습니다. 네트워크 상태를 확인해 주세요.")
            } finally {
                connection?.disconnect()
            }
        }

    fun parseRestaurantsResponse(jsonString: String): RestaurantsResponse {
        val root = JSONObject(jsonString)
        val restaurantsArray = root.getJSONArray("restaurants")
        val list = mutableListOf<Restaurant>()

        for (i in 0 until restaurantsArray.length()) {
                val obj = restaurantsArray.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val category = obj.getString("category")
                val lat = obj.getDouble("latitude")
                val lng = obj.getDouble("longitude")
                val distanceMeters = obj.optionalInt("distanceMeters")
                val address = obj.optionalString("address")
                val roadAddress = obj.optionalString("roadAddress")
                val phone = obj.optionalString("phone")
                val placeUrl = obj.optionalString("placeURL")

                val isOpenNow = obj.optionalBoolean("isOpenNow")

                val curatedMenus = mutableListOf<String>()
                val menusArray = obj.optJSONArray("curatedMenus")
                if (menusArray != null) {
                    for (j in 0 until menusArray.length()) {
                        curatedMenus.add(menusArray.getString(j))
                    }
                }

                val evidenceObj = obj.optJSONObject("photoMatchEvidence")
                val photoEvidence = evidenceObj?.let {
                    PhotoMatchEvidence(
                        exactNormalizedName = it.optionalBoolean("exactNormalizedName"),
                        addressMatch = it.optionalBoolean("addressMatch"),
                        distanceMeters = it.optionalInt("distanceMeters"),
                        phoneMatch = it.optionalBoolean("phoneMatch"),
                        previouslyVerifiedPlaceId = it.optionalBoolean("previouslyVerifiedPlaceId")
                    )
                }

                list.add(
                    Restaurant(
                        id = id,
                        name = name,
                        category = category,
                        lat = lat,
                        lng = lng,
                        distanceMeters = distanceMeters,
                        address = address,
                        roadAddress = roadAddress,
                        phone = phone,
                        placeUrl = placeUrl,
                        isOpenNow = isOpenNow,
                        curatedMenus = curatedMenus,
                        photoUrl = obj.optionalString("photoURL"),
                        photoKind = obj.optionalString("photoKind"),
                        photoProvider = obj.optionalString("photoProvider"),
                        photoSourceUrl = obj.optionalString("photoSourceURL"),
                        photoAttribution = obj.optionalString("photoAttribution"),
                        photoCreator = obj.optionalString("photoCreator"),
                        photoCreatorUrl = obj.optionalString("photoCreatorURL"),
                        photoLicense = obj.optionalString("photoLicense"),
                        photoLicenseUrl = obj.optionalString("photoLicenseURL"),
                        photoTitle = obj.optionalString("photoTitle"),
                        photoMatchEvidence = photoEvidence
                    )
                )
        }

        val source = root.optString("source").takeIf { it.isNotEmpty() }
        val disclaimer = root.optString("disclaimer").takeIf { it.isNotEmpty() }

        return RestaurantsResponse(
            restaurants = list,
            source = source,
            disclaimer = disclaimer
        )
    }

    private fun JSONObject.optionalString(key: String): String? =
        if (has(key) && !isNull(key)) getString(key).takeIf { it.isNotEmpty() } else null

    private fun JSONObject.optionalInt(key: String): Int? =
        if (has(key) && !isNull(key)) getInt(key) else null

    private fun JSONObject.optionalBoolean(key: String): Boolean? =
        if (has(key) && !isNull(key)) getBoolean(key) else null
}
