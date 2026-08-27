package com.nasfinder.whattoeat.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.nasfinder.whattoeat.model.MapProvider
import com.nasfinder.whattoeat.model.Restaurant
import java.net.URLEncoder

object MapProviderHelper {

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getInstalledProviders(context: Context): List<MapProvider> {
        val providers = mutableListOf<MapProvider>()
        if (isAppInstalled(context, MapProvider.NAVER.packageName)) providers.add(MapProvider.NAVER)
        if (isAppInstalled(context, MapProvider.KAKAO.packageName)) providers.add(MapProvider.KAKAO)
        if (isAppInstalled(context, MapProvider.GOOGLE.packageName)) providers.add(MapProvider.GOOGLE)
        return providers
    }

    fun openMap(context: Context, provider: MapProvider, restaurant: Restaurant): Boolean {
        val lat = restaurant.lat
        val lng = restaurant.lng
        val encodedName = try {
            URLEncoder.encode(restaurant.name, "UTF-8")
        } catch (e: Exception) {
            restaurant.name
        }

        return when (provider) {
            MapProvider.NAVER -> {
                val uriStr = "nmap://place?lat=$lat&lng=$lng&name=$encodedName&appname=com.nasfinder.whattoeat"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                    setPackage(MapProvider.NAVER.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    true
                } catch (e: Exception) {
                    false
                }
            }

            MapProvider.KAKAO -> {
                val uriStr = if (restaurant.id.isNotEmpty() && restaurant.id.all { it.isDigit() }) {
                    "kakaomap://place?id=${restaurant.id}"
                } else {
                    "kakaomap://look?p=$lat,$lng"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                    setPackage(MapProvider.KAKAO.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    true
                } catch (e: Exception) {
                    false
                }
            }

            MapProvider.GOOGLE -> {
                val uriStr = "geo:$lat,$lng?q=$lat,$lng($encodedName)"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                    setPackage(MapProvider.GOOGLE.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    true
                } catch (e: Exception) {
                    false
                }
            }

            MapProvider.APPLE -> {
                // Apple Maps is not available on Android
                false
            }
        }
    }

    fun buildMenuSearchQuery(menu: String, region: String? = null): String {
        val cleanRegion = region?.trim()?.takeIf {
            it.isNotEmpty() && it != "현 위치" && it != "지정 지역" && it != "지역 다시 선택"
        }
        val cleanMenu = menu.trim().ifEmpty { "맛집" }
        return if (cleanRegion != null) "$cleanMenu $cleanRegion" else cleanMenu
    }

    fun buildMenuSearchUri(
        provider: MapProvider,
        menu: String,
        region: String? = null,
        lat: Double? = null,
        lng: Double? = null
    ): String? {
        val query = buildMenuSearchQuery(menu, region)
        val encodedQuery = try {
            URLEncoder.encode(query, "UTF-8")
        } catch (e: Exception) {
            query
        }
        return when (provider) {
            MapProvider.NAVER -> "nmap://search?query=$encodedQuery&appname=com.nasfinder.whattoeat"
            MapProvider.KAKAO -> "kakaomap://search?q=$encodedQuery"
            MapProvider.GOOGLE -> if (lat != null && lng != null) {
                "geo:$lat,$lng?q=$encodedQuery"
            } else {
                "geo:0,0?q=$encodedQuery"
            }
            MapProvider.APPLE -> null
        }
    }

    fun searchMapMenu(
        context: Context,
        provider: MapProvider,
        menu: String,
        region: String? = null,
        lat: Double? = null,
        lng: Double? = null
    ): Boolean {
        return when (provider) {
            MapProvider.NAVER -> {
                if (!isAppInstalled(context, MapProvider.NAVER.packageName)) return false
                val uriStr = buildMenuSearchUri(provider, menu, region, lat, lng) ?: return false
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                    setPackage(MapProvider.NAVER.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    true
                } catch (e: Exception) {
                    false
                }
            }

            MapProvider.KAKAO -> {
                if (!isAppInstalled(context, MapProvider.KAKAO.packageName)) return false
                val uriStr = buildMenuSearchUri(provider, menu, region, lat, lng) ?: return false
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                    setPackage(MapProvider.KAKAO.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    true
                } catch (e: Exception) {
                    false
                }
            }

            MapProvider.GOOGLE -> {
                if (!isAppInstalled(context, MapProvider.GOOGLE.packageName)) return false
                val uriStr = buildMenuSearchUri(provider, menu, region, lat, lng) ?: return false
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                    setPackage(MapProvider.GOOGLE.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    true
                } catch (e: Exception) {
                    false
                }
            }

            MapProvider.APPLE -> {
                // Apple Maps app is not available on Android; returns false for caller to handle dialog or web fallback
                false
            }
        }
    }

    fun openPlayStore(context: Context, provider: MapProvider) {
        val packageName = provider.packageName
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}
