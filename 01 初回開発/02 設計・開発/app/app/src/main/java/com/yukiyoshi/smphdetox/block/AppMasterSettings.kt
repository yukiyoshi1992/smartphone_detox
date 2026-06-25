package com.yukiyoshi.smphdetox.block

import android.content.Context

private const val PREFS_NAME = "app_master_settings"
private const val KEY_ENABLED = "enabled"

/** アプリ全体のブロック機能ON/OFF。OFF中はBlockAccessibilityServiceが何もしない。 */
class AppMasterSettings(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()
}
