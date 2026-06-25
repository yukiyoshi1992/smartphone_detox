package com.yukiyoshi.smphdetox

import android.app.Application
import com.yukiyoshi.smphdetox.notification.NotificationAlarmScheduler
import com.yukiyoshi.smphdetox.notification.NotificationRuleWorker

class SmphDetoxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 厳密なアラームでルール境界の瞬間に切り替え、15分おきのWorkManagerは
        // アラームが何らかの理由で失敗した場合の保険として併用する。
        NotificationAlarmScheduler.reschedule(this)
        NotificationRuleWorker.schedule(this)
    }
}
