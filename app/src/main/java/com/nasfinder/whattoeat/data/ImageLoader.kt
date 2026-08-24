package com.nasfinder.whattoeat.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import android.net.http.HttpResponseCache
import android.util.LruCache
import androidx.annotation.DrawableRes
import com.nasfinder.whattoeat.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object ImageLoader {
    private const val TIMEOUT_MS = 15000
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun installHttpCache(context: Context) {
        if (HttpResponseCache.getInstalled() != null) return
        runCatching {
            HttpResponseCache.install(java.io.File(context.cacheDir, "restaurant-images"), 24L * 1024L * 1024L)
        }
    }

    suspend fun loadImage(url: String, provider: String? = null): Bitmap? = withContext(Dispatchers.IO) {
        if (!url.startsWith("https://", ignoreCase = true)) return@withContext null
        val bypassCache = provider.equals("foursquare", ignoreCase = true)

        if (!bypassCache) memoryCache.get(url)?.let { return@withContext it }

        var connection: HttpsURLConnection? = null
        try {
            val u = URL(url)
            connection = (u.openConnection() as? HttpsURLConnection) ?: return@withContext null
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.doInput = true
            connection.useCaches = !bypassCache
            if (bypassCache) connection.setRequestProperty("Cache-Control", "no-cache, no-store")
            connection.setRequestProperty("User-Agent", "WhattoEat/0.3.6 (https://nasfinder.com)")
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val input = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                ensureActive()
                if (bitmap != null) {
                    if (!bypassCache) memoryCache.put(url, bitmap)
                }
                return@withContext bitmap
            }
        } catch (e: Exception) {
            // Ignore error, fallback image will display
        } finally {
            connection?.disconnect()
        }
        null
    }

    enum class FallbackType(@DrawableRes val drawableRes: Int) {
        FOOD_MAIN(R.drawable.img_food_main),
        FOOD_BIBIMBAP(R.drawable.img_food_bibimbap),
        FOOD_SIDE1(R.drawable.img_food_side1),
        FOOD_SIDE2(R.drawable.img_food_side2),
        FOOD_SIDE3(R.drawable.img_food_side3),
        FOOD_GRILLED_PORK(R.drawable.img_food_grilled_pork),
        FOOD_JJAMPPONG(R.drawable.img_food_jjamppong),
        FOOD_NAENGMYEON(R.drawable.img_food_naengmyeon),
        FOOD_SEAFOOD(R.drawable.img_food_seafood),
        FOOD_CHICKEN(R.drawable.img_food_chicken),
        FOOD_TTEOKBOKKI(R.drawable.img_food_tteokbokki),
        FOOD_SUSHI(R.drawable.img_food_sushi),
        FOOD_SHABU(R.drawable.img_food_shabu),
        FOOD_BRUNCH(R.drawable.img_food_brunch)
    }

    fun resolveFallbackType(
        category: String,
        menu: String,
        seed: String,
        excluding: Set<FallbackType> = emptySet()
    ): FallbackType {
        val all = FallbackType.entries
        val text = "$menu $category".lowercase()
        fun hasAny(vararg keywords: String) = keywords.any(text::contains)
        val preferred = when {
            hasAny("카페", "베이커리", "디저트", "브런치", "커피") -> listOf(FallbackType.FOOD_BRUNCH, FallbackType.FOOD_SIDE1)
            hasAny("파스타", "피자", "양식") -> listOf(FallbackType.FOOD_SIDE1, FallbackType.FOOD_BRUNCH)
            hasAny("짜장", "짬뽕", "탕수육", "중식", "중국요리") -> listOf(FallbackType.FOOD_JJAMPPONG, FallbackType.FOOD_SIDE2, FallbackType.FOOD_TTEOKBOKKI)
            hasAny("냉면", "막국수") -> listOf(FallbackType.FOOD_NAENGMYEON, FallbackType.FOOD_SIDE2)
            hasAny("초밥", "스시", "일식") -> listOf(FallbackType.FOOD_SUSHI, FallbackType.FOOD_SIDE3, FallbackType.FOOD_SEAFOOD)
            hasAny("해물", "생선", "장어", "회", "수산") -> listOf(FallbackType.FOOD_SEAFOOD, FallbackType.FOOD_SUSHI, FallbackType.FOOD_JJAMPPONG)
            hasAny("치킨", "닭", "백숙", "삼계탕") -> listOf(FallbackType.FOOD_CHICKEN, FallbackType.FOOD_MAIN, FallbackType.FOOD_SHABU)
            hasAny("떡볶이", "김밥", "분식") -> listOf(FallbackType.FOOD_TTEOKBOKKI, FallbackType.FOOD_BIBIMBAP, FallbackType.FOOD_SIDE2)
            hasAny("샤브", "전골", "훠궈") -> listOf(FallbackType.FOOD_SHABU, FallbackType.FOOD_MAIN, FallbackType.FOOD_SEAFOOD)
            hasAny("국수", "칼국수", "국밥", "탕", "찌개", "죽", "순대") -> listOf(FallbackType.FOOD_SIDE2, FallbackType.FOOD_MAIN, FallbackType.FOOD_JJAMPPONG, FallbackType.FOOD_SHABU)
            hasAny("돈가스", "돈까스") -> listOf(FallbackType.FOOD_SIDE3, FallbackType.FOOD_GRILLED_PORK)
            hasAny("고기", "육류", "구이", "갈비", "삼겹살") -> listOf(FallbackType.FOOD_GRILLED_PORK, FallbackType.FOOD_MAIN, FallbackType.FOOD_CHICKEN, FallbackType.FOOD_SHABU)
            hasAny("비빔밥") -> listOf(FallbackType.FOOD_BIBIMBAP, FallbackType.FOOD_MAIN, FallbackType.FOOD_TTEOKBOKKI)
            hasAny("한식", "한정식") -> listOf(FallbackType.FOOD_BIBIMBAP, FallbackType.FOOD_MAIN, FallbackType.FOOD_GRILLED_PORK, FallbackType.FOOD_SEAFOOD, FallbackType.FOOD_TTEOKBOKKI, FallbackType.FOOD_CHICKEN, FallbackType.FOOD_NAENGMYEON, FallbackType.FOOD_SHABU)
            else -> all
        }
        val candidates = preferred + all.filterNot(preferred::contains)
        val stable = seed.encodeToByteArray().fold(14_695_981_039_346_656_037uL) { hash, byte ->
            (hash xor byte.toUByte().toULong()) * 1_099_511_628_211uL
        }
        val start = (stable % candidates.size.toULong()).toInt()
        val rotated = candidates.drop(start) + candidates.take(start)
        return rotated.firstOrNull { it !in excluding } ?: candidates[start]
    }
}
