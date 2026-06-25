package com.yukiyoshi.smphdetox.home

import android.content.Context

private const val PREFS_NAME = "home_wifi"
private const val KEY_HOME_SSIDS = "home_ssids"

/** 自宅Wi-FiのSSIDを複数登録できる（2.4GHz/5GHzで別SSIDの場合など）。 */
class HomeWifiSettings(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var homeSsids: Set<String>
        get() = prefs.getStringSet(KEY_HOME_SSIDS, emptySet())?.toSet() ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_HOME_SSIDS, value).apply()

    fun addHomeSsid(ssid: String) {
        if (ssid.isBlank()) return
        homeSsids = homeSsids + ssid
    }

    fun removeHomeSsid(ssid: String) {
        homeSsids = homeSsids - ssid
    }
}
