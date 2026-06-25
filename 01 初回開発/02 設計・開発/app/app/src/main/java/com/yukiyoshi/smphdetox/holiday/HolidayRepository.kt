package com.yukiyoshi.smphdetox.holiday

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

private const val PREFS_NAME = "holiday_cache"
private const val KEY_DATES = "holiday_dates"
private const val KEY_FETCHED_AT = "fetched_at"

/** 無料公開の祝日API（holidays-jp）から日本の祝日一覧を取得し、ローカルにキャッシュする。 */
class HolidayRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun cachedHolidayDates(): Set<LocalDate> =
        prefs.getStringSet(KEY_DATES, emptySet())
            ?.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?.toSet()
            ?: emptySet()

    fun lastFetchedAt(): Long = prefs.getLong(KEY_FETCHED_AT, 0L)

    /** 取得失敗時は何もしない（既存キャッシュ・同梱フォールバックがあればそれを使い続ける）。 */
    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = URL(HOLIDAY_API_URL).openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            val text = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            val dates = json.keys().asSequence().toSet()
            prefs.edit()
                .putStringSet(KEY_DATES, dates)
                .putLong(KEY_FETCHED_AT, System.currentTimeMillis())
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun refreshIfStale(maxAgeMillis: Long = 24L * 60 * 60 * 1000) {
        if (System.currentTimeMillis() - lastFetchedAt() < maxAgeMillis) return
        refresh()
    }

    companion object {
        const val HOLIDAY_API_URL = "https://holidays-jp.github.io/api/v1/date.json"
    }
}
