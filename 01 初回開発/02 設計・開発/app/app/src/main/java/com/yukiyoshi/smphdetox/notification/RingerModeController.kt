package com.yukiyoshi.smphdetox.notification

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager

/**
 * Android 7以降、リンガーモードの変更には「通知へのアクセス」権限
 * （ACCESS_NOTIFICATION_POLICY、設定画面でユーザーが許可）が必要。
 */
fun hasNotificationPolicyAccess(context: Context): Boolean {
    val nm = context.getSystemService(NotificationManager::class.java)
    return nm?.isNotificationPolicyAccessGranted == true
}

/** quiet=trueならマナーモード（バイブ有効）、falseなら通常モードに切り替える。 */
fun applyRingerMode(context: Context, quiet: Boolean): Boolean {
    if (!hasNotificationPolicyAccess(context)) return false
    val audioManager = context.getSystemService(AudioManager::class.java) ?: return false
    audioManager.ringerMode = if (quiet) AudioManager.RINGER_MODE_VIBRATE else AudioManager.RINGER_MODE_NORMAL
    return true
}
