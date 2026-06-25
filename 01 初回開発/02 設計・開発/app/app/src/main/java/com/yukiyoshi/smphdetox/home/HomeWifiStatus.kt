package com.yukiyoshi.smphdetox.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

/**
 * 現在接続中のWi-FiのSSIDを返す。Wi-Fi未接続や位置情報の許可がない場合はnull。
 * OSが返すSSIDは前後にダブルクオートが付くことがあるため取り除く。
 */
fun currentWifiSsid(context: Context): String? {
    val cm = context.getSystemService(ConnectivityManager::class.java) ?: return null
    val network = cm.activeNetwork ?: return null
    val capabilities = cm.getNetworkCapabilities(network) ?: return null
    if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
    val wifiInfo = capabilities.transportInfo as? WifiInfo ?: return null
    val ssid = wifiInfo.ssid
    if (ssid.isNullOrEmpty() || ssid == WifiManager.UNKNOWN_SSID) return null
    return ssid.removeSurrounding("\"")
}

fun isHomeWifiConnected(context: Context, homeSsid: String?): Boolean {
    if (homeSsid.isNullOrBlank()) return false
    return currentWifiSsid(context) == homeSsid
}
