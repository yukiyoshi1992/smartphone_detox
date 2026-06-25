package com.yukiyoshi.smphdetox

import android.app.Application
import com.yukiyoshi.smphdetox.notification.NotificationRuleWorker

class SmphDetoxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationRuleWorker.schedule(this)
    }
}
