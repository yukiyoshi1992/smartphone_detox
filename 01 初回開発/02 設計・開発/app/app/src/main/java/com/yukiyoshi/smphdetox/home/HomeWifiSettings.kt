package com.yukiyoshi.smphdetox.home

import android.content.Context

private const val PREFS_NAME = "home_wifi"
private const val KEY_HOME_SSID = "home_ssid"

class HomeWifiSettings(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var homeSsid: String?
        get() = prefs.getString(KEY_HOME_SSID, null)
        set(value) = prefs.edit().putString(KEY_HOME_SSID, value).apply()
}
