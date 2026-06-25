package com.yukiyoshi.smphdetox.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** 再起動するとAlarmManagerの予約は消えるため、起動完了時に再スケジュールする。 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationAlarmScheduler.reschedule(context)
        }
    }
}
