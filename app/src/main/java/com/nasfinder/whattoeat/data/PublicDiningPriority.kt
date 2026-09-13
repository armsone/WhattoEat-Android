package com.nasfinder.whattoeat.data

import android.content.Context
import com.nasfinder.whattoeat.model.Restaurant
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.text.Normalizer
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

/** Apple과 동일한 번들 명단·18개월·상호 및 주소 일치 정책. */
object PublicDiningPriority {
    data class Record(val name: String, val address: String, val region: String?,
        val departmentCount: Int, val lastPaymentDate: LocalDate, val sourceURL: String,
        val coverageDescription: String, val generatedAt: String, val sourceURLs: List<String>,
        val nameVariants: List<String>)
    @Volatile private var index: Map<String, List<Record>> = emptyMap()
    private var loaded = false
    private val aliases = mapOf(
        "서울특별시" to "서울", "서울시" to "서울", "서울" to "서울",
        "부산광역시" to "부산", "부산시" to "부산", "부산" to "부산",
        "대구광역시" to "대구", "대구시" to "대구", "대구" to "대구",
        "인천광역시" to "인천", "인천시" to "인천", "인천" to "인천",
        "광주광역시" to "광주", "광주시" to "광주", "광주" to "광주",
        "대전광역시" to "대전", "대전시" to "대전", "대전" to "대전",
        "울산광역시" to "울산", "울산시" to "울산", "울산" to "울산",
        "세종특별자치시" to "세종", "세종시" to "세종", "세종" to "세종",
        "경기도" to "경기", "경기" to "경기", "강원특별자치도" to "강원", "강원도" to "강원", "강원" to "강원",
        "충청북도" to "충북", "충북" to "충북", "충청남도" to "충남", "충남" to "충남",
        "전북특별자치도" to "전북", "전라북도" to "전북", "전북" to "전북", "전라남도" to "전남", "전남" to "전남",
        "경상북도" to "경북", "경북" to "경북", "경상남도" to "경남", "경남" to "경남",
        "제주특별자치도" to "제주", "제주도" to "제주", "제주" to "제주")
    private fun JSONArray.strings() = (0 until length()).map { getString(it) }
    private fun https(raw: String) = runCatching { URI(raw).let { it.scheme.equals("https", true) && it.host != null } }.getOrDefault(false)

    @Synchronized fun load(context: Context) {
        if (loaded) return
        index = runCatching {
            val root = context.assets.open("PublicDiningCatalog.json").bufferedReader().use { JSONObject(it.readText()) }
            require(root.getInt("schemaVersion") == 1)
            val generated = root.getString("generatedAt")
            OffsetDateTime.parse(generated)
            val coverage = root.getString("coverageDescription").also { require(it.isNotBlank()) }
            val sources = root.getJSONArray("sourceURLs").strings().also { require(it.isNotEmpty() && it.all(::https)) }
            val records = root.getJSONArray("restaurants")
            val result = mutableMapOf<String, MutableList<Record>>()
            for (i in 0 until records.length()) {
                val record = runCatching {
                    val row = records.getJSONObject(i)
                    val name = row.getString("name").also { require(it.isNotBlank()) }
                    val address = row.getString("address").also { require(it.isNotBlank()) }
                    val region = row.optString("region").takeIf { it.isNotBlank() }
                    require(region == null || region in aliases.values)
                    val departments = row.getInt("departmentCount").also { require(it >= 0) }
                    val date = LocalDate.parse(row.getString("lastPaymentDate"))
                    val url = row.getString("sourceURL").also { require(https(it)) }
                    val urls = row.optJSONArray("sourceURLs")?.strings()?.also { require(it.all(::https)) }?.takeIf { it.isNotEmpty() } ?: sources
                    Record(name, address, region, departments, date, url,
                        row.optString("coverageDescription").takeIf { it.isNotBlank() } ?: coverage,
                        generated, urls, row.optJSONArray("nameVariants")?.strings() ?: emptyList())
                }.getOrNull() ?: continue
                (record.nameVariants + record.name).map(::normalizedName).filter { it.isNotEmpty() }.distinct().forEach {
                    result.getOrPut(it) { mutableListOf() }.add(record)
                }
            }
            result.mapValues { it.value.distinct() }
        }.getOrDefault(emptyMap())
        loaded = true
    }
    fun normalizedName(raw: String): String {
        var value = Normalizer.normalize(raw, Normalizer.Form.NFKC).lowercase().trim()
        val markers = listOf("주식회사", "(주)", "㈜", "유한회사", "(유)", "합자회사", "(합)", "농업회사법인", "영농조합법인")
        do {
            val before = value
            for (marker in markers) {
                if (value.startsWith(marker)) value = value.removePrefix(marker).trim()
                else if (value.endsWith(marker)) value = value.removeSuffix(marker).trim()
            }
        } while (value != before)
        return value.filterNot { it.isWhitespace() }
    }
    private fun tokens(raw: String): List<String> {
        val pieces = Normalizer.normalize(raw, Normalizer.Form.NFKC).replace(Regex("\\([^)]*\\)"), " ")
            .replace(',', ' ').trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val result = mutableListOf<String>()
        pieces.forEachIndexed { i, piece ->
            var token = if (i == 0 && piece in setOf("전남광주통합특별시", "전남광주시", "전남광주")) {
                if (pieces.getOrNull(1) in setOf("동구", "서구", "남구", "북구", "광산구")) "광주" else "전남"
            } else aliases[piece] ?: piece
            if (token.matches(Regex("[0-9-]+번지"))) token = token.removeSuffix("번지")
            if (token != "세종" || result.lastOrNull() != "세종") result.add(token)
        }
        return result
    }
    private fun contains(haystack: List<String>, needle: List<String>) = needle.isNotEmpty() && haystack.size >= needle.size && haystack.windowed(needle.size).any { it == needle }
    private fun addressMatches(record: Record, candidate: List<String>): Boolean {
        if (candidate.size < 3) return false
        val address = tokens(record.address)
        if (address.size < 2) return false
        val candidateRegion = aliases[candidate.first()]
        val addressRegion = address.firstOrNull()?.let { aliases[it] }
        val region = record.region ?: addressRegion
        if (candidateRegion != null && region != null && candidateRegion != region) return false
        return contains(address, candidate) || (candidate.size >= 4 && candidateRegion != null && addressRegion == null && contains(address, candidate.drop(1)))
    }
    fun match(restaurant: Restaurant, today: LocalDate = LocalDate.now(ZoneId.of("Asia/Seoul"))): Record? {
        val candidates = index[normalizedName(restaurant.name)] ?: return null
        val addresses = listOfNotNull(restaurant.roadAddress, restaurant.address).map(::tokens)
        val record = candidates.filter { row -> addresses.any { addressMatches(row, it) } }.singleOrNull() ?: return null
        return record.takeIf { !it.lastPaymentDate.isBefore(today.minusMonths(18)) && !it.lastPaymentDate.isAfter(today) }
    }
    fun prioritized(pool: List<Restaurant>): List<Restaurant> {
        val (matched, rest) = pool.partition { match(it) != null }
        return matched.sortedBy { it.distanceMeters ?: Int.MAX_VALUE } + rest
    }
}
