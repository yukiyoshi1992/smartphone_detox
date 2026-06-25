package com.yukiyoshi.smphdetox.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

private fun WifiInfo?.toCleanSsidOrNull(): String? {
    val ssid = this?.ssid
    if (ssid.isNullOrEmpty() || ssid == WifiManager.UNKNOWN_SSID) return null
    return ssid.removeSurrounding("\"")
}

/**
 * 現在接続中のWi-FiのSSIDを返す。Wi-Fi未接続や位置情報の許可がない場合はnull。
 * OSが返すSSIDは前後にダブルクオートが付くことがあるため取り除く。
 *
 * NetworkCapabilities経由のWifiInfoは機種によって取得できないことがあるため、
 * WifiManager.connectionInfo（非推奨だが実機での実績がある）にフォールバックする。
 */
fun currentWifiSsid(context: Context): String? {
    val cm = context.getSystemService(ConnectivityManager::class.java)
    val viaNetworkCapabilities = cm?.activeNetwork
        ?.let { cm.getNetworkCapabilities(it) }
        ?.takeIf { it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) }
        ?.transportInfo as? WifiInfo
    viaNetworkCapabilities.toCleanSsidOrNull()?.let { return it }

    @Suppress("DEPRECATION")
    val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
    @Suppress("DEPRECATION")
    return wifiManager?.connectionInfo.toCleanSsidOrNull()
}

fun isHomeWifiConnected(context: Context, homeSsids: Set<String>): Boolean {
    if (homeSsids.isEmpty()) return false
    return currentWifiSsid(context) in homeSsids
}
