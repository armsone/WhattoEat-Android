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
