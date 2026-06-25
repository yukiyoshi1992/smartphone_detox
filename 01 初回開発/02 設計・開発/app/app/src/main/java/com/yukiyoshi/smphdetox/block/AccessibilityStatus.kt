package com.yukiyoshi.smphdetox.block

import android.content.Context
import android.provider.Settings

/** BlockAccessibilityServiceがOSの「アクセシビリティ」設定でONになっているかを確認する。 */
fun isBlockAccessibilityServiceEnabled(context: Context): Boolean {
    val expected = "${context.packageName}/${BlockAccessibilityService::class.java.name}"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false
    return enabledServices.split(':').any { it.equals(expected, ignoreCase = true) }
}
