package com.nasfinder.whattoeat.model

import androidx.annotation.DrawableRes
import com.nasfinder.whattoeat.R

enum class AppPage {
    HOME,
    REGION,
    RESULT,
    DECISION,
    HISTORY,
    FAVORITES,
    PROFILE // Settings
}

enum class LocationMode {
    AUTO,
    MANUAL
}

enum class MapProvider(
    val displayName: String,
    val shortName: String,
    val packageName: String,
    val isSupportedOnAndroid: Boolean,
    @DrawableRes val iconRes: Int
) {
    APPLE("Apple", "Apple", "com.apple.maps", false, R.drawable.img_map_apple),
    NAVER("네이버", "네이버", "com.nhn.android.nmap", true, R.drawable.img_map_naver),
    KAKAO("카카오", "카카오", "net.daum.android.map", true, R.drawable.img_map_kakao),
    GOOGLE("Google", "Google", "com.google.android.apps.maps", true, R.drawable.img_map_google)
}

enum class ReminderLeadTime(val label: String, val leadMinutes: Int) {
    AT_TIME("정각", 0),
    MIN_5("5분 전", 5),
    MIN_10("10분 전", 10),
    MIN_15("15분 전", 15),
    MIN_30("30분 전", 30);

    companion object {
        fun fromMinutes(minutes: Int): ReminderLeadTime =
            entries.firstOrNull { it.leadMinutes == minutes } ?: MIN_5
    }
}

data class PhotoMatchEvidence(
    val exactNormalizedName: Boolean? = null,
    val addressMatch: Boolean? = null,
    val distanceMeters: Int? = null,
    val phoneMatch: Boolean? = null,
    val previouslyVerifiedPlaceId: Boolean? = null
)

data class PhotoInformation(
    val kind: String? = null,
    val provider: String? = null,
    val sourceUrl: String? = null,
    val attribution: String? = null,
    val creator: String? = null,
    val creatorUrl: String? = null,
    val license: String? = null,
    val licenseUrl: String? = null,
    val title: String? = null
) {
    val hasDetails: Boolean
        get() = kind != null || attribution != null || creator != null || license != null

    val isCategoryExample: Boolean
        get() = kind == "categoryExample"
}

data class Restaurant(
    val id: String,
    val name: String,
    val category: String,
    val lat: Double,
    val lng: Double,
    val distanceMeters: Int? = null,
    val address: String? = null,
    val roadAddress: String? = null,
    val phone: String? = null,
    val placeUrl: String? = null,
    val isOpenNow: Boolean? = null,
    val curatedMenus: List<String> = emptyList(),
    val photoUrl: String? = null,
    val photoKind: String? = null,
    val photoProvider: String? = null,
    val photoSourceUrl: String? = null,
    val photoAttribution: String? = null,
    val photoCreator: String? = null,
    val photoCreatorUrl: String? = null,
    val photoLicense: String? = null,
    val photoLicenseUrl: String? = null,
    val photoTitle: String? = null,
    val photoMatchEvidence: PhotoMatchEvidence? = null
) {
    val photoInformation: PhotoInformation
        get() = PhotoInformation(
            kind = photoKind,
            provider = photoProvider,
            sourceUrl = photoSourceUrl,
            attribution = photoAttribution,
            creator = photoCreator,
            creatorUrl = photoCreatorUrl,
            license = photoLicense,
            licenseUrl = photoLicenseUrl,
            title = photoTitle
        )
}

data class RestaurantsResponse(
    val restaurants: List<Restaurant>,
    val source: String? = null,
    val disclaimer: String? = null
)

data class Decision(
    val menu: String,
    val restaurant: Restaurant
) {
    val id: String get() = "$menu|${restaurant.id}"
}

data class ChoiceRecord(
    val menu: String,
    val restaurantName: String,
    val date: Long,
    val region: String? = null,
    val imageUrl: String? = null,
    val category: String? = null,
    val photoKind: String? = null,
    val photoProvider: String? = null,
    val photoSourceUrl: String? = null,
    val photoAttribution: String? = null,
    val photoCreator: String? = null,
    val photoCreatorUrl: String? = null,
    val photoLicense: String? = null,
    val photoLicenseUrl: String? = null,
    val photoTitle: String? = null,
    val restaurantId: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val address: String? = null
)

data class FavoriteRecord(
    val restaurantId: String,
    val restaurantName: String,
    val category: String,
    val region: String? = null,
    val imageUrl: String? = null,
    val photoKind: String? = null,
    val photoProvider: String? = null,
    val photoSourceUrl: String? = null,
    val photoAttribution: String? = null,
    val photoCreator: String? = null,
    val photoCreatorUrl: String? = null,
    val photoLicense: String? = null,
    val photoLicenseUrl: String? = null,
    val photoTitle: String? = null,
    val date: Long,
    val lat: Double? = null,
    val lng: Double? = null,
    val address: String? = null,
    val curatedMenus: List<String> = emptyList(),
    val phone: String? = null,
    val placeUrl: String? = null
)

data class RegionUsage(
    val name: String,
    val count: Int,
    val lastUsed: Long,
    val lat: Double,
    val lng: Double
)

enum class RecommendationPhase {
    IDLE,
    LOADING,
    SUCCESS,
    LOCATION_DENIED,
    EMPTY,
    ERROR
}
