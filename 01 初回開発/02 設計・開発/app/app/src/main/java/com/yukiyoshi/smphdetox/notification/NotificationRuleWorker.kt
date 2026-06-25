package com.yukiyoshi.smphdetox.notification

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.yukiyoshi.smphdetox.block.AccessibilityWatchdog
import com.yukiyoshi.smphdetox.block.AppMasterSettings
import com.yukiyoshi.smphdetox.holiday.HolidayRepository
import com.yukiyoshi.smphdetox.home.HomeWifiSettings
import com.yukiyoshi.smphdetox.home.isHomeWifiConnected
import com.yukiyoshi.smphdetox.rule.AppRuleSettings
import com.yukiyoshi.smphdetox.rule.RuleTargetType
import com.yukiyoshi.smphdetox.rule.activeRules
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "notification_rule_check"
private const val TAG = "NotificationRuleWorker"

/**
 * 通知ルール（時間帯でマナーモードに自動切替）を定期的に評価し、リンガーモードに反映する。
 * Android/WorkManagerの制約上、正確に時刻ぴったりではなく数分〜十数分のずれが出ることがある。
 */
class NotificationRuleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        AccessibilityWatchdog(context).checkAndNotifyIfDisabled(context)
        if (!AppMasterSettings(context).enabled) return Result.success()

        val ruleSettings = AppRuleSettings(context)
        val homeWifiSettings = HomeWifiSettings(context)
        val holidayRepository = HolidayRepository(context)

        val isHome = isHomeWifiConnected(context, homeWifiSettings.homeSsids)
        val holidayDates = holidayRepository.cachedHolidayDates()
        val notificationRules = ruleSettings.rules.filter { it.targetType == RuleTargetType.NOTIFICATION }
        val active = activeRules(notificationRules, LocalDateTime.now(), isHome, holidayDates)
        val applied = applyRingerMode(context, quiet = active.isNotEmpty())
        Log.d(TAG, "checked notification rules: active=${active.map { it.label }} applied=$applied")
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<NotificationRuleWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.NONE)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
