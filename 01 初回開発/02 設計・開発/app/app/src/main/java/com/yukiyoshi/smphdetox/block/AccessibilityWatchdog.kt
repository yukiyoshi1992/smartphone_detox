package com.yukiyoshi.smphdetox.block

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat

private const val PREFS_NAME = "accessibility_watchdog"
private const val KEY_LAST_KNOWN_ENABLED = "last_known_enabled"
private const val CHANNEL_ID = "accessibility_watchdog"
private const val NOTIFICATION_ID = 1001

/**
 * アクセシビリティが「有効→無効」に切り替わった瞬間だけ通知する。
 * 毎回チェックするたびに前回の状態をSharedPreferencesに記録し、
 * trueからfalseへの遷移時のみ通知することで、無効なままの間に
 * 何度も通知が出ないようにしている。
 */
class AccessibilityWatchdog(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun checkAndNotifyIfDisabled(context: Context) {
        val enabledNow = isBlockAccessibilityServiceEnabled(context)
        val wasEnabled = prefs.getBoolean(KEY_LAST_KNOWN_ENABLED, true)
        if (wasEnabled && !enabledNow) {
            notifyAccessibilityDisabled(context)
        }
        prefs.edit().putBoolean(KEY_LAST_KNOWN_ENABLED, enabledNow).apply()
    }
}

private fun notifyAccessibilityDisabled(context: Context) {
    val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
    if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
        notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "アクセシビリティ監視", NotificationManager.IMPORTANCE_HIGH),
        )
    }

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("アクセシビリティが無効になりました")
        .setContentText("アプリ・サイトのブロックが動作していません。タップして再度有効にしてください")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(NOTIFICATION_ID, notification)
}
