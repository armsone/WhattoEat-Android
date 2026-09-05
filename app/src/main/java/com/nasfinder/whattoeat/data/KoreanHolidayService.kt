package com.nasfinder.whattoeat.data

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

/** HanClip's Korean holiday source and local-cache fallback, shared by both apps. */
object KoreanHolidayService {
    private const val ENDPOINT = "https://holidays.hyunbin.page/basic.json"
    private const val CACHE = "whattoEatKoreanHolidayJSON"

    private fun decode(json: String): Map<String, String> {
        val root = JSONObject(json)
        val dates = mutableMapOf<String, String>()
        fun read(objectValue: JSONObject) {
            objectValue.keys().forEach { key ->
                val value = objectValue.get(key)
                if (value is JSONObject) read(value)
                else if (key.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) dates[key] = value.toString()
            }
        }
        read(root)
        return dates
    }

    @Synchronized
    fun refresh(context: Context) {
        val prefs = context.getSharedPreferences(CACHE, Context.MODE_PRIVATE)
        val age = System.currentTimeMillis() - prefs.getLong("fetchedAt", 0)
        if (age in 0 until 86_400_000 && prefs.contains("json")) return
        var connection: HttpURLConnection? = null
        try {
            connection = URL(ENDPOINT).openConnection() as HttpURLConnection
            connection.connectTimeout = 3500
            connection.readTimeout = 3500
            if (connection.responseCode !in 200..299) return
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            if (decode(json).isNotEmpty()) {
                prefs.edit().putString("json", json).putLong("fetchedAt", System.currentTimeMillis()).apply()
            }
        } catch (_: Exception) {
            // Keep the last successful download and bundled calendar while offline.
        } finally {
            connection?.disconnect()
        }
    }

    fun isWorkingDay(context: Context, date: LocalDate): Boolean {
        if (date.dayOfWeek.value >= 6) return false
        val bundled = runCatching {
            context.assets.open("KoreanHolidays.json").bufferedReader().use { decode(it.readText()) }
        }.getOrDefault(emptyMap())
        val cached = runCatching {
            decode(context.getSharedPreferences(CACHE, Context.MODE_PRIVATE).getString("json", "{}") ?: "{}")
        }.getOrDefault(emptyMap())
        val prefix = "${date.year}-"
        val dates = if (cached.keys.any { it.startsWith(prefix) }) cached else bundled
        // Unknown years must not silently produce holiday notifications.
        return dates.keys.any { it.startsWith(prefix) } && !dates.containsKey(date.toString())
    }
}
