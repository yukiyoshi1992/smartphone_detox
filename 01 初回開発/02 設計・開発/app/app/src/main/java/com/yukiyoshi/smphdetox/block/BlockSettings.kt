package com.yukiyoshi.smphdetox.block

import android.content.Context

private const val PREFS_NAME = "block_settings"
private const val KEY_BLOCKED_PACKAGES = "blocked_packages"
private const val KEY_BLOCKED_DOMAINS = "blocked_domains"

/** ブロック対象アプリ（パッケージ名）とブロック対象サイト（URLに含まれる文字列）の設定。 */
class BlockSettings(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var blockedPackages: Set<String>
        get() = prefs.getStringSet(KEY_BLOCKED_PACKAGES, emptySet())?.toSet() ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, value).apply()

    var blockedDomains: Set<String>
        get() = prefs.getStringSet(KEY_BLOCKED_DOMAINS, emptySet())?.toSet() ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_BLOCKED_DOMAINS, value).apply()

    fun addBlockedPackage(packageName: String) {
        blockedPackages = blockedPackages + packageName
    }

    fun removeBlockedPackage(packageName: String) {
        blockedPackages = blockedPackages - packageName
    }

    fun addBlockedDomain(domain: String) {
        if (domain.isBlank()) return
        blockedDomains = blockedDomains + domain.trim()
    }

    fun removeBlockedDomain(domain: String) {
        blockedDomains = blockedDomains - domain
    }
}
